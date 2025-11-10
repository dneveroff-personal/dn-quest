<template>
  <div class="flex flex-col items-center justify-start min-h-screen px-4 md:px-0 bg-[var(--color-bg)] py-8">
    <!-- Заголовок страницы -->
    <div class="text-center mb-8">
      <h1 class="text-4xl font-bold mb-2 text-[var(--color-text-strong)] flex items-center justify-center gap-3">
        <span class="text-5xl">🎯</span>
        Квесты
      </h1>
      <p class="text-lg text-[var(--color-text)] opacity-80">
        Откройте для себя удивительные приключения
      </p>
    </div>

    <!-- Фильтры и поиск -->
    <div class="w-full max-w-4xl mb-6">
      <div class="flex flex-col md:flex-row gap-4 items-center justify-between">
        <div class="flex-1 w-full">
          <n-input
              v-model:value="searchQuery"
              placeholder="Поиск квестов..."
              clearable
              @input="handleSearch"
              class="w-full"
          >
            <template #prefix>
              <span>🔍</span>
            </template>
          </n-input>
        </div>
        
        <div class="flex gap-2">
          <n-select
              v-model:value="selectedDifficulty"
              :options="difficultyOptions"
              placeholder="Сложность"
              clearable
              class="w-40"
              @update:value="handleFilter"
          />
          <n-select
              v-model:value="selectedType"
              :options="typeOptions"
              placeholder="Тип"
              clearable
              class="w-40"
              @update:value="handleFilter"
          />
        </div>
      </div>
    </div>

    <!-- Статистика -->
    <div class="w-full max-w-4xl mb-6">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <n-card class="text-center">
          <div class="text-2xl font-bold text-[var(--color-primary)]">{{ totalQuests }}</div>
          <div class="text-sm text-[var(--color-text)] opacity-70">Всего квестов</div>
        </n-card>
        <n-card class="text-center">
          <div class="text-2xl font-bold text-green-500">{{ activeQuests }}</div>
          <div class="text-sm text-[var(--color-text)] opacity-70">Активные</div>
        </n-card>
        <n-card class="text-center">
          <div class="text-2xl font-bold text-orange-500">{{ completedQuests }}</div>
          <div class="text-sm text-[var(--color-text)] opacity-70">Завершено</div>
        </n-card>
      </div>
    </div>

    <!-- Список квестов -->
    <div class="w-full max-w-6xl">
      <n-spin :show="loading" class="w-full">
        <div v-if="filteredQuests.length === 0 && !loading" class="text-center py-12">
          <div class="text-6xl mb-4">🔍</div>
          <h3 class="text-xl font-semibold text-[var(--color-text)] mb-2">
            {{ searchQuery ? 'Квесты не найдены' : 'Квестов пока нет' }}
          </h3>
          <p class="text-[var(--color-text)] opacity-70">
            {{ searchQuery ? 'Попробуйте изменить параметры поиска' : 'Загляните позже!' }}
          </p>
        </div>
        
        <div v-else class="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <quest-card
              v-for="quest in filteredQuests"
              :key="quest.id"
              :quest="quest"
              :can-edit="canEdit(quest)"
              :currentUser="props.currentUser"
              @play="openQuest"
              @edit="editQuest"
              class="transition-all duration-300 hover:scale-[1.02]"
          />
        </div>
      </n-spin>
    </div>

    <!-- Пагинация -->
    <div v-if="totalPages > 1" class="mt-8">
      <n-pagination
          v-model:page="currentPage"
          :page-count="totalPages"
          :page-size="pageSize"
          show-size-picker
          :page-sizes="[6, 12, 24]"
          @update:page="handlePageChange"
          @update:page-size="handlePageSizeChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from "vue";
import { useRouter } from "vue-router";
import {
  NSpin,
  NInput,
  NSelect,
  NCard,
  NPagination,
  useMessage
} from "naive-ui";
import { questService } from "@/services/api";
import { handleError } from "@/services/errorHandler";
import QuestCard from "@/components/QuestCard.vue";

const props = defineProps({
  currentUser: { type: Object, default: null }
});

const router = useRouter();
const message = useMessage();

// Данные
const quests = ref([]);
const loading = ref(false);
const searchQuery = ref("");
const selectedDifficulty = ref(null);
const selectedType = ref(null);
const currentPage = ref(1);
const pageSize = ref(12);

// Опции для фильтров
const difficultyOptions = [
  { label: "Легкий", value: "EASY" },
  { label: "Средний", value: "MEDIUM" },
  { label: "Сложный", value: "HARD" }
];

const typeOptions = [
  { label: "Соло", value: "SOLO" },
  { label: "Команда", value: "TEAM" }
];

// Вычисляемые свойства
const filteredQuests = computed(() => {
  let filtered = quests.value;

  // Поиск по названию
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    filtered = filtered.filter(quest =>
      quest.title.toLowerCase().includes(query) ||
      quest.description?.toLowerCase().includes(query)
    );
  }

  // Фильтр по сложности
  if (selectedDifficulty.value) {
    filtered = filtered.filter(quest => quest.difficulty === selectedDifficulty.value);
  }

  // Фильтр по типу
  if (selectedType.value) {
    filtered = filtered.filter(quest => quest.type === selectedType.value);
  }

  return filtered;
});

const totalPages = computed(() => Math.ceil(filteredQuests.value.length / pageSize.value));

const paginatedQuests = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return filteredQuests.value.slice(start, end);
});

// Статистика
const totalQuests = computed(() => quests.value.length);
const activeQuests = computed(() =>
  quests.value.filter(q => q.published && !q.endAt).length
);
const completedQuests = computed(() =>
  quests.value.filter(q => q.endAt && new Date(q.endAt) < new Date()).length
);

// Методы
async function loadQuests() {
  if (!props.currentUser) return;
  
  loading.value = true;
  try {
    let response;
    if (props.currentUser.role === "ADMIN") {
      response = await questService.getQuests();
      quests.value = response.data || [];
    } else if (props.currentUser.role === "AUTHOR") {
      response = await questService.getQuests();
      quests.value = (response.data || []).filter(
          (q) =>
              q.published ||
              q.authors?.some((a) => Number(a.id) === Number(props.currentUser.id))
      );
    } else {
      response = await questService.getPublishedQuests();
      quests.value = response.data || [];
    }
  } catch (err) {
    handleError(err, {
      context: 'Loading quests',
      customMessage: 'Не удалось загрузить квесты',
    });
    quests.value = [];
  } finally {
    loading.value = false;
  }
}

function canEdit(quest) {
  if (!props.currentUser) return false;
  if (props.currentUser.role === "ADMIN") return true;
  if (props.currentUser.role === "AUTHOR") {
    return quest.authors?.some((a) => a.id === props.currentUser.id);
  }
  return false;
}

function openQuest(id) {
  router.push(`/play/${id}`);
}

function editQuest(id) {
  router.push(`/quests/${id}/edit`);
}

function handleSearch() {
  currentPage.value = 1;
}

function handleFilter() {
  currentPage.value = 1;
}

function handlePageChange(page) {
  currentPage.value = page;
}

function handlePageSizeChange(size) {
  pageSize.value = size;
  currentPage.value = 1;
}

// Жизненный цикл
onMounted(loadQuests);

watch(() => props.currentUser, loadQuests);
</script>

<style scoped>
/* Анимации для карточек */
.transition-all {
  transition: all 0.3s ease;
}

.hover\:scale-\[1\.02\]:hover {
  transform: scale(1.02);
}

/* Стили для статистики */
.n-card {
  transition: all 0.3s ease;
}

.n-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}
</style>
