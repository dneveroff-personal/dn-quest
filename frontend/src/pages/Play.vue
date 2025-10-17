<template>
  <div v-if="data?.finished">
    <Finish :place="data?.place" />
  </div>

  <div v-else>
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

          <!-- Автопереход -->
          <div class="text-left rounded-xl mb-2 text-yellow-500">
            {{ apTimer !== null ? `До автоперехода: ${apTimer} сек.` : "Автоперехода нет" }}
          </div>

          <!-- История кодов -->
          <div
              class="overflow-y-auto max-h-[9rem] mt-4 border-t pt-2"
              @scroll="onScroll"
              ref="attemptsContainer"
          >
            <ul>
              <li v-for="(a, idx) in attempts" :key="idx" :class="a.color">
                {{ a.code }} <span class="ml-2">{{ a.symbol }}</span>
              </li>
            </ul>
            <div v-if="loadingMore" class="text-center text-sm text-gray-500 py-2">Загрузка...</div>
          </div>

          <!-- Статистика кодов -->
          <div class="text-left">
            <strong>Кодов введено:</strong>
            {{ data.progress.sectorsClosed }} из {{ data.level.requiredSectors }}
          </div>

          <!-- === Сектора (NORMAL) === -->
          <div v-if="data?.sectors?.length" class="w-full mt-4">
            <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
              <div
                  v-for="s in data.sectors"
                  :key="s.sectorNo"
                  class="flex items-center gap-3 p-3 rounded-lg border font-medium"
                  :class="s.closed ? 'bg-green-800 text-green-100 border-green-600' : 'bg-gray-700 text-gray-300 border-gray-600'"
              >
                <div
                    class="w-9 h-9 rounded-full flex items-center justify-center font-semibold text-sm"
                    :class="s.closed ? 'bg-green-700 text-white' : 'bg-gray-600 text-gray-200'"
                >
                  {{ s.sectorNo }}
                </div>
                <div class="flex-1">
                  <div v-if="s.closed" class="font-medium break-all text-[var(--color-text-strong)]">
                    {{ s.value }}
                  </div>
                  <div v-else class="text-gray-400 italic">— — — — —</div>
                </div>
              </div>
            </div>
          </div>

          <!-- === Бонусные коды === -->
          <div v-if="data.bonusCodes?.length" class="mb-3 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-3">
            <div
                v-for="b in data.bonusCodes"
                :key="b.id"
                class="flex items-center gap-3 p-3 rounded-lg border font-medium"
                :class="b.closed ? 'bg-blue-800 text-blue-200 border-blue-600' : 'bg-gray-700 text-gray-300 border-gray-600'"
            >
              <div
                  class="w-9 h-9 rounded-full flex items-center justify-center font-semibold text-sm"
                  :class="b.closed ? 'bg-blue-700 text-blue-100' : 'bg-gray-600 text-gray-200'"
              >
                🎁
              </div>
              <div class="flex-1 break-all">
                <div class="flex items-center justify-between">
                  <div class="truncate">{{ b.value }}</div>
                  <div class="text-xs ml-2">
                    <template v-if="b.sectorNo">
                      <span class="px-2 py-0.5 rounded text-xs font-semibold">#{{ b.sectorNo }}</span>
                    </template>
                  </div>
                </div>
                <div v-if="b.closed" class="mt-1 text-xs text-blue-200">
                  +{{ b.shiftSeconds || 0 }} сек
                </div>
              </div>
            </div>
          </div>

          <!-- === Штрафные коды === -->
          <div v-if="data.penaltyCodes?.length" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-3">
            <div
                v-for="p in data.penaltyCodes"
                :key="p.id"
                class="flex items-center gap-3 p-3 rounded-lg border font-medium"
                :class="p.closed ? 'bg-red-800 text-red-200 border-red-600' : 'bg-gray-700 text-gray-300 border-gray-600'"
            >
              <div
                  class="w-9 h-9 rounded-full flex items-center justify-center font-semibold text-sm"
                  :class="p.closed ? 'bg-red-700 text-red-100' : 'bg-gray-600 text-gray-200'"
              >
                ⛔
              </div>
              <div class="flex-1 break-all">
                <div class="flex items-center justify-between">
                  <div class="truncate">{{ p.value }}</div>
                  <div class="text-xs ml-2">
                    <template v-if="p.sectorNo">
                      <span class="px-2 py-0.5 rounded text-xs font-semibold">#{{ p.sectorNo }}</span>
                    </template>
                  </div>
                </div>
                <div v-if="p.closed" class="mt-1 text-xs text-red-200">
                  -{{ Math.abs(p.shiftSeconds || 0) }} сек
                </div>
              </div>
            </div>
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
  </div>

  <!-- Вспышка при переходе -->
  <div v-show="flashVisible" class="level-flash"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from "vue";
