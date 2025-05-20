import React from 'react';
import { Route, Routes, useMatch } from 'react-router-dom';
import CustomerList from '../components/CustomerList';
import CustomerForm from '../components/CustomerForm';

const Customers = ({ history }) => {
  const { path } = useMatch();

  return (
    <Routes>
      <Route exact path={path}>
        <CustomerList history={history} />
      </Route>
      <Route path={`${path}/new`}>
        <CustomerForm history={history} />
      </Route>
      <Route path={`${path}/:id/edit`}>
        <CustomerForm history={history} />
      </Route>
    </Routes>
  );
};

export default Customers;