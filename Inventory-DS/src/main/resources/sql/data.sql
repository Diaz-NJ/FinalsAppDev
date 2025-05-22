-- Insert sample categories
INSERT INTO categories (name) VALUES 
('Electronics'), ('Clothing'), ('Groceries');

-- Insert sample admin/user (password = "admin123" hashed)
INSERT INTO users (username, password, role) VALUES 
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MQDq5phQ5DSo0rB7iYukB4V/7wJQnCO', 'admin'),  -- BCrypt hash
('user1', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MQDq5phQ5DSo0rB7iYukB4V/7wJQnCO', 'user');

-- Insert sample products
INSERT INTO products (name, category_id, stock, price, description) VALUES 
('Laptop', 1, 10, 999.99, '16GB RAM, 512GB SSD'),
('T-Shirt', 2, 50, 19.99, 'Cotton, Size M');