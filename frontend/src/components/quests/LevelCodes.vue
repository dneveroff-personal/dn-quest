<template>
  <div class="mt-4 max-h-[600px] overflow-y-auto">
    <div class="flex items-center justify-between mb-3">
      <h4 class="text-lg font-medium">Коды уровня</h4>
      <div class="flex gap-2">
        <n-button size="small" @click="addSector">+ Добавить сектор</n-button>
        <n-button size="small" @click="addSpecial('BONUS')">+ Добавить бонус</n-button>
        <n-button size="small" @click="addSpecial('PENALTY')">+ Добавить штраф</n-button>
      </div>
    </div>

    <div v-if="loading" class="text-sm opacity-70">Загрузка...</div>

    <div v-else>
      <n-collapse v-model:value="openSectors" accordion>
        <n-collapse-item
            v-for="group in groupedArray"
            :key="group.key"
            :name="group.key"
        >
          <template #header>
            <div class="flex items-center gap-2">
              <n-input
                  v-if="isNumericKey(group.key)"
                  v-model:value="sectorTitles[group.key]"
                  size="small"
                  style="width: 160px;"
              />
              <span v-else>{{ groupTitle(group.key, group.items.length) }}</span>
              <span class="text-gray-400 text-xs">({{ group.items.length }})</span>
            </div>
          </template>

          <!-- Существующие коды -->
          <n-data-table
              :columns="columns"
              :data="group.items"
              :pagination="false"
              size="small"
              striped
          />

          <!-- Inline форма добавления -->
          <div class="mt-3 flex items-center gap-2">
            <n-input
                v-model:value="drafts[group.key].value"
                placeholder="Новый код"
                size="small"
                style="width: 200px;"
            />
            <n-select
                v-model:value="drafts[group.key].type"
                :options="codeTypeOptions"
                size="small"
                style="width: 120px;"
            />
            <n-input-number
                v-if="isNumericKey(group.key)"
                v-model:value="drafts[group.key].sectorNo"
                :min="1"
                size="small"
                style="width: 80px;"
            />
            <n-input-number
                v-if="drafts[group.key].type !== 'NORMAL'"
                v-model:value="drafts[group.key].shiftSeconds"
                size="small"
                style="width: 100px;"
            />
            <n-button size="small" type="primary" @click="saveDraft(group.key)">💾</n-button>
            <n-button size="small" @click="cancelDraft(group.key)">✖</n-button>
          </div>
        </n-collapse-item>
      </n-collapse>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, h } from "vue";
import api from "@/services/api";
import {
  NButton,
  NDataTable,
  NInput,
  NInputNumber,
  NSelect,
  NCollapse,
  NCollapseItem
} from "naive-ui";

const props = defineProps({
  levelId: { type: Number, required: true }
});

const loading = ref(false);
const codes = ref([]);
const openSectors = ref([]);
const drafts = reactive({});
const sectorTitles = reactive({}); // кастомные названия секторов

const codeTypeOptions = [
  { label: "Обычный", value: "NORMAL" },
  { label: "Бонусный", value: "BONUS" },
  { label: "Штрафной", value: "PENALTY" }
];

// ================= таблица =================
const columns = [
  {
    title: "Код",
    key: "value",
    render(row) {
      return h(NInput, {
        value: row.value,
        size: "small",
        onUpdateValue: v => (row.value = v)
      });
    }
  },
  {
    title: "Тип",
    key: "type",
    render(row) {
      return h(NSelect, {
        value: row.type,
        size: "small",
        options: codeTypeOptions,
        onUpdateValue: v => (row.type = v)
      });
    }
  },
  {
    title: "Сектор",
    key: "sectorNo",
    render(row) {
      return row.type === "NORMAL"
          ? h(NInputNumber, {
            value: row.sectorNo,
            size: "small",
            min: 1,
            onUpdateValue: v => (row.sectorNo = v)
          })
          : null;
    }
  },
  {
    title: "Сдвиг (сек)",
    key: "shiftSeconds",
    render(row) {
      return row.type !== "NORMAL"
          ? h(NInputNumber, {
            value: row.shiftSeconds,
            size: "small",
            min: 0,
            onUpdateValue: v => (row.shiftSeconds = v)
          })
          : null;
    }
  },
  {
    title: "Действия",
    key: "actions",
    render(row) {
      return h("div", { class: "flex gap-2" }, [
        h(
            NButton,
            {
              size: "tiny",
              type: "primary",
              onClick: () => saveCode(row)
            },
            { default: () => "💾" }
        ),
        h(
            NButton,
            {
              size: "tiny",
              type: "error",
              onClick: () => removeCode(row.id)
            },
            { default: () => "🗑" }
        )
      ]);
    }
  }
];

