<template>
  <div class="flex flex-col gap-4 mt-6">
    <div class="flex justify-between items-center">
      <h4 class="text-lg font-semibold">Подсказки</h4>
      <n-button size="small" type="primary" @click="openCreate">+ Добавить подсказку</n-button>
    </div>

    <draggable
        v-model="hints"
        item-key="id"
        tag="div"
        class="flex flex-col gap-2"
        @change="updateIndexes"
    >
      <template #item="{ element: hint }">
        <n-card>
          <div class="flex justify-between items-center">
            <div>
              <strong>#{{ hint.orderIndex }}</strong>
              <span class="ml-2 text-gray-400">+{{ hint.offsetSec }} сек</span>
              <div class="mt-1">{{ hint.text }}</div>
            </div>
            <div class="flex gap-2">
              <n-button size="tiny" @click="openEdit(hint)">Редактировать</n-button>
              <n-button size="tiny" type="error" @click="removeHint(hint.id)">Удалить</n-button>
            </div>
          </div>
        </n-card>
      </template>
    </draggable>

    <n-modal v-model:show="modalVisible">
      <n-card style="width: 500px;" :title="editingHintId ? 'Редактировать подсказку' : 'Новая подсказка'">
        <n-form label-placement="top">
          <n-form-item label="Текст подсказки">
            <n-input type="textarea" v-model:value="form.text" />
          </n-form-item>
          <n-form-item label="Задержка (сек)">
            <n-input-number v-model:value="form.offsetSec" min="0" />
          </n-form-item>
        </n-form>
        <template #footer>
          <div class="flex justify-end gap-3">
            <n-button @click="modalVisible = false">Отмена</n-button>
            <n-button type="primary" @click="saveHint">Сохранить</n-button>
          </div>
        </template>
      </n-card>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import api from "@/services/api";
import { NButton, NInput, NInputNumber, NCard, NModal, NForm, NFormItem } from "naive-ui";
import draggable from "vuedraggable";

const props = defineProps({
  levelId: { type: Number, required: true }
});

const hints = ref([]);
const modalVisible = ref(false);
const editingHintId = ref(null);
const form = ref({ text: "", offsetSec: 0 });

async function loadHints() {
  const { data } = await api.get(`/levels/${props.levelId}/hints`);
  hints.value = data.sort((a, b) => a.orderIndex - b.orderIndex);
}

function openCreate() {
  editingHintId.value = null;
  form.value = { text: "", offsetSec: 0 };
  modalVisible.value = true;
}

function openEdit(hint) {
  editingHintId.value = hint.id;
  form.value = { ...hint };
  modalVisible.value = true;
}

async function saveHint() {
  const payload = {
    text: form.value.text,
    offsetSec: form.value.offsetSec
    // НЕ передаем orderIndex!
  };

  if (editingHintId.value) {
    await api.put(`/levels/${props.levelId}/hints/${editingHintId.value}`, payload);
  } else {
    await api.post(`/levels/${props.levelId}/hints`, payload);
  }

  modalVisible.value = false;
  await loadHints();
}

async function removeHint(id) {
  if (!confirm("Удалить подсказку?")) return;
  await api.delete(`/levels/${props.levelId}/hints/${id}`);
  await loadHints();
}

function updateIndexes() {
  hints.value.forEach((h, i) => (h.orderIndex = i + 1));
  saveOrder();
}

async function saveOrder() {
  const orderedIds = hints.value.map((h) => h.id);
  await api.put(`/levels/${props.levelId}/hints/reorder`, orderedIds);
}

onMounted(loadHints);
</script>
