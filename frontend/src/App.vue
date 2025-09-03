<template>
  <n-config-provider :theme="darkTheme">
    <n-message-provider>
      <n-layout class="min-h-screen">
        <!-- HEADER -->
        <n-layout-header bordered class="p-4 flex items-center justify-between">
          <AppHeader />
        </n-layout-header>

        <!-- MAIN CONTENT -->
        <n-layout-content content-style="padding: 24px;" class="bg-gray-900">
          <div class="max-w-5xl mx-auto">
            <router-view />
          </div>
        </n-layout-content>

        <!-- FOOTER -->
        <n-layout-footer bordered class="text-center p-4">
          <n-text depth="3">DN Quest Engine © 2025</n-text>
        </n-layout-footer>
      </n-layout>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup>
import { darkTheme } from "naive-ui"
import AppHeader from "@/components/AppHeader.vue"
</script>

<style>
/* Чтобы фон тёмной темы смотрелся стильно */
body {
  background-color: #0f172a; /* slate-900 */
  color: #e2e8f0; /* slate-200 */
}
</style>
