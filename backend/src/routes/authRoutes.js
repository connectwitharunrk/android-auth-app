const express = require('express');
const router = express.Router();
const authController = require('../controllers/authController');
const { authenticate } = require('../middleware/auth');

router.post('/signup', authController.signup);
router.post('/signin', authController.signin);
router.post('/logout', authenticate, authController.logout);
router.get('/me', authenticate, authController.getCurrentUser);

module.exports = router;
