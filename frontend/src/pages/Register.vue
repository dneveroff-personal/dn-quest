<template>
  <n-config-provider :theme="darkTheme">
    <n-message-provider>
      <div class="flex flex-col items-center justify-center min-h-screen p-6">
        <n-card title="Регистрация" class="w-full max-w-md shadow-lg">
          <div class="flex flex-col gap-4">

            <!-- Username -->
            <n-input
                v-model:value="username"
                placeholder="Username (обязательно)"
                clearable
            />

            <!-- Email -->
            <n-input
                v-model:value="email"
                placeholder="Email (необязательно)"
                clearable
            />

            <!-- Public Name -->
            <n-input
                v-model:value="publicName"
                placeholder="Public name (ник)"
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
            <n-button type="primary" block @click="doRegister" :loading="loading">
              Зарегистрироваться
            </n-button>
            <n-button text block @click="$router.push('/login')">
              Уже есть аккаунт? Войти
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
const email = ref("");
const publicName = ref("");
const password = ref("");
const loading = ref(false);

async function doRegister() {
  // Проверяем обязательные поля
  if (!username.value.trim() || !password.value.trim()) {
    message.error("Username и Password обязательны");
    return;
  }

  loading.value = true;

  try {
    await api.post("/register", {
      username: username.value.trim(),
      email: email.value.trim(),
      publicName: publicName.value.trim(),
      password: password.value.trim()
    });
    message.success("Аккаунт создан! Теперь можно войти.");
    router.push("/login");
  } catch (err) {
    if (err.response?.status === 409) {
      message.error("Пользователь с таким именем или email уже существует");
    } else if (err.response?.status === 400) {
      message.error("Некорректные данные регистрации");
    } else {
      message.error("Ошибка регистрации");
    }
    console.error(err);
  } finally {
    loading.value = false;
  }
}
</script>
