<template>
  <n-config-provider :theme="darkTheme">
    <n-message-provider>
      <n-layout class="min-h-screen bg-[var(--color-bg)]">

        <!-- GAME MODE -->
        <template v-if="isGameMode">
          <GameHeader
              :currentUser="currentUser"
              :theme="theme"
              :questId="questId"
              @exit="exitGame"
              @viewStats="goToStats"
          />
          <n-layout-content>
            <router-view :currentUser="currentUser" />
          </n-layout-content>
        </template>

        <!-- NORMAL LAYOUT -->
        <template v-else>
          <!-- Заголовок (с переключателем темы) -->
          <div class="flex items-center gap-4 mb-6 p-4">
            <h1 class="text-xl font-bold text-[var(--color-text-strong)]">DN Quest Engine</h1>
            <select
                v-model="theme"
                @change="applyTheme"
                class="bg-[var(--color-bg-card)] text-[var(--color-text)] p-2 rounded"
            >
              <option value="indigo">Indigo</option>
              <option value="emerald">Emerald</option>
              <option value="rose">Rose</option>
            </select>
          </div>

          <!-- HEADER -->
          <n-layout-header bordered class="p-4 flex items-center justify-between">
            <AppHeader :currentUser="currentUser" />
          </n-layout-header>

          <!-- MAIN CONTENT -->
          <n-layout-content content-style="padding: 24px;" class="bg-[var(--color-bg)]">
            <div class="app-content">
              <router-view :currentUser="currentUser" />
            </div>
          </n-layout-content>

          <!-- FOOTER -->
          <n-layout-footer bordered class="text-center p-4 text-[var(--color-text)]">
            <n-text depth="3">DN Quest Engine © 2025</n-text>
          </n-layout-footer>
        </template>
      </n-layout>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup>
  import { darkTheme, NButton } from "naive-ui";
  import AppHeader from "@/components/AppHeader.vue";
  import GameHeader from "@/components/GameHeader.vue";
  import { ref, onMounted, computed, watch } from "vue";
  import { useRoute, useRouter } from "vue-router";
  import { fetchCurrentUser } from "@/services/auth";
  import api from "@/services/api";

  const route = useRoute();
  const router = useRouter();
  const theme = ref(localStorage.getItem("theme") || "indigo");
  const currentUser = ref(null);
  const questId = ref(null);

  // Определяем игровой режим
  const isGameMode = computed(() => route.path.startsWith("/play/"));

  // Загружаем текущего юзера
  async function loadUser() {
    currentUser.value = await fetchCurrentUser();
  }

  // Загружаем квест по sessionId, когда входим в режим игры
  async function loadQuestForSession() {
    if (!isGameMode.value) return;

    const sessionId = route.params.sessionId;
    if (!sessionId) return;

    try {
      const resp = await api.get(`/quests/${sessionId}/quest`);
      questId.value = resp.data.id;
    } catch (e) {
      console.error("Ошибка загрузки сессии 1:", e);
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

  // Следим за сменой роутов — если вошли в игру, подгружаем квест
  watch(
      () => route.fullPath,
      async () => {
        await loadQuestForSession();
      },
      { immediate: true }
  );

  onMounted(() => {
    applyTheme();
    loadUser();
    window.addEventListener("user-changed", loadUser);
  });
</script>
