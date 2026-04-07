// src/tests/setup.js
import { vi } from 'vitest';
import { config } from '@vue/test-utils';

// Глобальная конфигурация Vue Test Utils
config.global.stubs = {
  'n-button': true,
  'n-input': true,
  'n-card': true,
  'n-form': true,
  'n-form-item': true,
  'n-select': true,
  'n-switch': true,
  'n-date-picker': true,
  'n-tabs': true,
  'n-tab-pane': true,
  'n-spin': true,
  'n-progress': true,
  'n-divider': true,
  'n-alert': true,
  'n-upload': true,
  'n-upload-dragger': true,
  'n-text': true,
  'n-p': true,
  'n-avatar': true,
  'n-tag': true,
  'n-badge': true,
  'n-dropdown': true,
  'n-pagination': true,
  'n-message-provider': true,
  'n-notification-provider': true,
  'n-dialog-provider': true,
  'n-loading-bar-provider': true,
  'n-config-provider': true,
  'n-layout': true,
  'n-layout-header': true,
  'n-layout-content': true,
  'n-layout-footer': true,
  'router-link': true,
  'router-view': true
};

// Мокаем useMessage и другие composables из naive-ui
vi.mock('naive-ui', () => ({
  useMessage: () => ({
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn(),
    info: vi.fn()
  }),
  useDialog: () => ({
    error: vi.fn(),
    warning: vi.fn(),
    info: vi.fn(),
    success: vi.fn()
  }),
  useNotification: () => ({
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn(),
    info: vi.fn()
  })
}));

// Мокаем Vue Router
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: vi.fn(),
    replace: vi.fn(),
    go: vi.fn(),
    back: vi.fn(),
    forward: vi.fn()
  }),
  useRoute: () => ({
    params: {},
    query: {},
    path: '/',
    name: 'home',
    meta: {}
  }),
  createRouter: vi.fn(),
  createWebHistory: vi.fn()
}));

// Мокаем environment variables
Object.defineProperty(window, 'location', {
  value: {
    href: 'http://localhost:5173',
    origin: 'http://localhost:5173',
    protocol: 'http:',
    host: 'localhost:5173',
    hostname: 'localhost',
    port: '5173',
    pathname: '/',
    search: '',
    hash: ''
  },
  writable: true
});

// Мокаем localStorage
const localStorageMock = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn(),
  length: 0,
  key: vi.fn()
};
Object.defineProperty(window, 'localStorage', {
  value: localStorageMock
});

// Мокаем sessionStorage
const sessionStorageMock = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn(),
  length: 0,
  key: vi.fn()
};
Object.defineProperty(window, 'sessionStorage', {
  value: sessionStorageMock
});

// Мокаем WebSocket
class MockWebSocket {
  constructor(url) {
    this.url = url;
    this.readyState = WebSocket.CONNECTING;
    this.onopen = null;
    this.onclose = null;
    this.onmessage = null;
    this.onerror = null;
    
    // Симулируем успешное подключение
    setTimeout(() => {
      this.readyState = WebSocket.OPEN;
      if (this.onopen) {
        this.onopen({ type: 'open' });
      }
    }, 100);
  }
  
  send(data) {
    // Мокаем отправку данных
  }
  
  close(code, reason) {
    this.readyState = WebSocket.CLOSED;
    if (this.onclose) {
      this.onclose({ code, reason, type: 'close' });
    }
  }
  
  addEventListener(type, listener) {
    switch (type) {
      case 'open':
        this.onopen = listener;
        break;
      case 'close':
        this.onclose = listener;
        break;
      case 'message':
        this.onmessage = listener;
        break;
      case 'error':
        this.onerror = listener;
        break;
    }
  }
  
  removeEventListener(type, listener) {
    // Мокаем удаление слушателей
  }
}

// Устанавливаем константы для WebSocket
MockWebSocket.CONNECTING = 0;
MockWebSocket.OPEN = 1;
MockWebSocket.CLOSING = 2;
MockWebSocket.CLOSED = 3;

Object.defineProperty(window, 'WebSocket', {
  value: MockWebSocket
});

// Мокаем File и FileReader
global.File = class File {
  constructor(chunks, filename, options = {}) {
    this.chunks = chunks;
    this.name = filename;
    this.size = chunks.reduce((acc, chunk) => acc + chunk.length, 0);
    this.type = options.type || '';
    this.lastModified = Date.now();
  }
};

global.FileReader = class FileReader {
  constructor() {
    this.readyState = 0;
    this.result = null;
    this.onerror = null;
    this.onload = null;
  }
  
  readAsDataURL(file) {
    setTimeout(() => {
      this.result = `data:${file.type};base64,mock-data`;
      this.readyState = 2;
      if (this.onload) {
        this.onload({ target: this });
      }
    }, 100);
  }
};

// Мокаем URL.createObjectURL и URL.revokeObjectURL
global.URL.createObjectURL = vi.fn(() => 'mock-object-url');
global.URL.revokeObjectURL = vi.fn();

// Мокаем ResizeObserver
global.ResizeObserver = class ResizeObserver {
  constructor(callback) {
    this.callback = callback;
  }
  
  observe() {
    // Мокаем наблюдение
  }
  
  unobserve() {
    // Мокаем прекращение наблюдения
  }
  
  disconnect() {
    // Мокаем отключение
  }
};

// Мокаем IntersectionObserver
global.IntersectionObserver = class IntersectionObserver {
  constructor(callback) {
    this.callback = callback;
  }
  
  observe() {
    // Мокаем наблюдение
  }
  
  unobserve() {
    // Мокаем прекращение наблюдения
  }
  
  disconnect() {
    // Мокаем отключение
  }
};

// Глобальные утилиты для тестов
global.createMockFile = (name = 'test.jpg', type = 'image/jpeg', size = 1024) => {
  const content = new Array(size).fill('a').join('');
  return new File([content], name, { type });
};

global.createMockEvent = (type, data = {}) => {
  return {
    type,
    preventDefault: vi.fn(),
    stopPropagation: vi.fn(),
    target: {
      value: '',
      checked: false,
      files: [],
      ...data
    },
    ...data
  };
};

// Очистка после каждого теста
afterEach(() => {
  vi.clearAllMocks();
  localStorageMock.getItem.mockClear();
  localStorageMock.setItem.mockClear();
  localStorageMock.removeItem.mockClear();
  localStorageMock.clear.mockClear();
  sessionStorageMock.getItem.mockClear();
  sessionStorageMock.setItem.mockClear();
  sessionStorageMock.removeItem.mockClear();
  sessionStorageMock.clear.mockClear();
});