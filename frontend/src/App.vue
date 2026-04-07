<template>
  <n-config-provider :theme="darkTheme" :locale="ruRU" :date-locale="dateRuRU">
    <n-message-provider>
      <n-loading-bar-provider>
        <n-dialog-provider>
          <n-notification-provider>
            <n-layout class="min-h-screen bg-[var(--color-bg)] transition-colors duration-300">

              <!-- GAME MODE -->
              <template v-if="isGameMode">
                <GameHeader
                    :currentUser="currentUser"
                    :theme="theme"
                    :questId="questId"
                    @exit="exitGame"
                    @viewStats="goToStats"
                />
                <n-layout-content class="transition-all duration-300">
                  <router-view :currentUser="currentUser" />
                </n-layout-content>
              </template>

              <!-- NORMAL LAYOUT -->
              <template v-else>
                <!-- Заголовок (с переключателем темы) -->
                <div class="flex items-center justify-between mb-6 p-4 bg-[var(--color-bg-card)] shadow-sm">
                  <div class="flex items-center gap-4">
                    <h1 class="text-2xl font-bold text-[var(--color-text-strong)] flex items-center gap-2">
                      <span class="text-3xl">🎯</span>
                      DN Quest Engine
                    </h1>
                  </div>
                  
                  <!-- Переключатель темы -->
                  <n-select
                      v-model:value="theme"
                      @update:value="applyTheme"
                      :options="themeOptions"
                      placeholder="Выберите тему"
                      class="w-40"
                  />
                </div>

                <!-- HEADER -->
                <n-layout-header bordered class="p-4 flex items-center justify-between bg-[var(--color-bg-card)] shadow-sm">
                  <AppHeader :currentUser="currentUser" />
                </n-layout-header>

                <!-- MAIN CONTENT -->
                <n-layout-content content-style="padding: 24px;" class="bg-[var(--color-bg)] transition-all duration-300">
                  <div class="app-content max-w-7xl mx-auto">
                    <router-view :currentUser="currentUser" />
                  </div>
                </n-layout-content>

                <!-- FOOTER -->
                <n-layout-footer bordered class="text-center p-4 text-[var(--color-text)] bg-[var(--color-bg-card)]">
                  <div class="flex flex-col items-center gap-2">
                    <n-text depth="3">DN Quest Engine © 2025</n-text>
                    <n-text depth="3" class="text-xs">
                      Версия 1.0.0 | Сделано с ❤️ для квестеров
                    </n-text>
                  </div>
                </n-layout-footer>
              </template>
            </n-layout>
          </n-notification-provider>
        </n-dialog-provider>
      </n-loading-bar-provider>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup>
import {
  darkTheme,
  NSelect,
  NText,
  ruRU,
  dateRuRU
} from "naive-ui";
import AppHeader from "@/components/AppHeader.vue";
import GameHeader from "@/components/GameHeader.vue";
import { ref, onMounted, computed, watch, onUnmounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { fetchCurrentUser } from "@/services/auth";
import api from "@/services/api";

const route = useRoute();
const router = useRouter();
const theme = ref(localStorage.getItem("theme") || "indigo");
const currentUser = ref(null);
const questId = ref(null);
const loading = ref(false);

// Опции для выбора темы
const themeOptions = [
  { label: "Indigo", value: "indigo" },
  { label: "Emerald", value: "emerald" },
  { label: "Rose", value: "rose" }
];

// Определяем игровой режим
const isGameMode = computed(() => route.path.startsWith("/play/"));

// Загружаем текущего пользователя с обработкой ошибок
async function loadUser() {
  if (loading.value) return;
  
  loading.value = true;
  try {
    currentUser.value = await fetchCurrentUser();
  } catch (error) {
    console.warn("Не удалось загрузить пользователя:", error);
    currentUser.value = null;
  } finally {
    loading.value = false;
  }
}

// Загружаем квест по sessionId, когда входим в режим игры
async function loadQuestForSession() {
  if (!isGameMode.value) return;

  const sessionId = route.params.sessionId;
  if (!sessionId) return;

  try {
    const resp = await api.get(`/sessions/${sessionId}/quest`);
    questId.value = resp.data.id;
  } catch (error) {
    console.error("Ошибка загрузки сессии:", error);
    questId.value = null;
  }
}

function goToStats() {
  if (!questId.value) return;
  router.push(`/quests/${questId.value}/stats`);
}

function exitGame() {
  router.push("/");
}

function applyTheme() {
  document.documentElement.setAttribute("data-theme", theme.value);
  localStorage.setItem("theme", theme.value);
}

// Обработчик события изменения пользователя
function handleUserChanged() {
  loadUser();
}

// Следим за сменой роутов
watch(
  () => route.fullPath,
  async () => {
    await loadQuestForSession();
  },
  { immediate: true }
);

// Следим за изменением темы
watch(theme, (newTheme) => {
  applyTheme();
});

onMounted(() => {
  applyTheme();
  loadUser();
  window.addEventListener("user-changed", handleUserChanged);
});

onUnmounted(() => {
  window.removeEventListener("user-changed", handleUserChanged);
});
</script>

<style>
/* Глобальные стили для улучшения UX */
.app-content {
  animation: fadeIn 0.3s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Улучшение доступности */
.n-layout-header,
.n-layout-footer {
  transition: all 0.3s ease;
}

/* Стили для тем */
[data-theme="indigo"] {
  --color-primary: #6366f1;
  --color-primary-hover: #4f46e5;
}

[data-theme="emerald"] {
  --color-primary: #10b981;
  --color-primary-hover: #059669;
}

[data-theme="rose"] {
  --color-primary: #f43f5e;
  --color-primary-hover: #e11d48;
}
</style>
