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
            :currentUser="props.currentUser"
            @play="openQuest"
            @edit="editQuest"
        />
      </div>
    </n-spin>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from "vue";
import { useRouter } from "vue-router";
import { NSpin } from "naive-ui";
import api from "@/services/api";
import QuestCard from "@/components/QuestCard.vue";

const props = defineProps({
  currentUser: { type: Object, default: null }
});

const quests = ref([]);
const loading = ref(false);
const router = useRouter();

async function loadQuests() {
  if (!props.currentUser) return;
  loading.value = true;
  try {
    let response;
    if (props.currentUser.role === "ADMIN") {
      response = await api.get("/quests");
      quests.value = response.data;
    } else if (props.currentUser.role === "AUTHOR") {
      response = await api.get("/quests");
      quests.value = response.data.filter(
          (q) =>
              q.published ||
              q.authors?.some((a) => Number(a.id) === Number(props.currentUser.id))
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
  if (!props.currentUser) return false;
  if (props.currentUser.role === "ADMIN") return true;
  if (props.currentUser.role === "AUTHOR") {
    return quest.authors?.some((a) => a.id === props.currentUser.id);
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

// перезагрузка квестов при смене текущего пользователя
watch(() => props.currentUser, loadQuests);
</script>
