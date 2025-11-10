// src/services/websocket.js
import { getToken } from './auth';
import { handleError } from './errorHandler';

// Конфигурация WebSocket
const WS_CONFIG = {
  baseURL: import.meta.env.VITE_WS_BASE_URL || 'ws://localhost:8080/ws',
  reconnectInterval: 5000,
  maxReconnectAttempts: 10,
  heartbeatInterval: 30000,
  connectionTimeout: 10000,
};

// Типы WebSocket сообщений
export const WSMessageTypes = {
  // Игровые события
  GAME_SESSION_STARTED: 'GAME_SESSION_STARTED',
  GAME_SESSION_FINISHED: 'GAME_SESSION_FINISHED',
  LEVEL_COMPLETED: 'LEVEL_COMPLETED',
  CODE_SUBMITTED: 'CODE_SUBMITTED',
  HINT_REVEALED: 'HINT_REVEALED',
  
  // Командные события
  TEAM_MEMBER_ADDED: 'TEAM_MEMBER_ADDED',
  TEAM_MEMBER_REMOVED: 'TEAM_MEMBER_REMOVED',
  TEAM_UPDATED: 'TEAM_UPDATED',
  INVITATION_RECEIVED: 'INVITATION_RECEIVED',
  INVITATION_ACCEPTED: 'INVITATION_ACCEPTED',
  INVITATION_REJECTED: 'INVITATION_REJECTED',
  
  // Уведомления
  NOTIFICATION: 'NOTIFICATION',
  SYSTEM_ANNOUNCEMENT: 'SYSTEM_ANNOUNCEMENT',
  
  // Системные события
  CONNECTED: 'CONNECTED',
  DISCONNECTED: 'DISCONNECTED',
  ERROR: 'ERROR',
  PING: 'PING',
  PONG: 'PONG',
};

// Класс для управления WebSocket соединением
export class WebSocketManager {
  constructor() {
    this.ws = null;
    this.reconnectAttempts = 0;
    this.heartbeatTimer = null;
    this.connectionTimer = null;
    this.reconnectTimer = null;
    this.isConnected = false;
    this.subscribers = new Map();
    this.messageQueue = [];
    this.connectionPromise = null;
  }

  // Подключение к WebSocket
  async connect() {
    if (this.connectionPromise) {
      return this.connectionPromise;
    }

    this.connectionPromise = new Promise((resolve, reject) => {
      const token = getToken();
      if (!token) {
        reject(new Error('No authentication token available'));
        return;
      }

      try {
        const wsUrl = `${WS_CONFIG.baseURL}?token=${encodeURIComponent(token)}`;
        this.ws = new WebSocket(wsUrl);

        // Таймаут подключения
        this.connectionTimer = setTimeout(() => {
          if (this.ws?.readyState === WebSocket.CONNECTING) {
            this.ws.close();
            reject(new Error('WebSocket connection timeout'));
          }
        }, WS_CONFIG.connectionTimeout);

        this.ws.onopen = () => {
          clearTimeout(this.connectionTimer);
          this.isConnected = true;
          this.reconnectAttempts = 0;
          
          // Отправляем сообщения из очереди
          this.flushMessageQueue();
          
          // Запускаем heartbeat
          this.startHeartbeat();
          
          // Уведомляем подписчиков
          this.notifySubscribers(WSMessageTypes.CONNECTED, { connected: true });
          
          resolve(this.ws);
        };

        this.ws.onmessage = (event) => {
          try {
            const message = JSON.parse(event.data);
            this.handleMessage(message);
          } catch (error) {
            console.error('Error parsing WebSocket message:', error);
          }
        };

        this.ws.onclose = (event) => {
          clearTimeout(this.connectionTimer);
          this.isConnected = false;
          this.stopHeartbeat();
          
          // Уведомляем подписчиков
          this.notifySubscribers(WSMessageTypes.DISCONNECTED, { 
            code: event.code, 
            reason: event.reason 
          });

          // Пытаемся переподключиться
          if (event.code !== 1000 && this.reconnectAttempts < WS_CONFIG.maxReconnectAttempts) {
            this.scheduleReconnect();
          }
        };

        this.ws.onerror = (error) => {
          clearTimeout(this.connectionTimer);
          console.error('WebSocket error:', error);
          
          // Уведомляем подписчиков об ошибке
          this.notifySubscribers(WSMessageTypes.ERROR, { error });
          
          reject(error);
        };

      } catch (error) {
        clearTimeout(this.connectionTimer);
        reject(error);
      }
    });

    return this.connectionPromise;
  }

