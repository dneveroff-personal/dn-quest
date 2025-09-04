// src/router/index.js
import { createRouter, createWebHistory } from "vue-router";
import Register from "@/pages/Register.vue";
import Login from "@/pages/Login.vue";
import Home from "@/pages/Home.vue";
import { getToken, fetchCurrentUser } from "@/services/auth";

const routes = [
    { path: "/", component: Home },
    { path: "/login", component: Login },
    { path: "/register", component: Register }
];

const router = createRouter({
    history: createWebHistory(),
    routes
});

router.beforeEach(async (to, from, next) => {
    const token = getToken();

    if (to.meta.requiresAuth) {
        if (!token) {
            return next("/login");
        }

        // Можно проверять валидность токена
        const user = await fetchCurrentUser();
        if (!user) return next("/login");
    }

    next();
});

export default router;
