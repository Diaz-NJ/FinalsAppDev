import React, { useState, useEffect } from 'react';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import { styled } from '@mui/material/styles';
import { 
  TextField, 
  Button, 
  Paper,
  Box 
} from '@mui/material';
import { useNavigate, useParams } from 'react-router-dom';
import { 
  createProduct, 
  updateProduct, 
  getProductById 
} from '../services/api';

// Modern styling with `styled()`
const StyledPaper = styled(Paper)(({ theme }) => ({
  padding: theme.spacing(3),
  marginTop: theme.spacing(3),
}));

const StyledForm = styled('form')(({ theme }) => ({
  display: 'flex',
  flexDirection: 'column',
  gap: theme.spacing(3),
}));

const ProductForm = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const isEdit = !!id;
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
          const response = await getProductById(id);
          setInitialValues(response.data);
        } catch (error) {
          console.error('Error fetching product:', error);
        }
      };
      fetchProduct();
    }
  }, [isEdit, id]);

  const validationSchema = Yup.object({
    name: Yup.string().required('Required'),
    description: Yup.string(),
    price: Yup.number().required('Required').positive('Must be positive'),
    quantity: Yup.number().required('Required').integer('Must be integer').min(0, 'Must be ≥ 0'),
  });

  const formik = useFormik({
    initialValues,
    enableReinitialize: true,
    validationSchema,
    onSubmit: async (values) => {
      try {
        if (isEdit) {
          await updateProduct(id, values);
        } else {
          await createProduct(values);
        }
        navigate('/products'); // React Router v6 navigation
      } catch (error) {
        console.error('Error saving product:', error);
      }
    },
  });

  return (
    <StyledPaper>
      <StyledForm onSubmit={formik.handleSubmit}>
        <TextField
          fullWidth
          name="name"
          label="Product Name"
          value={formik.values.name}
          onChange={formik.handleChange}
          error={formik.touched.name && Boolean(formik.errors.name)}
          helperText={formik.touched.name && formik.errors.name}
        />
        <TextField
          fullWidth
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
          name="quantity"
          label="Quantity"
          type="number"
          value={formik.values.quantity}
          onChange={formik.handleChange}
          error={formik.touched.quantity && Boolean(formik.errors.quantity)}
          helperText={formik.touched.quantity && formik.errors.quantity}
        />
        <Button 
          type="submit" 
          variant="contained" 
          color="primary"
          sx={{ mt: 2 }}
        >
          {isEdit ? 'Update Product' : 'Add Product'}
        </Button>
      </StyledForm>
    </StyledPaper>
  );
};

export default ProductForm;