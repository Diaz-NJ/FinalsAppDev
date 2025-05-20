import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:5000/api',
});

export const getCustomers = () => api.get('/customers');
export const getProducts = () => api.get('/products');
export const getSales = () => api.get('/sales');
export const createCustomer = (customer) => api.post('/customers', customer);
export const createProduct = (product) => api.post('/products', product);
export const createSale = (sale) => api.post('/sales', sale);
export const updateCustomer = (id, customer) => api.put(`/customers/${id}`, customer);
export const updateProduct = (id, product) => api.put(`/products/${id}`, product);
export const deleteCustomer = (id) => api.delete(`/customers/${id}`);
export const deleteProduct = (id) => api.delete(`/products/${id}`);
export const getCustomerById = (id) => api.get(`/customers/${id}`);
export const getProductById = (id) => api.get(`/products/${id}`);
export const deleteSale = (id) => api.delete(`/sales/${id}`);
export const getSaleById = (id) => api.get(`/sales/${id}`); // Added for fetching a sale by ID
export const updateSale = (id, sale) => api.put(`/sales/${id}`, sale); // Added for updating a sale

export default api;