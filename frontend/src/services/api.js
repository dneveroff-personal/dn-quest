// src/services/api.js
import axios from "axios";
import { getToken, getRefreshToken, setToken, setRefreshToken, logout } from "./auth";

// Конфигурация API
const API_CONFIG = {
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  timeout: parseInt(import.meta.env.VITE_API_TIMEOUT) || 10000,
  maxRetries: parseInt(import.meta.env.VITE_MAX_RETRIES) || 3,
  retryDelay: parseInt(import.meta.env.VITE_RETRY_DELAY) || 1000,
  cacheTTL: parseInt(import.meta.env.VITE_CACHE_TTL) || 300000,
};

// Создаем основной экземпляр axios
const api = axios.create({
  baseURL: API_CONFIG.baseURL,
  timeout: API_CONFIG.timeout,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Простой кэш для ответов
const cache = new Map();

// Функция для генерации ключа кэша
function getCacheKey(config) {
  return `${config.method}-${config.url}-${JSON.stringify(config.params)}-${JSON.stringify(config.data)}`;
}

// Функция для проверки валидности кэша
function isCacheValid(timestamp) {
  return Date.now() - timestamp < API_CONFIG.cacheTTL;
}

// Функция для retry логики
async function retryRequest(config, retryCount = 0) {
  try {
    return await api(config);
  } catch (error) {
    const shouldRetry = 
      retryCount < API_CONFIG.maxRetries &&
      (error.code === 'ECONNABORTED' || 
       error.code === 'NETWORK_ERROR' ||
       (error.response && error.response.status >= 500));

    if (shouldRetry) {
      await new Promise(resolve => setTimeout(resolve, API_CONFIG.retryDelay * Math.pow(2, retryCount)));
      return retryRequest(config, retryCount + 1);
    }
    throw error;
  }
}

// Request interceptor: добавляем токен и обрабатываем кэш
api.interceptors.request.use(
  (config) => {
    // Добавляем токен в заголовок
    const token = getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    // Добавляем ID запроса для отладки
    config.metadata = { startTime: Date.now() };
    config.headers['X-Request-ID'] = `req-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;

    // Для GET запросов проверяем кэш
    if (config.method === 'get' && config.cache !== false) {
      const cacheKey = getCacheKey(config);
      const cached = cache.get(cacheKey);
      if (cached && isCacheValid(cached.timestamp)) {
        config.adapter = () => Promise.resolve({
          data: cached.data,
          status: 200,
          statusText: 'OK',
          headers: {},
          config,
          fromCache: true,
        });
      }
    }

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor: обрабатываем ошибки, обновляем токены, кэшируем ответы
api.interceptors.response.use(
  (response) => {
    // Кэшируем успешные GET ответы
    if (response.config.method === 'get' && response.config.cache !== false) {
      const cacheKey = getCacheKey(response.config);
      cache.set(cacheKey, {
        data: response.data,
        timestamp: Date.now(),
      });
    }

    // Логируем время выполнения запроса
    if (import.meta.env.VITE_DEBUG === 'true') {
      const duration = Date.now() - response.config.metadata.startTime;
      console.log(`API Request ${response.config.headers['X-Request-ID']} completed in ${duration}ms`);
    }

    return response;
  },
  async (error) => {
    const originalRequest = error.config;

    // Если ошибка 401 и запрос не является повторным запросом на обновление токена
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const refreshToken = getRefreshToken();
        if (refreshToken) {
          // Пытаемся обновить токен
          const response = await axios.post(`${API_CONFIG.baseURL}/auth/refresh`, {
            refreshToken,
          });

          const { accessToken, refreshToken: newRefreshToken } = response.data;
          setToken(accessToken);
          if (newRefreshToken) {
            setRefreshToken(newRefreshToken);
          }

          // Повторяем оригинальный запрос с новым токеном
          originalRequest.headers.Authorization = `Bearer ${accessToken}`;
          return api(originalRequest);
        }
      } catch (refreshError) {
        // Если не удалось обновить токен, разлогиниваем пользователя
        logout();
        window.dispatchEvent(new Event("user-changed"));
        return Promise.reject(refreshError);
      }
    }

    // Обработка circuit breaker ошибок от Gateway
    if (error.response?.status === 503 && error.response?.data?.code === 'CIRCUIT_BREAKER_OPEN') {
      error.message = 'Сервис временно недоступен. Попробуйте позже.';
    }

    // Обработка rate limiting ошибок
    if (error.response?.status === 429) {
      const retryAfter = error.response.headers['retry-after'];
      error.message = retryAfter 
        ? `Слишком много запросов. Попробуйте через ${retryAfter} секунд.`
        : 'Слишком много запросов. Попробуйте позже.';
    }

    return Promise.reject(error);
  }
);

// Вспомогательные функции для работы с API
export const apiUtils = {
  // Очистка кэша
  clearCache: (pattern) => {
    if (pattern) {
      for (const key of cache.keys()) {
        if (key.includes(pattern)) {
          cache.delete(key);
        }
      }
    } else {
      cache.clear();
    }
  },

  // Получение размера кэша
  getCacheSize: () => cache.size,

  // Отмена запроса
  createCancelToken: () => axios.CancelToken.source(),

  // Проверка является ли ошибка отменой запроса
  isCancel: axios.isCancel,

  // Retry функция для ручного использования
  retry: retryRequest,
};

// Специализированные методы для разных сервисов
export const authService = {
  login: (credentials) => api.post('/auth/login', credentials),
  register: (userData) => api.post('/auth/register', userData),
  refresh: (refreshToken) => api.post('/auth/refresh', { refreshToken }),
  logout: () => api.post('/auth/logout'),
  forgotPassword: (email) => api.post('/auth/forgot-password', { email }),
  resetPassword: (token, password) => api.post('/auth/reset-password', { token, password }),
  changePassword: (oldPassword, newPassword) => api.post('/auth/change-password', { oldPassword, newPassword }),
  updateProfile: (profileData) => api.put('/auth/profile', profileData),
  getCurrentUser: () => api.get('/auth/me'),
};

export const userService = {
  getCurrentUser: () => api.get('/users/me'),
  updateProfile: (userData) => api.put('/users/profile', userData),
  getUsers: (params) => api.get('/users', { params }),
  getUserById: (id) => api.get(`/users/${id}`),
  updateUser: (id, userData) => api.put(`/users/${id}`, userData),
  deleteUser: (id) => api.delete(`/users/${id}`),
  getAuthors: () => api.get('/users?role=AUTHOR'),
};

export const questService = {
  getQuests: (params) => api.get('/quests', { params }),
  getQuestById: (id) => api.get(`/quests/${id}`),
  createQuest: (questData) => api.post('/quests', questData),
  updateQuest: (id, questData) => api.put(`/quests/${id}`, questData),
  deleteQuest: (id) => api.delete(`/quests/${id}`),
  getPublishedQuests: (params) => api.get('/quests/published', { params }),
  getMyQuests: (params) => api.get('/quests/my', { params }),
  participateInQuest: (questId, teamId) => api.post(`/quests/${questId}/participate`, { teamId }),
  getParticipationRequests: (questId) => api.get(`/participation/by-quest/${questId}`),
  updateParticipationStatus: (requestId, status) => api.post(`/participation/${requestId}/status`, null, { params: { status } }),
};

export const gameService = {
  getCurrentLevel: (sessionId) => api.get(`/game/sessions/${sessionId}/current`),
  submitCode: (sessionId, codeData) => api.post(`/game/sessions/${sessionId}/code`, codeData),
  autoPass: (sessionId) => api.post(`/game/sessions/${sessionId}/auto-pass`),
  getSessionAttempts: (sessionId, params) => api.get(`/game/sessions/${sessionId}/last-attempts`, { params }),
  startSession: (questId, teamId) => api.post('/game/sessions/start', { questId, teamId }),
  finishSession: (sessionId) => api.post(`/game/sessions/${sessionId}/finish`),
  getSessionStats: (sessionId) => api.get(`/game/sessions/${sessionId}/stats`),
};

export const teamService = {
  getTeams: (params) => api.get('/teams', { params }),
  getTeamById: (id) => api.get(`/teams/${id}`),
  createTeam: (teamData) => api.post('/teams', teamData),
  updateTeam: (id, teamData) => api.put(`/teams/${id}`, teamData),
  deleteTeam: (id) => api.delete(`/teams/${id}`),
  joinTeam: (teamId, inviteCode) => api.post(`/teams/${teamId}/join`, { inviteCode }),
  leaveTeam: (teamId) => api.post(`/teams/${teamId}/leave`),
  inviteToTeam: (teamId, userId) => api.post(`/teams/${teamId}/invite`, { userId }),
  getInvitations: () => api.get('/teams/invitations'),
  respondToInvitation: (invitationId, response) => api.post(`/teams/invitations/${invitationId}/respond`, { response }),
  getInvitationCount: () => api.get('/teams/invitations/count'),
};

export const fileService = {
  uploadFile: (file, onUploadProgress) => {
    const formData = new FormData();
    formData.append('file', file);
    
    return api.post('/files/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      onUploadProgress,
    });
  },
  uploadMultipleFiles: (files, onUploadProgress) => {
    const formData = new FormData();
    files.forEach(file => formData.append('files', file));
    
    return api.post('/files/batch-upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      onUploadProgress,
    });
  },
  getFileUrl: (fileId) => `${API_CONFIG.baseURL}/files/${fileId}`,
  deleteFile: (fileId) => api.delete(`/files/${fileId}`),
  getFileInfo: (fileId) => api.get(`/files/${fileId}/info`),
  searchFiles: (searchParams) => api.get('/files/search', { params: searchParams }),
  getStorageStats: () => api.get('/files/stats'),
};

export const notificationService = {
  getNotifications: (params) => api.get('/notifications', { params }),
  markAsRead: (notificationId) => api.put(`/notifications/${notificationId}/read`),
  markAllAsRead: () => api.put('/notifications/read-all'),
  deleteNotification: (notificationId) => api.delete(`/notifications/${notificationId}`),
  getUnreadCount: () => api.get('/notifications/unread-count'),
};

export const statisticsService = {
  getQuestStats: (questId) => api.get(`/statistics/quests/${questId}`),
  getTeamStats: (teamId) => api.get(`/statistics/teams/${teamId}`),
  getUserStats: (userId) => api.get(`/statistics/users/${userId}`),
  getLeaderboard: (params) => api.get('/statistics/leaderboard', { params }),
  getGameStats: (sessionId) => api.get(`/statistics/games/${sessionId}`),
};

// Экспортируем основной экземпляр API для обратной совместимости
export default api;
