<template>
  <div class="flex items-center justify-between w-full p-4 bg-gray-800">
    <div class="text-xl font-bold text-white">
      <router-link to="/">DN Quest</router-link>
    </div>

    <div class="flex gap-4 items-center">
      <!-- Показываем Login/Register если нет пользователя -->
      <router-link v-if="!currentUser" to="/login" class="text-white hover:text-blue-400 transition">
        Login
      </router-link>
      <router-link v-if="!currentUser" to="/register" class="text-white hover:text-blue-400 transition">
        Register
      </router-link>

      <!-- Показываем publicName и Logout если есть пользователь -->
      <div v-if="currentUser" class="flex items-center gap-2">
        <span class="text-white">{{ currentUser.publicName }}</span>
        <button @click="logout" class="text-white hover:text-red-400 transition">
          Logout
        </button>
      </div>
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
