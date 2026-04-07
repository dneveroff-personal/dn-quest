<template>
  <n-card
      hoverable
      class="quest-card rounded-2xl overflow-hidden shadow-lg bg-[var(--color-bg-card)] text-[var(--color-text)] flex flex-col justify-between w-full transition-all duration-300 hover:shadow-xl hover:scale-[1.02]"
  >
    <div class="flex flex-col md:flex-row gap-4 p-6">
      <!-- Левый блок - информация о квесте -->
      <div class="w-full md:w-48 flex-shrink-0">
        <div class="h-40 w-full rounded-xl bg-gradient-to-br from-[var(--color-bg)] to-[var(--color-bg-card)] flex flex-col items-center justify-center border border-white/10 relative overflow-hidden">
          <!-- Иконка типа квеста -->
          <div class="text-4xl mb-2">
            {{ getQuestTypeIcon(quest.type) }}
          </div>
          <div class="text-sm font-medium text-[var(--color-text)]">
            {{ getQuestTypeName(quest.type) }}
          </div>
          <div class="mt-2">
            <n-tag :type="getDifficultyType(quest.difficulty)" size="small">
              {{ getDifficultyName(quest.difficulty) }}
            </n-tag>
          </div>
          
          <!-- Статус квеста -->
          <div class="absolute top-2 right-2">
            <n-tag
              :type="getStatusType(quest)"
              size="small"
              round
            >
              {{ getStatusText(quest) }}
            </n-tag>
          </div>
        </div>
      </div>

      <!-- Правый блок - основная информация -->
      <div class="flex-1 flex flex-col">
        <div class="flex items-start justify-between gap-4">
          <div class="flex-1">
            <h3 class="text-2xl font-bold text-[var(--color-text-strong)] leading-tight mb-2">
              {{ quest.title }}
            </h3>
            <div class="text-sm text-[var(--color-text)] opacity-80 line-clamp-3" v-html="quest.descriptionHtml"></div>
          </div>
          <div class="flex flex-col items-end gap-2 text-right min-w-[150px]">
            <div class="text-sm opacity-70 flex items-center gap-1">
              <span>📅</span>
              {{ formatDate(quest.startAt) }}
            </div>
            <div class="text-sm opacity-70 flex items-center gap-1">
              <span>🏁</span>
              {{ formatDate(quest.endAt) }}
            </div>
            <div v-if="quest.startAt && !isQuestStarted(quest)" class="mt-2">
              <n-countdown
                :duration="getTimeUntilStart(quest.startAt)"
                :active="true"
                @finish="handleCountdownFinish"
              >
                <template #default="{ hours, minutes, seconds }">
                  <div class="text-xs bg-[var(--color-primary)]/10 px-2 py-1 rounded text-[var(--color-primary)] font-medium">
                    До старта: {{ hours }}ч {{ minutes }}м {{ seconds }}с
                  </div>
                </template>
              </n-countdown>
            </div>
          </div>
        </div>

        <div class="mt-4 flex items-center justify-between">
          <div class="flex items-center gap-2 flex-wrap">
            <n-tag :type="getDifficultyType(quest.difficulty)" size="small">
              {{ getDifficultyName(quest.difficulty) }}
            </n-tag>
            <n-tag type="info" size="small">
              {{ getQuestTypeName(quest.type) }}
            </n-tag>
            <div class="flex items-center gap-1 ml-2">
              <span class="text-xs opacity-70">Автор:</span>
              <n-avatar-group :options="authorOptions" :size="24" />
            </div>
          </div>
          <div class="text-sm opacity-60 font-mono">#{{ quest.id }}</div>
        </div>
      </div>
    </div>

    <!-- Кнопки действий -->
    <div class="p-4 border-t border-white/10 bg-[var(--color-bg-card)]">
      <div class="flex gap-3 items-center mb-3">
        <!-- Войти в игру -->
        <n-button
            v-if="canEnter"
            type="primary"
            size="large"
            class="flex-1"
            :loading="starting"
            @click="startGame"
        >
          <template #icon>
            <span>🎮</span>
          </template>
          Войти в игру
        </n-button>

        <!-- Подать заявку / Отозвать (видит капитан) -->
        <template v-if="isCaptain">
          <n-button
              v-if="!myApplication || ['REJECTED','CANCELLED'].includes(myApplication.status)"
              type="info"
              size="large"
              class="flex-1"
              @click="applyToQuest"
          >
            <template #icon>
              <span>📝</span>
            </template>
            Подать заявку
          </n-button>
          <n-button
              v-else-if="myApplication.status === 'PENDING'"
              type="warning"
              size="large"
              class="flex-1"
              @click="withdrawApplication"
          >
            <template #icon>
              <span>❌</span>
            </template>
            Отозвать заявку
          </n-button>
          <n-tag v-else-if="myApplication.status === 'ACCEPTED'" type="success" size="large">
            <template #icon>
              <span>✅</span>
            </template>
            Ваша команда принята
          </n-tag>
        </template>

        <!-- Редактировать (автор / админ) -->
        <n-button
            v-if="canEdit"
            type="warning"
            size="large"
            class="flex-1"
            @click="$emit('edit', quest.id)"
        >
          <template #icon>
            <span>✏️</span>
          </template>
          Редактировать
        </n-button>
      </div>

      <!-- Листы заявок (автор/админ видит все) -->
      <n-collapse v-if="canSeeApplications" class="mt-4">
        <n-collapse-item title="📋 Заявки на участие" name="applications">
          <div class="space-y-4">
            <!-- Ожидающие заявки -->
            <div>
              <h4 class="font-semibold mb-2 text-[var(--color-text)] flex items-center gap-2">
                <span>⏳</span>
                Ожидают рассмотрения ({{ pending.length }})
              </h4>
              <div v-if="pending.length" class="space-y-2">
                <div
                  v-for="p in pending"
                  :key="p.id"
                  class="flex justify-between items-center p-3 bg-[var(--color-bg)] rounded-lg"
                >
                  <div class="flex items-center gap-2">
                    <n-avatar round :size="32">
                      {{ getTeamInitials(p.teamName) }}
                    </n-avatar>
                    <div>
                      <div class="font-medium">{{ p.teamName || ('Команда #' + p.teamId) }}</div>
                      <div class="text-xs opacity-70">Заявка #{{ p.id }}</div>
                    </div>
                  </div>
                  <div class="flex gap-2">
                    <n-button size="small" type="success" @click="changeStatus(p.id, 'ACCEPTED')">
                      Принять
                    </n-button>
                    <n-button size="small" type="error" @click="changeStatus(p.id, 'REJECTED')">
                      Отклонить
                    </n-button>
                  </div>
                </div>
              </div>
              <n-empty v-else description="Нет ожидающих заявок" size="small" />
            </div>

            <!-- Принятые команды -->
            <div>
              <h4 class="font-semibold mb-2 text-[var(--color-text)] flex items-center gap-2">
                <span>✅</span>
                Принятые команды ({{ accepted.length }})
              </h4>
              <div v-if="accepted.length" class="space-y-2">
                <div
                  v-for="r in accepted"
                  :key="r.id"
                  class="flex justify-between items-center p-3 bg-green-50 dark:bg-green-900/20 rounded-lg"
                >
                  <div class="flex items-center gap-2">
                    <n-avatar round :size="32" type="success">
                      {{ getTeamInitials(r.teamName) }}
                    </n-avatar>
                    <div>
                      <div class="font-medium">{{ r.teamName || ('Команда #' + r.teamId) }}</div>
                      <div class="text-xs opacity-70">Принята</div>
                    </div>
                  </div>
                  <n-button size="small" type="error" @click="changeStatus(r.id, 'REJECTED')">
                    Исключить
                  </n-button>
                </div>
              </div>
              <n-empty v-else description="Пока нет принятых команд" size="small" />
            </div>
          </div>
        </n-collapse-item>
      </n-collapse>
    </div>
  </n-card>
