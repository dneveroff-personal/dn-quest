<template>
  <div class="flex flex-col items-center justify-center min-h-screen px-4 md:px-16 lg:px-32 bg-[var(--color-bg)]">
    <h2 class="text-3xl font-bold mb-8 text-center text-[var(--color-text-strong)]">Список квестов</h2>

    <n-spin :show="loading">
      <n-grid :x-gap="24" :y-gap="24" cols="1 640:2 1024:3">
        <n-grid-item v-for="quest in quests" :key="quest.id">
          <n-card hoverable class="rounded-3xl overflow-hidden shadow-2xl bg-[var(--color-bg-card)] text-[var(--color-text)]">
            <template #cover>
              <n-image v-if="quest.imageUrl" :src="quest.imageUrl" alt="Quest cover"
                       class="rounded-t-3xl" height="220" object-fit="cover" />
            </template>
            <div class="p-6 flex flex-col gap-4">
              <h3 class="text-2xl font-semibold text-[var(--color-text-strong)]">{{ quest.title }}</h3>
              <p class="opacity-80">{{ quest.description }}</p>
            </div>
            <template #action>
              <n-button block @click="openQuest(quest.id)" class="btn-accent rounded-xl py-4 text-lg font-semibold m-4">
                Играть
              </n-button>
            </template>
          </n-card>
        </n-grid-item>
      </n-grid>
    </n-spin>
  </div>
</template>


<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { NCard, NButton, NSpin, NGrid, NGridItem, NImage } from "naive-ui";
import api from "@/services/api";

const quests = ref([]);
const loading = ref(false);
const router = useRouter();

async function loadQuests() {
  loading.value = true;
  try {
    const response = await api.get("/quests/published");
    quests.value = response.data;
  } catch (err) {
    console.error("Ошибка загрузки квестов", err);
  } finally {
    loading.value = false;
  }
}

function openQuest(id) {
  router.push(`/quests/${id}`);
}

onMounted(loadQuests);
</script>
