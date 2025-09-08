<!-- src/App.vue -->
<template>
  <n-config-provider :theme="darkTheme">
    <div class="flex items-center gap-4 mb-6">
      <h1 class="text-xl font-bold text-[var(--color-text-strong)]">DN Quest Engine</h1>
      <select v-model="theme" @change="applyTheme" class="bg-[var(--color-bg-card)] text-[var(--color-text)] p-2 rounded">
        <option value="indigo">Indigo</option>
        <option value="emerald">Emerald</option>
        <option value="rose">Rose</option>
      </select>
    </div>

    <n-message-provider>
      <n-layout class="min-h-screen">

        <!-- HEADER -->
        <n-layout-header bordered class="p-4 flex items-center justify-between">
          <AppHeader />
        </n-layout-header>

        <!-- MAIN CONTENT -->
        <n-layout-content content-style="padding: 24px;" class="bg-[var(--color-bg)]">
          <div class="app-content">
            <router-view />
          </div>
        </n-layout-content>

        <!-- FOOTER -->
        <n-layout-footer bordered class="text-center p-4 text-[var(--color-text)]">
          <n-text depth="3">DN Quest Engine © 2025</n-text>
        </n-layout-footer>
      </n-layout>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup>
import { darkTheme } from "naive-ui";
import AppHeader from "@/components/AppHeader.vue";
import { ref } from "vue";

const theme = ref(localStorage.getItem("theme") || "indigo");

function applyTheme() {
  document.documentElement.setAttribute("data-theme", theme.value);
  localStorage.setItem("theme", theme.value);
}

// Применяем сразу при загрузке
applyTheme();
</script>
