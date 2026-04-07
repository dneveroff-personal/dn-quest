
/**
 * Утилитарные функции для форматирования данных на фронтенде
 */

/**
 * Форматирует дату в строку формата dd.MM.yyyy HH:mm
 */
export function formatDateTime(date) {
  if (!date) return "";
  const d = new Date(date);
  return d.toLocaleString('ru-RU', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
}

/**
 * Форматирует дату в строку формата dd.MM.yyyy
 */
export function formatDate(date) {
  if (!date) return "";
  const d = new Date(date);
  return d.toLocaleDateString('ru-RU');
}

/**
 * Форматирует время в строку формата HH:mm
 */
export function formatTime(date) {
  if (!date) return "";
  const d = new Date(date);
  return d.toLocaleTimeString('ru-RU', {
    hour: '2-digit',
    minute: '2-digit'
  });
}

/**
 * Вычисляет время до указанной даты
 */
export function getTimeUntil(targetDate) {
  if (!targetDate) return "";
  
  const now = new Date();
  const target = new Date(targetDate);
