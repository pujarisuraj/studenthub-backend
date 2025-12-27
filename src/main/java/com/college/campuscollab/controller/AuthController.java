package com.college.campuscollab.controller;

import com.college.campuscollab.dto.LoginRequest;
import com.college.campuscollab.dto.LoginResponse;
import com.college.campuscollab.dto.RegisterRequest;
import com.college.campuscollab.dto.ForgotPasswordRequest;
import com.college.campuscollab.dto.ResetPasswordRequest;
import com.college.campuscollab.entity.User;
import com.college.campuscollab.security.jwt.JwtUtil;
import com.college.campuscollab.service.UserService;
import com.college.campuscollab.service.PasswordResetService;
import com.college.campuscollab.service.ActivityLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:3001" }, allowCredentials = "true")
public class AuthController {

        private final AuthenticationManager authenticationManager;
        private final JwtUtil jwtUtil;
        private final UserService userService;
        private final PasswordResetService passwordResetService;
        private final ActivityLogService activityLogService;

        public AuthController(AuthenticationManager authenticationManager,
                        JwtUtil jwtUtil,
                        UserService userService,
                        PasswordResetService passwordResetService,
                        ActivityLogService activityLogService) {
                this.authenticationManager = authenticationManager;
                this.jwtUtil = jwtUtil;
                this.userService = userService;
                this.passwordResetService = passwordResetService;
                this.activityLogService = activityLogService;
        }

        @PostMapping("/register")
        public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
                try {
                        if (!request.getPassword().equals(request.getConfirmPassword())) {
                                return ResponseEntity
                                                .badRequest()
                                                .body(java.util.Map.of("message",
                                                                "Password and Confirm Password do not match"));
                        }

                        userService.registerUser(request);

                        // Log the registration activity
                        User registeredUser = userService.getUserByEmail(request.getEmail());
                        activityLogService.logActivity(
                                        registeredUser,
                                        "USER_REGISTRATION",
                                        "AUTH",
                                        "User " + registeredUser.getFullName() + " registered successfully");

                        return ResponseEntity.ok(java.util.Map.of("message", "User registered successfully"));
                } catch (RuntimeException e) {
                        // Return specific error messages from service layer (e.g., "Email already
                        // registered")
                        return ResponseEntity
                                        .badRequest()
                                        .body(java.util.Map.of("message", e.getMessage()));
                }
        }

        /**
         * Admin Registration - Restricted to authorized email only
         * Authorized Email: surajpujari8383@gmail.com
         */
        @PostMapping("/register-admin")
        public ResponseEntity<?> registerAdmin(@RequestBody RegisterRequest request) {
                try {
                        // Authorized admin email
                        final String AUTHORIZED_ADMIN_EMAIL = "surajpujari8383@gmail.com";

                        // Check if email is authorized
                        if (!request.getEmail().equalsIgnoreCase(AUTHORIZED_ADMIN_EMAIL)) {
                                return ResponseEntity
                                                .status(403)
                                                .body(java.util.Map.of("message",
                                                                "Unauthorized: Only authorized email can register as admin"));
                        }

                        if (!request.getPassword().equals(request.getConfirmPassword())) {
                                return ResponseEntity
                                                .badRequest()
                                                .body(java.util.Map.of("message",
                                                                "Password and Confirm Password do not match"));
                        }

                        userService.registerAdmin(request);

                        // Log the admin registration activity
                        User registeredAdmin = userService.getUserByEmail(request.getEmail());
                        activityLogService.logActivity(
                                        registeredAdmin,
                                        "ADMIN_REGISTRATION",
                                        "AUTH",
                                        "Admin " + registeredAdmin.getFullName() + " registered successfully");

                        return ResponseEntity.ok(java.util.Map.of("message", "Admin registered successfully"));
                } catch (RuntimeException e) {
                        return ResponseEntity
                                        .badRequest()
                                        .body(java.util.Map.of("message", e.getMessage()));
                }
        }

        @PostMapping("/login")
        public ResponseEntity<LoginResponse> login(
                        @RequestBody LoginRequest request) {

                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                request.getEmail(),
                                                request.getPassword()));

                SecurityContextHolder.getContext()
                                .setAuthentication(authentication);

                User user = userService.getUserByEmail(request.getEmail());

                String token = jwtUtil.generateToken(user.getEmail());

                // Log the login activity
                activityLogService.logActivity(
                                user,
                                "USER_LOGIN",
                                "AUTH",
                                "User " + user.getFullName() + " logged in successfully");

                return ResponseEntity.ok(
                                new LoginResponse(
                                                token,
                                                user.getEmail(),
                                                user.getRole().name()));
        }

        @PostMapping("/logout")
        public ResponseEntity<?> logout() {
                // Get current user before clearing context
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                        try {
                                String email = auth.getName();
                                User user = userService.getUserByEmail(email);

                                // Log the logout activity
                                activityLogService.logActivity(
                                                user,
                                                "USER_LOGOUT",
                                                "AUTH",
                                                "User " + user.getFullName() + " logged out");
                        } catch (Exception e) {
                                // Ignore errors during logout logging
                        }
                }

                // Clear security context
                SecurityContextHolder.clearContext();

                return ResponseEntity.ok("Logged out successfully");
        }

        /**
         * Send password reset email
         */
        @PostMapping("/forgot-password")
        public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
                try {
                        passwordResetService.sendPasswordResetEmail(request.getEmail());

                        // Log password reset request
                        User user = userService.getUserByEmail(request.getEmail());
                        activityLogService.logActivity(
                                        user,
                                        "PASSWORD_RESET_REQUEST",
                                        "AUTH",
                                        "User " + user.getFullName() + " requested password reset");

                        return ResponseEntity.ok("Password reset link has been sent to your email");
                } catch (RuntimeException e) {
                        return ResponseEntity.badRequest().body(e.getMessage());
                }
        }

        /**
         * Reset password using token
         */
        @PostMapping("/reset-password")
        public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
                try {
                        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());

                        // Get user info from the token for logging
                        // Since resetPassword doesn't return email, we can extract it from the token
                        // For now, we'll just log without user context
                        // TODO: Modify PasswordResetService to return user email

                        return ResponseEntity.ok("Password has been reset successfully");
                } catch (RuntimeException e) {
                        return ResponseEntity.badRequest().body(e.getMessage());
                }
        }

        /**
         * Validate reset token
         */
        @GetMapping("/validate-reset-token")
        public ResponseEntity<?> validateToken(@RequestParam String token) {
                boolean isValid = passwordResetService.isTokenValid(token);
                if (isValid) {
                        return ResponseEntity.ok("Token is valid");
                } else {
                        return ResponseEntity.badRequest().body("Invalid or expired token");
                }
        }

}
