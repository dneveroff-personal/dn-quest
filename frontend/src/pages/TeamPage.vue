<template>
  <div class="flex justify-center w-full px-4 py-8 bg-[var(--color-bg)]">
    <n-card class="w-full max-w-2xl shadow-lg rounded-2xl p-6">
      <!-- Команда -->
      <h2 class="text-2xl font-bold mb-4">{{ team?.name }}</h2>
      <div class="mb-4 text-gray-400">
        <span class="font-medium">Капитан:</span> {{ team?.captain?.publicName }}
      </div>

      <h3 class="text-lg font-semibold mb-2">Участники:</h3>
      <ul class="list-disc list-inside space-y-1">
        <li v-for="member in team?.members" :key="member.id">
          {{ member.publicName }}
        </li>
      </ul>

      <!-- Приглашения -->
      <InvitationsList />

      <!-- Пригласить -->
      <div v-if="currentUser?.captain" class="flex mt-4 gap-2">
        <n-input v-model:value="inviteUsername" placeholder="Введите username" clearable />
        <n-button type="primary" @click="invitePlayer">Пригласить</n-button>
      </div>
    </n-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRoute } from "vue-router";
import { NButton, NCard, NInput, useMessage } from "naive-ui";
import api from "@/services/api";
import InvitationsList from "@/components/InvitationsList.vue";

const props = defineProps({
  currentUser: Object
});

const route = useRoute();
const team = ref(null);
const inviteUsername = ref("");
const message = useMessage();

async function loadTeam() {
  try {
    const { data } = await api.get(`/teams/${route.params.id}`);
    team.value = data;
  } catch (err) {
    console.error("Ошибка загрузки команды", err);
  }
}

async function invitePlayer() {
  if (!inviteUsername.value) return;
  try {
    await api.post(`/teams/${team.value.id}/invite/${inviteUsername.value}`);
    await loadTeam();
    message.success(`Приглашение отправлено игроку ${inviteUsername.value}`);
    inviteUsername.value = "";
  } catch (err) {
    console.error("Ошибка приглашения:", err);
    message.error("Не удалось отправить приглашение");
  }
}

onMounted(loadTeam);
</script>
