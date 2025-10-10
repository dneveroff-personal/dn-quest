<template>
  <div class="flex flex-col w-full p-6 bg-[var(--color-bg-card)] shadow-md">
    <!-- Верхняя строка -->
    <div class="flex items-center justify-between w-full">
      <div class="flex items-center gap-4">
        <router-link to="/" class="dn-quest-link text-2xl font-bold transition-opacity hover:opacity-80">
          DN Quest
        </router-link>

        <!-- Имя пользователя -->
        <div v-if="currentUser" class="flex items-center gap-2 text-[var(--color-text)] text-lg font-medium">
          <span>{{ getUserEmoji(currentUser.role) }}</span>
          <span>{{ currentUser.publicName }}</span>
          <span v-if="currentUser.captain" title="Капитан">©</span>
        </div>

        <!-- Ссылка на команду -->
        <router-link
            v-if="currentUser?.team"
            :to="`/teams/${currentUser.team.id}`"
            class="ml-2 text-base font-semibold text-[var(--color-accent)] hover:text-white transition-colors"
        >
          {{ currentUser.team.name }}
        </router-link>

        <!-- Приглашения -->
        <router-link
            v-if="currentUser && !currentUser.team"
            to="/invitations"
            class="ml-2 text-base font-semibold text-[var(--color-accent)] hover:text-white transition-colors"
        >
          Приглашения в команду
        </router-link>
      </div>

      <!-- Logout -->
      <div v-if="currentUser">
        <button
            @click="logout"
            class="text-[var(--color-text)] text-xs hover:text-[var(--color-accent)] transition-colors font-semibold"
        >
          Logout
        </button>
      </div>
    </div>

    <!-- Нижняя строка -->
    <div class="flex flex-wrap gap-3 mt-3 justify-center">
      <router-link v-if="!currentUser" to="/login" class="btn-accent text-lg font-semibold px-6 py-3 rounded-xl hover:text-white transition-colors">
        Login
      </router-link>
      <router-link v-if="!currentUser" to="/register" class="btn-accent text-lg font-semibold px-6 py-3 rounded-xl hover:text-white transition-colors">
        Register
      </router-link>
      <router-link v-if="currentUser?.role === 'ADMIN'" to="/admin/users/manage" class="btn-accent text-lg font-semibold px-6 py-3 rounded-xl hover:text-white transition-colors">
        Manage Users
      </router-link>
      <router-link v-if="currentUser?.role === 'PLAYER'" to="/teams/create" class="btn-accent text-lg font-semibold px-6 py-3 rounded-xl hover:text-white transition-colors">
        Создать команду
      </router-link>
      <router-link v-if="currentUser?.role === 'AUTHOR'" to="/quests/create" class="btn-accent text-lg font-semibold px-6 py-3 rounded-xl hover:text-white transition-colors">
        Создать квест
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from "vue-router";
import { logout as authLogout } from "@/services/auth";

defineProps({
  currentUser: Object
});

const router = useRouter();
const roleEmojis = {
  ADMIN: "🤴",
  AUTHOR: "👩‍🍳",
  PLAYER: "🕵️‍♂️"
};

function logout() {
  authLogout();
  window.dispatchEvent(new Event("user-changed")); // триггерим обновление
  router.push("/login");
}

function getUserEmoji(role) {
  return roleEmojis[role] || "🧑";
}
</script>