</template>

<script setup>
import { ref, computed, onMounted, watch } from "vue";
import {
  NCard,
  NButton,
  NTag,
  NAvatarGroup,
  NCollapse,
  NCollapseItem,
  NEmpty,
  NCountdown,
  NAvatar,
  useMessage
} from "naive-ui";
import api from "@/services/api";
import { fetchCurrentUser } from "@/services/auth";
import { useRouter } from "vue-router";

const props = defineProps({
  quest: { type: Object, required: true },
  canEdit: { type: Boolean, default: false },
  currentUser: { type: Object, default: null }
});

const emit = defineEmits(["edit"]);
const message = useMessage();
const router = useRouter();
const currentUser = ref(props.currentUser || null);

watch(() => props.currentUser, (v) => { currentUser.value = v; });

const applications = ref([]);
const loading = ref(false);
const starting = ref(false);

// Утилитарные функции
function getQuestTypeIcon(type) {
  const icons = {
    SOLO: '👤',
    TEAM: '👥'
  };
  return icons[type] || '🎯';
}

function getQuestTypeName(type) {
  const names = {
    SOLO: 'Соло',
    TEAM: 'Команда'
  };
  return names[type] || type;
}

function getDifficultyType(difficulty) {
  const types = {
    EASY: 'success',
    MEDIUM: 'warning',
    HARD: 'error'
  };
  return types[difficulty] || 'default';
}

function getDifficultyName(difficulty) {
  const names = {
    EASY: 'Легкий',
    MEDIUM: 'Средний',
    HARD: 'Сложный'
  };
  return names[difficulty] || difficulty;
}

function getStatusType(quest) {
  if (!quest.published) return 'default';
  if (quest.endAt && new Date(quest.endAt) < new Date()) return 'error';
  if (quest.startAt && new Date(quest.startAt) > new Date()) return 'warning';
  return 'success';
}

function getStatusText(quest) {
  if (!quest.published) return 'Черновик';
  if (quest.endAt && new Date(quest.endAt) < new Date()) return 'Завершен';
  if (quest.startAt && new Date(quest.startAt) > new Date()) return 'Скоро';
  return 'Активен';
}

function isQuestStarted(quest) {
  if (!quest?.startAt) return true;
  return new Date(quest.startAt) <= new Date();
}

