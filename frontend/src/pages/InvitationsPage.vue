<template>
  <div class="flex justify-center w-full px-4 py-8 bg-[var(--color-bg)]">
    <n-card class="w-full max-w-2xl shadow-lg rounded-2xl p-6">
      <InvitationsList />
    </n-card>
  </div>
</template>

<script setup>
import { NCard } from "naive-ui";
import InvitationsList from "@/components/InvitationsList.vue";
</script>
