import React, { useState, useEffect } from 'react';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import { makeStyles } from '@mui/styles';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import Paper from '@mui/material/Paper';
import MenuItem from '@mui/material/MenuItem';
import { Typography } from '@mui/material';
import { 
  createSale, 
  updateSale, 
  getSaleById, 
  getCustomers, 
  getProducts 
} from '../services/api';

const useStyles = makeStyles((theme) => ({
  paper: {
    padding: theme.spacing(3),
    marginTop: theme.spacing(3),
  },
  form: {
    display: 'flex',
    flexDirection: 'column',
    gap: theme.spacing(3),
  },
  itemsContainer: {
    marginTop: theme.spacing(3),
  },
  itemRow: {
    display: 'flex',
    gap: theme.spacing(2),
    marginBottom: theme.spacing(2),
  },
}));

const SaleForm = ({ history, match }) => {
  const classes = useStyles();
  const isEdit = match.path.includes('edit');
  const [customers, setCustomers] = useState([]);
  const [products, setProducts] = useState([]);
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
          const saleRes = await getSaleById(match.params.id);
          setInitialValues(saleRes.data);
        }
      } catch (error) {
        console.error('Error fetching data:', error);
      }
    };
    fetchData();
  }, [isEdit, match.params.id]);

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
    initialValues: initialValues,
    enableReinitialize: true,
    validationSchema: validationSchema,
    onSubmit: async (values) => {
      try {
        // Calculate total amount
        const total = values.items.reduce((sum, item) => {
          const product = products.find(p => p.id === item.product_id);
          return sum + (product.price * item.quantity);
        }, 0);

        const saleData = {
          ...values,
          total_amount: total,
        };

        if (isEdit) {
          await updateSale(match.params.id, saleData);
        } else {
          await createSale(saleData);
        }
        history.push('/sales');
      } catch (error) {
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
    <Paper className={classes.paper}>
      <form onSubmit={formik.handleSubmit} className={classes.form}>
        <TextField
          select
          fullWidth
          id="customer_id"
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
          id="sale_date"
          name="sale_date"
          label="Sale Date"
          type="date"
          InputLabelProps={{
            shrink: true,
          }}
          value={formik.values.sale_date}
          onChange={formik.handleChange}
          error={formik.touched.sale_date && Boolean(formik.errors.sale_date)}
          helperText={formik.touched.sale_date && formik.errors.sale_date}
        />

        <div className={classes.itemsContainer}>
          <Typography variant="h6">Items</Typography>
          {formik.values.items.map((item, index) => (
            <div key={index} className={classes.itemRow}>
              <TextField
                select
                style={{ flex: 2 }}
                id={`items[${index}].product_id`}
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
                style={{ flex: 1 }}
                id={`items[${index}].quantity`}
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
                style={{ flex: 1 }}
                id={`items[${index}].unit_price`}
                name={`items[${index}].unit_price`}
                label="Unit Price"
                type="number"
                value={item.unit_price}
                disabled
              />

              <Button
                variant="contained"
                color="secondary"
                onClick={() => handleRemoveItem(index)}
              >
                Remove
              </Button>
            </div>
          ))}

          <Button
            variant="contained"
            color="primary"
            onClick={handleAddItem}
            style={{ marginTop: '10px' }}
          >
            Add Item
          </Button>
        </div>

        <Button color="primary" variant="contained" fullWidth type="submit">
          {isEdit ? 'Update Sale' : 'Create Sale'}
        </Button>
      </form>
    </Paper>
  );
};

export default SaleForm;