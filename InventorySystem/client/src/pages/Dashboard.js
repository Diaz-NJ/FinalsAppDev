import React, { useState, useEffect } from 'react';
import { makeStyles } from '@mui/styles';
import Paper from '@mui/material/Paper';
import Grid from '@mui/material/Grid';
import Typography from '@mui/material/Typography';
import { getProducts, getCustomers, getSales } from '../services/api';

const useStyles = makeStyles((theme) => ({
  root: {
    flexGrow: 1,
    padding: theme.spacing(3),
  },
  paper: {
    padding: theme.spacing(2),
    textAlign: 'center',
    color: theme.palette.text.secondary,
    height: '100%',
  },
  title: {
    marginBottom: theme.spacing(2),
  },
}));

const Dashboard = () => {
  const classes = useStyles();
  const [stats, setStats] = useState({
    productCount: 0,
    customerCount: 0,
    saleCount: 0,
    totalRevenue: 0,
  });

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
      } catch (error) {
        console.error('Error fetching stats:', error);
      }
    };

    fetchStats();
  }, []);

  return (
    <div className={classes.root}>
      <Typography variant="h4" className={classes.title}>
        Dashboard
      </Typography>
      <Grid container spacing={3}>
        <Grid item xs={12} sm={6} md={3}>
          <Paper className={classes.paper}>
            <Typography variant="h6">Products</Typography>
            <Typography variant="h4">{stats.productCount}</Typography>
          </Paper>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Paper className={classes.paper}>
            <Typography variant="h6">Customers</Typography>
            <Typography variant="h4">{stats.customerCount}</Typography>
          </Paper>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Paper className={classes.paper}>
            <Typography variant="h6">Sales</Typography>
            <Typography variant="h4">{stats.saleCount}</Typography>
          </Paper>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Paper className={classes.paper}>
            <Typography variant="h6">Total Revenue</Typography>
            <Typography variant="h4">
              ${stats.totalRevenue.toFixed(2)}
            </Typography>
          </Paper>
        </Grid>
      </Grid>
    </div>
  );
};

export default Dashboard;