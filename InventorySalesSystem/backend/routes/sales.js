const express = require('express');
const router = express.Router();
const { getSales, createSale, deleteSale, getSaleById, updateSale } = require('../controllers/sales');

router.get('/', getSales);
router.get('/:id', getSaleById);
router.post('/', createSale);
router.put('/:id', updateSale);
router.delete('/:id', deleteSale);

module.exports = router;