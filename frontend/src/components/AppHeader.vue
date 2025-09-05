<template>
  <div class="flex flex-col w-full p-6 bg-[var(--color-bg-card)] shadow-md">
    <!-- Верхняя строка: DN Quest + пользователь + Logout -->
    <div class="flex items-center justify-between w-full">
      <div class="flex items-center gap-4">
        <!-- DN Quest: градиентный текст -->
        <router-link to="/" class="dn-quest-link text-2xl font-bold transition-opacity hover:opacity-80">
          DN Quest
        </router-link>

        <!-- Имя пользователя с иконкой -->
        <div v-if="currentUser" class="flex items-center gap-2 text-[var(--color-text)] text-lg font-medium">
          <span>🧑‍</span>
          <span>{{ currentUser.publicName }}</span>
        </div>
      </div>

      <!-- Logout кнопка -->
      <div v-if="currentUser">
        <button @click="logout"
                class="text-[var(--color-text)] hover:text-[var(--color-accent)] transition-colors font-semibold">
          Logout
        </button>
      </div>
    </div>

    <!-- Нижняя строка: навигационные кнопки -->
    <div class="flex flex-wrap gap-3 mt-3 justify-center">
      <router-link v-if="!currentUser" to="/login"
                   class="btn-accent text-lg font-semibold px-6 py-3 rounded-xl hover:text-white transition-colors">
        Login
      </router-link>
      <router-link v-if="!currentUser" to="/register"
                   class="btn-accent text-lg font-semibold px-6 py-3 rounded-xl hover:text-white transition-colors">
        Register
      </router-link>
      <router-link v-if="currentUser?.role === 'ADMIN'" to="/admin/users/manage"
                   class="btn-accent text-lg font-semibold px-6 py-3 rounded-xl hover:text-white transition-colors">
        Manage Users
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { fetchCurrentUser, logout as authLogout } from "@/services/auth";

const router = useRouter();
const currentUser = ref(null);

async function loadUser() {
  currentUser.value = await fetchCurrentUser();
}

onMounted(() => {
  loadUser();
  window.addEventListener("user-changed", loadUser);
});

function logout() {
  authLogout();
  currentUser.value = null;
  router.push("/login");
}
</script>
