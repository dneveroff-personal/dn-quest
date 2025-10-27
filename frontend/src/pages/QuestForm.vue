<!-- src/pages/QuestForm.vue -->
<template>
  <div class="flex items-center justify-center min-h-screen w-full px-4 bg-[var(--color-bg)]">
    <n-card class="w-full max-w-5xl p-8 shadow-2xl rounded-3xl bg-[var(--color-bg-card)] text-[var(--color-text)]">

      <!-- Заголовок + статус -->
      <div class="flex items-center justify-between mb-6">
        <h2 class="text-2xl font-bold">
          {{ isEditMode ? "Редактировать квест" : "Создать квест" }}
        </h2>
        <div class="flex items-center gap-2">
          <span class="text-sm opacity-70">Опубликован</span>
          <n-switch v-model:value="form.published" />
        </div>
      </div>

      <n-tabs type="line" animated>
        <!-- Вкладка 1: Основное -->
        <n-tab-pane name="main" tab="Основная информация">
          <n-form @submit.prevent="handleSubmit" label-placement="top">

            <!-- Название -->
            <n-form-item label="Название">
              <n-input v-model:value="form.title" placeholder="Введите название квеста" />
            </n-form-item>

            <!-- Описание -->
            <n-form-item label="Описание (HTML)">
              <n-input
                  type="textarea"
                  v-model:value="form.descriptionHtml"
                  placeholder="Введите описание (html)"
                  :autosize="{ minRows: 5, maxRows: 12 }"
              />
            </n-form-item>

            <!-- Сложность и Тип -->
            <div class="flex gap-6">
              <n-form-item label="Сложность" class="flex-1 max-w-xs">
                <n-select
                    v-model:value="form.difficulty"
                    :options="difficultyOptions"
                    size="small"
                />
              </n-form-item>

              <n-form-item label="Тип" class="flex-1 max-w-xs">
                <n-select
                    v-model:value="form.type"
                    :options="typeOptions"
                    size="small"
                />
              </n-form-item>
            </div>

            <!-- Даты -->
            <div class="flex gap-6">
              <n-form-item label="Дата начала">
                <n-date-picker
                    v-model:value="form.startAtLocal"
                    type="datetime"
                    clearable
                    style="width: 100%"
                />
              </n-form-item>

              <n-form-item label="Дата окончания">
                <n-date-picker
                    v-model:value="form.endAtLocal"
                    type="datetime"
                    clearable
                    style="width: 100%"
                />
              </n-form-item>
            </div>

            <!-- Авторы -->
            <n-form-item label="Авторы">
              <n-select
                  v-model:value="form.authors"
                  multiple
                  :options="authorOptions"
                  placeholder="Выберите авторов"
                  style="width: 100%"
              />
            </n-form-item>

            <!-- Изображение -->
            <n-form-item label="Изображение (URL)">
              <n-input v-model:value="form.imageUrl" placeholder="https://..." />
            </n-form-item>
          </n-form>
        </n-tab-pane>

        <!-- Вкладка 2: Уровни -->
        <n-tab-pane name="levels" tab="Уровни">
          <LevelsManager ref="levelsManagerRef" :quest-id="questId" />
        </n-tab-pane>

        <!-- Участники / заявки (видна только в режиме редактирования) -->
        <n-tab-pane v-if="isEditMode" name="participants" tab="Участники / Заявки">
          <div class="py-4">
            <n-button size="small" @click="loadParticipationRequests">Обновить</n-button>
            <div class="mt-4">
              <h4 class="font-semibold mb-2">Ожидают</h4>
              <div v-if="requestsPending.length">
                <ul class="list-disc list-inside">
                  <li v-for="r in requestsPending" :key="r.id" class="flex justify-between items-center py-1">
                    <div>
                      <strong>{{ r.teamName || ('Команда #' + r.teamId) }}</strong>
                      <span class="text-xs text-gray-400 ml-2">({{ r.createdAt ? new Date(r.createdAt).toLocaleString() : '' }})</span>
                    </div>
                    <div class="flex gap-2">
                      <n-button size="small" type="success" @click="changeParticipationStatus(r.id, 'ACCEPTED')">Принять</n-button>
                      <n-button size="small" type="error" @click="changeParticipationStatus(r.id, 'REJECTED')">Отклонить</n-button>
                    </div>
                  </li>
                </ul>
              </div>
              <div v-else class="text-gray-400">Нет ожидающих заявок</div>

              <h4 class="font-semibold mt-4 mb-2">Приняты</h4>
              <div v-if="requestsAccepted.length">
                <ul class="list-disc list-inside">
                  <li v-for="r in requestsAccepted" :key="r.id" class="flex justify-between items-center py-1">
                    <div>
                      <strong>{{ r.teamName || ('Команда #' + r.teamId) }}</strong>
                    </div>
                    <div class="flex gap-2">
                      <n-button size="small" type="error" @click="changeParticipationStatus(r.id, 'REJECTED')">
                        Исключить
                      </n-button>
                    </div>
                  </li>
                </ul>
              </div>
              <div v-else class="text-gray-400">Пока нет принятых команд</div>
            </div>
          </div>
        </n-tab-pane>
      </n-tabs>

      <!-- Кнопки -->
      <div class="flex justify-between mt-8 pt-6 border-t border-white/10">
        <div v-if="isEditMode">
          <n-button @click="deleteQuest" type="error" ghost>Удалить игру</n-button>
        </div>
        <div class="flex gap-4">
          <n-button @click="cancel" type="default">Отмена</n-button>
          <n-button type="primary" @click="handleSubmit">
            {{ isEditMode ? "Сохранить" : "Создать" }}
          </n-button>
        </div>
      </div>

    </n-card>
  </div>
