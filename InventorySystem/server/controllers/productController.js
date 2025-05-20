const db = require('../config/database');

// Get all products
exports.getAllProducts = async (req, res) => {
  try {
    const [products] = await db.query('SELECT * FROM products');
    res.json(products);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
};

// Create a product
exports.createProduct = async (req, res) => {
  try {
    const { name, description, price, quantity } = req.body;
    const [result] = await db.query(
      'INSERT INTO products (name, description, price, quantity) VALUES (?, ?, ?, ?)',
      [name, description, price, quantity]
    );
    res.status(201).json({ id: result.insertId, ...req.body });
  } catch (err) {
    res.status(400).json({ message: err.message });
  }
};

// Get single product
exports.getProductById = async (req, res) => {
  try {
    const [product] = await db.query('SELECT * FROM products WHERE id = ?', [req.params.id]);
    if (product.length === 0) {
      return res.status(404).json({ message: 'Product not found' });
    }
    res.json(product[0]);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
};

// Update product
exports.updateProduct = async (req, res) => {
  try {
    const { name, description, price, quantity } = req.body;
    await db.query(
      'UPDATE products SET name = ?, description = ?, price = ?, quantity = ? WHERE id = ?',
      [name, description, price, quantity, req.params.id]
    );
    res.json({ id: req.params.id, ...req.body });
  } catch (err) {
    res.status(400).json({ message: err.message });
  }
};

// Delete product
exports.deleteProduct = async (req, res) => {
  try {
    await db.query('DELETE FROM products WHERE id = ?', [req.params.id]);
    res.json({ message: 'Product deleted' });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
};