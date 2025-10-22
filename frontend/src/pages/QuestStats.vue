<template>
  <div class="p-4">
    <h2 class="text-xl font-semibold mb-4">Статистика квеста {{ questId }}</h2>

    <n-data-table
        :columns="columns"
        :data="rows"
        :bordered="true"
        :striped="true"
        :single-line="false"
        :pagination="false"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, h } from "vue";
import api from "@/services/api";
import { useRoute } from "vue-router";
import { NDataTable } from "naive-ui";

const route = useRoute();
const questId = route.params.id;

const leaderboard = ref([]);
const loading = ref(false);

// Загружаем данные
async function fetchLeaderboard() {
  loading.value = true;
  try {
    const res = await api.get(`/quests/${questId}/leaderboard`);
    console.log("LEADERBOARD DATA:", res.data);
    leaderboard.value = res.data;
  } finally {
    loading.value = false;
  }
}

onMounted(fetchLeaderboard);

// 🔹 Уровни
const levelTitles = computed(() => {
  const map = new Map();
  leaderboard.value.forEach(item => {
    if (!map.has(item.levelId)) map.set(item.levelId, item.levelTitle);
  });
  return Array.from(map.entries()).sort((a, b) => a[0] - b[0]);
});

// 🔹 Команды
const teamNames = computed(() => {
  const set = new Set();
  leaderboard.value.forEach(i => set.add(i.teamName));
  return Array.from(set);
});

// 🔹 Колонки
const columns = computed(() => {
  const cols = [
    { title: "#", key: "index", align: "center", width: 50 },
  ];

  levelTitles.value.forEach(([levelId, title]) => {
    cols.push({
      title,
      key: `level_${levelId}`,
      align: "center",
      render(row) {
        const entry = row.levels[levelId];
        if (!entry) return "-";
        return h("div", { class: "flex flex-col items-center text-xs space-y-1" }, [
          h("div", { class: "font-semibold text-gray-800" }, entry.teamName),
          h("div", entry.durationHHMMSS || "—"),
          h("div", new Date(entry.passTime).toLocaleString()),
          h("div", entry.passedByPublicName || "автопереход")
        ]);
      }
    });
  });

  cols.push({ title: "Итого", key: "total", align: "center" });
  cols.push({ title: "#", key: "index2", align: "center", width: 50 });
  return cols;
});

// 🔹 Строки
const rows = computed(() => {
  return teamNames.value.map((teamName, index) => {
    const teamEntries = leaderboard.value.filter(i => i.teamName === teamName);
    const levels = {};
    teamEntries.forEach(e => (levels[e.levelId] = e));

    return {
      index: index + 1,
      teamName,
      levels,
      total: "" // позже добавим вычисление очков
    };
  });
});
</script>

<style scoped>
.n-data-table {
  font-size: 14px;
}
</style>
