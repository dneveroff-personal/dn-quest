<template>
  <div class="flex flex-col w-full p-6 bg-[var(--color-bg-card)] shadow-md">
    <!-- Верхняя строка -->
    <div class="flex items-center justify-between w-full">
      <div class="flex items-center gap-4">
        <router-link
          to="/"
          class="dn-quest-link text-2xl font-bold transition-all duration-300 hover:scale-105 flex items-center gap-2"
        >
          <span class="text-3xl">🎯</span>
          DN Quest
        </router-link>

        <!-- Имя пользователя -->
        <div v-if="currentUser" class="flex items-center gap-2 text-[var(--color-text)] text-lg font-medium">
          <n-avatar
            :size="32"
            round
            class="bg-[var(--color-primary)] text-white flex items-center justify-center"
          >
            {{ getUserEmoji(currentUser.role) }}
          </n-avatar>
          <span class="font-semibold">{{ currentUser.publicName }}</span>
          <n-tag
            v-if="currentUser.captain"
            type="warning"
            size="small"
            class="ml-1"
          >
            🎖️ Капитан
          </n-tag>
        </div>

        <!-- Ссылка на команду -->
        <n-button
            v-if="currentUser?.team"
            :to="`/teams/${currentUser.team.id}`"
            text
            tag="router-link"
            type="primary"
            class="ml-2"
        >
          <template #icon>
            <span>👥</span>
          </template>
          {{ currentUser.team.name }}
        </n-button>

        <!-- Приглашения -->
        <n-badge :value="invitationCount" :max="99" :show="invitationCount > 0">
          <n-button
              v-if="currentUser && !currentUser.team"
              to="/invitations"
              text
              tag="router-link"
              type="primary"
              class="ml-2"
          >
            <template #icon>
              <span>📨</span>
            </template>
            Приглашения
          </n-button>
        </n-badge>
      </div>

      <!-- Пользовательское меню -->
      <div v-if="currentUser" class="flex items-center gap-3">
        <n-dropdown
            trigger="click"
            :options="userMenuOptions"
            @select="handleUserMenuSelect"
        >
          <n-button circle quaternary>
            <template #icon>
              <span class="text-xl">⚙️</span>
            </template>
          </n-button>
        </n-dropdown>
        
        <n-button
            @click="logout"
            type="error"
            quaternary
            circle
            title="Выйти"
        >
          <template #icon>
            <span>🚪</span>
          </template>
        </n-button>
      </div>
    </div>

    <!-- Навигационные кнопки -->
    <div class="flex flex-wrap gap-3 mt-4 justify-center">
      <n-button
          v-if="!currentUser"
          to="/login"
          tag="router-link"
          type="primary"
          size="large"
          class="px-6"
      >
        <template #icon>
          <span>🔐</span>
        </template>
        Войти
      </n-button>
      
      <n-button
          v-if="!currentUser"
          to="/register"
          tag="router-link"
          type="info"
          size="large"
          class="px-6"
      >
        <template #icon>
          <span>📝</span>
        </template>
        Регистрация
      </n-button>
      
      <n-button
          v-if="currentUser?.role === 'ADMIN'"
          to="/admin/users/manage"
          tag="router-link"
          type="warning"
          size="large"
          class="px-6"
      >
        <template #icon>
          <span>👑</span>
        </template>
        Управление
      </n-button>
      
      <n-button
          v-if="currentUser?.role === 'PLAYER'"
          to="/teams/create"
          tag="router-link"
          type="success"
          size="large"
          class="px-6"
      >
        <template #icon>
          <span>🏗️</span>
        </template>
        Создать команду
      </n-button>
      
      <n-button
          v-if="currentUser?.role === 'AUTHOR'"
          to="/quests/create"
          tag="router-link"
          type="primary"
          size="large"
          class="px-6"
      >
        <template #icon>
          <span>✍️</span>
        </template>
        Создать квест
      </n-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from "vue";
import { useRouter } from "vue-router";
import {
  NButton,
  NAvatar,
  NTag,
  NBadge,
  NDropdown,
  useMessage
} from "naive-ui";
import { logout as authLogout } from "@/services/auth";
import { teamService } from "@/services/api";
import { handleError } from "@/services/errorHandler";
import { useTeamEvents } from "@/services/websocket";

const props = defineProps({
  currentUser: { type: Object, default: null }
});

const router = useRouter();
const message = useMessage();
const invitationCount = ref(0);

const roleEmojis = {
  ADMIN: "👑",
  AUTHOR: "✍️",
  PLAYER: "🎮"
};

// Опции меню пользователя
const userMenuOptions = computed(() => [
  {
    label: "Профиль",
    key: "profile",
    icon: () => "👤"
  },
  {
    label: "Мои команды",
    key: "teams",
    icon: () => "👥",
    disabled: !props.currentUser?.team
  },
  {
    label: "Мои квесты",
    key: "quests",
    icon: () => "📜",
    disabled: props.currentUser?.role !== 'AUTHOR'
  },
  {
    type: "divider"
  },
  {
    label: "Настройки",
    key: "settings",
    icon: () => "⚙️"
  }
]);

// WebSocket события для реального обновления приглашений
const { unsubscribeAll } = useTeamEvents({
  onInvitationReceived: () => {
    loadInvitationCount();
  },
  onInvitationAccepted: () => {
    loadInvitationCount();
  },
  onInvitationRejected: () => {
    loadInvitationCount();
  },
});

// Загрузка количества приглашений
async function loadInvitationCount() {
  if (!props.currentUser || props.currentUser.team) return;
  
  try {
    const response = await teamService.getInvitationCount();
    invitationCount.value = response.data || 0;
  } catch (error) {
    handleError(error, {
      context: 'Loading invitation count',
      showMessage: false,
    });
  }
}

function logout() {
  authLogout();
  window.dispatchEvent(new Event("user-changed"));
  router.push("/login");
  message.success("Вы успешно вышли из системы");
}

function handleUserMenuSelect(key) {
  switch (key) {
    case 'profile':
      router.push(`/profile/${props.currentUser.id}`);
      break;
    case 'teams':
      if (props.currentUser?.team) {
        router.push(`/teams/${props.currentUser.team.id}`);
      }
      break;
    case 'quests':
      router.push('/quests/my');
      break;
    case 'settings':
      router.push('/settings');
      break;
  }
}

function getUserEmoji(role) {
  return roleEmojis[role] || "👤";
}

// Загружаем приглашения при монтировании и при изменении пользователя
onMounted(() => {
  if (props.currentUser) {
    loadInvitationCount();
  }
});

watch(() => props.currentUser, (newUser) => {
  if (newUser) {
    loadInvitationCount();
  } else {
    invitationCount.value = 0;
  }
});

// Очищаем подписки при уничтожении компонента
onUnmounted(() => {
  unsubscribeAll();
});
</script>

<style scoped>
.dn-quest-link {
  text-decoration: none;
  color: var(--color-text-strong);
}

.dn-quest-link:hover {
  color: var(--color-primary);
  transform: translateY(-1px);
}

/* Анимации для кнопок */
.n-button {
  transition: all 0.3s ease;
}

.n-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}
</style>
