<template>
  <div class="flex items-center justify-between w-full p-6 bg-purple-900 shadow-md">
    <div class="text-2xl font-bold text-white">
      <router-link to="/">DN Quest</router-link>
    </div>

    <div class="flex gap-5 items-center">
      <router-link v-if="!currentUser"
                   to="/login"
                   class="px-6 py-3 rounded-xl text-white bg-indigo-600 hover:bg-indigo-500 transition text-lg font-semibold">
        Login
      </router-link>
      <router-link v-if="!currentUser"
                   to="/register"
                   class="px-6 py-3 rounded-xl text-white bg-purple-600 hover:bg-purple-500 transition text-lg font-semibold">
        Register
      </router-link>

      <div v-if="currentUser" class="flex items-center gap-3">
        <span class="text-white text-lg font-medium">{{ currentUser.publicName }}</span>
        <router-link v-if="currentUser?.role === 'ADMIN'"
                     to="/admin/users/manage"
                     class="px-6 py-3 rounded-xl text-white bg-yellow-600 hover:bg-yellow-500 transition text-lg font-semibold">
          Manage Users
        </router-link>
        <button @click="logout"
                class="px-6 py-3 rounded-xl text-white bg-red-600 hover:bg-red-500 transition text-lg font-semibold">
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
