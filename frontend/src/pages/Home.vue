<template>
  <div
      class="flex flex-col items-center justify-start min-h-screen px-4 md:px-0 bg-[var(--color-bg)] py-8"
  >
    <h2 class="text-3xl font-bold mb-6 text-center text-[var(--color-text-strong)]">
      Список квестов
    </h2>

    <n-spin :show="loading" class="w-full">
      <div class="flex flex-col items-center gap-6 w-full">
        <quest-card
            v-for="quest in quests"
            :key="quest.id"
            :quest="quest"
            :can-edit="canEdit(quest)"
            @play="openQuest"
            @edit="editQuest"
        />
      </div>
    </n-spin>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { NSpin } from "naive-ui";
import api from "@/services/api";
import { fetchCurrentUser } from "@/services/auth";
import QuestCard from "@/components/QuestCard.vue";

const quests = ref([]);
const user = ref(null);
const loading = ref(false);
const router = useRouter();

async function loadQuests() {
  loading.value = true;
  try {
    user.value = await fetchCurrentUser();
    if (user.value == null) return;
    let response;
    if (user.value.role === "ADMIN") {
      response = await api.get("/quests");
      quests.value = response.data;
    } else if (user.value.role === "AUTHOR") {
      response = await api.get("/quests");
      quests.value = response.data.filter(
          (q) =>
              q.published ||
              q.authors?.some((a) => Number(a.id) === Number(user.value.id))
      );
    } else {
      response = await api.get("/quests/published");
      quests.value = response.data;
    }
  } catch (err) {
    console.error("Ошибка загрузки квестов", err);
  } finally {
    loading.value = false;
  }
}

function canEdit(quest) {
  if (!user.value) return false;
  if (user.value.role === "ADMIN") return true;
  if (user.value.role === "AUTHOR") {
    return quest.authors?.some((a) => a.id === user.value.id);
  }
  return false;
}

function openQuest(id) {
  router.push(`/quests/${id}`);
}

function editQuest(id) {
  router.push(`/quests/${id}/edit`);
}

onMounted(loadQuests);
</script>
