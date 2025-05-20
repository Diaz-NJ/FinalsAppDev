import { Routes, Route } from 'react-router-dom';
import CustomerList from '../components/CustomerList';
import CustomerForm from '../components/CustomerForm';

const Customers = () => {
  return (
    <Routes>
      <Route index element={<CustomerList />} />
      <Route path="new" element={<CustomerForm />} />
      <Route path=":id/edit" element={<CustomerForm />} />
    </Routes>
  );
};

export default Customers;