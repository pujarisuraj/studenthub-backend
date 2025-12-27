package com.college.campuscollab.security.jwt;

import com.college.campuscollab.security.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil,
            CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String authHeader = request.getHeader("Authorization");

            // Log the request for debugging (remove in production)
            System.out.println("Processing request: " + request.getMethod() + " " + request.getRequestURI());
            System.out.println("Content-Type: " + request.getContentType());
            System.out.println("Authorization header present: " + (authHeader != null));

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                try {
                    String username = jwtUtil.extractUsername(token);

                    if (username != null &&
                            SecurityContextHolder.getContext().getAuthentication() == null) {

                        var userDetails = userDetailsService.loadUserByUsername(username);

                        // Validate token before setting authentication
                        if (jwtUtil.validateToken(token, userDetails)) {
                            var authenticationToken = new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities());

                            authenticationToken.setDetails(
                                    new WebAuthenticationDetailsSource()
                                            .buildDetails(request));

                            SecurityContextHolder.getContext()
                                    .setAuthentication(authenticationToken);

                            System.out.println("Authentication successful for user: " + username);
                        } else {
                            System.out.println("Token validation failed for user: " + username);
                        }
                    }
                } catch (Exception tokenException) {
                    // Log token-specific errors but don't stop the filter chain
                    System.err.println("JWT token processing error: " + tokenException.getMessage());
                }
            }

        } catch (Exception e) {
            // Log general filter errors but ensure filter chain continues
            System.err.println("JWT Authentication Filter error: " + e.getMessage());
            e.printStackTrace();
        }

        // IMPORTANT: Always call filterChain.doFilter to continue the request
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        // Skip JWT filter for authentication and file serving endpoints
        return path.startsWith("/api/auth/") || path.startsWith("/api/files/");
    }
}
