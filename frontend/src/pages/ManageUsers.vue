<template>
  <div class="p-6 space-y-6 bg-[var(--color-bg)] min-h-screen">
    <!-- Профиль пользователя -->
    <n-card v-if="user" class="bg-[var(--color-bg-card)] text-[var(--color-text)]">
      <h2 class="text-lg font-bold mb-2 text-[var(--color-text-strong)]">Ваш профиль</h2>
      <p><b>Имя:</b> {{ user.publicName }}</p>
      <p><b>Username:</b> {{ user.username }}</p>
      <p><b>Email:</b> {{ user.email }}</p>
      <p><b>Роль:</b> {{ user.role }}</p>
    </n-card>

    <!-- Таблица пользователей -->
    <n-card class="bg-[var(--color-bg-card)] text-[var(--color-text)]">
      <h2 class="text-lg font-bold mb-4 text-[var(--color-text-strong)]">Пользователи</h2>
      <n-spin :show="loading">
        <n-data-table :columns="columns" :data="users" :pagination="{ pageSize: 10 }" />
      </n-spin>
    </n-card>
  </div>
</template>

<script setup>
import { ref, onMounted, h } from "vue";
import api from "../services/api";
import {
  NCard,
  NButton,
  NDataTable,
  useMessage,
  NSpin,
  NSpace,
} from "naive-ui";

const message = useMessage();

const user = ref(null);
const users = ref([]);
const loading = ref(false);

async function loadUser() {
  try {
    const { data } = await api.get("/users/me");
    user.value = data;
  } catch {
    message.error("Ошибка загрузки профиля");
  }
}

async function loadUsers() {
  loading.value = true;
  try {
    const { data } = await api.get("/users");
    users.value = data;
  } catch {
    message.error("Ошибка загрузки пользователей");
  } finally {
    loading.value = false;
  }
}

async function makeAuthor(id) {
  try {
    await api.patch(`/users/${id}/role`, null, { params: { role: "AUTHOR" } });
    message.success("Пользователь стал автором");
    loadUsers();
  } catch (err) {
    console.error("Ошибка при изменении роли", err);
    message.error("Не удалось изменить роль");
  }
}

async function deleteUser(id) {
  try {
    await api.delete(`/users/${id}`);
    message.success("Пользователь удалён");
    loadUsers();
  } catch (err) {
    console.error("Ошибка при удалении", err);
    message.error("Не удалось удалить пользователя");
  }
}

const columns = [
  { title: "ID", key: "id" },
  { title: "Username", key: "username" },
  { title: "Имя", key: "publicName", render: row => row.publicName || "-" },
  { title: "Email", key: "email", render: row => row.email || "-" },
  { title: "Роль", key: "role" },
  {
    title: "Действия",
    key: "actions",
    render(row) {
      return h(NSpace, {}, {
        default: () => [
          row.role !== "AUTHOR"
              ? h(
                  NButton,
                  {
                    size: "small",
                    type: "success",
                    onClick: () => makeAuthor(row.id),
                  },
                  { default: () => "Сделать автором" }
              )
              : h("span", { class: "text-gray-400" }, "Автор"),

          h(
              NButton,
              {
                size: "small",
                type: "error",
                onClick: () => deleteUser(row.id),
                disabled: row.role === "ADMIN", // админа удалить нельзя
              },
              { default: () => "Удалить" }
          ),
        ],
      });
    },
  },
];

onMounted(() => {
  loadUser();
  loadUsers();
});
</script>
