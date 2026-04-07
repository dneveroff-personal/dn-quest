<!-- src/components/GameHeader.vue -->
<template>
  <n-layout-header
      class="p-3 flex items-center justify-between bg-[var(--color-bg-card)] rounded-2xl"
      :class="`bg-${theme}-600`"
  >
    <div class="text-white font-semibold">
      {{ currentUser?.publicName || "Игрок" }}
    </div>

    <div class="flex gap-2">
      <n-button size="small" ghost @click="goToStats">
        📊 Статистика
      </n-button>
      <n-button size="small" ghost type="error" @click="$emit('exit')">
        ⤤
      </n-button>
    </div>
  </n-layout-header>
</template>

<script setup>
import { NButton } from "naive-ui";
import { useRouter } from "vue-router";

// Пробрасываем questId из App.vue или родителя
const props = defineProps({
  currentUser: Object,
  theme: String,
  questId: [String, Number]
});

defineEmits(["exit"]);

// router для перехода
const router = useRouter();

// Метод для перехода на страницу статистики
function goToStats() {
  if (!props.questId) return;
  router.push(`/quests/${props.questId}/stats`);
}
</script>
