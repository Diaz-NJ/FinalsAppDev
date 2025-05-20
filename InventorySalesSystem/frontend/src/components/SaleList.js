import React, { useState, useEffect } from 'react';
import { 
  Table, 
  TableBody, 
  TableCell, 
  TableContainer, 
  TableHead, 
  TableRow, 
  Paper, 
  Button,
  Box,
  Alert
} from '@mui/material';
import { useNavigate, useParams } from 'react-router-dom';
import { getSales, deleteSale } from '../services/api';

const SaleList = () => {
  const [sales, setSales] = useState([]);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    fetchSales();
  }, []);

  const fetchSales = async () => {
    try {
      const response = await getSales();
      setSales(response.data);
      setError(null);
    } catch (error) {
      setError('Failed to load sales. Please try again.');
      console.error('Error fetching sales:', error);
    }
  };

  const handleDelete = async (id) => {
    try {
      await deleteSale(id);
      fetchSales();
    } catch (error) {
      setError('Failed to delete sale.');
      console.error('Error deleting sale:', error);
    }
  };

  return (
    <Box sx={{ p: 3 }}>
      <Button 
        variant="contained" 
        color="primary" 
        onClick={() => navigate('/sales/new')}
        sx={{ mb: 3 }}
      >
        Create Sale
      </Button>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      <TableContainer component={Paper}>
        <Table sx={{ minWidth: 650 }} aria-label="sales table">
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
                    onClick={() => navigate(`/sales/${sale.id}`)}
                    sx={{ mr: 1 }}
                  >
                    View
                  </Button>
                  <Button 
                    variant="contained" 
                    color="error" 
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
    </Box>
  );
};

export default SaleList;