const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const pool = require('../config/database');
const { v4: uuidv4 } = require('uuid');

exports.signup = async (req, res, next) => {
  try {
    const { email, password } = req.body;

    if (!email || !password) {
      return res.status(400).json({
        success: false,
        message: 'Email and password are required'
      });
    }

    const connection = await pool.getConnection();

    try {
      // Check if user exists
      const [existingUser] = await connection.execute(
        'SELECT id FROM users WHERE email = ?',
        [email]
      );

      if (existingUser.length > 0) {
        return res.status(409).json({
          success: false,
          message: 'User already exists'
        });
      }

      // Hash password
      const hashedPassword = await bcrypt.hash(password, 10);
      const userId = uuidv4();

      // Create user
      await connection.execute(
        'INSERT INTO users (id, email, password) VALUES (?, ?, ?)',
        [userId, email, hashedPassword]
      );

      // Generate token
      const token = jwt.sign(
        { userId, email },
        process.env.JWT_SECRET,
        { expiresIn: '7d' }
      );

      res.status(201).json({
        success: true,
        message: 'User created successfully',
        data: {
          id: userId,
          email
        },
        token
      });
    } finally {
      connection.release();
    }
  } catch (error) {
    next(error);
  }
};

exports.signin = async (req, res, next) => {
  try {
    const { email, password } = req.body;

    if (!email || !password) {
      return res.status(400).json({
        success: false,
        message: 'Email and password are required'
      });
    }

    const connection = await pool.getConnection();

    try {
      // Find user
      const [users] = await connection.execute(
        'SELECT * FROM users WHERE email = ?',
        [email]
      );

      if (users.length === 0) {
        return res.status(401).json({
          success: false,
          message: 'Invalid email or password'
        });
      }

      const user = users[0];

      // Verify password
      const isPasswordValid = await bcrypt.compare(password, user.password);

      if (!isPasswordValid) {
        return res.status(401).json({
          success: false,
          message: 'Invalid email or password'
        });
      }

      // Generate token
      const token = jwt.sign(
        { userId: user.id, email: user.email },
        process.env.JWT_SECRET,
        { expiresIn: '7d' }
      );

      res.json({
        success: true,
        message: 'Login successful',
        data: {
          id: user.id,
          email: user.email,
          name: user.name
        },
        token
      });
    } finally {
      connection.release();
    }
  } catch (error) {
    next(error);
  }
};

exports.logout = async (req, res) => {
  res.json({
    success: true,
    message: 'Logged out successfully'
  });
};

exports.getCurrentUser = async (req, res, next) => {
  try {
    const connection = await pool.getConnection();

    try {
      const [users] = await connection.execute(
        'SELECT id, email, name FROM users WHERE id = ?',
        [req.userId]
      );

      if (users.length === 0) {
        return res.status(404).json({
          success: false,
          message: 'User not found'
        });
      }

      res.json({
        success: true,
        data: users[0]
      });
    } finally {
      connection.release();
    }
  } catch (error) {
    next(error);
  }
};
