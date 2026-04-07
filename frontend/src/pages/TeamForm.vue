<!-- src/pages/TeamForm.vue -->
<template>
  <div class="flex items-center justify-center min-h-screen w-full px-4 bg-[var(--color-bg)]">
    <n-card class="w-full max-w-lg p-8 shadow-2xl rounded-3xl bg-[var(--color-bg-card)] text-[var(--color-text)]">
      <h2 class="text-2xl font-bold mb-6">Создать команду</h2>

      <n-form @submit.prevent="handleSubmit" label-placement="top">
        <n-form-item label="Название команды">
          <n-input v-model:value="teamName" placeholder="Введите название команды" />
        </n-form-item>

        <div class="flex justify-end mt-6">
          <n-button type="primary" @click="handleSubmit">Создать</n-button>
        </div>
      </n-form>
    </n-card>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { NCard, NForm, NFormItem, NInput, NButton, useMessage } from "naive-ui";
import api from "@/services/api";
import { fetchCurrentUser } from "@/services/auth";

const router = useRouter();
const message = useMessage();
const teamName = ref("");

async function handleSubmit() {
  try {
    const currentUser = await fetchCurrentUser();
    if (!currentUser) {
      message.error("Вы должны быть авторизованы");
      return;
    }

    const { data } = await api.post("/teams", null, {
      params: {
        name: teamName.value,
        captainUserId: currentUser.id
      }
    });

    message.success(`Команда "${data.name}" создана!`);
    router.push(`/teams/${data.id}`);
  } catch (err) {
    console.error("Ошибка создания команды", err);
    message.error("Не удалось создать команду");
  }
}
</script>
