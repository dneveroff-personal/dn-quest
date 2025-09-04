import api from "./api";

export function getToken() {
    return localStorage.getItem("token");
}

export function setToken(token) {
    localStorage.setItem("token", token);
    window.dispatchEvent(new Event("user-changed"));
}

export function logout() {
    localStorage.removeItem("token");
    window.dispatchEvent(new Event("user-changed"));
}

export async function fetchCurrentUser() {
    const token = getToken();
    if (!token) return null;
    try {
        const resp = await api.get("/users/me");
        return resp.data;
    } catch (err) {
        console.error("fetchCurrentUser failed", err);
        return null;
    }
}
