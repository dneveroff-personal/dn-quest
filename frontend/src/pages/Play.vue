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

        <!-- Автопереход -->
        <div class="text-left rounded-xl mb-2 text-yellow-500">
          {{ data.level.autoTransition > 0
            ? `Автопереход: ${data.level.autoTransition} сек.`
            :  "Автоперехода  нет" }}
        </div>

        <!-- История кодов -->
        <div
            class="overflow-y-auto max-h-[9rem] mt-4 border-t pt-2"
            @scroll="onScroll"
            ref="attemptsContainer"
        >
          <ul>
            <li
                v-for="(a, idx) in attempts"
                :key="idx"
                :class="a.color"
            >
              {{ a.code }} <span class="ml-2">{{ a.symbol }}</span>
            </li>
          </ul>
          <div v-if="loadingMore" class="text-center text-sm text-gray-500 py-2">Загрузка...</div>
        </div>

        <!-- Описание уровня -->
        <div class="p-6 text-justify shadow w-full">
          <div v-html="data.level.descriptionHtml" class="prose text-[var(--color-text)] text-justify"></div>
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
const attemptsLimit = ref(25);
const loadingMore = ref(false);
const hasMore = ref(true);
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

async function loadAttempts(reset = false) {
  if (reset) {
    attempts.value = [];
    hasMore.value = true;
  }
  if (!hasMore.value) return;
  loadingMore.value = true;
  try {
    const resp = await api.get(`/sessions/${sessionId}/last-attempts`, {
      params: {
        levelId: data.value.level.id,
        limit: attemptsLimit.value
      }
    });
    if (resp.data.length < attemptsLimit.value) hasMore.value = false;
    const mapped = resp.data.map(a => ({
      code: a.submittedRaw,
      color: mapColor(a.result),
      symbol: mapSymbol(a.result)
    }));
    attempts.value.push(...mapped);
  } finally {
    loadingMore.value = false;
  }
}

function onScroll(e) {
  const bottom = e.target.scrollHeight - e.target.scrollTop <= e.target.clientHeight + 10;
  if (bottom && !loadingMore.value) loadAttempts();
}

function mapColor(result) {
  switch (result) {
    case 'ACCEPTED_NORMAL': return 'text-green-500';
    case 'ACCEPTED_BONUS': return 'text-blue-400';
    case 'ACCEPTED_PENALTY': return 'text-red-500';
    case 'DUPLICATE': return 'text-orange-400';
    default: return 'text-gray-400';
  }
}
function mapSymbol(result) {
  switch (result) {
    case 'ACCEPTED_NORMAL': return '✅';
    case 'ACCEPTED_BONUS': return '🎁';
    case 'ACCEPTED_PENALTY': return '⛔';
    case 'DUPLICATE': return '🔁';
    default: return '❌';
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

// История попыток (загружаем первые 25 и включаем скролл)
    await loadAttempts(true);

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
