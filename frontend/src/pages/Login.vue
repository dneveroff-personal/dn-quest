<template>
  <n-config-provider :theme="darkTheme">
    <n-message-provider>
      <div class="flex flex-col items-center justify-center min-h-screen p-6">
        <n-card title="Вход" class="w-full max-w-md shadow-lg">
          <div class="flex flex-col gap-4">

            <!-- Username -->
            <n-input
                v-model:value="username"
                placeholder="Username"
                clearable
            />

            <!-- Password -->
            <n-input
                type="password"
                v-model:value="password"
                placeholder="Password"
                clearable
            />

            <!-- Buttons -->
            <n-button type="primary" block @click="doLogin" :loading="loading">
              Войти
            </n-button>
            <n-button text block @click="$router.push('/register')">
              Нет аккаунта? Зарегистрироваться
            </n-button>

          </div>
        </n-card>
      </div>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { darkTheme, useMessage } from "naive-ui";
import api from "@/services/api";

const router = useRouter();
const message = useMessage();

const username = ref("");
const password = ref("");
const loading = ref(false);

async function doLogin() {
  // Проверяем обязательные поля
  if (!username.value.trim() || !password.value.trim()) {
    message.error("Username и Password обязательны!");
    return;
  }

  loading.value = true;

  try {
    const response = await api.post("/login", {
      username: username.value.trim(),
      password: password.value.trim()
    });

    // Сохраняем токен, если бэкенд его возвращает
    if (response.data?.token) {
      localStorage.setItem("token", response.data.token);
    }

    message.success("Вы вошли в систему!");
    router.push("/"); // или ваша главная страница
  } catch (err) {
    if (err.response?.status === 401) {
      message.error("Неверный username или password");
    } else {
      message.error("Ошибка входа");
    }
    console.error(err);
  } finally {
    loading.value = false;
  }
}
</script>
