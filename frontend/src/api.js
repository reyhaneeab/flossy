import axios from 'axios';
import { authService } from './authService';

const apiClient = axios.create({
    baseURL: 'http://localhost:8091/api', // Replace with your backend URL
    headers: {
        'Content-Type': 'application/json',
    },
});

// Add a request interceptor
apiClient.interceptors.request.use((config) => {
    const token = authService.getToken();
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// Handle response errors globally
apiClient.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response && error.response.status === 401) {
            authService.logout(); // Log out on unauthorized error
            window.location.href = '/login'; // Redirect to login page
        }
        return Promise.reject(error);
    }
);

export default apiClient;
