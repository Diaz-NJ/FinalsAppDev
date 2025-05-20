    const db = require('../config/database');

// Get all customers
exports.getAllCustomers = async (req, res) => {
  try {
    const [customers] = await db.query('SELECT * FROM customers');
    res.json(customers);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
};

// Create a customer
exports.createCustomer = async (req, res) => {
  try {
    const { name, email, phone, address } = req.body;
    const [result] = await db.query(
      'INSERT INTO customers (name, email, phone, address) VALUES (?, ?, ?, ?)',
      [name, email, phone, address]
    );
    res.status(201).json({ id: result.insertId, ...req.body });
  } catch (err) {
    res.status(400).json({ message: err.message });
  }
};

// Get single customer
exports.getCustomerById = async (req, res) => {
  try {
    const [customer] = await db.query('SELECT * FROM customers WHERE id = ?', [req.params.id]);
    if (customer.length === 0) {
      return res.status(404).json({ message: 'Customer not found' });
    }
    res.json(customer[0]);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
};

// Update customer
exports.updateCustomer = async (req, res) => {
  try {
    const { name, email, phone, address } = req.body;
    await db.query(
      'UPDATE customers SET name = ?, email = ?, phone = ?, address = ? WHERE id = ?',
      [name, email, phone, address, req.params.id]
    );
    res.json({ id: req.params.id, ...req.body });
  } catch (err) {
    res.status(400).json({ message: err.message });
  }
};

// Delete customer
exports.deleteCustomer = async (req, res) => {
  try {
    await db.query('DELETE FROM customers WHERE id = ?', [req.params.id]);
    res.json({ message: 'Customer deleted' });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
};