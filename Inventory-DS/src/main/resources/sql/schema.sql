-- Database creation
CREATE DATABASE IF NOT EXISTS inventory_ds;
USE inventory_ds;

-- Users table (for login/role control)
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,  -- Store hashed passwords
    role ENUM('admin', 'user') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Categories table
CREATE TABLE categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

-- Products table
CREATE TABLE products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category_id INT NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    price DECIMAL(10, 2) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- Inventory logs (audit trail)
CREATE TABLE inventory_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    user_id INT NOT NULL,
    action ENUM('add', 'remove', 'update') NOT NULL,
    quantity INT NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Add to schema.sql (after CREATE TABLE statements)
CREATE INDEX idx_product_name ON products(name);
CREATE INDEX idx_product_category ON products(category_id);

-- Prevent negative stock/price
ALTER TABLE products ADD CONSTRAINT chk_stock CHECK (stock >= 0);
ALTER TABLE products ADD CONSTRAINT chk_price CHECK (price >= 0);