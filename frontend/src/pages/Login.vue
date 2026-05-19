<template>
  <div class="flex items-center justify-center min-h-screen w-full px-4 bg-[var(--color-bg)]">
    <n-card
        class="w-full max-w-3xl min-w-[40vw] p-12 shadow-2xl rounded-3xl bg-[var(--color-bg-card)] text-[var(--color-text)]"
    >
      <h2 class="text-3xl font-bold text-center mb-8 text-[var(--color-text-strong)]">Вход</h2>
      <form @submit.prevent="doLogin" class="flex flex-col gap-6">
        <n-input
            v-model:value="username"
            placeholder="Username"
            clearable
            size="large"
            class="rounded-xl p-5 text-xl text-[var(--color-text)] bg-[var(--color-bg)] focus:ring-2 focus:ring-[var(--color-accent)] transition-all duration-300"
        />
        <n-input
            v-model:value="password"
            type="password"
            placeholder="Password"
            clearable
            size="large"
            class="rounded-xl p-5 text-xl text-[var(--color-text)] bg-[var(--color-bg)] focus:ring-2 focus:ring-[var(--color-accent)] transition-all duration-300"
        />

        <n-button
            type="primary"
            block
            :loading="loading"
            attr-type="submit"
            class="btn-accent rounded-xl py-4 text-lg font-semibold hover:bg-transparent hover:text-[var(--color-accent)] hover:border hover:border-[var(--color-accent)] active:scale-95 transition-all duration-200"
        >
          Войти
        </n-button>

        <n-button
            text
            block
            @click="$router.push('/register')"
            size="large"
            class="rounded-xl py-4 text-lg font-semibold text-[var(--color-text)] hover:text-[var(--color-accent)] active:scale-95 transition-all duration-200"
        >
          Нет аккаунта? Зарегистрироваться
        </n-button>
      </form>
    </n-card>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { useMessage } from "naive-ui";
import api from "@/services/api";
import { setToken, setRefreshToken, setCurrentUser } from "@/services/auth";

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
  let attempt = 0;
  const maxAttempts = 3;
  const baseDelay = 1000; // 1 second
  let lastError = null;

  while (attempt < maxAttempts) {
    try {
      const resp = await api.post("/auth/login", {
        username: username.value,
        password: password.value
      });

      // success
      setToken(resp.data.accessToken);
      setRefreshToken(resp.data.refreshToken);
      setCurrentUser(resp.data.user);
      window.dispatchEvent(new Event("user-changed"));
      message.success("Добро пожаловать!");
      router.push("/");
      return; // exit the function on success
    } catch (err) {
      lastError = err;
      if (err.response && err.response.status === 503 && attempt < maxAttempts - 1) {
        // Show retry message and wait
        const delaySeconds = baseDelay * Math.pow(2, attempt) / 1000;
        message.warning(`Сервис временно недоступен. Повторная попытка через ${delaySeconds} секунд...`);
        await new Promise(resolve => setTimeout(resolve, baseDelay * Math.pow(2, attempt)));
        attempt++;
        continue;
      } else {
        // For non-503 errors or if we've exhausted retries for 503, break the loop
        break;
      }
    }
  }

  // If we reach here, all attempts failed
  if (lastError) {
    if (lastError.response && lastError.response.status === 401) {
      message.error("Неправильный логин или пароль");
    } else if (lastError.response && lastError.response.status === 503) {
      message.error("Сервис временно недоступен. Пожалуйста, попробуйте позже.");
    } else {
      message.error("Произошла ошибка при входе. Пожалуйста, попробуйте позже.");
    }
    console.error("Ошибка логина", lastError);
  }

  loading.value = false;
}
</script>
