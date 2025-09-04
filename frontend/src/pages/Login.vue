<template>
  <div class="flex items-center justify-center min-h-screen bg-gray-900">
    <n-card class="w-full max-w-md p-6 shadow-2xl rounded-2xl">
      <h2 class="text-2xl font-bold text-center mb-6">Вход</h2>
      <form @submit.prevent="doLogin" class="flex flex-col gap-4">
        <n-input v-model:value="username" placeholder="Username" clearable />
        <n-input v-model:value="password" type="password" placeholder="Password" clearable />
        <n-button type="primary" block :loading="loading" attr-type="submit">
          Войти
        </n-button>
        <n-button text block @click="$router.push('/register')">
          Нет аккаунта? Зарегистрироваться
        </n-button>
      </form>
    </n-card>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { darkTheme, useMessage } from "naive-ui";
import api from "@/services/api";
import { fetchCurrentUser } from "@/services/auth";
import { setToken } from "@/services/auth";

const router = useRouter();
const message = useMessage();

const username = ref("");
const password = ref("");
const loading = ref(false);

async function doLogin() {
  if (!username.value || !password.value) {
    message.error("Username и Password обязательны");
    return;
  }

  loading.value = true;

  try {
    const resp = await api.post("/login", {
      username: username.value,
      password: password.value,
    });

    const token = resp.data.token;
    setToken(token);

    const user = await fetchCurrentUser();
    message.success(`Добро пожаловать, ${user.publicName}`);

    router.push("/");
  } catch (err) {
    message.error("Неправильный логин или пароль");
    console.error("Ошибка логина", err);
  } finally {
    loading.value = false;
  }
}
</script>
