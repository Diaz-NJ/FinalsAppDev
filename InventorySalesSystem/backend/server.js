require('dotenv').config();
const express = require('express');
const bodyParser = require('body-parser');
const cors = require('cors');
const db = require('./config/database');

const app = express();

// Middleware
app.use(cors());
app.use(bodyParser.json());

// Test database connection
async function testDbConnection() {
  try {
    const connection = await db.getConnection();
    await connection.query('SELECT 1');
    connection.release(); // Release the connection back to the pool
    console.log('Database connected...');
  } catch (err) {
    console.error('Database connection error:', err);
    process.exit(1); // Exit if connection fails
  }
}

// Run connection test and start server
testDbConnection().then(() => {
  // Routes
  app.use('/api/products', require('./routes/products'));
  app.use('/api/customers', require('./routes/customers'));
  app.use('/api/sales', require('./routes/sales'));

  const PORT = process.env.PORT || 5000;
  app.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
  });
});

// Handle uncaught errors
process.on('uncaughtException', (err) => {
  console.error('Uncaught Exception:', err);
  process.exit(1);
});