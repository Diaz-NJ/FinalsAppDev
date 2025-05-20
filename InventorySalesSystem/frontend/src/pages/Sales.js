import React from 'react';
import { Routes, Route } from 'react-router-dom';
import SaleList from '../components/SaleList';
import SaleForm from '../components/SaleForm';

const Sales = () => {
  return (
    <Routes>
      <Route index element={<SaleList />} />
      <Route path="new" element={<SaleForm />} />
      <Route path=":id/edit" element={<SaleForm />} />
    </Routes>
  );
};

export default Sales;