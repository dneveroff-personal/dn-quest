<template>
  <div class="mt-6">
    <h4 class="text-lg font-medium mb-2">Коды уровня</h4>
    <n-data-table :columns="codeColumns" :data="codes" :pagination="false" />
    <n-button size="small" type="primary" class="mt-2" @click="openAddCode">+ Добавить код</n-button>

    <n-modal v-model:show="codeModalVisible">
      <n-card style="width: 500px;" title="Код уровня" :bordered="false">
        <n-form label-placement="top">
          <n-form-item label="Значение">
            <n-input v-model:value="codeForm.value" />
          </n-form-item>
          <n-form-item label="Тип">
            <n-select :options="codeTypeOptions" v-model:value="codeForm.type" />
          </n-form-item>
          <n-form-item v-if="codeForm.type === 'NORMAL'" label="Сектор">
            <n-input-number v-model:value="codeForm.sectorNo" min="1" />
          </n-form-item>
          <n-form-item v-if="codeForm.type !== 'NORMAL'" label="Сдвиг времени (сек)">
            <n-input-number v-model:value="codeForm.shiftSeconds" />
          </n-form-item>
        </n-form>
        <template #footer>
          <div class="flex justify-end gap-3">
            <n-button @click="codeModalVisible=false">Отмена</n-button>
            <n-button type="primary" @click="saveCode">Сохранить</n-button>
          </div>
        </template>
      </n-card>
    </n-modal>
  </div>
</template>

<script setup>
  import { ref, onMounted, h } from "vue";
  import api from "@/services/api";
  import { NButton, NDataTable } from "naive-ui";

  const props = defineProps({
    levelId: { type: Number, required: true }
  });

  const codes = ref([]);
  const codeModalVisible = ref(false);
  const codeForm = ref({});
  const editingCodeId = ref(null);

  const codeTypeOptions = [
    { label: "Обычный", value: "NORMAL" },
    { label: "Бонусный", value: "BONUS" },
    { label: "Штрафной", value: "PENALTY" }
  ];

  const codeColumns = [
    { title: "Код", key: "value" },
    { title: "Тип", key: "type" },
    { title: "Сектор", key: "sectorNo" },
    { title: "Сдвиг", key: "shiftSeconds" },
    {
      title: "Действия",
      key: "actions",
      render(row) {
        return [
          // если добавим редактирование — можно раскомментить
          // h(NButton, { size: "small", onClick: () => openEditCode(row) }, { default: () => "Редактировать" }),
          h(NButton, { size: "small", type: "error", onClick: () => removeCode(row.id) }, { default: () => "Удалить" })
        ];
      }
    }
  ];

  async function loadCodes() {
    const { data } = await api.get(`/level-codes/by-level/${props.levelId}`);
    codes.value = data;
  }

  function openAddCode() {
    editingCodeId.value = null;
    codeForm.value = { value: "", type: "NORMAL", sectorNo: null, shiftSeconds: 0 };
    codeModalVisible.value = true;
  }

  async function saveCode() {
    const payload = { ...codeForm.value, levelId: props.levelId };
    await api.post("/level-codes", payload);
    codeModalVisible.value = false;
    await loadCodes();
  }

  async function removeCode(id) {
    if (!confirm("Удалить код?")) return;
    await api.delete(`/level-codes/${id}`);
    await loadCodes();
  }

  onMounted(loadCodes);
</script>
