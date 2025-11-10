// src/tests/auth.test.js
import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { 
  login, 
  register, 
  logout, 
  fetchCurrentUser, 
  refreshAccessToken,
  restoreSession,
  isTokenValid,
  shouldRefreshToken,
  hasRole,
  isAdmin,
  isAuthor,
  isPlayer
} from '../services/auth';
import { authService } from '../services/api';

// Мокаем localStorage
const localStorageMock = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn()
};
global.localStorage = localStorageMock;

// Мокаем window
global.window = {
  dispatchEvent: vi.fn(),
  location: { href: '' }
};

// Мокаем authService
vi.mock('../services/api', () => ({
  authService: {
    login: vi.fn(),
    register: vi.fn(),
    refresh: vi.fn(),
    logout: vi.fn(),
    getCurrentUser: vi.fn()
  }
}));

describe('Auth Service', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  describe('Token Management', () => {
    it('should validate token correctly', () => {
      // Создаем валидный токен (истекает через 1 час)
      const payload = { exp: Math.floor(Date.now() / 1000) + 3600 };
      const validToken = `header.${btoa(JSON.stringify(payload))}.signature`;
      
      expect(isTokenValid(validToken)).toBe(true);
    });

    it('should invalidate expired token', () => {
      // Создаем просроченный токен (истек 1 час назад)
      const payload = { exp: Math.floor(Date.now() / 1000) - 3600 };
      const expiredToken = `header.${btoa(JSON.stringify(payload))}.signature`;
      
      expect(isTokenValid(expiredToken)).toBe(false);
    });

    it('should invalidate malformed token', () => {
      const malformedToken = 'invalid.token';
      
      expect(isTokenValid(malformedToken)).toBe(false);
    });

    it('should return false for null token', () => {
      expect(isTokenValid(null)).toBe(false);
      expect(isTokenValid(undefined)).toBe(false);
    });

    it('should check if token needs refresh', () => {
      // Токен истекает через 2 минуты (должен обновляться)
      const payload = { exp: Math.floor(Date.now() / 1000) + 120 };
      const soonToExpireToken = `header.${btoa(JSON.stringify(payload))}.signature`;
      
      expect(shouldRefreshToken(soonToExpireToken)).toBe(true);
    });

    it('should not refresh valid token', () => {
      // Токен истекает через 10 минут (не нужно обновлять)
      const payload = { exp: Math.floor(Date.now() / 1000) + 600 };
      const validToken = `header.${btoa(JSON.stringify(payload))}.signature`;
      
      expect(shouldRefreshToken(validToken)).toBe(false);
    });
  });

  describe('Login', () => {
    it('should login successfully', async () => {
      const mockResponse = {
        data: {
          accessToken: 'access-token',
          refreshToken: 'refresh-token',
          user: { id: 1, username: 'testuser', role: 'PLAYER' }
        }
      };
      
      authService.login.mockResolvedValue(mockResponse);
      
      const credentials = { username: 'testuser', password: 'password' };
      const result = await login(credentials);
      
      expect(authService.login).toHaveBeenCalledWith(credentials);
      expect(localStorage.setItem).toHaveBeenCalledWith('dn_quest_access_token', 'access-token');
      expect(localStorage.setItem).toHaveBeenCalledWith('dn_quest_refresh_token', 'refresh-token');
      expect(localStorage.setItem).toHaveBeenCalledWith('dn_quest_current_user', JSON.stringify(mockResponse.data.user));
      expect(window.dispatchEvent).toHaveBeenCalledWith(new Event('user-changed'));
      expect(result).toEqual(mockResponse.data);
    });

    it('should handle login error', async () => {
      const mockError = new Error('Login failed');
      authService.login.mockRejectedValue(mockError);
      
      const credentials = { username: 'testuser', password: 'wrongpassword' };
      
      await expect(login(credentials)).rejects.toThrow('Login failed');
      expect(authService.login).toHaveBeenCalledWith(credentials);
    });
  });

  describe('Register', () => {
    it('should register successfully', async () => {
      const mockResponse = {
        data: { id: 1, username: 'newuser', email: 'test@example.com' }
      };
      
      authService.register.mockResolvedValue(mockResponse);
      
      const userData = {
        username: 'newuser',
        email: 'test@example.com',
        password: 'password'
      };
      
      const result = await register(userData);
      
      expect(authService.register).toHaveBeenCalledWith(userData);
      expect(result).toEqual(mockResponse.data);
    });

    it('should handle registration error', async () => {
      const mockError = new Error('Registration failed');
      authService.register.mockRejectedValue(mockError);
      
      const userData = {
        username: 'newuser',
        email: 'test@example.com',
        password: 'password'
      };
      
      await expect(register(userData)).rejects.toThrow('Registration failed');
    });
  });

  describe('Logout', () => {
    it('should logout successfully', async () => {
      authService.logout.mockResolvedValue({ status: 200 });
      
      await logout();
      
      expect(authService.logout).toHaveBeenCalled();
      expect(localStorage.removeItem).toHaveBeenCalledWith('dn_quest_access_token');
      expect(localStorage.removeItem).toHaveBeenCalledWith('dn_quest_refresh_token');
      expect(localStorage.removeItem).toHaveBeenCalledWith('dn_quest_current_user');
      expect(window.dispatchEvent).toHaveBeenCalledWith(new Event('user-changed'));
    });

    it('should handle logout API error gracefully', async () => {
      authService.logout.mockRejectedValue(new Error('API Error'));
      
      await logout();
      
      expect(authService.logout).toHaveBeenCalled();
      expect(localStorage.removeItem).toHaveBeenCalledWith('dn_quest_access_token');
      expect(localStorage.removeItem).toHaveBeenCalledWith('dn_quest_refresh_token');
      expect(localStorage.removeItem).toHaveBeenCalledWith('dn_quest_current_user');
      expect(window.dispatchEvent).toHaveBeenCalledWith(new Event('user-changed'));
    });
  });

  describe('Fetch Current User', () => {
    it('should fetch current user successfully', async () => {
      localStorageMock.getItem.mockImplementation((key) => {
        if (key === 'dn_quest_access_token') return 'valid-token';
        return null;
      });
      
      const mockUser = { id: 1, username: 'testuser', role: 'PLAYER' };
      authService.getCurrentUser.mockResolvedValue({ data: mockUser });
      
      const result = await fetchCurrentUser();
      
      expect(authService.getCurrentUser).toHaveBeenCalled();
      expect(localStorage.setItem).toHaveBeenCalledWith('dn_quest_current_user', JSON.stringify(mockUser));
      expect(result).toEqual(mockUser);
    });

    it('should return null when no token', async () => {
      localStorageMock.getItem.mockReturnValue(null);
      
      const result = await fetchCurrentUser();
      
      expect(result).toBeNull();
      expect(authService.getCurrentUser).not.toHaveBeenCalled();
    });

    it('should handle invalid token', async () => {
      localStorageMock.getItem.mockImplementation((key) => {
        if (key === 'dn_quest_access_token') return 'invalid-token';
        return null;
      });
      
      const result = await fetchCurrentUser();
      
      expect(result).toBeNull();
      expect(authService.getCurrentUser).not.toHaveBeenCalled();
    });

    it('should handle API error', async () => {
      localStorageMock.getItem.mockImplementation((key) => {
        if (key === 'dn_quest_access_token') return 'valid-token';
        return null;
      });
      
      const mockError = {
        response: { status: 401 }
      };
      authService.getCurrentUser.mockRejectedValue(mockError);
      
      const result = await fetchCurrentUser();
      
      expect(result).toBeNull();
      expect(localStorage.removeItem).toHaveBeenCalledWith('dn_quest_access_token');
      expect(localStorage.removeItem).toHaveBeenCalledWith('dn_quest_refresh_token');
      expect(localStorage.removeItem).toHaveBeenCalledWith('dn_quest_current_user');
      expect(window.dispatchEvent).toHaveBeenCalledWith(new Event('user-changed'));
    });
  });

  describe('Refresh Access Token', () => {
    it('should refresh token successfully', async () => {
      localStorageMock.getItem.mockImplementation((key) => {
        if (key === 'dn_quest_refresh_token') return 'refresh-token';
        return null;
      });
      
      const mockResponse = {
        data: {
          accessToken: 'new-access-token',
          refreshToken: 'new-refresh-token'
        }
      };
      authService.refresh.mockResolvedValue(mockResponse);
      
      const result = await refreshAccessToken();
      
      expect(authService.refresh).toHaveBeenCalledWith('refresh-token');
      expect(localStorage.setItem).toHaveBeenCalledWith('dn_quest_access_token', 'new-access-token');
      expect(localStorage.setItem).toHaveBeenCalledWith('dn_quest_refresh_token', 'new-refresh-token');
      expect(result).toBe('new-access-token');
    });

    it('should handle refresh error', async () => {
      localStorageMock.getItem.mockImplementation((key) => {
        if (key === 'dn_quest_refresh_token') return 'invalid-refresh-token';
        return null;
      });
      
      const mockError = new Error('Refresh failed');
      authService.refresh.mockRejectedValue(mockError);
      
      await expect(refreshAccessToken()).rejects.toThrow('Refresh failed');
      expect(localStorage.removeItem).toHaveBeenCalledWith('dn_quest_access_token');
      expect(localStorage.removeItem).toHaveBeenCalledWith('dn_quest_refresh_token');
      expect(localStorage.removeItem).toHaveBeenCalledWith('dn_quest_current_user');
      expect(window.dispatchEvent).toHaveBeenCalledWith(new Event('user-changed'));
    });

    it('should throw error when no refresh token', async () => {
      localStorageMock.getItem.mockReturnValue(null);
      
      await expect(refreshAccessToken()).rejects.toThrow('No refresh token available');
    });
  });

  describe('Session Restoration', () => {
    it('should restore session with valid token', async () => {
      localStorageMock.getItem.mockImplementation((key) => {
        if (key === 'dn_quest_access_token') return 'valid-token';
        if (key === 'dn_quest_refresh_token') return 'refresh-token';
        return null;
      });
      
      const mockUser = { id: 1, username: 'testuser', role: 'PLAYER' };
      authService.getCurrentUser.mockResolvedValue({ data: mockUser });
      
      const result = await restoreSession();
      
      expect(result).toEqual(mockUser);
      expect(authService.getCurrentUser).toHaveBeenCalled();
    });

    it('should restore session with refresh token', async () => {
      localStorageMock.getItem.mockImplementation((key) => {
        if (key === 'dn_quest_access_token') return 'expired-token';
        if (key === 'dn_quest_refresh_token') return 'refresh-token';
        return null;
      });
      
      const mockRefreshResponse = {
        data: { accessToken: 'new-access-token' }
      };
      authService.refresh.mockResolvedValue(mockRefreshResponse);
      
      const mockUser = { id: 1, username: 'testuser', role: 'PLAYER' };
      authService.getCurrentUser.mockResolvedValue({ data: mockUser });
      
      const result = await restoreSession();
      
      expect(result).toEqual(mockUser);
      expect(authService.refresh).toHaveBeenCalled();
      expect(authService.getCurrentUser).toHaveBeenCalled();
    });

    it('should return null when no tokens', async () => {
      localStorageMock.getItem.mockReturnValue(null);
      
      const result = await restoreSession();
      
      expect(result).toBeNull();
    });
  });

  describe('Role Helpers', () => {
    it('should check user role correctly', () => {
      const adminUser = { role: 'ADMIN' };
      const authorUser = { role: 'AUTHOR' };
      const playerUser = { role: 'PLAYER' };
      
      expect(hasRole(adminUser, 'ADMIN')).toBe(true);
      expect(hasRole(adminUser, 'AUTHOR')).toBe(false);
      expect(hasRole(authorUser, 'AUTHOR')).toBe(true);
      expect(hasRole(playerUser, 'PLAYER')).toBe(true);
      expect(hasRole(null, 'ADMIN')).toBe(false);
    });

    it('should check admin role correctly', () => {
      expect(isAdmin({ role: 'ADMIN' })).toBe(true);
      expect(isAdmin({ role: 'AUTHOR' })).toBe(false);
      expect(isAdmin(null)).toBe(false);
    });

    it('should check author role correctly', () => {
      expect(isAuthor({ role: 'AUTHOR' })).toBe(true);
      expect(isAuthor({ role: 'ADMIN' })).toBe(false);
      expect(isAuthor(null)).toBe(false);
    });

    it('should check player role correctly', () => {
      expect(isPlayer({ role: 'PLAYER' })).toBe(true);
      expect(isPlayer({ role: 'ADMIN' })).toBe(false);
      expect(isPlayer(null)).toBe(false);
    });
  });
});