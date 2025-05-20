import React, { useState, useEffect } from 'react';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import { makeStyles } from '@mui/styles';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import Paper from '@mui/material/Paper';
import { createProduct, updateProduct, getProductById } from '../services/api';

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
}));

const ProductForm = ({ history, match }) => {
  const classes = useStyles();
  const isEdit = match.path.includes('edit');
  const [initialValues, setInitialValues] = useState({
    name: '',
    description: '',
    price: '',
    quantity: '',
  });

  useEffect(() => {
    if (isEdit) {
      const fetchProduct = async () => {
        try {
          const response = await getProductById(match.params.id);
          setInitialValues(response.data);
        } catch (error) {
          console.error('Error fetching product:', error);
        }
      };
      fetchProduct();
    }
  }, [isEdit, match.params.id]);

  const validationSchema = Yup.object({
    name: Yup.string().required('Required'),
    description: Yup.string(),
    price: Yup.number().required('Required').positive('Must be positive'),
    quantity: Yup.number().required('Required').integer('Must be integer').min(0, 'Must be 0 or more'),
  });

  const formik = useFormik({
    initialValues: initialValues,
    enableReinitialize: true,
    validationSchema: validationSchema,
    onSubmit: async (values) => {
      try {
        if (isEdit) {
          await updateProduct(match.params.id, values);
        } else {
          await createProduct(values);
        }
        history.push('/products');
      } catch (error) {
        console.error('Error saving product:', error);
      }
    },
  });

  return (
    <Paper className={classes.paper}>
      <form onSubmit={formik.handleSubmit} className={classes.form}>
        <TextField
          fullWidth
          id="name"
          name="name"
          label="Product Name"
          value={formik.values.name}
          onChange={formik.handleChange}
          error={formik.touched.name && Boolean(formik.errors.name)}
          helperText={formik.touched.name && formik.errors.name}
        />
        <TextField
          fullWidth
          id="description"
          name="description"
          label="Description"
          multiline
          rows={4}
          value={formik.values.description}
          onChange={formik.handleChange}
          error={formik.touched.description && Boolean(formik.errors.description)}
          helperText={formik.touched.description && formik.errors.description}
        />
        <TextField
          fullWidth
          id="price"
          name="price"
          label="Price"
          type="number"
          value={formik.values.price}
          onChange={formik.handleChange}
          error={formik.touched.price && Boolean(formik.errors.price)}
          helperText={formik.touched.price && formik.errors.price}
        />
        <TextField
          fullWidth
          id="quantity"
          name="quantity"
          label="Quantity"
          type="number"
          value={formik.values.quantity}
          onChange={formik.handleChange}
          error={formik.touched.quantity && Boolean(formik.errors.quantity)}
          helperText={formik.touched.quantity && formik.errors.quantity}
        />
        <Button color="primary" variant="contained" fullWidth type="submit">
          {isEdit ? 'Update Product' : 'Add Product'}
        </Button>
      </form>
    </Paper>
  );
};

export default ProductForm;