<template>
  <div class="flex items-center justify-between w-full p-4 bg-gray-800">
    <div class="text-xl font-bold text-white">
      <router-link to="/">DN Quest</router-link>
    </div>

    <div class="flex gap-4 items-center">
      <!-- Если нет пользователя -->
      <router-link
          v-if="!currentUser"
          to="/login"
          class="px-4 py-2 rounded-lg text-white bg-blue-600 hover:bg-blue-500 transition text-lg font-medium"
      >
        Login
      </router-link>
      <router-link
          v-if="!currentUser"
          to="/register"
          class="px-4 py-2 rounded-lg text-white bg-green-600 hover:bg-green-500 transition text-lg font-medium"
      >
        Register
      </router-link>

      <!-- Если есть пользователь -->
      <div v-if="currentUser" class="flex items-center gap-2">
        <span class="text-white">{{ currentUser.publicName }}</span>

        <router-link
            v-if="currentUser?.role === 'ADMIN'"
            to="/admin/users/manage"
            class="px-4 py-2 rounded-lg text-white bg-yellow-600 hover:bg-yellow-500 transition text-lg font-medium"
        >
          Manage Users
        </router-link>

        <!-- Logout -->
        <button
            @click="logout"
            class="px-4 py-2 rounded-lg text-white bg-red-600 hover:bg-red-500 transition text-lg font-medium"
        >
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
