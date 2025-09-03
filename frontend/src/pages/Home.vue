<template>
  <div>
    <h2 class="text-2xl font-bold mb-6 text-center">Список квестов</h2>

    <n-spin :show="loading">
      <n-grid :x-gap="16" :y-gap="16" cols="1 400:2 800:3">
        <n-grid-item v-for="quest in quests" :key="quest.id">
          <n-card hoverable>
            <template #cover>
              <n-image
                  v-if="quest.imageUrl"
                  :src="quest.imageUrl"
                  alt="Quest cover"
                  class="rounded-t-xl"
                  height="180"
                  object-fit="cover"
              />
            </template>

            <h3 class="text-lg font-semibold mb-2">{{ quest.title }}</h3>
            <p class="text-sm text-gray-400 mb-4">
              {{ quest.description }}
            </p>

            <template #action>
              <n-button type="primary" @click="openQuest(quest.id)">
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
    quests.value = response.data; // предполагаю, что возвращается массив DTO
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
