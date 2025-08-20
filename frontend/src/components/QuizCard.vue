<template>
  <n-card class="bg-gray-900 text-white shadow-lg p-6 max-w-md mx-auto my-4 rounded-lg">
    <h2 class="text-xl font-bold mb-2">{{ quiz.title }}</h2>
    <p class="mb-4 text-gray-400">{{ quiz.text }}</p>

    <div class="flex flex-col gap-2 mb-4">
      <n-button
          v-for="(option, index) in quiz.options"
          :key="index"
          :type="selectedAnswers.includes(index) ? 'primary' : 'default'"
          :ghost="!selectedAnswers.includes(index)"
          @click="toggleAnswer(index)"
          class="transition-colors duration-200"
      >
        {{ option }}
      </n-button>
    </div>

    <n-button
        type="success"
        :disabled="selectedAnswers.length === 0 || submitting"
        @click="submitAnswer"
        class="w-full"
    >
      Ответить
    </n-button>

    <n-alert
        v-if="resultMessage"
        :type="resultSuccess ? 'success' : 'error'"
        class="mt-4"
        :closable="false"
        :bordered="false"
        size="small"
    >
      {{ resultMessage }}
    </n-alert>
  </n-card>
</template>

<script setup>
import { ref } from "vue"
import { NCard, NButton, NAlert } from "naive-ui"
import api from "../services/api"

const props = defineProps({
  quiz: { type: Object, required: true }
})
const emit = defineEmits(["answered"])

const selectedAnswers = ref([])
const submitting = ref(false)
const resultMessage = ref("")
const resultSuccess = ref(false)

function toggleAnswer(index) {
  if (selectedAnswers.value.includes(index)) {
    selectedAnswers.value = selectedAnswers.value.filter(i => i !== index)
  } else {
    selectedAnswers.value.push(index)
  }
}

async function submitAnswer() {
  submitting.value = true
  try {
    const response = await api.post(`/post/${props.quiz.id}/solve`, {
      answer: selectedAnswers.value
    })
    emit("answered", { id: props.quiz.id, result: response.data })

    resultMessage.value = response.data.feedback
    resultSuccess.value = response.data.success
  } catch (error) {
    console.error("Ошибка отправки ответа", error)
    resultMessage.value = "Ошибка сервера"
    resultSuccess.value = false
  } finally {
    submitting.value = false
  }
}
</script>