  // Отключение от WebSocket
  disconnect() {
    this.reconnectAttempts = WS_CONFIG.maxReconnectAttempts; // Предотвращаем переподключение
    
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }
    
    this.stopHeartbeat();
    
    if (this.ws) {
      this.ws.close(1000, 'Client disconnect');
      this.ws = null;
    }
    
    this.isConnected = false;
    this.connectionPromise = null;
  }

  // Отправка сообщения
  send(type, data) {
    const message = {
      type,
      data,
      timestamp: new Date().toISOString(),
    };

    if (this.isConnected && this.ws?.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(message));
    } else {
      // Добавляем в очередь, если соединение не установлено
      this.messageQueue.push(message);
    }
  }

  // Подписка на события
  subscribe(type, callback) {
    if (!this.subscribers.has(type)) {
      this.subscribers.set(type, new Set());
    }
    this.subscribers.get(type).add(callback);

    // Возвращаем функцию отписки
    return () => {
      const callbacks = this.subscribers.get(type);
      if (callbacks) {
        callbacks.delete(callback);
        if (callbacks.size === 0) {
          this.subscribers.delete(type);
        }
      }
    };
  }

  // Обработка входящих сообщений
  handleMessage(message) {
    const { type, data } = message;

    // Обработка специальных сообщений
    switch (type) {
      case WSMessageTypes.PING:
        this.send(WSMessageTypes.PONG, { timestamp: Date.now() });
        break;
      
      case WSMessageTypes.PONG:
        // Обновляем heartbeat
        break;
      
      default:
        // Уведомляем подписчиков
        this.notifySubscribers(type, data);
    }
  }

  // Уведомление подписчиков
  notifySubscribers(type, data) {
    const callbacks = this.subscribers.get(type);
    if (callbacks) {
      callbacks.forEach(callback => {
        try {
          callback(data);
        } catch (error) {
          console.error('Error in WebSocket subscriber callback:', error);
        }
      });
    }
  }

  // Отправка сообщений из очереди
  flushMessageQueue() {
    while (this.messageQueue.length > 0 && this.isConnected) {
      const message = this.messageQueue.shift();
      this.send(message.type, message.data);
    }
  }

  // Планирование переподключения
  scheduleReconnect() {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
    }

    this.reconnectTimer = setTimeout(async () => {
      this.reconnectAttempts++;
      
      try {
        await this.connect();
      } catch (error) {
        console.error('WebSocket reconnection failed:', error);
        
        if (this.reconnectAttempts < WS_CONFIG.maxReconnectAttempts) {
          this.scheduleReconnect();
        }
      }
    }, WS_CONFIG.reconnectInterval);
  }

  // Запуск heartbeat
  startHeartbeat() {
    this.heartbeatTimer = setInterval(() => {
      if (this.isConnected) {
        this.send(WSMessageTypes.PING, { timestamp: Date.now() });
      }
    }, WS_CONFIG.heartbeatInterval);
  }

  // Остановка heartbeat
  stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer);
      this.heartbeatTimer = null;
    }
  }

  // Получение статуса соединения
  getConnectionStatus() {
    return {
      isConnected: this.isConnected,
      readyState: this.ws?.readyState,
      reconnectAttempts: this.reconnectAttempts,
    };
  }
}

// Создаем глобальный экземпляр WebSocket менеджера
const wsManager = new WebSocketManager();

