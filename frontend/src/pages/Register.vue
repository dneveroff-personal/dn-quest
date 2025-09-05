<template>
  <n-config-provider>
    <n-message-provider>
      <div class="flex items-center justify-center min-h-screen w-full px-4 bg-gradient-to-br from-purple-700 to-purple-900">
        <n-card class="w-full max-w-3xl min-w-[40vw] p-12 shadow-2xl rounded-3xl bg-purple-900 text-white">
          <h2 class="text-3xl font-bold text-center mb-8 text-white">Регистрация</h2>
          <form @submit.prevent="doRegister" class="flex flex-col gap-8">
            <n-input
                v-model:value="username"
                placeholder="Username (обязательно)"
                clearable
                size="large"
                class="rounded-xl p-4 text-lg"
            />
            <n-input
                v-model:value="email"
                placeholder="Email (необязательно)"
                clearable
                size="large"
                class="rounded-xl p-4 text-lg"
            />
            <n-input
                v-model:value="publicName"
                placeholder="Public name (ник)"
                clearable
                size="large"
                class="rounded-xl p-4 text-lg"
            />
            <n-input
                v-model:value="password"
                type="password"
                placeholder="Password"
                clearable
                size="large"
                class="rounded-xl p-4 text-lg"
            />

            <n-button
                type="primary"
                block
                :loading="loading"
                attr-type="submit"
                size="large"
                class="rounded-xl py-4 text-lg font-semibold"
            >
              Зарегистрироваться
            </n-button>
            <n-button
                text
                block
                @click="$router.push('/login')"
                size="large"
                class="rounded-xl py-4 text-lg font-semibold"
            >
              Уже есть аккаунт? Войти
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
