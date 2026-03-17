// frontend/src/tests/integration.test.js
import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createRouter, createWebHistory } from 'vue-router';
import { createPinia } from 'pinia';
import axios from 'axios';
import App from '../App.vue';
import { authService, questService, gameService, teamService, fileService } from '../services/api';
import { useAuthStore } from '../stores/auth';
import { useQuestStore } from '../stores/quest';
import { useGameStore } from '../stores/game';

// Мокаем axios
vi.mock('axios');

describe('Frontend Integration Tests', () => {
  let router;
  let pinia;
  let wrapper;

  beforeEach(async () => {
    // Создаем экземпляры роутера и хранилища
    router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/', component: { template: '<div>Home</div>' } },
        { path: '/login', component: { template: '<div>Login</div>' } },
        { path: '/register', component: { template: '<div>Register</div>' } },
        { path: '/quests', component: { template: '<div>Quests</div>' } },
        { path: '/play', component: { template: '<div>Play</div>' } },
        { path: '/teams', component: { template: '<div>Teams</div>' } },
      ]
    });

    pinia = createPinia();

    // Мокаем localStorage
    const localStorageMock = {
      getItem: vi.fn(),
      setItem: vi.fn(),
      removeItem: vi.fn(),
      clear: vi.fn(),
    };
    global.localStorage = localStorageMock;

    wrapper = mount(App, {
      global: {
        plugins: [router, pinia],
      },
    });

    await router.isReady();
  });

  afterEach(() => {
    vi.clearAllMocks();
    wrapper?.unmount();
  });

  describe('Authentication Integration', () => {
    it('should handle complete user registration flow', async () => {
      // Given
      const mockRegisterResponse = {
        data: {
          accessToken: 'test-access-token',
          refreshToken: 'test-refresh-token',
          user: {
            id: 1,
            username: 'testuser',
            email: 'test@example.com',
            role: 'PLAYER'
          }
        }
      };

      axios.post.mockResolvedValue(mockRegisterResponse);

      const authStore = useAuthStore();

      // When
      await authStore.register({
        username: 'testuser',
        email: 'test@example.com',
        password: 'Password123',
        publicName: 'Test User'
      });

      // Then
      expect(axios.post).toHaveBeenCalledWith('/auth/register', {
        username: 'testuser',
        email: 'test@example.com',
        password: 'Password123',
        publicName: 'Test User'
      });

      expect(authStore.isAuthenticated).toBe(true);
      expect(authStore.user).toEqual(mockRegisterResponse.data.user);
      expect(localStorage.setItem).toHaveBeenCalledWith('token', 'test-access-token');
    });

    it('should handle complete login flow', async () => {
      // Given
      const mockLoginResponse = {
        data: {
          accessToken: 'test-access-token',
          refreshToken: 'test-refresh-token',
          user: {
            id: 1,
            username: 'testuser',
            email: 'test@example.com',
            role: 'PLAYER'
          }
        }
      };

      axios.post.mockResolvedValue(mockLoginResponse);

      const authStore = useAuthStore();

      // When
      await authStore.login({
        username: 'testuser',
        password: 'Password123'
      });

      // Then
      expect(axios.post).toHaveBeenCalledWith('/auth/login', {
        username: 'testuser',
        password: 'Password123'
      });

      expect(authStore.isAuthenticated).toBe(true);
      expect(authStore.user).toEqual(mockLoginResponse.data.user);
    });

    it('should handle token refresh flow', async () => {
      // Given
      const mockRefreshResponse = {
        data: {
          accessToken: 'new-access-token',
          refreshToken: 'new-refresh-token'
        }
      };

      axios.post.mockResolvedValue(mockRefreshResponse);
      localStorage.getItem.mockReturnValue('old-refresh-token');

      const authStore = useAuthStore();

      // When
      await authStore.refreshToken();

      // Then
      expect(axios.post).toHaveBeenCalledWith('/auth/refresh', {
        refreshToken: 'old-refresh-token'
      });

      expect(localStorage.setItem).toHaveBeenCalledWith('token', 'new-access-token');
    });

    it('should handle logout flow', async () => {
      // Given
      const authStore = useAuthStore();
      authStore.isAuthenticated = true;
      authStore.user = { id: 1, username: 'testuser' };

      axios.post.mockResolvedValue({ data: { message: 'Logout successful' } });

      // When
      await authStore.logout();

      // Then
      expect(axios.post).toHaveBeenCalledWith('/auth/logout', {});
      expect(authStore.isAuthenticated).toBe(false);
      expect(authStore.user).toBeNull();
      expect(localStorage.removeItem).toHaveBeenCalledWith('token');
    });
  });

  describe('Quest Management Integration', () => {
    beforeEach(() => {
      const authStore = useAuthStore();
      authStore.isAuthenticated = true;
      authStore.user = { id: 1, username: 'testuser', role: 'PLAYER' };
    });

    it('should handle complete quest creation flow', async () => {
      // Given
      const mockQuestResponse = {
        data: {
          id: 1,
          title: 'Test Quest',
          description: 'Test Description',
          difficulty: 'EASY',
          questType: 'SOLO',
          published: false,
          levels: [
            {
              id: 1,
              title: 'Level 1',
              description: 'First level',
              orderIndex: 1,
              codes: [
                {
                  id: 1,
                  value: 'TEST123',
                  type: 'TEXT',
                  points: 100
                }
              ]
            }
          ]
        }
      };

      axios.post.mockResolvedValue(mockQuestResponse);

      const questStore = useQuestStore();

      const questData = {
        title: 'Test Quest',
        description: 'Test Description',
        difficulty: 'EASY',
        questType: 'SOLO',
        levels: [
          {
            title: 'Level 1',
            description: 'First level',
            orderIndex: 1,
            codes: [
              {
                value: 'TEST123',
                type: 'TEXT',
                points: 100
              }
            ]
          }
        ]
      };

      // When
      await questStore.createQuest(questData);

      // Then
      expect(axios.post).toHaveBeenCalledWith('/quests', questData);
      expect(questStore.currentQuest).toEqual(mockQuestResponse.data);
    });

    it('should handle quest publishing flow', async () => {
      // Given
      const mockPublishResponse = {
        data: {
          id: 1,
          title: 'Test Quest',
          published: true
        }
      };

      axios.post.mockResolvedValue(mockPublishResponse);

      const questStore = useQuestStore();

      // When
      await questStore.publishQuest(1);

      // Then
      expect(axios.post).toHaveBeenCalledWith('/quests/1/publish');
      expect(mockPublishResponse.data.published).toBe(true);
    });

    it('should handle quest listing with pagination', async () => {
      // Given
      const mockQuestsResponse = {
        data: {
          content: [
            { id: 1, title: 'Quest 1', published: true },
            { id: 2, title: 'Quest 2', published: true }
          ],
          totalElements: 2,
          totalPages: 1,
          size: 10,
          number: 0
        }
      };

      axios.get.mockResolvedValue(mockQuestsResponse);

      const questStore = useQuestStore();

      // When
      await questStore.fetchQuests({ page: 0, size: 10, published: true });

      // Then
      expect(axios.get).toHaveBeenCalledWith('/quests', {
        params: { page: 0, size: 10, published: true }
      });

      expect(questStore.quests).toEqual(mockQuestsResponse.data.content);
      expect(questStore.totalQuests).toBe(2);
    });
  });

  describe('Game Session Integration', () => {
    beforeEach(() => {
      const authStore = useAuthStore();
      authStore.isAuthenticated = true;
      authStore.user = { id: 1, username: 'testuser', role: 'PLAYER' };
    });

    it('should handle complete game session flow', async () => {
      // Given
      const mockStartSessionResponse = {
        data: {
          id: 'session-123',
          questId: 1,
          userId: 1,
          status: 'ACTIVE',
          currentLevel: {
            id: 1,
            title: 'Level 1',
            orderIndex: 1
          },
          progress: {
            sectorsClosed: 0,
            startedAt: '2024-01-01T00:00:00Z'
          }
        }
      };

      const mockCodeSubmissionResponse = {
        data: {
          result: 'ACCEPTED_NORMAL',
          message: 'Code accepted',
          points: 100
        }
      };

      const mockFinishSessionResponse = {
        data: {
          id: 'session-123',
          status: 'COMPLETED',
          completedAt: '2024-01-01T01:00:00Z',
          totalPoints: 100
        }
      };

      axios.post
        .mockResolvedValueOnce(mockStartSessionResponse)
        .mockResolvedValueOnce(mockCodeSubmissionResponse)
        .mockResolvedValueOnce(mockFinishSessionResponse);

      const gameStore = useGameStore();

      // When - Start session
      await gameStore.startSession(1);

      // Then
      expect(axios.post).toHaveBeenCalledWith('/game/sessions', { questId: 1 });
      expect(gameStore.currentSession).toEqual(mockStartSessionResponse.data);

      // When - Submit code
      await gameStore.submitCode('session-123', {
        rawCode: 'TEST123',
        userId: 1
      });

      // Then
      expect(axios.post).toHaveBeenCalledWith('/game/sessions/session-123/code', {
        rawCode: 'TEST123',
        userId: 1
      });

      // When - Finish session
      await gameStore.finishSession('session-123');

      // Then
      expect(axios.post).toHaveBeenCalledWith('/game/sessions/session-123/finish');
      expect(mockFinishSessionResponse.data.status).toBe('COMPLETED');
    });

    it('should handle current level retrieval', async () => {
      // Given
      const mockCurrentLevelResponse = {
        data: {
          level: {
            id: 1,
            title: 'Level 1',
            description: 'First level',
            orderIndex: 1
          },
          progress: {
            sectorsClosed: 2,
            startedAt: '2024-01-01T00:00:00Z'
          }
        }
      };

      axios.get.mockResolvedValue(mockCurrentLevelResponse);

      const gameStore = useGameStore();

      // When
      await gameStore.fetchCurrentLevel('session-123');

      // Then
      expect(axios.get).toHaveBeenCalledWith('/game/sessions/session-123/current');
      expect(gameStore.currentLevel).toEqual(mockCurrentLevelResponse.data.level);
      expect(gameStore.progress).toEqual(mockCurrentLevelResponse.data.progress);
    });
  });

  describe('Team Management Integration', () => {
    beforeEach(() => {
      const authStore = useAuthStore();
      authStore.isAuthenticated = true;
      authStore.user = { id: 1, username: 'testuser', role: 'PLAYER' };
    });

    it('should handle complete team creation flow', async () => {
      // Given
      const mockCreateTeamResponse = {
        data: {
          id: 1,
          name: 'Test Team',
          description: 'Test team description',
          memberCount: 1,
          members: [
            {
              userId: 1,
              username: 'testuser',
              role: 'LEADER'
            }
          ]
        }
      };

      axios.post.mockResolvedValue(mockCreateTeamResponse);

      const teamData = {
        name: 'Test Team',
        description: 'Test team description',
        maxMembers: 5
      };

      // When
      const response = await teamService.createTeam(teamData);

      // Then
      expect(axios.post).toHaveBeenCalledWith('/teams', teamData);
      expect(response.data).toEqual(mockCreateTeamResponse.data);
    });

    it('should handle team joining flow', async () => {
      // Given
      const mockJoinTeamResponse = {
        data: {
          message: 'Successfully joined team'
        }
      };

      axios.post.mockResolvedValue(mockJoinTeamResponse);

      // When
      const response = await teamService.joinTeam(1, 'INVITE123');

      // Then
      expect(axios.post).toHaveBeenCalledWith('/teams/1/join', {
        inviteCode: 'INVITE123'
      });
      expect(response.data.message).toBe('Successfully joined team');
    });
  });

  describe('File Upload Integration', () => {
    beforeEach(() => {
      const authStore = useAuthStore();
      authStore.isAuthenticated = true;
      authStore.user = { id: 1, username: 'testuser', role: 'PLAYER' };
    });

    it('should handle file upload flow', async () => {
      // Given
      const mockFileUploadResponse = {
        data: {
          id: 'file-123',
          name: 'test.jpg',
          size: 1024,
          type: 'image/jpeg',
          url: 'http://example.com/files/test.jpg'
        }
      };

      axios.post.mockResolvedValue(mockFileUploadResponse);

      const file = new File(['test'], 'test.jpg', { type: 'image/jpeg' });
      const onUploadProgress = vi.fn();

      // When
      const response = await fileService.uploadFile(file, onUploadProgress);

      // Then
      expect(axios.post).toHaveBeenCalledWith(
        '/files/upload',
        expect.any(FormData),
        {
          headers: { 'Content-Type': 'multipart/form-data' },
          onUploadProgress
        }
      );

      expect(response.data).toEqual(mockFileUploadResponse.data);
    });

    it('should handle multiple file upload flow', async () => {
      // Given
      const mockBatchUploadResponse = {
        data: [
          {
            id: 'file-123',
            name: 'test1.jpg',
            size: 1024,
            type: 'image/jpeg'
          },
          {
            id: 'file-456',
            name: 'test2.jpg',
            size: 2048,
            type: 'image/jpeg'
          }
        ]
      };

      axios.post.mockResolvedValue(mockBatchUploadResponse);

      const files = [
        new File(['test1'], 'test1.jpg', { type: 'image/jpeg' }),
        new File(['test2'], 'test2.jpg', { type: 'image/jpeg' })
      ];

      // When
      const response = await fileService.uploadMultipleFiles(files);

      // Then
      expect(axios.post).toHaveBeenCalledWith(
        '/files/batch-upload',
        expect.any(FormData),
        {
          headers: { 'Content-Type': 'multipart/form-data' }
        }
      );

      expect(response.data).toEqual(mockBatchUploadResponse.data);
      expect(response.data).toHaveLength(2);
    });
  });

  describe('Error Handling Integration', () => {
    it('should handle authentication errors', async () => {
      // Given
      const authStore = useAuthStore();
      
      const mockError = {
        response: {
          status: 401,
          data: { message: 'Invalid credentials' }
        }
      };

      axios.post.mockRejectedValue(mockError);

      // When & Then
      await expect(authStore.login({
        username: 'testuser',
        password: 'wrongpassword'
      })).rejects.toThrow();

      expect(authStore.isAuthenticated).toBe(false);
      expect(authStore.user).toBeNull();
    });

    it('should handle network errors', async () => {
      // Given
      const authStore = useAuthStore();
      
      const mockNetworkError = new Error('Network Error');
      axios.post.mockRejectedValue(mockNetworkError);

      // When & Then
      await expect(authStore.login({
        username: 'testuser',
        password: 'password'
      })).rejects.toThrow();

      expect(authStore.isAuthenticated).toBe(false);
    });

    it('should handle server errors', async () => {
      // Given
      const questStore = useQuestStore();
      
      const mockServerError = {
        response: {
          status: 500,
          data: { message: 'Internal server error' }
        }
      };

      axios.post.mockRejectedValue(mockServerError);

      // When & Then
      await expect(questStore.createQuest({
        title: 'Test Quest'
      })).rejects.toThrow();
    });
  });

  describe('WebSocket Integration', () => {
    it('should handle WebSocket connection', async () => {
      // Given
      const mockWebSocket = {
        readyState: WebSocket.OPEN,
        send: vi.fn(),
        close: vi.fn(),
        addEventListener: vi.fn()
      };

      global.WebSocket = vi.fn(() => mockWebSocket);

      // When
      const ws = new WebSocket('ws://localhost:8080/ws');

      // Then
      expect(WebSocket).toHaveBeenCalledWith('ws://localhost:8080/ws');
      expect(ws.addEventListener).toHaveBeenCalledWith('open', expect.any(Function));
      expect(ws.addEventListener).toHaveBeenCalledWith('message', expect.any(Function));
      expect(ws.addEventListener).toHaveBeenCalledWith('error', expect.any(Function));
      expect(ws.addEventListener).toHaveBeenCalledWith('close', expect.any(Function));
    });

    it('should handle WebSocket message reception', async () => {
      // Given
      const mockWebSocket = {
        readyState: WebSocket.OPEN,
        send: vi.fn(),
        close: vi.fn(),
        addEventListener: vi.fn((event, callback) => {
          if (event === 'message') {
            callback({ data: JSON.stringify({ type: 'GAME_UPDATE', data: {} }) });
          }
        })
      };

      global.WebSocket = vi.fn(() => mockWebSocket);

      // When
      const ws = new WebSocket('ws://localhost:8080/ws');

      // Then
      expect(ws.addEventListener).toHaveBeenCalledWith('message', expect.any(Function));
    });
  });
});