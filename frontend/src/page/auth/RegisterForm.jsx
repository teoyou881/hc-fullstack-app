import React, {useState} from 'react';
import {useNavigate} from 'react-router-dom';
import userService from '../../services/user/userService.js';
// Uncomment to use the 'axios' library if installed
// import axios from 'axios';

function RegisterForm() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [username, setUsername] = useState(''); // Optional field
  const [phoneNumber, setPhoneNumber] = useState(''); // Optional field
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError('');
    setSuccess('');

    // Client-side validation
    if (!email || !password || !confirmPassword) {
      setError('Email and password are required fields.');
      return;
    }
    if (password.length < 6) {
      setError('Password must be at least 6 characters long.');
      return;
    }
    if (password !== confirmPassword) {
      setError('Password and confirmation password do not match.');
      return;
    }

    setLoading(true);

    try {
      const userData = {
        email      :email,
        password   :password,
        username   :username || null, // Send null if empty
        phoneNumber:phoneNumber || null, // Send null if empty
        // 'role' can often be set with a default value (e.g., USER) on the backend,
        // so it's not sent from here, or sent explicitly like: role: 'USER'
      };
      console.log('RegisterForm.jsx: 49', userData);
      const user = userService.createUser(userData);

      console.log('Registration attempt (simulation):', user);
      setSuccess(
          'Registration completed successfully! (Simulation) Redirecting to login page.');
      setEmail('');
      setPassword('');
      setConfirmPassword('');
      setUsername('');
      setPhoneNumber('');
      navigate('/login'); // Redirect to login page after simulation

    } catch (err) {
      console.error('Registration failed:', err);
      // Error messages might vary based on the backend response.
      setError(err.response?.data?.message ||
          'An error occurred during registration. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
      <div
          className="min-h-screen flex items-center justify-center bg-gray-100">
        <div className="bg-white p-8 rounded-lg shadow-xl w-full max-w-md">
          <h2 className="text-3xl font-bold text-center mb-6 text-gray-800">Register</h2>
          {error && (
              <div
                  className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-4"
                  role="alert">
                <span className="block sm:inline">{error}</span>
              </div>
          )}
          {success && (
              <div
                  className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded relative mb-4"
                  role="alert">
                <span className="block sm:inline">{success}</span>
              </div>
          )}
          <form onSubmit={handleSubmit} className="space-y-4">
            {/* Email (Required) */}
            <div>
              <label htmlFor="email"
                     className="block text-gray-700 text-sm font-bold mb-2">
                Email Address <span className="text-red-500">*</span>
              </label>
              <input
                  type="email"
                  id="email"
                  placeholder="Enter your email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                  className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline focus:border-blue-500"
              />
            </div>

            {/* Password (Required) */}
            <div>
              <label htmlFor="password"
                     className="block text-gray-700 text-sm font-bold mb-2">
                Password <span className="text-red-500">*</span>
              </label>
              <input
                  type="password"
                  id="password"
                  placeholder="Password (min 6 characters)"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                  className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 mb-3 leading-tight focus:outline-none focus:shadow-outline focus:border-blue-500"
              />
            </div>

            {/* Confirm Password (Required) */}
            <div>
              <label htmlFor="confirmPassword"
                     className="block text-gray-700 text-sm font-bold mb-2">
                Confirm Password <span className="text-red-500">*</span>
              </label>
              <input
                  type="password"
                  id="confirmPassword"
                  placeholder="Re-enter your password"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  required
                  className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline focus:border-blue-500"
              />
            </div>

            {/* Username (Optional) */}
            <div>
              <label htmlFor="username"
                     className="block text-gray-700 text-sm font-bold mb-2">
                Username (Optional)
              </label>
              <input
                  type="text"
                  id="username"
                  placeholder="Enter your username"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline focus:border-blue-500"
              />
            </div>

            {/* Phone Number (Optional) */}
            <div>
              <label htmlFor="phoneNumber"
                     className="block text-gray-700 text-sm font-bold mb-2">
                Phone Number (Optional)
              </label>
              <input
                  type="tel" // 'tel' type prompts numeric keypad on mobile
                  id="phoneNumber"
                  placeholder="Enter your phone number (e.g., 1-647-123-4567)"
                  value={phoneNumber}
                  onChange={(e) => setPhoneNumber(e.target.value)}
                  className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline focus:border-blue-500"
              />
            </div>

            <button
                type="submit"
                className="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline transition duration-200 ease-in-out"
                disabled={loading}
            >
              {loading ? 'Registering...' : 'Register'}
            </button>
          </form>

          <div className="text-center mt-6">
            <button
                onClick={() => navigate('/hc_h_m/login')}
                className="font-bold text-blue-600 hover:text-blue-800 text-sm focus:outline-none focus:shadow-outline"
            >
              Already have an account? Log In
            </button>
          </div>
        </div>
      </div>
  );
}

export default RegisterForm;