// Composable для использования в Vue компонентах
export function useWebSocket() {
  const connect = async () => {
    try {
      await wsManager.connect();
    } catch (error) {
      handleError(error, {
        context: 'WebSocket connection',
        showMessage: false,
      });
      throw error;
    }
  };

  const disconnect = () => {
    wsManager.disconnect();
  };

  const send = (type, data) => {
    wsManager.send(type, data);
  };

  const subscribe = (type, callback) => {
    return wsManager.subscribe(type, callback);
  };

  const getConnectionStatus = () => {
    return wsManager.getConnectionStatus();
  };

  return {
    connect,
    disconnect,
    send,
    subscribe,
    getConnectionStatus,
    isConnected: wsManager.isConnected,
  };
}

// Специализированные функции для подписки на разные типы событий
export function useGameEvents(callbacks = {}) {
  const { subscribe } = useWebSocket();

  const unsubscribers = [];

  // Подписываемся на игровые события
  if (callbacks.onSessionStarted) {
    unsubscribers.push(
      subscribe(WSMessageTypes.GAME_SESSION_STARTED, callbacks.onSessionStarted)
    );
  }

  if (callbacks.onSessionFinished) {
    unsubscribers.push(
      subscribe(WSMessageTypes.GAME_SESSION_FINISHED, callbacks.onSessionFinished)
    );
  }

  if (callbacks.onLevelCompleted) {
    unsubscribers.push(
      subscribe(WSMessageTypes.LEVEL_COMPLETED, callbacks.onLevelCompleted)
    );
  }

  if (callbacks.onCodeSubmitted) {
    unsubscribers.push(
      subscribe(WSMessageTypes.CODE_SUBMITTED, callbacks.onCodeSubmitted)
    );
  }

  if (callbacks.onHintRevealed) {
    unsubscribers.push(
      subscribe(WSMessageTypes.HINT_REVEALED, callbacks.onHintRevealed)
    );
  }

  // Функция отписки от всех событий
  const unsubscribeAll = () => {
    unsubscribers.forEach(unsubscribe => unsubscribe());
  };

  return { unsubscribeAll };
}

export function useTeamEvents(callbacks = {}) {
  const { subscribe } = useWebSocket();

  const unsubscribers = [];

  if (callbacks.onMemberAdded) {
    unsubscribers.push(
      subscribe(WSMessageTypes.TEAM_MEMBER_ADDED, callbacks.onMemberAdded)
    );
  }

  if (callbacks.onMemberRemoved) {
    unsubscribers.push(
      subscribe(WSMessageTypes.TEAM_MEMBER_REMOVED, callbacks.onMemberRemoved)
    );
  }

  if (callbacks.onTeamUpdated) {
    unsubscribers.push(
      subscribe(WSMessageTypes.TEAM_UPDATED, callbacks.onTeamUpdated)
    );
  }

  if (callbacks.onInvitationReceived) {
    unsubscribers.push(
      subscribe(WSMessageTypes.INVITATION_RECEIVED, callbacks.onInvitationReceived)
    );
  }

  const unsubscribeAll = () => {
    unsubscribers.forEach(unsubscribe => unsubscribe());
  };

  return { unsubscribeAll };
}

export function useNotifications(callbacks = {}) {
  const { subscribe } = useWebSocket();

  const unsubscribers = [];

  if (callbacks.onNotification) {
    unsubscribers.push(
      subscribe(WSMessageTypes.NOTIFICATION, callbacks.onNotification)
    );
  }

  if (callbacks.onSystemAnnouncement) {
    unsubscribers.push(
      subscribe(WSMessageTypes.SYSTEM_ANNOUNCEMENT, callbacks.onSystemAnnouncement)
    );
  }

  const unsubscribeAll = () => {
    unsubscribers.forEach(unsubscribe => unsubscribe());
  };

  return { unsubscribeAll };
}

// Автоматическое подключение при наличии токена
if (typeof window !== 'undefined' && getToken()) {
  wsManager.connect().catch(error => {
    console.warn('Auto WebSocket connection failed:', error);
  });
}

// Экспортируем WebSocket менеджер для продвинутого использования
export { wsManager };

export default {
  WSMessageTypes,
  WebSocketManager,
  useWebSocket,
  useGameEvents,
  useTeamEvents,
  useNotifications,
  wsManager,
};