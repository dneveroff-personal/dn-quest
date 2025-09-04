<template>
  <n-config-provider :theme="darkTheme">
    <n-message-provider>
      <div class="flex flex-col items-center justify-center min-h-screen p-6">
        <n-card title="Login" class="w-full max-w-md shadow-lg">
          <!-- Форма -->
          <form @submit.prevent="doLogin" class="flex flex-col gap-4">
            <!-- input username -->
            <n-input
                v-model:value="username"
                placeholder="Username"
                clearable
                @keyup.enter="doLogin"
            />

            <!-- input password -->
            <n-input
                v-model:value="password"
                type="password"
                placeholder="Password"
                clearable
                @keyup.enter="doLogin"
            />

            <!-- кнопка -->
            <n-button
                type="primary"
                block
                :loading="loading"
                attr-type="submit"
            >
            Войти
            </n-button>

            <n-button text block @click="$router.push('/register')">
              Нет аккаунта? Зарегистрироваться
            </n-button>
          </form>
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
import { fetchCurrentUser } from "@/services/auth";

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
    localStorage.setItem("token", token);

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
