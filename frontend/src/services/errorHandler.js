// src/services/errorHandler.js
import { useMessage, useDialog } from 'naive-ui';

// Типы ошибок
export const ErrorTypes = {
  NETWORK_ERROR: 'NETWORK_ERROR',
  API_ERROR: 'API_ERROR',
  VALIDATION_ERROR: 'VALIDATION_ERROR',
  AUTHENTICATION_ERROR: 'AUTHENTICATION_ERROR',
  AUTHORIZATION_ERROR: 'AUTHORIZATION_ERROR',
  NOT_FOUND_ERROR: 'NOT_FOUND_ERROR',
  SERVER_ERROR: 'SERVER_ERROR',
  RATE_LIMIT_ERROR: 'RATE_LIMIT_ERROR',
  CIRCUIT_BREAKER_ERROR: 'CIRCUIT_BREAKER_ERROR',
  FILE_UPLOAD_ERROR: 'FILE_UPLOAD_ERROR',
  WEBSOCKET_ERROR: 'WEBSOCKET_ERROR',
};

// Класс для обработки ошибок
export class AppError extends Error {
  constructor(message, type = ErrorTypes.API_ERROR, statusCode = null, details = null) {
    super(message);
    this.name = 'AppError';
    this.type = type;
    this.statusCode = statusCode;
    this.details = details;
    this.timestamp = new Date().toISOString();
  }

  // Преобразование в JSON для логирования
  toJSON() {
    return {
      name: this.name,
      message: this.message,
      type: this.type,
      statusCode: this.statusCode,
      details: this.details,
      timestamp: this.timestamp,
      stack: this.stack,
    };
  }
}

// Создаем экземпляры message и dialog (будут инициализированы при первом использовании)
let messageInstance = null;
let dialogInstance = null;

function getMessageInstance() {
  if (!messageInstance) {
    messageInstance = useMessage();
  }
  return messageInstance;
}

function getDialogInstance() {
  if (!dialogInstance) {
    dialogInstance = useDialog();
  }
  return dialogInstance;
}

// Функция для определения типа ошибки
export function getErrorType(error) {
  if (!error) return ErrorTypes.API_ERROR;

  // Ошибки сети
  if (error.code === 'NETWORK_ERROR' || error.code === 'ECONNABORTED') {
    return ErrorTypes.NETWORK_ERROR;
  }

  // Ошибки WebSocket
  if (error.type === 'websocket' || error.message?.includes('WebSocket')) {
    return ErrorTypes.WEBSOCKET_ERROR;
  }

  // Проверяем статус код HTTP ответа
  if (error.response) {
    const status = error.response.status;
    
    if (status === 401) return ErrorTypes.AUTHENTICATION_ERROR;
    if (status === 403) return ErrorTypes.AUTHORIZATION_ERROR;
    if (status === 404) return ErrorTypes.NOT_FOUND_ERROR;
    if (status === 422 || status === 400) return ErrorTypes.VALIDATION_ERROR;
    if (status === 429) return ErrorTypes.RATE_LIMIT_ERROR;
    if (status >= 500) return ErrorTypes.SERVER_ERROR;
  }

  // Проверяем специальные коды ошибок от Gateway
  if (error.response?.data?.code === 'CIRCUIT_BREAKER_OPEN') {
    return ErrorTypes.CIRCUIT_BREAKER_ERROR;
  }

  // Ошибки загрузки файлов
  if (error.config?.headers?.['Content-Type']?.includes('multipart/form-data')) {
    return ErrorTypes.FILE_UPLOAD_ERROR;
  }

  return ErrorTypes.API_ERROR;
}

// Функция для получения человекочитаемого сообщения об ошибке
export function getErrorMessage(error, type = null) {
  const errorType = type || getErrorType(error);

  // Если ошибка уже содержит человекочитаемое сообщение
  if (error.message && !error.response?.data?.message) {
    return error.message;
  }

  // Сообщение от сервера
  const serverMessage = error.response?.data?.message;
  if (serverMessage) {
    return serverMessage;
  }

  // Сообщения по типам ошибок
  switch (errorType) {
    case ErrorTypes.NETWORK_ERROR:
      return 'Ошибка соединения. Проверьте подключение к интернету.';
    
    case ErrorTypes.AUTHENTICATION_ERROR:
      return 'Сессия истекла. Пожалуйста, войдите снова.';
    
    case ErrorTypes.AUTHORIZATION_ERROR:
      return 'У вас нет прав для выполнения этого действия.';
    
    case ErrorTypes.NOT_FOUND_ERROR:
      return 'Запрошенный ресурс не найден.';
    
    case ErrorTypes.VALIDATION_ERROR:
      return 'Ошибка валидации данных. Проверьте введенные значения.';
    
    case ErrorTypes.RATE_LIMIT_ERROR:
      const retryAfter = error.response?.headers?.['retry-after'];
      return retryAfter 
        ? `Слишком много запросов. Попробуйте через ${retryAfter} секунд.`
        : 'Слишком много запросов. Попробуйте позже.';
    
    case ErrorTypes.SERVER_ERROR:
      return 'Внутренняя ошибка сервера. Попробуйте позже.';
    
    case ErrorTypes.CIRCUIT_BREAKER_ERROR:
      return 'Сервис временно недоступен. Попробуйте позже.';
    
    case ErrorTypes.FILE_UPLOAD_ERROR:
      return 'Ошибка загрузки файла. Проверьте размер и тип файла.';
    
    case ErrorTypes.WEBSOCKET_ERROR:
      return 'Ошибка соединения в реальном времени. Некоторые функции могут быть недоступны.';
    
    default:
      return 'Произошла непредвиденная ошибка. Попробуйте позже.';
  }
}

