<!-- src/pages/TeamPage.vue -->
<template>
  <div class="flex justify-center w-full px-4 py-8 bg-[var(--color-bg)]">
    <n-card class="w-full max-w-2xl shadow-lg rounded-2xl p-6 bg-[var(--color-bg-card)] text-[var(--color-text)]">
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
    </n-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRoute } from "vue-router";
import { NCard } from "naive-ui";
import api from "@/services/api";

const route = useRoute();
const team = ref(null);

async function loadTeam() {
  try {
    const { data } = await api.get(`/teams/${route.params.id}`);
    team.value = data;
  } catch (err) {
    console.error("Ошибка загрузки команды", err);
  }
}

onMounted(loadTeam);
</script>
