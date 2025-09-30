<!-- src/components/QuestCard.vue -->
<template>
  <n-card
      hoverable
      class="quest-card rounded-3xl overflow-hidden shadow-2xl bg-[var(--color-bg-card)] text-[var(--color-text)] flex flex-col justify-between w-full"
  >
    <div class="flex flex-col md:flex-row gap-4 p-6">
      <!-- левый блок -->
      <div class="w-full md:w-40 flex-shrink-0">
        <div class="h-36 w-full rounded-xl bg-[var(--color-bg)] flex items-center justify-center border border-white/6">
          <div class="text-sm opacity-80 text-[var(--color-text)]">
            {{ quest.type }}
            <div class="mt-2 text-xs opacity-60">{{ quest.difficulty }}</div>
          </div>
        </div>
      </div>

      <!-- правый блок -->
      <div class="flex-1 flex flex-col">
        <div class="flex items-start justify-between gap-4">
          <div class="flex-1">
            <h3 class="text-2xl font-semibold text-[var(--color-text-strong)] leading-tight">
              {{ quest.title }}
            </h3>
            <div class="mt-2 text-sm text-[var(--color-text)] opacity-80" v-html="quest.descriptionHtml"></div>
          </div>
          <div class="flex flex-col items-end gap-2 text-right">
            <div class="text-sm opacity-70">{{ formatDate(quest.startAt) }}</div>
            <div class="text-sm opacity-70">{{ formatDate(quest.endAt) }}</div>
            <div v-if="quest.startAt" class="mt-2 text-xs bg-white/5 px-2 py-1 rounded text-[var(--color-text)]">
              До старта: {{ timeUntil(quest.startAt) }}
            </div>
          </div>
        </div>

        <div class="mt-4 flex items-center justify-between">
          <div class="flex items-center gap-2">
            <span class="text-xs px-2 py-1 rounded bg-white/5">{{ quest.difficulty }}</span>
            <span class="text-xs px-2 py-1 rounded bg-white/5">{{ quest.type }}</span>
            <div class="flex items-center gap-2 ml-3">
              <span
                  v-for="a in quest.authors"
                  :key="a.id"
                  class="text-xs px-2 py-1 rounded bg-[var(--color-bg)] border border-white/6 text-[var(--color-text)]"
              >
                {{ a.publicName }}
              </span>
            </div>
          </div>
          <div class="text-sm opacity-60">#{{ quest.id }}</div>
        </div>
      </div>
    </div>

    <!-- кнопки и списки -->
    <div class="p-4 border-t border-white/6 bg-[var(--color-bg-card)]">
      <div class="flex gap-3 items-center mb-3">
        <!-- Войти в игру -->
        <n-button
            v-if="canEnter"
            class="btn-accent rounded-xl py-3 text-lg font-semibold flex-1"
            :loading="starting"
            @click="startGame"
        >
          Войти в игру
        </n-button>

        <!-- Подать заявку / Отозвать (видит капитан) -->
        <div v-if="isCaptain" class="flex-1 gap-2 btn-accent rounded-xl py-3 text-lg font-semibold">
          <n-button
              v-if="!myApplication || ['REJECTED','CANCELLED'].includes(myApplication.status)"
              type="primary"
              @click="applyToQuest"
          >
            Подать заявку
          </n-button>
          <n-button v-else-if="myApplication.status === 'PENDING'" type="error" @click="withdrawApplication">
            Отозвать заявку
          </n-button>
          <n-tag v-else-if="myApplication.status === 'ACCEPTED'">
            Ваша команда принята
          </n-tag>
        </div>

        <!-- Редактировать (автор / админ) -->
        <n-button
            v-if="canEdit"
            type="warning"
            class="rounded-xl py-3 font-semibold flex-1 text-lg"
            @click="$emit('edit', quest.id)"
        >
          Редактировать
        </n-button>
      </div>

      <!-- Листы заявок (автор/админ видит все) -->
      <div v-if="canSeeApplications">
        <h4 class="font-semibold mb-2">Подали заявки</h4>
        <div v-if="pending.length">
          <ul class="list-disc list-inside">
            <li v-for="p in pending" :key="p.id" class="flex justify-between items-center py-1">
              <span>{{ p.teamName || ('Команда #' + p.teamId) }}</span>
              <div class="flex gap-2">
                <n-button size="small" type="success" @click="changeStatus(p.id, 'ACCEPTED')">Принять</n-button>
                <n-button size="small" type="error" @click="changeStatus(p.id, 'REJECTED')">Отклонить</n-button>
              </div>
            </li>
          </ul>
        </div>
        <div v-else class="text-gray-400">Нет ожидающих заявок</div>

        <h4 class="font-semibold mt-4 mb-2">Приняты</h4>
        <div v-if="accepted.length">
          <ul class="list-disc list-inside">
            <li v-for="r in accepted" :key="r.id" class="flex justify-between items-center py-1">
              <div>
                <strong>{{ r.teamName || ('Команда #' + r.teamId) }}</strong>
              </div>
              <div class="flex gap-2">
                <n-button size="small" type="error" @click="changeStatus(r.id, 'REJECTED')">
                  Исключить
                </n-button>
              </div>
            </li>
          </ul>
        </div>
        <div v-else class="text-gray-400">Пока нет принятых команд</div>
      </div>
    </div>
  </n-card>
</template>

<script setup>
import { ref, computed, onMounted, watch } from "vue";
import { NCard, NButton, NTag } from "naive-ui";
import api from "@/services/api";
import { useMessage } from "naive-ui";
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

onMounted(async () => {
  await ensureUser();
  await loadApplications();
});
watch(() => props.quest?.id, () => loadApplications());

const pending = computed(() => applications.value.filter(a => String(a.status).toUpperCase().includes("PENDING")));
const accepted = computed(() => applications.value.filter(a => String(a.status).toUpperCase().includes("ACCEPTED")));
const isCaptain = computed(() => !!currentUser.value?.captain && !!currentUser.value?.team);
const canSeeApplications = computed(() => props.canEdit);

const myApplication = computed(() => {
  if (!currentUser.value || !currentUser.value.team) return null;
  return applications.value.find(a => a.teamId && currentUser.value.team && Number(a.teamId) === Number(currentUser.value.team.id)) || null;
});

const questStarted = computed(() => {
  if (!props.quest?.startAt) return false;
  return new Date(props.quest.startAt) <= new Date();
});
const canEnter = computed(() => {
  if (!questStarted.value || !currentUser.value?.team) return false;
  return accepted.value.some(a => Number(a.teamId) === Number(currentUser.value.team.id));
});

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
    const resp = await api.post("/sessions/start", {
      questId: props.quest.id,
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
  return new Date(iso).toLocaleString();
}

function timeUntil(date) {
  const now = new Date();
  const target = new Date(date);
  const diff = target - now;
  if (diff <= 0) return "уже начался";
  const days = Math.floor(diff / (1000 * 60 * 60 * 24));
  const hours = Math.floor((diff / (1000 * 60 * 60)) % 24);
  const minutes = Math.floor((diff / (1000 * 60)) % 60);
  return `${days}д ${hours}ч ${minutes}м`;
}
</script>
