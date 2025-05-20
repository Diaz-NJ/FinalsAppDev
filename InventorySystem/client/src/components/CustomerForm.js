import React, { useState, useEffect } from 'react';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import { makeStyles } from '@mui/styles';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import Paper from '@mui/material/Paper';
import { createCustomer, updateCustomer, getCustomerById } from '../services/api';

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

const CustomerForm = ({ history, match }) => {
  const classes = useStyles();
  const isEdit = match.path.includes('edit');
  const [initialValues, setInitialValues] = useState({
    name: '',
    email: '',
    phone: '',
    address: '',
  });

  useEffect(() => {
    if (isEdit) {
      const fetchCustomer = async () => {
        try {
          const response = await getCustomerById(match.params.id);
          setInitialValues(response.data);
        } catch (error) {
          console.error('Error fetching customer:', error);
        }
      };
      fetchCustomer();
    }
  }, [isEdit, match.params.id]);

  const validationSchema = Yup.object({
    name: Yup.string().required('Required'),
    email: Yup.string().email('Invalid email'),
    phone: Yup.string(),
    address: Yup.string(),
  });

  const formik = useFormik({
    initialValues: initialValues,
    enableReinitialize: true,
    validationSchema: validationSchema,
    onSubmit: async (values) => {
      try {
        if (isEdit) {
          await updateCustomer(match.params.id, values);
        } else {
          await createCustomer(values);
        }
        history.push('/customers');
      } catch (error) {
        console.error('Error saving customer:', error);
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
          label="Customer Name"
          value={formik.values.name}
          onChange={formik.handleChange}
          error={formik.touched.name && Boolean(formik.errors.name)}
          helperText={formik.touched.name && formik.errors.name}
        />
        <TextField
          fullWidth
          id="email"
          name="email"
          label="Email"
          value={formik.values.email}
          onChange={formik.handleChange}
          error={formik.touched.email && Boolean(formik.errors.email)}
          helperText={formik.touched.email && formik.errors.email}
        />
        <TextField
          fullWidth
          id="phone"
          name="phone"
          label="Phone"
          value={formik.values.phone}
          onChange={formik.handleChange}
          error={formik.touched.phone && Boolean(formik.errors.phone)}
          helperText={formik.touched.phone && formik.errors.phone}
        />
        <TextField
          fullWidth
          id="address"
          name="address"
          label="Address"
          multiline
          rows={4}
          value={formik.values.address}
          onChange={formik.handleChange}
          error={formik.touched.address && Boolean(formik.errors.address)}
          helperText={formik.touched.address && formik.errors.address}
        />
        <Button color="primary" variant="contained" fullWidth type="submit">
          {isEdit ? 'Update Customer' : 'Add Customer'}
        </Button>
      </form>
    </Paper>
  );
};

export default CustomerForm;