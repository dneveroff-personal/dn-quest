<template>
  <div class="flex items-center justify-between w-full">
    <!-- LOGO / TITLE -->
    <div class="text-xl font-bold text-white">
      <router-link to="/">DN Quest</router-link>
    </div>

    <!-- NAVIGATION -->
    <div class="flex gap-4 items-center">
      <router-link
          v-if="!loggedIn"
          to="/login"
          class="text-white hover:text-blue-400 transition"
      >
        Login
      </router-link>
      <router-link
          v-if="!loggedIn"
          to="/register"
          class="text-white hover:text-blue-400 transition"
      >
        Register
      </router-link>
      <button
          v-if="loggedIn"
          @click="logout"
          class="text-white hover:text-red-400 transition"
      >
        Logout
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue"
import { useRouter } from "vue-router"
import { getToken, logout as authLogout } from "@/services/auth"

const router = useRouter()
const loggedIn = ref(false)

onMounted(() => {
  loggedIn.value = !!getToken()
})

function logout() {
  authLogout()
  loggedIn.value = false
  router.push("/login")
}
</script>

<style scoped>
/* Плавные hover-эффекты уже через tailwind-классы */
</style>
