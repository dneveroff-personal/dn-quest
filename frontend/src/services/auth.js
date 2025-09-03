import api from "./api";

export async function login(username, password) {
    const response = await api.post("/login", { username, password });
    const token = response.data.token;
    localStorage.setItem("token", token);
    return token;
}

export function getToken() {
    return localStorage.getItem("token");
}

export function logout() {
    localStorage.removeItem("token");
}

// Axios интерцептор
api.interceptors.request.use(config => {
    const token = getToken();
    if (token) config.headers["Authorization"] = `Bearer ${token}`;
    return config;
});
