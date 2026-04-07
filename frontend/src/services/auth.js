// src/services/auth.js
import { authService } from './api';

// Константы для хранения токенов
const TOKEN_KEY = 'dn_quest_access_token';
const REFRESH_TOKEN_KEY = 'dn_quest_refresh_token';
const USER_KEY = 'dn_quest_current_user';

// Функции для работы с токенами
export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function getRefreshToken() {
  return localStorage.getItem(REFRESH_TOKEN_KEY);
}

export function setToken(token) {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token);
  } else {
    localStorage.removeItem(TOKEN_KEY);
  }
}

export function setRefreshToken(refreshToken) {
  if (refreshToken) {
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
  } else {
    localStorage.removeItem(REFRESH_TOKEN_KEY);
  }
}

export function clearTokens() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

// Функции для работы с пользователем
export function getCurrentUser() {
  try {
    const userStr = localStorage.getItem(USER_KEY);
    return userStr ? JSON.parse(userStr) : null;
  } catch (error) {
    console.error('Error parsing user from localStorage:', error);
    return null;
  }
}

export function setCurrentUser(user) {
  if (user) {
    localStorage.setItem(USER_KEY, JSON.stringify(user));
  } else {
    localStorage.removeItem(USER_KEY);
  }
}

// Проверка валидности токена
export function isTokenValid(token) {
  if (!token) return false;
  
  try {
    // Декодируем JWT токен (без верификации подписи)
    const payload = JSON.parse(atob(token.split('.')[1]));
    const currentTime = Date.now() / 1000;
    
    // Проверяем не истек ли токен (с запасом 30 секунд)
    return payload.exp > currentTime + 30;
  } catch (error) {
    console.error('Error validating token:', error);
    return false;
  }
}

// Проверка нужно ли обновлять токен
export function shouldRefreshToken(token) {
  if (!token) return false;
  
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    const currentTime = Date.now() / 1000;
    
    // Обновляем токен если он истечет в течение 5 минут
    return payload.exp < currentTime + 300;
  } catch (error) {
    console.error('Error checking token refresh:', error);
    return false;
  }
}

// Основные функции аутентификации
export async function login(credentials) {
  try {
    const response = await authService.login(credentials);
    const { accessToken, refreshToken, user } = response.data;
    
    setToken(accessToken);
    setRefreshToken(refreshToken);
    setCurrentUser(user);
    
    // Уведомляем об изменении пользователя
    window.dispatchEvent(new Event("user-changed"));
    
    return { user, accessToken, refreshToken };
  } catch (error) {
    console.error('Login error:', error);
    throw error;
  }
}

export async function register(userData) {
  try {
    const response = await authService.register(userData);
    return response.data;
  } catch (error) {
    console.error('Registration error:', error);
    throw error;
  }
}

export async function logout() {
  try {
    // Вызываем API для логаута (если доступно)
    await authService.logout().catch(() => {
      // Игнорируем ошибки при логауте на сервере
    });
  } catch (error) {
    console.error('Logout API error:', error);
  } finally {
    // Всегда очищаем локальные данные
    clearTokens();
    window.dispatchEvent(new Event("user-changed"));
  }
}

export async function refreshAccessToken() {
  const refreshToken = getRefreshToken();
  if (!refreshToken) {
    throw new Error('No refresh token available');
  }

  try {
    const response = await authService.refresh(refreshToken);
    const { accessToken, refreshToken: newRefreshToken } = response.data;
    
    setToken(accessToken);
    if (newRefreshToken) {
      setRefreshToken(newRefreshToken);
    }
    
    return accessToken;
  } catch (error) {
    console.error('Token refresh error:', error);
    // Если не удалось обновить токен, очищаем все данные
    clearTokens();
    window.dispatchEvent(new Event("user-changed"));
    throw error;
  }
}

