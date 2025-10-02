<template>
  <div class="flex flex-col items-start w-full px-6 py-8 bg-[var(--color-bg)] min-h-screen">
    <!-- Заголовок уровня -->
    <h2 class="text-xl font-bold mb-4 text-[var(--color-text-strong)]">
      {{ data?.level?.orderIndex }}. {{ data?.level?.title || "Текущий уровень" }}
    </h2>

    <!-- Кнопка обновления -->
    <n-button quaternary class="mb-4" @click="loadCurrentLevel">Обновить</n-button>

    <n-spin :show="loading" class="w-full">
      <div v-if="data" class="flex flex-col gap-6 w-full">
        <!-- Поле ввода кода -->
        <div class="flex gap-3 w-full">
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

        <!-- Статистика -->
        <div class="text-left">
          <strong>Кодов введено:</strong>
          {{ data.progress.sectorsClosed }} из {{ data.level.requiredSectors }}
        </div>

        <!-- История кодов -->
        <div v-if="attempts.length" class="p-2 max-h-32 overflow-y-auto w-full">
          <ul>
            <li v-for="(a, idx) in attempts" :key="idx" :class="a.color">
              {{ a.code }} <span class="ml-2">{{ a.correct ? '✔' : '✖' }}</span>
            </li>
          </ul>
        </div>

        <!-- Описание уровня -->
        <div class="p-6 text-justify shadow w-full">
          <div v-html="data.level.descriptionHtml" class="prose text-[var(--color-text)] text-justify"></div>
        </div>

        <!-- Автопереход -->
        <div v-if="data.level.apTime" class="p-4 rounded-xl text-left">
          <strong class="text-yellow-500">Авто-переход:</strong>
          <span>{{ apTimer }} сек.</span>
        </div>

        <!-- Подсказки -->
        <div v-if="data.hints?.length" class="flex flex-col gap-2 w-full">
          <div v-for="(hint, idx) in data.hints" :key="hint.id" class="p-3 rounded-lg text-left">
            <div v-if="hintVisible(idx)">
              <strong class="text-green-500">Подсказка {{ idx + 1 }}:</strong> {{ hint.text }}
            </div>
            <div v-else class="text-gray-400">
              Подсказка {{ idx + 1 }} откроется через {{ hintTimers[idx] }} сек.
            </div>
          </div>
        </div>
      </div>
    </n-spin>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from "vue";
import { useRoute } from "vue-router";
import { useMessage, NSpin, NButton, NInput } from "naive-ui";
import api from "@/services/api";
import { fetchCurrentUser } from "@/services/auth";

const props = defineProps({
  currentUser: { type: Object, default: null }
});

const route = useRoute();
const message = useMessage();
const sessionId = route.params.sessionId;

const loading = ref(false);
const submitting = ref(false);
const data = ref(null);
const code = ref("");
const attempts = ref([]);

const currentUser = ref(props.currentUser || null);
watch(() => props.currentUser, (v) => { currentUser.value = v; });

// Таймеры
const apTimer = ref(null);
const hintTimers = ref([]);
let timerInterval = null;

// Получение currentUser при необходимости
async function ensureCurrentUser() {
  if (!currentUser.value) {
    try {
      currentUser.value = await fetchCurrentUser();
    } catch (err) {
      console.warn("Не удалось подгрузить currentUser", err);
    }
  }
}

async function loadCurrentLevel() {
  loading.value = true;
  try {
    const { data: resp } = await api.get(`/sessions/${sessionId}/current`);
    data.value = resp;

    // Подсказки
    if (data.value.hints?.length) {
      data.value.hints.sort((a, b) => (a.offsetSec || 0) - (b.offsetSec || 0));
      const startedAt = new Date(data.value.progress.startedAt).getTime();
      hintTimers.value = data.value.hints.map(h => {
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

    // История попыток
    const { data: attemptsResp } = await api.get(`/sessions/${sessionId}/last-attempts`, {
      params: { limit: 5, levelId: data.value.level.id }
    });
    attempts.value = attemptsResp.map(a => ({
      code: a.submittedRaw,
      color: a.result.startsWith('ACCEPTED') ? 'text-green-500' : 'text-red-500',
      correct: a.result.startsWith('ACCEPTED')
    }));

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
    const startedAt = new Date(data.value.progress.startedAt).getTime();
    hintTimers.value = data.value.hints.map(h => {
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
  if (!currentUser.value?.id) {
    message.error("Неизвестный пользователь");
    return;
  }

  submitting.value = true;
  try {
    await api.post(`/sessions/${sessionId}/code`, {
      rawCode: code.value,
      userId: currentUser.value.id
    });
    code.value = "";
    await loadCurrentLevel();
  } catch (err) {
    message.error("Неверный код");
    console.error(err);
  } finally {
    submitting.value = false;
  }
}

onMounted(async () => {
  await ensureCurrentUser();
  await loadCurrentLevel();
});

onUnmounted(() => {
  if (timerInterval) clearInterval(timerInterval);
});
</script>
