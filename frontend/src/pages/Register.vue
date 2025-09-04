<template>
  <n-config-provider :theme="darkTheme">
    <n-message-provider>
      <div class="flex flex-col items-center justify-center min-h-screen p-6">
        <n-card title="Регистрация" class="w-full max-w-md shadow-lg">
          <form @submit.prevent="doRegister" class="flex flex-col gap-4">
            <n-input
                v-model:value="username"
                placeholder="Username (обязательно)"
                clearable
                @keyup.enter="doRegister"
            />
            <n-input
                v-model:value="email"
                placeholder="Email (необязательно)"
                clearable
                @keyup.enter="doRegister"
            />
            <n-input
                v-model:value="publicName"
                placeholder="Public name (ник)"
                clearable
                @keyup.enter="doRegister"
            />
            <n-input
                v-model:value="password"
                type="password"
                placeholder="Password"
                clearable
                @keyup.enter="doRegister"
            />

            <n-button
                type="primary"
                block
                :loading="loading"
                attr-type="submit"
            >
              Зарегистрироваться
            </n-button>

            <n-button text block @click="$router.push('/login')">
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
import { darkTheme, useMessage } from "naive-ui";
import api from "@/services/api";
import { fetchCurrentUser } from "@/services/auth";

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
    // Создаём пользователя
    await api.post("/register", {
      username: username.value,
      email: email.value,
      publicName: publicName.value,
      password: password.value,
    });

    message.success("Аккаунт создан! Выполняем вход...");

    // Сразу логиним (важно: путь зависит от бэка!)
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
    if (err.response?.status === 409) {
      message.error("Пользователь с таким именем или email уже существует");
    } else {
      message.error("Ошибка регистрации");
    }
    console.error("Ошибка регистрации", err);
  } finally {
    loading.value = false;
  }
}
</script>
