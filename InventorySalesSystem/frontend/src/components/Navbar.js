import React from 'react';
import { 
  AppBar, 
  Toolbar, 
  Typography, 
  Button,
  Box
} from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';

const Navbar = () => {
  return (
    <Box sx={{ flexGrow: 1 }}>
      <AppBar position="static">
        <Toolbar>
          <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
            Inventory & Sales System
          </Typography>
          <Button 
            color="inherit" 
            component={RouterLink} 
            to="/"
            sx={{ color: 'white', textDecoration: 'none' }}
          >
            Dashboard
          </Button>
          <Button 
            color="inherit" 
            component={RouterLink} 
            to="/products"
            sx={{ color: 'white', textDecoration: 'none' }}
          >
            Products
          </Button>
          <Button 
            color="inherit" 
            component={RouterLink} 
            to="/customers"
            sx={{ color: 'white', textDecoration: 'none' }}
          >
            Customers
          </Button>
          <Button 
            color="inherit" 
            component={RouterLink} 
            to="/sales"
            sx={{ color: 'white', textDecoration: 'none' }}
          >
            Sales
          </Button>
        </Toolbar>
      </AppBar>
    </Box>
  );
};

export default Navbar;