import { useRoute } from "vue-router";
import { useMessage, NSpin, NButton, NInput } from "naive-ui";
import api from "@/services/api";
import { fetchCurrentUser } from "@/services/auth";
import Finish from "@/pages/Finish.vue";

const props = defineProps({
  currentUser: { type: Object, default: null }
});

const route = useRoute();
const message = useMessage();
const sessionId = route.params.sessionId;

const autoPassed = ref(false);
const autoPassInProgress = ref(false);

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

const flashVisible = ref(false);

// Таймеры
const apTimer = ref(null);
const hintTimers = ref([]);
let timerInterval = null;

function playFlash() {
  flashVisible.value = true;
  setTimeout(() => { flashVisible.value = false; }, 350);
}

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
  if (!data.value || !data.value.level) return;
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

/**
 * Применяет LevelViewDTO (resp) к локальному состоянию:
 * - устанавливает data.value
 * - сбрасывает / запускает таймеры apTimer и hintTimers
 * - подгружает историю попыток (reset)
 */
function applyLevelView(resp) {
  if (!resp) return;
  // запомним предыдущее id до замены
  const prevLevelId = data.value?.level?.id;
  // обновляем state
  data.value = resp;

  // если уровень поменялся — сбрасываем флаг авто-прохождения
  if (prevLevelId !== resp.level?.id) {
    autoPassed.value = false;
  }

  // если игра завершена — остановим таймеры
  if (data.value.finished) {
    if (timerInterval) clearInterval(timerInterval);
    apTimer.value = null;
    hintTimers.value = [];
    return;
  }

  // подсказки
  if (data.value.hints?.length) {
    data.value.hints.sort((a, b) => (a.offsetSec || 0) - (b.offsetSec || 0));
    const startedAt = new Date(data.value.progress.startedAt).getTime();
    hintTimers.value = data.value.hints.map(h => {
      const hintDeadline = startedAt + (h.offsetSec || 0) * 1000;
      return Math.max(0, Math.floor((hintDeadline - Date.now()) / 1000));
    });
  } else {
    hintTimers.value = [];
  }

  // AP таймер
  if (data.value.level?.apTime != null && data.value.progress?.startedAt) {
    const startedAt = new Date(data.value.progress.startedAt).getTime();
    const deadline = startedAt + data.value.level.apTime * 1000;
    updateTimers(deadline);
    if (timerInterval) clearInterval(timerInterval);
    timerInterval = setInterval(() => updateTimers(deadline), 1000);
  } else {
    apTimer.value = null;
    if (timerInterval) clearInterval(timerInterval);
  }

  // история попыток (сброс) — грузим только если есть валидный levelId
  if (data.value?.level?.id) {
    awaitLoadAttemptsReset();
  }
}

// small helper to call loadAttempts but not block applyLevelView (keeps code cleaner)
function awaitLoadAttemptsReset() {
  // fire-and-forget but still handle errors
  loadAttempts(true).catch(err => {
    console.log("Не удалось загрузить попытки после applyLevelView:", err);
  });
}

async function loadCurrentLevel() {
  loading.value = true;
  try {
    const { data: resp } = await api.get(`/sessions/${sessionId}/current`);
    applyLevelView(resp);
  } catch (err) {
    message.error("Ошибка загрузки уровня");
    console.log("Ошибка загрузки уровня 1" + err);
  } finally {
    loading.value = false;
  }
}

