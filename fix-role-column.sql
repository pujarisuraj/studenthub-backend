-- Fix role column size issue in users table
-- Run this script in your MySQL database

USE campus_collab_db;

-- Option 1: Alter the role column to increase its size
ALTER TABLE users MODIFY COLUMN role VARCHAR(20);

-- Verify the change
DESCRIBE users;
