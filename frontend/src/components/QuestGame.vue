<template>
  <n-card class="hover:shadow-xl transition duration-200">
    <template #header>
      <n-text strong class="text-lg">{{ quest.title }}</n-text>
    </template>

    <n-text depth="3" class="block mb-2">
      {{ quest.description }}
    </n-text>

    <n-button type="primary" block @click="goToQuest">
      Играть
    </n-button>
  </n-card>
</template>

<script setup>
import { useRouter } from "vue-router";

const props = defineProps({
  quest: {
    type: Object,
    required: true
  }
});

const router = useRouter();

function goToQuest() {
  router.push(`/quests/${props.quest.id}`);
}
</script>