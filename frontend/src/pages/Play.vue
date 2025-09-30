<template>
  <div class="flex flex-col items-center w-full px-6 py-8 bg-[var(--color-bg)] min-h-screen">
    <h2 class="text-3xl font-bold mb-6 text-[var(--color-text-strong)]">
      {{ data?.level?.title || "Текущий уровень" }}
    </h2>

    <n-spin :show="loading" class="w-full">
      <div v-if="data" class="flex flex-col gap-6 w-full max-w-3xl">
        <!-- Описание уровня -->
        <div class="p-6 bg-[var(--color-bg-card)] rounded-2xl shadow">
          <div v-html="data.level.descriptionHtml" class="prose text-[var(--color-text)]"></div>
        </div>

        <!-- Автопроход -->
        <div v-if="data.level.apTime" class="p-4 bg-blue-900/20 rounded-xl">
          <strong>Авто-проход:</strong>
          <span>{{ apTimer }} сек.</span>
        </div>

        <!-- Сектора -->
        <div class="p-4 bg-green-900/20 rounded-xl">
          <strong>Прогресс:</strong>
          {{ data.progress.sectorsClosed }} / {{ data.level.requiredSectors }}
        </div>

        <!-- Подсказки -->
        <div v-if="data.hints?.length" class="flex flex-col gap-2">
          <div
              v-for="(hint, idx) in data.hints"
              :key="hint.id"
              class="p-3 bg-yellow-900/20 rounded-lg"
          >
            <div v-if="hintVisible(idx)">
              <strong>Подсказка {{ idx + 1 }}:</strong> {{ hint.text }}
            </div>
            <div v-else>
              Подсказка {{ idx + 1 }} откроется через {{ hintTimers[idx] }} сек.
            </div>
          </div>
        </div>

        <!-- Ввод кода -->
        <div class="flex gap-3 mt-4">
          <n-input
              v-model:value="code"
              placeholder="Введите код"
              class="flex-1"
              size="large"
              @keyup.enter="submitCode"
          />
          <n-button type="primary" size="large" :loading="submitting" @click="submitCode">
            Отправить
          </n-button>
        </div>

        <!-- Кнопка обновления -->
        <n-button quaternary @click="loadCurrentLevel">Обновить</n-button>
      </div>
    </n-spin>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from "vue";
import { useRoute } from "vue-router";
import { useMessage, NSpin, NButton, NInput } from "naive-ui";
import api from "@/services/api";

const route = useRoute();
const message = useMessage();
const { sessionId } = route.params;
const loading = ref(false);
const submitting = ref(false);
const data = ref(null);
const code = ref("");

// Таймеры
const apTimer = ref(null);
const hintTimers = ref([]);
let timerInterval = null;

async function loadCurrentLevel() {
  loading.value = true;
  try {
    const { data: resp } = await api.get(`/sessions/${sessionId}/current`);
    data.value = resp;

    // сортируем подсказки строго по offsetSec
    if (data.value.hints?.length) {
      data.value.hints.sort((a, b) => (a.offsetSec || 0) - (b.offsetSec || 0));

      const startedAt = new Date(data.value.progress.startedAt).getTime();
      hintTimers.value = data.value.hints.map((h) => {
        const hintDeadline = startedAt + (h.offsetSec || 0) * 1000;
        return Math.max(0, Math.floor((hintDeadline - Date.now()) / 1000));
      });
    }

    // AP таймер
    if (data.value.level.apTime && data.value.progress?.startedAt) {
      const startedAt = new Date(data.value.progress.startedAt).getTime();
      const deadline = startedAt + data.value.level.apTime * 1000;
      updateTimers(deadline);
      if (timerInterval) clearInterval(timerInterval);
      timerInterval = setInterval(() => updateTimers(deadline), 1000);
    }

    // Подсказки таймеры
    if (data.value.hints?.length) {
      const startedAt = new Date(data.value.progress.startedAt).getTime();
      hintTimers.value = data.value.hints.map((h) => {
        const hintDeadline = startedAt + (h.offsetSec || 0) * 1000;
        return Math.max(0, Math.floor((hintDeadline - Date.now()) / 1000));
      });
    }
  } catch (err) {
    message.error("Ошибка загрузки уровня");
    console.error(err);
  } finally {
    loading.value = false;
  }
}

function updateTimers(deadline) {
  apTimer.value = Math.max(0, Math.floor((deadline - Date.now()) / 1000));

  if (data.value?.hints?.length) {
    // сортировка по offsetSec
    data.value.hints.sort((a, b) => (a.offsetSec || 0) - (b.offsetSec || 0));

    const startedAt = new Date(data.value.progress.startedAt).getTime();
    hintTimers.value = data.value.hints.map((h) => {
      const hintDeadline = startedAt + (h.offsetSec || 0) * 1000;
      return Math.max(0, Math.floor((hintDeadline - Date.now()) / 1000));
    });
  }
}

function hintVisible(idx) {
  return hintTimers.value[idx] === 0;
}

async function submitCode() {
  if (!code.value) return;
  submitting.value = true;
  try {
    await api.post(`/sessions/${route.params.sessionId}/code`, { code: code.value });
    code.value = "";
    await loadCurrentLevel();
  } catch (err) {
    message.error("Неверный код");
  } finally {
    submitting.value = false;
  }
}

onMounted(loadCurrentLevel);
onUnmounted(() => {
  if (timerInterval) clearInterval(timerInterval);
});
</script>
