<!-- src/components/GameHeader.vue -->
<template>
  <n-layout-header
      class="p-3 flex items-center justify-between bg-[var(--color-bg-card)] rounded-2xl"
      :class="`bg-${theme}-600`"
  >
    <div class="text-white font-semibold">
      {{ currentUser?.publicName || "Игрок" }}
    </div>
    <n-button size="small" ghost type="error" @click="$emit('exit')">
      ⤤
    </n-button>
  </n-layout-header>
</template>

<script setup>
import { NButton } from "naive-ui";
defineProps({
  currentUser: Object,
  theme: String
});
defineEmits(["exit"]);
</script>
