// src/router/index.js
import { createRouter, createWebHistory } from "vue-router";
import { getToken, fetchCurrentUser } from "@/services/auth";
import Home from "@/pages/Home.vue";
import Login from "@/pages/Login.vue";
import Register from "@/pages/Register.vue";
import ManageUsers from "@/pages/ManageUsers.vue";

const routes = [
    { path: "/", component: Home },
    { path: "/login", component: Login },
    { path: "/register", component: Register },
    { path: "/admin/users/manage", component: ManageUsers, meta: { requiresAuth: true, role: "ADMIN" } },
    { path: "/quests/create", component: () => import("@/pages/QuestForm.vue"),
        meta: { requiresAuth: true, role: "AUTHOR" }
    },
    { path: "/quests/:id/edit", component: () => import("@/pages/QuestForm.vue"),
        meta: { requiresAuth: true, role: "AUTHOR" }
    }
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
