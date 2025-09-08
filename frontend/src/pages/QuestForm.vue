<!-- src/pages/QuestForm.vue -->
<template>
  <div class="flex items-center justify-center min-h-screen w-full px-4 bg-[var(--color-bg)]">
    <n-card class="w-full max-w-3xl p-8 shadow-2xl rounded-3xl bg-[var(--color-bg-card)] text-[var(--color-text)]">
      <h2 class="text-2xl font-bold mb-6">
        {{ isEditMode ? "Редактировать квест" : "Создать квест" }}
      </h2>

      <n-form @submit.prevent="handleSubmit" label-placement="top">
        <n-form-item label="Название">
          <n-input v-model:value="form.title" placeholder="Введите название квеста" />
        </n-form-item>

        <n-form-item label="Описание (HTML)">
          <n-input type="textarea" v-model:value="form.descriptionHtml" placeholder="Введите описание (html)" />
        </n-form-item>

        <n-form-item label="Изображение (URL)">
          <n-input v-model:value="form.imageUrl" placeholder="https://..." />
        </n-form-item>

        <n-form-item label="Сложность">
          <n-select v-model:value="form.difficulty" :options="difficultyOptions" placeholder="Выберите сложность" />
        </n-form-item>

        <n-form-item label="Тип">
          <n-select v-model:value="form.type" :options="typeOptions" placeholder="Выберите тип" />
        </n-form-item>

        <n-form-item label="Дата начала">
          <n-input v-model:value="form.startAtLocal" placeholder="yyyy-MM-ddTHH:mm" />
          <div class="text-xs opacity-60 mt-1">формат: yyyy-MM-ddTHH:mm (локальное время). Будет автоматически конвертировано в ISO UTC.</div>
        </n-form-item>

        <n-form-item label="Дата окончания">
          <n-input v-model:value="form.endAtLocal" placeholder="yyyy-MM-ddTHH:mm" />
        </n-form-item>

        <n-form-item label="Авторы">
          <n-select
              v-model:value="form.authors"
              multiple
              :options="authorOptions"
              placeholder="Выберите авторов"
              style="width: 100%"
          />
        </n-form-item>

        <n-form-item label="Опубликован">
          <n-switch v-model:value="form.published" />
        </n-form-item>

        <div class="flex justify-end gap-4 mt-6">
          <n-button @click="cancel" type="default">Отмена</n-button>
          <n-button type="primary" attr-type="submit">
            {{ isEditMode ? "Сохранить" : "Создать" }}
          </n-button>
        </div>
      </n-form>
    </n-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from "vue";
import { useRouter, useRoute } from "vue-router";
import { NCard, NButton, NForm, NFormItem, NInput, NSelect, NSwitch } from "naive-ui";
import api from "@/services/api";

const router = useRouter();
const route = useRoute();

const isEditMode = computed(() => !!route.params.id);

const form = ref({
  title: "",
  descriptionHtml: "",
  imageUrl: "",
  difficulty: null,
  type: null,
  // для удобства пользователю — временные локальные поля (yyyy-MM-ddTHH:mm)
  startAtLocal: "",
  endAtLocal: "",
  authors: [], // массив id
  published: false
});

const authorOptions = ref([]);
const difficultyOptions = [
  { label: "Легко", value: "EASY" },
  { label: "Средне", value: "MEDIUM" },
  { label: "Сложно", value: "HARD" },
];
const typeOptions = [
  { label: "Одиночный", value: "SINGLE" },
  { label: "Командный", value: "TEAM" },
  // используй реальные значения enum из бэка
];

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
  // yyyy-MM-ddTHH:mm
  const pad = n => (n < 10 ? "0" + n : n);
  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

function toIsoFromLocal(local) {
  if (!local) return null;
  // local: "yyyy-MM-ddTHH:mm" — treat as local time, convert to ISO
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
    form.value.startAtLocal = toLocalInput(data.startAt);
    form.value.endAtLocal = toLocalInput(data.endAt);
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
    } else {
      await api.post("/quests", payload);
    }
    router.push("/");
  } catch (err) {
    console.error("Ошибка сохранения квеста", err);
    // можно показать нативный message
  }
}

function cancel() {
  router.push("/");
}

onMounted(async () => {
  await loadAuthors();
  await loadQuest();
});
</script>
