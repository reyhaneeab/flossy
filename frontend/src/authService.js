import axios from 'axios';
import {jwtDecode} from "jwt-decode";

const API_URL = 'http://localhost:8091/api';

export const authService = {
    login(email, password) {
        return axios.post(`${API_URL}/user/login`, {"email": email.value, "password": password.value}).then((response) => {
            localStorage.setItem('token', response.data.token);
            return response;
        });
    },

    logout() {
        localStorage.removeItem('token'); // Clear token from storage
    },

    getToken() {
        return localStorage.getItem('token');
    },

    isTokenExpired() {
        const token = this.getToken();
        if (!token) return true;

        const { exp } = jwtDecode(token);
        const currentTime = Math.floor(Date.now() / 1000);
        return exp < currentTime;
    },

    isLoggedIn() {
        const token = this.getToken();
        return token && !this.isTokenExpired();
    },
};
