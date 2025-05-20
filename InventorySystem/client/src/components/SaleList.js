import React, { useState, useEffect } from 'react';
import { makeStyles } from '@mui/styles';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';
import Button from '@mui/material/Button';
import { getSales, deleteSale } from '../services/api';

const useStyles = makeStyles({
  table: {
    minWidth: 650,
  },
});

const SaleList = ({ history }) => {
  const classes = useStyles();
  const [sales, setSales] = useState([]);

  useEffect(() => {
    fetchSales();
  }, []);

  const fetchSales = async () => {
    try {
      const response = await getSales();
      setSales(response.data);
    } catch (error) {
      console.error('Error fetching sales:', error);
    }
  };

  const handleDelete = async (id) => {
    try {
      await deleteSale(id);
      fetchSales();
    } catch (error) {
      console.error('Error deleting sale:', error);
    }
  };

  return (
    <TableContainer component={Paper}>
      <Button 
        variant="contained" 
        color="primary" 
        onClick={() => history.push('/sales/new')}
        style={{ margin: '20px' }}
      >
        Create Sale
      </Button>
      <Table className={classes.table} aria-label="simple table">
        <TableHead>
          <TableRow>
            <TableCell>ID</TableCell>
            <TableCell>Customer</TableCell>
            <TableCell>Date</TableCell>
            <TableCell align="right">Total Amount</TableCell>
            <TableCell>Actions</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {sales.map((sale) => (
            <TableRow key={sale.id}>
              <TableCell>{sale.id}</TableCell>
              <TableCell>{sale.customer?.name || 'N/A'}</TableCell>
              <TableCell>{new Date(sale.sale_date).toLocaleDateString()}</TableCell>
              <TableCell align="right">${sale.total_amount.toFixed(2)}</TableCell>
              <TableCell>
                <Button 
                  variant="contained" 
                  color="primary" 
                  size="small"
                  onClick={() => history.push(`/sales/${sale.id}`)}
                  style={{ marginRight: '10px' }}
                >
                  View
                </Button>
                <Button 
                  variant="contained" 
                  color="secondary" 
                  size="small"
                  onClick={() => handleDelete(sale.id)}
                >
                  Delete
                </Button>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
};

export default SaleList;