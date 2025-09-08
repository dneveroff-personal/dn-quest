<template>
  <div class="p-6 space-y-6 bg-[var(--color-bg)] min-h-screen">
    <n-card class="card max-w-2xl mx-auto space-y-6">
      <h2 class="text-2xl font-bold text-[var(--color-text-strong)]">Создать квест</h2>

      <!-- Название -->
      <div class="flex flex-col">
        <label class="mb-1 font-semibold text-[var(--color-text)]" for="title">Название</label>
        <input
            id="title"
            type="text"
            v-model="form.title"
            placeholder="Название квеста"
            class="p-3 rounded-md bg-gray-900 text-white border border-gray-700 focus:outline-none focus:border-[var(--color-accent)]"
        />
      </div>

      <!-- Описание -->
      <div class="flex flex-col">
        <label class="mb-1 font-semibold text-[var(--color-text)]" for="description">Описание (HTML)</label>
        <textarea
            id="description"
            v-model="form.descriptionHtml"
            rows="5"
            placeholder="Описание квеста"
            class="p-3 rounded-md bg-gray-900 text-white border border-gray-700 focus:outline-none focus:border-[var(--color-accent)]"
        ></textarea>
      </div>

      <!-- Сложность -->
      <div class="flex flex-col">
        <label class="mb-1 font-semibold text-[var(--color-text)]" for="difficulty">Сложность</label>
        <select
            id="difficulty"
            v-model="form.difficulty"
            class="p-3 rounded-md bg-gray-900 text-white border border-gray-700 focus:outline-none focus:border-[var(--color-accent)]"
        >
          <option disabled value="">Выберите сложность</option>
          <option v-for="d in difficulties" :key="d" :value="d">{{ d }}</option>
        </select>
      </div>

      <!-- Тип -->
      <div class="flex flex-col">
        <label class="mb-1 font-semibold text-[var(--color-text)]" for="type">Тип</label>
        <select
            id="type"
            v-model="form.type"
            class="p-3 rounded-md bg-gray-900 text-white border border-gray-700 focus:outline-none focus:border-[var(--color-accent)]"
        >
          <option disabled value="">Выберите тип</option>
          <option v-for="t in types" :key="t" :value="t">{{ t }}</option>
        </select>
      </div>

      <!-- Даты -->
      <div class="flex gap-4">
        <div class="flex flex-col flex-1">
          <label class="mb-1 font-semibold text-[var(--color-text)]" for="startAt">Начало</label>
          <input
              id="startAt"
              type="datetime-local"
              v-model="form.startAt"
              class="p-3 rounded-md bg-gray-900 text-white border border-gray-700 focus:outline-none focus:border-[var(--color-accent)]"
          />
        </div>
        <div class="flex flex-col flex-1">
          <label class="mb-1 font-semibold text-[var(--color-text)]" for="endAt">Конец</label>
          <input
              id="endAt"
              type="datetime-local"
              v-model="form.endAt"
              class="p-3 rounded-md bg-gray-900 text-white border border-gray-700 focus:outline-none focus:border-[var(--color-accent)]"
          />
        </div>
      </div>

      <!-- Публикация -->
      <div class="flex items-center gap-2">
        <input type="checkbox" id="published" v-model="form.published" />
        <label for="published" class="text-[var(--color-text)]">Опубликован</label>
      </div>

      <!-- Авторы -->
      <div class="flex flex-col">
        <label class="mb-1 font-semibold text-[var(--color-text)]">Авторы</label>
        <select
            v-model="form.authors"
            multiple
            class="p-3 rounded-md bg-gray-900 text-white border border-gray-700 focus:outline-none focus:border-[var(--color-accent)] h-40"
        >
          <option v-for="author in availableAuthors" :key="author.id" :value="author">
            {{ author.publicName || author.username }}
          </option>
        </select>
      </div>

      <!-- Кнопка создания -->
      <div class="flex justify-end">
        <button @click="submit" class="btn-accent">Создать квест</button>
      </div>
    </n-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import api from "../services/api";

const form = ref({
  title: "",
  descriptionHtml: "",
  difficulty: "",
  type: "",
  startAt: "",
  endAt: "",
  published: false,
  authors: []
});

// Списки для селектов
const difficulties = ["EASY", "MEDIUM", "HARD"];
const types = ["SOLO", "TEAM"];

// Авторы
const availableAuthors = ref([]);

async function loadAuthors() {
  try {
    const { data } = await api.get("/users?role=AUTHOR");
    availableAuthors.value = data;
  } catch (err) {
    console.error("Не удалось загрузить авторов", err);
  }
}

// Отправка формы
async function submit() {
  try {
    const payload = {
      title: form.value.title,
      descriptionHtml: form.value.descriptionHtml,
      difficulty: form.value.difficulty || null,
      type: form.value.type || null,
      startAt: form.value.startAt ? new Date(form.value.startAt).toISOString() : null,
      endAt: form.value.endAt ? new Date(form.value.endAt).toISOString() : null,
      published: form.value.published,
      authors: form.value.authors.map(a => ({ id: a.id }))
    };

    console.log(payload); // проверьте в консоли

    await api.post("/quests", payload);
    alert("Квест создан!");
    form.value = {
      title: "",
      descriptionHtml: "",
      difficulty: "",
      type: "",
      startAt: "",
      endAt: "",
      published: false,
      authors: []
    };
  } catch (err) {
    console.error(err);
    alert("Ошибка создания квеста");
  }
}

onMounted(() => {
  loadAuthors();
});
</script>