// ================= grouping =================
const groupedArray = computed(() => {
  const map = {};
  codes.value.forEach(c => {
    const key = c.type === "NORMAL" ? String(c.sectorNo) : c.type;
    if (!map[key]) map[key] = [];
    map[key].push({ ...c });
    if (!sectorTitles[key]) {
      sectorTitles[key] =
          c.type === "NORMAL" ? `Сектор ${c.sectorNo}` : c.type;
    }
  });
  Object.keys(drafts).forEach(k => {
    if (!map[k]) map[k] = [];
  });
  return Object.entries(map).map(([k, items]) => ({ key: k, items }));
});

function isNumericKey(k) {
  return !isNaN(Number(k));
}
function groupTitle(key, count) {
  return `${sectorTitles[key] || key} (${count})`;
}

// ================= загрузка / API =================
async function loadCodes() {
  loading.value = true;
  try {
    const { data } = await api.get(`/codes/by-level/${props.levelId}`);
    codes.value = Array.isArray(data) ? data : [];
    buildDraftsForExistingGroups();
  } finally {
    loading.value = false;
  }
}

async function saveCode(row) {
  const payload = { ...row, levelId: props.levelId, shiftSeconds: row.shiftSeconds || 0 };
  if (row.id) {
    await api.put(`/codes/${row.id}`, payload);
  } else {
    const { data } = await api.post("/codes", payload);
    row.id = data.id;
  }
  await loadCodes();
}

async function removeCode(id) {
  if (!id) return;
  if (!confirm("Удалить код?")) return;
  await api.delete(`/codes/${id}`);
  await loadCodes();
}

// ================= drafts =================
function buildDraftsForExistingGroups() {
  const keys = new Set();
  codes.value.forEach(c =>
      keys.add(c.type === "NORMAL" ? String(c.sectorNo) : c.type)
  );
  keys.forEach(k => {
    if (!drafts[k]) {
      drafts[k] = {
        value: "",
        type: k === "BONUS" || k === "PENALTY" ? k : "NORMAL",
        sectorNo: isNumericKey(k) ? Number(k) : null,
        shiftSeconds: 0
      };
    }
  });
}

async function saveDraft(key) {
  const d = drafts[key];
  if (!d || !d.value) return;
  const payload = {
    value: d.value,
    type: d.type,
    sectorNo: d.type === "NORMAL" ? d.sectorNo : null,
    shiftSeconds: d.shiftSeconds || 0,
    levelId: props.levelId
  };
  await api.post("/codes", payload);
  await loadCodes();
  drafts[key].value = "";
}
function cancelDraft(key) {
  if (drafts[key]) drafts[key].value = "";
}

// ================= добавление групп =================
function addSector() {
  const nums = codes.value
      .filter(c => c.type === "NORMAL")
      .map(c => Number(c.sectorNo));
  const next = nums.length ? Math.max(...nums) + 1 : 1;
  const key = String(next);
  if (!drafts[key]) {
    drafts[key] = { value: "", type: "NORMAL", sectorNo: next, shiftSeconds: 0 };
    sectorTitles[key] = `Сектор ${next}`;
  }
  openSectors.value.push(key);
}
function addSpecial(kind) {
  if (!drafts[kind]) {
    drafts[kind] = { value: "", type: kind, sectorNo: null, shiftSeconds: 0 };
    sectorTitles[kind] = kind;
  }
  openSectors.value.push(kind);
}

onMounted(loadCodes);
</script>
