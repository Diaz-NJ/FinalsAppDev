import React, { useState, useEffect } from 'react';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import { 
  TextField, 
  Button, 
  Paper, 
  MenuItem, 
  Typography,
  Box,
  Alert
} from '@mui/material';
import { useNavigate, useParams } from 'react-router-dom';
import { 
  createSale, 
  updateSale, 
  getSaleById, 
  getCustomers, 
  getProducts 
} from '../services/api';

const SaleForm = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const isEdit = !!id;
  const [customers, setCustomers] = useState([]);
  const [products, setProducts] = useState([]);
  const [error, setError] = useState(null);
  const [initialValues, setInitialValues] = useState({
    customer_id: '',
    sale_date: new Date().toISOString().split('T')[0],
    items: [{ product_id: '', quantity: 1, unit_price: 0 }],
    total_amount: 0,
  });

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [customersRes, productsRes] = await Promise.all([
          getCustomers(),
          getProducts(),
        ]);
        setCustomers(customersRes.data);
        setProducts(productsRes.data);

        if (isEdit) {
          const saleRes = await getSaleById(id);
          setInitialValues(saleRes.data);
        }
      } catch (error) {
        setError('Failed to load form data');
        console.error('Error fetching data:', error);
      }
    };
    fetchData();
  }, [isEdit, id]);

  const validationSchema = Yup.object({
    customer_id: Yup.string().required('Required'),
    sale_date: Yup.date().required('Required'),
    items: Yup.array().of(
      Yup.object().shape({
        product_id: Yup.string().required('Required'),
        quantity: Yup.number().required('Required').min(1, 'Must be at least 1'),
      })
    ).min(1, 'At least one item is required'),
  });

  const formik = useFormik({
    initialValues,
    enableReinitialize: true,
    validationSchema,
    onSubmit: async (values) => {
      try {
        const total = values.items.reduce((sum, item) => {
          const product = products.find(p => p.id === item.product_id);
          return sum + (product.price * item.quantity);
        }, 0);

        const saleData = {
          ...values,
          total_amount: total,
        };

        if (isEdit) {
          await updateSale(id, saleData);
        } else {
          await createSale(saleData);
        }
        navigate('/sales');
      } catch (error) {
        setError('Failed to save sale');
        console.error('Error saving sale:', error);
      }
    },
  });

  const handleAddItem = () => {
    formik.setFieldValue('items', [
      ...formik.values.items,
      { product_id: '', quantity: 1, unit_price: 0 },
    ]);
  };

  const handleRemoveItem = (index) => {
    const newItems = [...formik.values.items];
    newItems.splice(index, 1);
    formik.setFieldValue('items', newItems);
  };

  const handleProductChange = (index, productId) => {
    const product = products.find(p => p.id === productId);
    if (product) {
      const newItems = [...formik.values.items];
      newItems[index] = {
        ...newItems[index],
        product_id: productId,
        unit_price: product.price,
      };
      formik.setFieldValue('items', newItems);
    }
  };

  return (
    <Paper sx={{ p: 3, mt: 3 }}>
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      <Box 
        component="form" 
        onSubmit={formik.handleSubmit}
        sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}
      >
        <TextField
          select
          fullWidth
          name="customer_id"
          label="Customer"
          value={formik.values.customer_id}
          onChange={formik.handleChange}
          error={formik.touched.customer_id && Boolean(formik.errors.customer_id)}
          helperText={formik.touched.customer_id && formik.errors.customer_id}
        >
          {customers.map((customer) => (
            <MenuItem key={customer.id} value={customer.id}>
              {customer.name}
            </MenuItem>
          ))}
        </TextField>

        <TextField
          fullWidth
          name="sale_date"
          label="Sale Date"
          type="date"
          InputLabelProps={{ shrink: true }}
          value={formik.values.sale_date}
          onChange={formik.handleChange}
          error={formik.touched.sale_date && Boolean(formik.errors.sale_date)}
          helperText={formik.touched.sale_date && formik.errors.sale_date}
        />

        <Box sx={{ mt: 3 }}>
          <Typography variant="h6">Items</Typography>
          {formik.values.items.map((item, index) => (
            <Box key={index} sx={{ display: 'flex', gap: 2, mb: 2 }}>
              <TextField
                select
                sx={{ flex: 2 }}
                name={`items[${index}].product_id`}
                label="Product"
                value={item.product_id}
                onChange={(e) => handleProductChange(index, e.target.value)}
                error={
                  formik.touched.items?.[index]?.product_id && 
                  Boolean(formik.errors.items?.[index]?.product_id)
                }
                helperText={
                  formik.touched.items?.[index]?.product_id && 
                  formik.errors.items?.[index]?.product_id
                }
              >
                {products.map((product) => (
                  <MenuItem key={product.id} value={product.id}>
                    {product.name} (${product.price.toFixed(2)})
                  </MenuItem>
                ))}
              </TextField>

              <TextField
                sx={{ flex: 1 }}
                name={`items[${index}].quantity`}
                label="Quantity"
                type="number"
                value={item.quantity}
                onChange={formik.handleChange}
                error={
                  formik.touched.items?.[index]?.quantity && 
                  Boolean(formik.errors.items?.[index]?.quantity)
                }
                helperText={
                  formik.touched.items?.[index]?.quantity && 
                  formik.errors.items?.[index]?.quantity
                }
              />

              <TextField
                sx={{ flex: 1 }}
                name={`items[${index}].unit_price`}
                label="Unit Price"
                type="number"
                value={item.unit_price}
                disabled
              />

              <Button
                variant="contained"
                color="error"
                onClick={() => handleRemoveItem(index)}
              >
                Remove
              </Button>
            </Box>
          ))}

          <Button
            variant="contained"
            onClick={handleAddItem}
            sx={{ mt: 1 }}
          >
            Add Item
          </Button>
        </Box>

        <Button 
          type="submit" 
          variant="contained" 
          fullWidth
          sx={{ mt: 2 }}
        >
          {isEdit ? 'Update Sale' : 'Create Sale'}
        </Button>
      </Box>
    </Paper>
  );
};

export default SaleForm;