</template>


<script setup>
import { ref, onMounted, computed } from "vue";
import { useRouter, useRoute } from "vue-router";
import { NCard, NButton, NForm, NFormItem, NInput, NSelect, NSwitch, NDatePicker, useMessage } from "naive-ui";
import api from "@/services/api";
import LevelsManager from "@/components/quests/LevelsManager.vue";

const levelsManagerRef = ref(null);
const router = useRouter();
const route = useRoute();
const message = useMessage();
const isEditMode = computed(() => !!route.params.id);
const questId = route.params.id;
const participationRequests = ref([]);

const form = ref({
  title: "",
  descriptionHtml: "",
  imageUrl: "",
  difficulty: null,
  type: null,
  startAtLocal: null,  // ← было ""
  endAtLocal: null,    // ← было ""
  authors: [],
  published: false
});

const authorOptions = ref([]);
const difficultyOptions = [
  { label: "Легко", value: "EASY" },
  { label: "Средне", value: "MEDIUM" },
  { label: "Сложно", value: "HARD" },
];
const typeOptions = [
  { label: "Одиночный", value: "SOLO" },
  { label: "Командный", value: "TEAM" },
];

const requestsPending = computed(() =>
    participationRequests.value.filter(r => r.status === "PENDING")
);
const requestsAccepted = computed(() =>
    participationRequests.value.filter(r => r.status === "ACCEPTED")
);

async function loadParticipationRequests() {
  if (!isEditMode.value) return;
  try {
    const { data } = await api.get(`/participation/by-quest/${questId}`);
    participationRequests.value = data || [];
  } catch (err) {
    console.error("Ошибка загрузки заявок", err);
  }
}

async function changeParticipationStatus(id, status) {
  try {
    await api.post(`/participation/${id}/status`, null, { params: { status } });
    message.success(status === "ACCEPTED" ? "Принято" : "Отклонено");
    await loadParticipationRequests();
  } catch (err) {
    console.error("Ошибка изменения статуса", err);
    message.error("Не удалось изменить статус заявки");
  }
}

async function loadAuthors() {
  try {
    const { data } = await api.get("/users?role=AUTHOR");
    authorOptions.value = data.map(a => ({ label: a.publicName, value: a.id }));
  } catch (err) {
    console.error("Ошибка загрузки авторов", err);
  }
}

function toLocalInput(iso) {
  if (!iso) return "";
  const d = new Date(iso);
  const pad = n => (n < 10 ? "0" + n : n);
  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

function toIsoFromLocal(local) {
  if (!local) return null;
  const d = new Date(local);
  return d.toISOString();
}

async function loadQuest() {
  if (!isEditMode.value) return;
  try {
    const { data } = await api.get(`/quests/${route.params.id}`);

    form.value.title = data.title || "";
    form.value.descriptionHtml = data.descriptionHtml || "";
    form.value.imageUrl = data.imageUrl || "";
    form.value.difficulty = data.difficulty || null;
    form.value.type = data.type || null;

    // тут преобразуем ISO → timestamp
    form.value.startAtLocal = data.startAt ? new Date(data.startAt).getTime() : null;
    form.value.endAtLocal = data.endAt ? new Date(data.endAt).getTime() : null;

    form.value.authors = (data.authors || []).map(a => a.id);
    form.value.published = !!data.published;
  } catch (err) {
    console.error("Ошибка загрузки квеста", err);
  }
}

async function handleSubmit() {
  try {
    const payload = {
      title: form.value.title,
      descriptionHtml: form.value.descriptionHtml,
      imageUrl: form.value.imageUrl,
      difficulty: form.value.difficulty,
      type: form.value.type,
      startAt: toIsoFromLocal(form.value.startAtLocal),
      endAt: toIsoFromLocal(form.value.endAtLocal),
      published: form.value.published,
      authors: form.value.authors.map(id => ({ id }))
    };

    if (isEditMode.value) {
      await api.put(`/quests/${route.params.id}`, payload);

      // 🔹 сохраняем порядок уровней
      if (levelsManagerRef.value) {
        const orderedIds = levelsManagerRef.value.getOrderedIds();
        await api.put("/levels/reorder", orderedIds);
      }

    } else {
      await api.post("/quests", payload);
    }

    router.push("/");
  } catch (err) {
    console.error("Ошибка сохранения квеста", err);
  }
}

function cancel() {
  router.push("/");
}

async function deleteQuest() {
  const confirmed = window.confirm("Вы действительно хотите удалить игру?");
  if (!confirmed) {
    message.info("Удаление отменено");
    return;
  }

  try {
    await api.delete(`/quests/${route.params.id}`);
    message.success(`Игра #${route.params.id} была удалена`);
    await router.push("/");
  } catch (err) {
    message.error("Ошибка удаления квеста");
    console.error("Ошибка удаления квеста", err);
  }
}

onMounted(async () => {
  await loadAuthors();
  await loadQuest();
  await loadParticipationRequests();
});
</script>