function getTimeUntilStart(startDate) {
  const now = new Date();
  const start = new Date(startDate);
  return Math.max(0, start.getTime() - now.getTime());
}

function getTeamInitials(teamName) {
  if (!teamName) return 'К';
  return teamName
    .split(' ')
    .map(word => word[0])
    .join('')
    .toUpperCase()
    .slice(0, 2);
}

// Опции для группы аватаров авторов
const authorOptions = computed(() => {
  if (!props.quest?.authors) return [];
  return props.quest.authors.map(author => ({
    src: null,
    fallbackSrc: null,
    name: author.publicName,
    round: true
  }));
});

// Вычисляемые свойства
const pending = computed(() =>
  applications.value.filter(a => String(a.status).toUpperCase().includes("PENDING"))
);

const accepted = computed(() =>
  applications.value.filter(a => String(a.status).toUpperCase().includes("ACCEPTED"))
);

const isCaptain = computed(() =>
  !!currentUser.value?.captain && !!currentUser.value?.team
);

const canSeeApplications = computed(() => props.canEdit);

const myApplication = computed(() => {
  if (!currentUser.value || !currentUser.value.team) return null;
  return applications.value.find(a =>
    a.teamId && currentUser.value.team &&
    Number(a.teamId) === Number(currentUser.value.team.id)
  ) || null;
});

const questStarted = computed(() => isQuestStarted(props.quest));

const canEnter = computed(() => {
  if (!questStarted.value || !currentUser.value?.team) return false;
  return accepted.value.some(a =>
    Number(a.teamId) === Number(currentUser.value.team.id)
  );
});

// Методы
async function ensureUser() {
  if (!currentUser.value) {
    try {
      currentUser.value = await fetchCurrentUser();
    } catch (e) {
      console.warn("Не удалось подгрузить currentUser в QuestCard", e);
    }
  }
}

async function loadApplications() {
  if (!props.quest?.id) return;
  loading.value = true;
  try {
    const { data } = await api.get(`/participation/by-quest/${props.quest.id}`);
    applications.value = data || [];
  } catch (err) {
    console.error("Ошибка загрузки заявок:", err);
  } finally {
    loading.value = false;
  }
}

async function startGame() {
  if (!currentUser.value) {
    message.error("Неизвестный пользователь");
    return;
  }
  if (!currentUser.value.team?.id) {
    message.error("Вы должны быть в команде");
    return;
  }
  try {
    starting.value = true;
    const resp = await api.post(`/quests/${props.quest.id}/start`, {
      userId: currentUser.value.id,
      teamId: currentUser.value.team.id
    });
    const sessionId = resp.data.id;
    if (sessionId) {
      router.push(`/play/${sessionId}`);
    } else {
      message.error("Не удалось получить id сессии");
    }
  } catch (err) {
    console.error("Ошибка старта игры:", err);
    const text = err?.response?.data?.message || "Не удалось начать игру";
    message.error(text);
  } finally {
    starting.value = false;
  }
}

async function applyToQuest() {
  if (!currentUser.value?.team?.id) {
    message.error("Ваша учётная запись не привязана к команде");
    return;
  }
  if (!currentUser.value?.captain) {
    message.error("Только капитан команды может подать заявку");
    return;
  }
  try {
    await api.post(`/participation`, {
      questId: props.quest.id,
      teamId: currentUser.value.team.id,
      type: "TEAM",
      status: "PENDING"
    });
    message.success("Заявка отправлена");
    await loadApplications();
  } catch (err) {
    console.error("Ошибка отправки заявки:", err);
    const text = err?.response?.data?.message || "Не удалось отправить заявку";
    message.error(text);
  }
}

async function withdrawApplication() {
  if (!myApplication.value) return;
  try {
    await api.delete(`/participation/${myApplication.value.id}`);
    message.success("Заявка отозвана");
    await loadApplications();
  } catch (err) {
    console.error("Ошибка отзыва заявки:", err);
    message.error("Не удалось отозвать заявку");
  }
}

async function changeStatus(id, status) {
  try {
    await api.post(`/participation/${id}/status`, null, { params: { status } });
    message.success(status === "ACCEPTED" ? "Принято" : "Отклонено");
    await loadApplications();
  } catch (err) {
    console.error("Ошибка изменения статуса:", err);
    message.error("Не удалось изменить статус");
  }
}

function formatDate(iso) {
  if (!iso) return "";
  return new Date(iso).toLocaleString('ru-RU', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
}

function handleCountdownFinish() {
  message.info(`Квест "${props.quest.title}" начался!`);
}

// Жизненный цикл
onMounted(async () => {
  await ensureUser();
  await loadApplications();
});

watch(() => props.quest?.id, () => loadApplications());
</script>

<style scoped>
.line-clamp-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* Анимации для карточки */
.quest-card {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.quest-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

/* Стили для градиентов */
.from-\[var\(--color-bg\)\] {
  --tw-gradient-from: var(--color-bg);
}

.to-\[var\(--color-bg-card\)\] {
  --tw-gradient-to: var(--color-bg-card);
}
</style>
