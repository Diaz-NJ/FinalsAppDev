import React, { useState, useEffect } from 'react';
import { 
  Paper, 
  Grid, 
  Typography,
  Box,
  CircularProgress,
  Alert
} from '@mui/material';
import { getProducts, getCustomers, getSales } from '../services/api';

const Dashboard = () => {
  const [stats, setStats] = useState({
    productCount: 0,
    customerCount: 0,
    saleCount: 0,
    totalRevenue: 0,
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const [productsRes, customersRes, salesRes] = await Promise.all([
          getProducts(),
          getCustomers(),
          getSales(),
        ]);

        const totalRevenue = salesRes.data.reduce(
          (sum, sale) => sum + sale.total_amount,
          0
        );

        setStats({
          productCount: productsRes.data.length,
          customerCount: customersRes.data.length,
          saleCount: salesRes.data.length,
          totalRevenue,
        });
        setLoading(false);
      } catch (error) {
        console.error('Error fetching stats:', error);
        setError('Failed to load dashboard data');
        setLoading(false);
      }
    };

    fetchStats();
  }, []);

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" mt={4}>
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return (
      <Alert severity="error" sx={{ m: 2 }}>
        {error}
      </Alert>
    );
  }

  return (
    <Box sx={{ flexGrow: 1, p: 3 }}>
      <Typography variant="h4" sx={{ mb: 2 }}>
        Dashboard
      </Typography>
      <Grid container spacing={3}>
        <Grid item xs={12} sm={6} md={3}>
          <Paper sx={{ p: 2, textAlign: 'center', height: '100%' }}>
            <Typography variant="h6">Products</Typography>
            <Typography variant="h4">{stats.productCount}</Typography>
          </Paper>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Paper sx={{ p: 2, textAlign: 'center', height: '100%' }}>
            <Typography variant="h6">Customers</Typography>
            <Typography variant="h4">{stats.customerCount}</Typography>
          </Paper>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Paper sx={{ p: 2, textAlign: 'center', height: '100%' }}>
            <Typography variant="h6">Sales</Typography>
            <Typography variant="h4">{stats.saleCount}</Typography>
          </Paper>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Paper sx={{ p: 2, textAlign: 'center', height: '100%' }}>
            <Typography variant="h6">Total Revenue</Typography>
            <Typography variant="h4">
              ${stats.totalRevenue.toFixed(2)}
            </Typography>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
};

export default Dashboard;