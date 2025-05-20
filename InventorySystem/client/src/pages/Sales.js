import React from 'react';
import { Route, Routes, useMatch } from 'react-router-dom';
import SaleList from '../components/SaleList';
import SaleForm from '../components/SaleForm';

const Sales = ({ history }) => {
  const { path } = useMatch();

  return (
    <Routes>
      <Route exact path={path}>
        <SaleList history={history} />
      </Route>
      <Route path={`${path}/new`}>
        <SaleForm history={history} />
      </Route>
      <Route path={`${path}/:id/edit`}>
        <SaleForm history={history} />
      </Route>
    </Routes>
  );
};

export default Sales;