// Функция для логирования ошибок
export function logError(error, context = null) {
  const errorData = {
    error: error instanceof AppError ? error.toJSON() : {
      message: error.message,
      name: error.name,
      stack: error.stack,
    },
    context,
    userAgent: navigator.userAgent,
    url: window.location.href,
    timestamp: new Date().toISOString(),
  };

  // В development режиме выводим в консоль
  if (import.meta.env.VITE_DEBUG === 'true') {
    console.error('Application Error:', errorData);
  }

  // В production можно отправлять на сервер логирования
  if (import.meta.env.VITE_NODE_ENV === 'production') {
    // TODO: Отправка ошибок на сервер логирования
    // sendErrorToServer(errorData);
  }
}

// Основная функция обработки ошибок
export function handleError(error, options = {}) {
  const {
    showMessage = true,
    showDialog = false,
    logError: shouldLog = true,
    context = null,
    customMessage = null,
    onRetry = null,
  } = options;

  const errorType = getErrorType(error);
  const message = customMessage || getErrorMessage(error, errorType);

  // Логируем ошибку
  if (shouldLog) {
    logError(error, context);
  }

  // Показываем уведомление
  if (showMessage) {
    const messageInstance = getMessageInstance();
    
    switch (errorType) {
      case ErrorTypes.VALIDATION_ERROR:
        messageInstance.warning(message);
        break;
      case ErrorTypes.AUTHENTICATION_ERROR:
        messageInstance.error(message);
        break;
      case ErrorTypes.AUTHORIZATION_ERROR:
        messageInstance.warning(message);
        break;
      case ErrorTypes.NETWORK_ERROR:
      case ErrorTypes.SERVER_ERROR:
      case ErrorTypes.CIRCUIT_BREAKER_ERROR:
        messageInstance.error(message);
        break;
      default:
        messageInstance.error(message);
    }
  }

  // Показываем диалоговое окно для критических ошибок
  if (showDialog) {
    const dialogInstance = getDialogInstance();
    
    dialogInstance.error({
      title: 'Ошибка',
      content: message,
      positiveText: onRetry ? 'Повторить' : 'ОК',
      onPositiveClick: onRetry,
    });
  }

  // Для ошибок аутентификации перенаправляем на страницу логина
  if (errorType === ErrorTypes.AUTHENTICATION_ERROR) {
    // Очищаем токены и перенаправляем
    import('./auth.js').then(({ logout }) => {
      logout();
      window.location.href = '/login';
    });
  }

  // Возвращаем обработанную ошибку
  return new AppError(message, errorType, error.response?.status, error.response?.data);
}

// Функция для создания обработчика ошибок с опциями
export function createErrorHandler(options = {}) {
  return (error, context = null) => handleError(error, { ...options, context });
}

// React/Vue composable для использования в компонентах
export function useErrorHandler() {
  const message = useMessage();
  const dialog = useDialog();

  return {
    handleError: (error, options = {}) => handleError(error, { showMessage: true, ...options }),
    showError: (message, type = 'error') => {
      switch (type) {
        case 'warning':
          message.warning(message);
          break;
        case 'info':
          message.info(message);
          break;
        case 'success':
          message.success(message);
          break;
        default:
          message.error(message);
      }
    },
    showErrorDialog: (title, content, onPositiveClick) => {
      dialog.error({
        title,
        content,
        positiveText: 'ОК',
        onPositiveClick,
      });
    },
    showRetryDialog: (title, content, onRetry) => {
      dialog.error({
        title,
        content,
        positiveText: 'Повторить',
        negativeText: 'Отмена',
        onPositiveClick: onRetry,
      });
    },
  };
}

// Глобальный обработчик непойманных ошибок
if (typeof window !== 'undefined') {
  window.addEventListener('error', (event) => {
    handleError(event.error || new Error(event.message), {
      context: 'Global error handler',
      showMessage: false,
    });
  });

  window.addEventListener('unhandledrejection', (event) => {
    handleError(event.reason, {
      context: 'Unhandled promise rejection',
      showMessage: false,
    });
  });
}

// Экспортируем утилиты
export default {
  ErrorTypes,
  AppError,
  handleError,
  createErrorHandler,
  useErrorHandler,
  getErrorType,
  getErrorMessage,
  logError,
};