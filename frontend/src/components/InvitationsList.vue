<template>
  <div class="mt-4">
    <h3 class="text-lg font-semibold mb-2">Ваши приглашения</h3>

    <div v-if="invitations.length > 0">
      <ul>
        <li v-for="inv in invitations" :key="inv.id" class="flex justify-between items-center">
          <span>{{ inv.team.name }}</span>
          <div class="flex gap-2">
            <n-button size="small" type="success" @click="respond(inv.id, true)">Принять</n-button>
            <n-button size="small" type="error" @click="respond(inv.id, false)">Отказаться</n-button>
          </div>
        </li>
      </ul>
    </div>
    <div v-else class="text-gray-400">У вас нет приглашений</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { NButton, useMessage } from "naive-ui";
import api from "@/services/api";

const invitations = ref([]);
const message = useMessage();

async function loadInvitations() {
  try {
    const { data } = await api.get(`/users/me/invitations`);
    invitations.value = data;
  } catch (err) {
    console.error("Ошибка загрузки приглашений:", err);
  }
}

async function respond(invId, accept) {
  try {
    await api.post(`/teams/invitations/${invId}/${accept ? "accept" : "reject"}`);
    await loadInvitations();
    message.success(accept ? "Приглашение принято" : "Приглашение отклонено");
  } catch (err) {
    console.error("Ошибка ответа на приглашение:", err);
    message.error("Не удалось обработать приглашение");
  }
}

onMounted(loadInvitations);
</script>
