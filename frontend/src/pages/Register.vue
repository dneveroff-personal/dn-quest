<template>
  <div class="flex items-center justify-center min-h-screen w-full px-4 bg-[var(--color-bg)]">
    <n-card class="w-full max-w-3xl min-w-[40vw] p-12 shadow-2xl rounded-3xl bg-[var(--color-bg-card)] text-[var(--color-text)]">
      <h2 class="text-3xl font-bold text-center mb-8 text-[var(--color-text-strong)]">Регистрация</h2>
      <form @submit.prevent="doRegister" class="flex flex-col gap-6">
        <n-input v-model:value="username" placeholder="Username (обязательно)" clearable size="large"
                 class="rounded-xl p-5 text-xl text-[var(--color-text)] bg-[var(--color-bg)] focus:ring-2 focus:ring-[var(--color-accent)] transition-all duration-300" />
        <n-input v-model:value="email" placeholder="Email (необязательно)" clearable size="large"
                 class="rounded-xl p-5 text-xl text-[var(--color-text)] bg-[var(--color-bg)] focus:ring-2 focus:ring-[var(--color-accent)] transition-all duration-300" />
        <n-input v-model:value="publicName" placeholder="Public name (ник)" clearable size="large"
                 class="rounded-xl p-5 text-xl text-[var(--color-text)] bg-[var(--color-bg)] focus:ring-2 focus:ring-[var(--color-accent)] transition-all duration-300" />
        <n-input v-model:value="password" type="password" placeholder="Password" clearable size="large"
                 class="rounded-xl p-5 text-xl text-[var(--color-text)] bg-[var(--color-bg)] focus:ring-2 focus:ring-[var(--color-accent)] transition-all duration-300" />

        <n-button type="primary" block :loading="loading" attr-type="submit"
                  class="btn-accent rounded-xl py-4 text-lg font-semibold hover:bg-transparent hover:text-[var(--color-accent)] hover:border hover:border-[var(--color-accent)] active:scale-95 transition-all duration-200">
          Зарегистрироваться
        </n-button>

        <n-button text block @click="$router.push('/login')" size="large"
                  class="rounded-xl py-4 text-lg font-semibold text-[var(--color-text)] hover:text-[var(--color-accent)] active:scale-95 transition-all duration-200">
          Уже есть аккаунт? Войти
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
import { fetchCurrentUser, setToken } from "@/services/auth";

const router = useRouter();
const message = useMessage();

const username = ref("");
const email = ref("");
const publicName = ref("");
const password = ref("");
const loading = ref(false);

async function doRegister() {
  if (!username.value || !password.value) {
    message.error("Username и Password обязательны");
    return;
  }

  loading.value = true;
  try {
    await api.post("/register", { username: username.value, email: email.value, publicName: publicName.value, password: password.value });
    message.success("Аккаунт создан! Выполняем вход...");

    const resp = await api.post("/login", { username: username.value, password: password.value });
    setToken(resp.data.token);

    const user = await fetchCurrentUser();
    message.success(`Добро пожаловать, ${user.publicName}`);

    router.push("/");
  } catch (err) {
    if (err.response?.status === 409) message.error("Пользователь с таким именем или email уже существует");
    else message.error("Ошибка регистрации");
    console.error("Ошибка регистрации", err);
  } finally {
    loading.value = false;
  }
}
</script>
