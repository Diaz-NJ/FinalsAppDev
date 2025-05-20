import React from 'react';
import { makeStyles } from '@mui/styles';
import AppBar from '@mui/material/AppBar';
import Toolbar from '@mui/material/Toolbar';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import { Link as RouterLink } from 'react-router-dom';

const useStyles = makeStyles((theme) => ({
  root: {
    flexGrow: 1,
  },
  title: {
    flexGrow: 1,
  },
  link: {
    color: 'white',
    textDecoration: 'none',
  },
}));

const Navbar = () => {
  const classes = useStyles();

  return (
    <div className={classes.root}>
      <AppBar position="static">
        <Toolbar>
          <Typography variant="h6" className={classes.title}>
            Inventory & Sales System
          </Typography>
          <Button color="inherit">
            <RouterLink to="/" className={classes.link}>
              Dashboard
            </RouterLink>
          </Button>
          <Button color="inherit">
            <RouterLink to="/products" className={classes.link}>
              Products
            </RouterLink>
          </Button>
          <Button color="inherit">
            <RouterLink to="/customers" className={classes.link}>
              Customers
            </RouterLink>
          </Button>
          <Button color="inherit">
            <RouterLink to="/sales" className={classes.link}>
              Sales
            </RouterLink>
          </Button>
        </Toolbar>
      </AppBar>
    </div>
  );
};

export default Navbar;