function updateTimers(deadline) {
  if (!data.value || !data.value.level) return;

  if (data.value.level.apTime != null) {
    apTimer.value = Math.max(0, Math.floor((deadline - Date.now()) / 1000));
    if (apTimer.value === 0 && !autoPassed.value && !autoPassInProgress.value) {
      triggerAutoPass();
    }
  } else {
    apTimer.value = null;
  }

  if (data.value?.hints?.length) {
    const startedAt = new Date(data.value.progress.startedAt).getTime();
    hintTimers.value = data.value.hints.map(h => {
      const hintDeadline = startedAt + (h.offsetSec || 0) * 1000;
      return Math.max(0, Math.floor((hintDeadline - Date.now()) / 1000));
    });
  }
}

async function triggerAutoPass() {
  if (autoPassInProgress.value || autoPassed.value) return;
  autoPassInProgress.value = true;
  autoPassed.value = true;

  const prevLevelId = data.value?.level?.id;

  try {
    // POST возвращает LevelViewDTO (или текущий view, если ничего не изменилось)
    const { data: resp } = await api.post(`/sessions/${sessionId}/auto-pass`);
    if (!resp) {
      message.error("Пустой ответ от сервера при автопереходе");
      return;
    }

    // применим view, затем сравним
    applyLevelView(resp);

    // если уровень реально не сменился — уведомим об этом
    const newLevelId = data.value?.level?.id;
    if (prevLevelId === newLevelId) {
      autoPassed.value = false;
    } else {
      // уровень поменялся — показываем обычное сообщение
      message.success("Уровень пройден по автопереходу");
      playFlash();
    }
  } catch (err) {
    console.log("Ошибка автоперехода:", err);
    if (err?.response?.data) {
      console.log("server response:", err.response.data);
    }
    message.error("Ошибка при автопереходе");
    autoPassed.value = false;
  } finally {
    autoPassInProgress.value = false;
  }
}

function hintVisible(idx) {
  return hintTimers.value[idx] === 0;
}

function findAndMarkClosedLive(codeVal) {
  if (!data.value) return false;
  const sector = data.value.sectors?.find(s => s.value === codeVal);
  if (sector) { sector.closed = true; return true; }
  const bonus = data.value.bonusCodes?.find(b => b.value === codeVal);
  if (bonus) { bonus.closed = true; return true; }
  const pen = data.value.penaltyCodes?.find(p => p.value === codeVal);
  if (pen) { pen.closed = true; return true; }
  return false;
}

async function submitCode() {
  if (!code.value) return;
  if (!currentUser.value?.id) {
    message.error("Неизвестный пользователь");
    return;
  }

  submitting.value = true;
  const submitted = code.value.trim();

  await api.post(`/sessions/${sessionId}/code`, {
    rawCode: submitted,
    userId: currentUser.value.id
  });

  // Мгновенный отклик
  findAndMarkClosedLive(submitted);

  const prevLevelId = data.value?.level?.id;
  // перезагрузим view с сервера (можно использовать loadCurrentLevel)
  await loadCurrentLevel();
  if (data.value?.level?.id !== prevLevelId) {
    message.success(`Уровень ${data.value.level.orderIndex - 1} закрыт кодом: ${submitted}`);
    playFlash();
  }

  submitting.value = false;
  code.value = "";
}

function isClosedByCode(codeVal) {
  return !!(
      (data.value?.sectors && data.value.sectors.some(s => s.value === codeVal && s.closed)) ||
      (data.value?.bonusCodes && data.value.bonusCodes.some(b => b.value === codeVal && b.closed)) ||
      (data.value?.penaltyCodes && data.value.penaltyCodes.some(p => p.value === codeVal && p.closed))
  );
}

onMounted(async () => {
  await ensureCurrentUser();
  await loadCurrentLevel();
});

onUnmounted(() => {
  if (timerInterval) clearInterval(timerInterval);
});
</script>

<style scoped>
.level-flash {
  position: fixed;
  inset: 0;
  background: var(--color-accent, #6366f1);
  opacity: 0;
  pointer-events: none;
  z-index: 9999;
  transition: opacity 0.3s ease;
}

.level-flash[style*="display: block"],
.level-flash[style*="display:block"] {
  opacity: 0.6;
  animation: flash-fade 0.35s ease forwards;
}

@keyframes flash-fade {
  0% { opacity: 0.7; }
  100% { opacity: 0; }
}
</style>
