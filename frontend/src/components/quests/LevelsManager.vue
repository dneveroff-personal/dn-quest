<template>
  <div class="flex flex-col gap-4">
    <div class="flex justify-between items-center mb-4">
      <h3 class="text-xl font-semibold">Уровни квеста</h3>
      <div class="flex gap-3">
        <n-button type="primary" @click="openCreateLevel">+ Добавить уровень</n-button>
        <n-button @click="saveOrder" :disabled="savingOrder">Сохранить порядок</n-button>
      </div>
    </div>

    <draggable
        v-model="levels"
        item-key="id"
        tag="div"
        class="flex flex-col gap-2"
        @change="updateIndexes"
    >
      <template #item="{ element: level }">
        <n-card style="margin-bottom: 4px;">
          <div class="flex justify-between items-center">
            <div>
              <strong>{{ level.orderIndex }}.</strong> {{ level.title }}
            </div>
            <div class="flex gap-2">
              <n-button size="small" @click="openEditLevel(level)">Редактировать</n-button>
              <n-button size="small" type="error" @click="removeLevel(level.id)">Удалить</n-button>
            </div>
          </div>
        </n-card>
      </template>
    </draggable>

    <n-modal v-model:show="levelModalVisible">
      <n-card style="width: 600px;" title="Редактировать уровень" :bordered="false">
        <n-form label-placement="top">
          <n-form-item label="Название">
            <n-input v-model:value="levelForm.title" placeholder="Введите название" />
          </n-form-item>
          <n-form-item label="Описание (HTML)">
            <n-input type="textarea" v-model:value="levelForm.descriptionHtml" />
          </n-form-item>
          <div class="flex gap-6">
            <n-form-item label="Порядковый номер">
              <n-input-number v-model:value="levelForm.orderIndex" min="1" />
            </n-form-item>
            <n-form-item label="Секторов">
              <n-input-number v-model:value="levelForm.requiredSectors" min="0" />
            </n-form-item>
          </div>
          <n-form-item label="Авто-проход (сек)">
            <n-input-number v-model:value="levelForm.apTime" min="0" />
          </n-form-item>

          <LevelCodes v-if="editingLevelId" :level-id="editingLevelId" />
        </n-form>

        <template #footer>
          <div class="flex justify-end gap-3">
            <n-button @click="levelModalVisible = false">Отмена</n-button>
            <n-button type="primary" @click="saveLevel">Сохранить</n-button>
          </div>
        </template>
      </n-card>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, defineExpose } from "vue";
import api from "@/services/api";
import { NButton, NInput, NInputNumber, NCard, NModal, NForm, NFormItem } from "naive-ui";
import draggable from "vuedraggable";
import LevelCodes from "./LevelCodes.vue";

const props = defineProps({
  questId: { type: String, required: true }
});

const levels = ref([]);
const levelModalVisible = ref(false);
const levelForm = ref({});
const editingLevelId = ref(null);
const savingOrder = ref(false);

async function loadLevels() {
  const { data } = await api.get(`/levels/by-quest/${props.questId}`);
  levels.value = data.sort((a, b) => a.orderIndex - b.orderIndex);
}

function openCreateLevel() {
  editingLevelId.value = null;
  levelForm.value = { title: "", descriptionHtml: "", orderIndex: levels.value.length + 1, requiredSectors: 0, apTime: null };
  levelModalVisible.value = true;
}

function openEditLevel(level) {
  editingLevelId.value = level.id;
  levelForm.value = { ...level };
  levelModalVisible.value = true;
}

async function saveLevel() {
  const payload = { ...levelForm.value, questId: props.questId };
  if (editingLevelId.value) {
    await api.put(`/levels/${editingLevelId.value}`, payload);
  } else {
    await api.post("/levels", payload);
  }
  levelModalVisible.value = false;
  await loadLevels();
}

async function removeLevel(id) {
  if (!confirm("Удалить уровень?")) return;
  await api.delete(`/levels/${id}`);
  await loadLevels();
}

// 🔹 Обновляем orderIndex после drag & drop
function updateIndexes() {
  levels.value.forEach((l, i) => (l.orderIndex = i + 1));
}

// 🔹 Сохраняем порядок уровней на сервер
async function saveOrder() {
  savingOrder.value = true;
  try {
    const orderedIds = levels.value.map(l => l.id);
    await api.put("/levels/reorder", orderedIds);
    await loadLevels();
  } finally {
    savingOrder.value = false;
  }
}

// 🔹 Экспорт метода для родителя (QuestForm)
function getOrderedIds() {
  return levels.value.map(l => l.id);
}

// 🔹 expose для script setup
defineExpose({
  getOrderedIds,
  loadLevels,
  saveOrder
});

onMounted(loadLevels);
</script>
