import axios from 'axios';

const api = axios.create({
    baseURL: '/api',
    auth: {
        username: 'dn2@localhost.com',
        password: 'pass2'
    }
});

export default api;
