const db = require('../config/database');

exports.getSales = async (req, res) => {
  try {
    const [rows] = await db.query('SELECT * FROM sales');
    res.json(rows);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
};

exports.getSaleById = async (req, res) => {
  try {
    const { id } = req.params;
    const [saleRows] = await db.query('SELECT * FROM sales WHERE id = ?', [id]);
    if (saleRows.length === 0) {
      return res.status(404).json({ error: 'Sale not found' });
    }
    const [saleItems] = await db.query('SELECT * FROM sale_items WHERE sale_id = ?', [id]);
    res.json({ ...saleRows[0], items: saleItems });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
};

exports.createSale = async (req, res) => {
  const { customer_id, total_amount, items } = req.body;
  try {
    const [saleResult] = await db.query(
      'INSERT INTO sales (customer_id, total_amount) VALUES (?, ?)',
      [customer_id, total_amount]
    );
    const saleId = saleResult.insertId;

    for (const item of items) {
      const { product_id, quantity, unit_price } = item;
      const total_price = quantity * unit_price;
      await db.query(
        'INSERT INTO sale_items (sale_id, product_id, quantity, unit_price, total_price) VALUES (?, ?, ?, ?, ?)',
        [saleId, product_id, quantity, unit_price, total_price]
      );
      await db.query(
        'UPDATE products SET quantity = quantity - ? WHERE id = ?',
        [quantity, product_id]
      );
    }

    res.json({ id: saleId, customer_id, total_amount, items });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
};

exports.updateSale = async (req, res) => {
  const { id } = req.params;
  const { customer_id, total_amount, items } = req.body;
  try {
    const [saleResult] = await db.query(
      'UPDATE sales SET customer_id = ?, total_amount = ? WHERE id = ?',
      [customer_id, total_amount, id]
    );
    if (saleResult.affectedRows === 0) {
      return res.status(404).json({ error: 'Sale not found' });
    }

    const [existingItems] = await db.query('SELECT product_id, quantity FROM sale_items WHERE sale_id = ?', [id]);
    for (const item of existingItems) {
      await db.query('UPDATE products SET quantity = quantity + ? WHERE id = ?', [item.quantity, item.product_id]);
    }
    await db.query('DELETE FROM sale_items WHERE sale_id = ?', [id]);

    for (const item of items) {
      const { product_id, quantity, unit_price } = item;
      const total_price = quantity * unit_price;
      await db.query(
        'INSERT INTO sale_items (sale_id, product_id, quantity, unit_price, total_price) VALUES (?, ?, ?, ?, ?)',
        [id, product_id, quantity, unit_price, total_price]
      );
      await db.query(
        'UPDATE products SET quantity = quantity - ? WHERE id = ?',
        [quantity, product_id]
      );
    }

    res.json({ id, customer_id, total_amount, items });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
};

exports.deleteSale = async (req, res) => {
  try {
    const { id } = req.params;
    const [saleItems] = await db.query('SELECT product_id, quantity FROM sale_items WHERE sale_id = ?', [id]);
    for (const item of saleItems) {
      await db.query('UPDATE products SET quantity = quantity + ? WHERE id = ?', [item.quantity, item.product_id]);
    }
    await db.query('DELETE FROM sale_items WHERE sale_id = ?', [id]);
    const [result] = await db.query('DELETE FROM sales WHERE id = ?', [id]);
    if (result.affectedRows === 0) {
      return res.status(404).json({ error: 'Sale not found' });
    }
    res.json({ message: 'Sale deleted' });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
};