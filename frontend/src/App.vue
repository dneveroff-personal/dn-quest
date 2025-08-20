<template>
  <n-config-provider :theme="darkTheme">
    <n-message-provider>
      <div class="p-6">
        <h1 class="text-2xl font-bold mb-6 text-center">Quiz Engine</h1>

        <n-spin :show="loading">
          <div class="grid gap-4">
            <QuizCard
                v-for="quiz in quizzes"
                :key="quiz.id"
                :quiz="quiz"
                @answered="handleAnswered"
            />
          </div>
        </n-spin>
      </div>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup>
import { ref, onMounted } from "vue"
import { darkTheme, NConfigProvider, NMessageProvider, NSpin } from "naive-ui"
import QuizCard from "./components/QuizCard.vue"
import api from "@/services/api"

const quizzes = ref([])
const loading = ref(false)

async function loadQuizzes() {
  loading.value = true
  try {
    const response = await api.get("/get/all") // твой эндпоинт
    quizzes.value = response.data.content
  } catch (error) {
    console.error("Ошибка загрузки квизов", error)
  } finally {
    loading.value = false
  }
}

function handleAnswered({ id, result }) {
  console.log("Результат квиза:", id, result)
  // Можно показывать сообщение или перезагружать квизы
}

onMounted(loadQuizzes)
</script>
