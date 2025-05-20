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
import { getProducts, deleteProduct } from '../services/api';

const useStyles = makeStyles({
  table: {
    minWidth: 650,
  },
});

const ProductList = ({ history }) => {
  const classes = useStyles();
  const [products, setProducts] = useState([]);

  useEffect(() => {
    fetchProducts();
  }, []);

  const fetchProducts = async () => {
    try {
      const response = await getProducts();
      setProducts(response.data);
    } catch (error) {
      console.error('Error fetching products:', error);
    }
  };

  const handleDelete = async (id) => {
    try {
      await deleteProduct(id);
      fetchProducts();
    } catch (error) {
      console.error('Error deleting product:', error);
    }
  };

  return (
    <TableContainer component={Paper}>
      <Button 
        variant="contained" 
        color="primary" 
        onClick={() => history.push('/products/new')}
        style={{ margin: '20px' }}
      >
        Add Product
      </Button>
      <Table className={classes.table} aria-label="simple table">
        <TableHead>
          <TableRow>
            <TableCell>ID</TableCell>
            <TableCell>Name</TableCell>
            <TableCell>Description</TableCell>
            <TableCell align="right">Price</TableCell>
            <TableCell align="right">Quantity</TableCell>
            <TableCell>Actions</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {products.map((product) => (
            <TableRow key={product.id}>
              <TableCell>{product.id}</TableCell>
              <TableCell>{product.name}</TableCell>
              <TableCell>{product.description}</TableCell>
              <TableCell align="right">${product.price.toFixed(2)}</TableCell>
              <TableCell align="right">{product.quantity}</TableCell>
              <TableCell>
                <Button 
                  variant="contained" 
                  color="primary" 
                  size="small"
                  onClick={() => history.push(`/products/${product.id}`)}
                  style={{ marginRight: '10px' }}
                >
                  Edit
                </Button>
                <Button 
                  variant="contained" 
                  color="secondary" 
                  size="small"
                  onClick={() => handleDelete(product.id)}
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

export default ProductList;