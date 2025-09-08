<template>
  <div
      class="flex flex-col items-center justify-start min-h-screen px-4 md:px-0 bg-[var(--color-bg)] py-8"
  >
    <h2
        class="text-3xl font-bold mb-6 text-center text-[var(--color-text-strong)]"
    >
      Список квестов 07
    </h2>

    <n-spin :show="loading" class="w-full">
      <div class="flex flex-col items-center gap-6 w-full">
        <n-card
            v-for="quest in quests"
            :key="quest.id"
            hoverable
            class="quest-card rounded-3xl overflow-hidden shadow-2xl bg-[var(--color-bg-card)] text-[var(--color-text)] flex flex-col justify-between w-full"
        >
          <div class="flex flex-col md:flex-row gap-4 p-6">
            <!-- левый блок -->
            <div class="w-full md:w-40 flex-shrink-0">
              <div class="h-36 w-full rounded-xl bg-[var(--color-bg)] flex items-center justify-center border border-white/6">
                <div class="text-sm opacity-80 text-[var(--color-text)]">
                  {{ quest.type }}
                  <div class="mt-2 text-xs opacity-60">{{ quest.difficulty }}</div>
                </div>
              </div>
            </div>

            <!-- правый блок -->
            <div class="flex-1 flex flex-col">
              <div class="flex items-start justify-between gap-4">
                <div class="flex-1">
                  <h3 class="text-2xl font-semibold text-[var(--color-text-strong)] leading-tight">
                    {{ quest.title }}
                  </h3>
                  <div class="mt-2 text-sm text-[var(--color-text)] opacity-80" v-html="quest.descriptionHtml"></div>
                </div>
                <div class="flex flex-col items-end gap-2 text-right">
                  <div class="text-sm opacity-70">{{ new Date(quest.startAt).toLocaleString() }}</div>
                  <div class="text-sm opacity-70">{{ new Date(quest.endAt).toLocaleString() }}</div>
                  <div v-if="quest.startAt" class="mt-2 text-xs bg-white/5 px-2 py-1 rounded text-[var(--color-text)]">
                    До старта: {{ timeUntil(quest.startAt) }}
                  </div>
                </div>
              </div>

              <div class="mt-4 flex items-center justify-between">
                <div class="flex items-center gap-2">
                  <span class="text-xs px-2 py-1 rounded bg-white/5">{{ quest.difficulty }}</span>
                  <span class="text-xs px-2 py-1 rounded bg-white/5">{{ quest.type }}</span>
                  <div class="flex items-center gap-2 ml-3">
                <span
                    v-for="a in quest.authors"
                    :key="a.id"
                    class="text-xs px-2 py-1 rounded bg-[var(--color-bg)] border border-white/6 text-[var(--color-text)]"
                >
                  {{ a.publicName }}
                </span>
                  </div>
                </div>
                <div class="text-sm opacity-60">#{{ quest.id }}</div>
              </div>
            </div>
          </div>

          <!-- кнопки -->
          <div class="flex gap-3 p-4 border-t border-white/6 bg-[var(--color-bg-card)]">
            <n-button
                class="btn-accent rounded-xl py-3 text-lg font-semibold flex-1"
                @click="openQuest(quest.id)"
            >
              Играть
            </n-button>

            <n-button
                v-if="canEdit(quest)"
                type="warning"
                class="rounded-xl py-3 font-semibold"
                @click="editQuest(quest.id)"
            >
              Редактировать
            </n-button>
          </div>
        </n-card>
      </div>
    </n-spin>

  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { NCard, NButton, NSpin } from "naive-ui";
import api from "@/services/api";
import { fetchCurrentUser } from "@/services/auth";

const quests = ref([]);
const user = ref(null);
const loading = ref(false);
const router = useRouter();

async function loadQuests() {
  loading.value = true;
  try {
    user.value = await fetchCurrentUser();
    if (user.value == null) return
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

function timeUntil(date) {
  const now = new Date();
  const target = new Date(date);
  const diff = target - now;
  if (diff <= 0) return "уже начался";
  const days = Math.floor(diff / (1000 * 60 * 60 * 24));
  const hours = Math.floor((diff / (1000 * 60 * 60)) % 24);
  const minutes = Math.floor((diff / (1000 * 60)) % 60);
  return `${days}д ${hours}ч ${minutes}м`;
}

onMounted(loadQuests);
</script>
