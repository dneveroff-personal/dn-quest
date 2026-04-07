<template>
  <div
      class="finish-page relative flex flex-col items-center justify-center h-screen text-center overflow-hidden"
      :style="{
      background: `linear-gradient(to bottom, var(--color-bg-card), var(--color-bg))`,
      color: 'var(--color-text)'
    }"
  >

    <!-- Конфетти -->
    <div
        class="confetti-explosion"
        v-for="n in 50"
        :key="'explosion-'+n"
        :style="explosionStyle()"
    ></div>

    <!-- Основной блок -->
    <div class="z-10 flex flex-col items-center space-y-4">
      <h1 class="text-6xl font-extrabold drop-shadow-lg animate-sparkle"
          :style="{ color: 'var(--color-accent)' }">
        Поздравляем! 🎉
      </h1>

      <p class="text-lg">Вы успешно завершили квест!</p>
      <p v-if="place" class="text-xl font-bold" :style="{ color: 'var(--color-accent)' }">
        Ваше место: {{ place }} 🏅
      </p>

      <button
          @click="goHome"
          class="mt-6 px-8 py-3 font-bold rounded-lg shadow-lg animate-pulse-button"
          :style="{
          backgroundColor: 'var(--color-accent)',
          color: 'var(--color-bg-card)',
          border: 'none'
        }"
      >
        Вернуться на главную
      </button>
    </div>

    <!-- Медали -->
    <div class="medal" v-for="n in 5" :key="'medal-'+n" :style="medalStyle()">
      🏅
    </div>

  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const props = defineProps({ place: Number })
const router = useRouter()

function goHome() {
  router.push('/')
}

// Конфетти
function explosionStyle() {
  return {
    left: Math.random() * 100 + 'vw',
    top: Math.random() * 100 + 'vh',
    backgroundColor: 'var(--color-accent)',
    width: Math.random() * 12 + 6 + 'px',
    height: Math.random() * 12 + 6 + 'px',
    animationDelay: (Math.random() * 0.5) + 's',
    transform: `rotate(${Math.random() * 360}deg)`
  }
}

// Медали
function medalStyle() {
  return {
    left: Math.random() * 80 + 10 + 'vw',
    animationDuration: 2 + Math.random() * 2 + 's',
    fontSize: 24 + Math.random() * 20 + 'px'
  }
}
</script>

<style scoped>
.finish-page {
  position: relative;
  overflow: hidden;
}

/* Конфетти */
.confetti-explosion {
  position: absolute;
  border-radius: 2px;
  opacity: 0.9;
  animation: explode 3s ease-out forwards;
}

@keyframes explode {
  0% {
    transform: translate(0,0) scale(1) rotate(0deg);
    opacity: 1;
  }
  100% {
    transform: translate(calc(-50vw + 100px), -100vh) scale(0.5) rotate(720deg);
    opacity: 0;
  }
}

/* Медали */
.medal {
  position: absolute;
  top: 100%;
  animation: floatUp linear infinite;
}

@keyframes floatUp {
  0% { transform: translateY(0) scale(1); opacity: 1; }
  100% { transform: translateY(-120vh) scale(1.2); opacity: 0; }
}

/* Искрящийся текст */
.animate-sparkle {
  background: linear-gradient(90deg,
  var(--color-accent),
  var(--color-text),
  var(--color-accent),
  var(--color-text)
  );
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: sparkle 3s linear infinite;
}

@keyframes sparkle {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

/* Кнопка — пульс */
.animate-pulse-button {
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.05); opacity: 0.95; }
}
</style>
