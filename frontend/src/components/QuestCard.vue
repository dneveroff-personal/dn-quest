<!-- src/components/QuestCard.vue -->
<template>
  <n-card
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
          @click="$emit('play', quest.id)"
      >
        Играть
      </n-button>

      <n-button
          v-if="canEdit"
          type="warning"
          class="rounded-xl py-3 font-semibold"
          @click="$emit('edit', quest.id)"
      >
        Редактировать
      </n-button>
    </div>
  </n-card>
</template>

<script setup>
import { NCard, NButton } from "naive-ui";

defineProps({
  quest: { type: Object, required: true },
  canEdit: { type: Boolean, default: false }
});

defineEmits(["play", "edit"]);

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
</script>