export async function fetchCurrentUser() {
  const token = getToken();
  if (!token) {
    return null;
  }

  // Проверяем валидность токена
  if (!isTokenValid(token)) {
    // Пробуем обновить токен
    try {
      await refreshAccessToken();
    } catch (error) {
      console.warn('Failed to refresh token:', error);
      return null;
    }
  }

  try {
    const response = await authService.getCurrentUser();
    const user = response.data;
    setCurrentUser(user);
    return user;
  } catch (error) {
    console.error('fetchCurrentUser failed:', error);
    
    // Если ошибка 401, очищаем токены
    if (error.response?.status === 401) {
      clearTokens();
      window.dispatchEvent(new Event("user-changed"));
    }
    
    return null;
  }
}

// Функции для восстановления сессии
export async function restoreSession() {
  const token = getToken();
  const refreshToken = getRefreshToken();
  
  if (!token && !refreshToken) {
    return null;
  }

  try {
    // Если есть токен, проверяем его валидность
    if (token && isTokenValid(token)) {
      const user = await fetchCurrentUser();
      return user;
    }
    
    // Если токен невалиден но есть refresh токен, пробуем обновить
    if (refreshToken) {
      await refreshAccessToken();
      const user = await fetchCurrentUser();
      return user;
    }
    
    // Если ничего не помогло, очищаем данные
    clearTokens();
    return null;
  } catch (error) {
    console.error('Session restoration failed:', error);
    clearTokens();
    return null;
  }
}

// Функции для работы с паролем
export async function forgotPassword(email) {
  try {
    const response = await authService.forgotPassword(email);
    return response.data;
  } catch (error) {
    console.error('Forgot password error:', error);
    throw error;
  }
}

export async function resetPassword(token, password) {
  try {
    const response = await authService.resetPassword(token, password);
    return response.data;
  } catch (error) {
    console.error('Reset password error:', error);
    throw error;
  }
}

export async function changePassword(oldPassword, newPassword) {
  try {
    const response = await authService.changePassword(oldPassword, newPassword);
    return response.data;
  } catch (error) {
    console.error('Change password error:', error);
    throw error;
  }
}

// Функции для работы с профилем
export async function updateProfile(profileData) {
  try {
    const response = await authService.updateProfile(profileData);
    const updatedUser = response.data;
    setCurrentUser(updatedUser);
    window.dispatchEvent(new Event("user-changed"));
    return updatedUser;
  } catch (error) {
    console.error('Update profile error:', error);
    throw error;
  }
}

// Утилиты для проверки прав доступа
export function hasRole(user, role) {
  return user?.role === role;
}

export function hasAnyRole(user, roles) {
  return roles.some(role => hasRole(user, role));
}

export function isAdmin(user) {
  return hasRole(user, 'ADMIN');
}

export function isAuthor(user) {
  return hasRole(user, 'AUTHOR');
}

export function isPlayer(user) {
  return hasRole(user, 'PLAYER');
}

// Автоматическое обновление токена
let refreshTimer = null;

export function startTokenRefreshTimer() {
  // Останавливаем предыдущий таймер
  if (refreshTimer) {
    clearInterval(refreshTimer);
  }

  // Проверяем токен каждую минуту
  refreshTimer = setInterval(async () => {
    const token = getToken();
    if (token && shouldRefreshToken(token)) {
      try {
        await refreshAccessToken();
      } catch (error) {
        console.warn('Auto token refresh failed:', error);
      }
    }
  }, 60000); // 1 минута
}

export function stopTokenRefreshTimer() {
  if (refreshTimer) {
    clearInterval(refreshTimer);
    refreshTimer = null;
  }
}

// Инициализация при загрузке модуля
if (typeof window !== 'undefined') {
  // Запускаем таймер обновления токена
  startTokenRefreshTimer();
  
  // Останавливаем таймер при выгрузке страницы
  window.addEventListener('beforeunload', stopTokenRefreshTimer);
}

// Экспортируем утилиты для очистки токенов (для обратной совместимости)
export function clearToken() {
  clearTokens();
}
