// src/tests/api.test.js
import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import axios from 'axios';
import { authService, questService, gameService, teamService, fileService } from '../services/api';

// Мокаем axios
vi.mock('axios');

describe('API Services', () => {
  beforeEach(() => {
    // Очищаем все моки перед каждым тестом
    vi.clearAllMocks();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  describe('AuthService', () => {
    it('should login successfully', async () => {
      const mockResponse = {
        data: {
          accessToken: 'test-token',
          refreshToken: 'refresh-token',
          user: { id: 1, username: 'testuser' }
        }
      };
      
      axios.post.mockResolvedValue(mockResponse);
      
      const credentials = { username: 'testuser', password: 'password' };
      const result = await authService.login(credentials);
      
      expect(axios.post).toHaveBeenCalledWith('/auth/login', credentials);
      expect(result.data).toEqual(mockResponse.data);
    });

    it('should register successfully', async () => {
      const mockResponse = {
        data: { id: 1, username: 'newuser' }
      };
      
      axios.post.mockResolvedValue(mockResponse);
      
      const userData = {
        username: 'newuser',
        email: 'test@example.com',
        password: 'password'
      };
      
      const result = await authService.register(userData);
      
      expect(axios.post).toHaveBeenCalledWith('/auth/register', userData);
      expect(result.data).toEqual(mockResponse.data);
    });

    it('should refresh token successfully', async () => {
      const mockResponse = {
        data: {
          accessToken: 'new-token',
          refreshToken: 'new-refresh-token'
        }
      };
      
      axios.post.mockResolvedValue(mockResponse);
      
      const refreshToken = 'refresh-token';
      const result = await authService.refresh(refreshToken);
      
      expect(axios.post).toHaveBeenCalledWith('/auth/refresh', { refreshToken });
      expect(result.data).toEqual(mockResponse.data);
    });

    it('should handle login error', async () => {
      const mockError = {
        response: {
          status: 401,
          data: { message: 'Invalid credentials' }
        }
      };
      
      axios.post.mockRejectedValue(mockError);
      
      const credentials = { username: 'testuser', password: 'wrongpassword' };
      
      await expect(authService.login(credentials)).rejects.toThrow();
      expect(axios.post).toHaveBeenCalledWith('/auth/login', credentials);
    });
  });

  describe('QuestService', () => {
    it('should get quests successfully', async () => {
      const mockResponse = {
        data: [
          { id: 1, title: 'Quest 1', published: true },
          { id: 2, title: 'Quest 2', published: false }
        ]
      };
      
      axios.get.mockResolvedValue(mockResponse);
      
      const params = { page: 1, limit: 10 };
      const result = await questService.getQuests(params);
      
      expect(axios.get).toHaveBeenCalledWith('/quests', { params });
      expect(result.data).toEqual(mockResponse.data);
    });

    it('should get quest by id successfully', async () => {
      const mockResponse = {
        data: { id: 1, title: 'Test Quest', description: 'Test Description' }
      };
      
      axios.get.mockResolvedValue(mockResponse);
      
      const questId = 1;
      const result = await questService.getQuestById(questId);
      
      expect(axios.get).toHaveBeenCalledWith(`/quests/${questId}`);
      expect(result.data).toEqual(mockResponse.data);
    });

    it('should create quest successfully', async () => {
      const mockResponse = {
        data: { id: 1, title: 'New Quest', published: false }
      };
      
      axios.post.mockResolvedValue(mockResponse);
      
      const questData = {
        title: 'New Quest',
        description: 'New Description',
        difficulty: 'EASY',
        type: 'SOLO'
      };
      
      const result = await questService.createQuest(questData);
      
      expect(axios.post).toHaveBeenCalledWith('/quests', questData);
      expect(result.data).toEqual(mockResponse.data);
    });

    it('should update quest successfully', async () => {
      const mockResponse = {
        data: { id: 1, title: 'Updated Quest', published: true }
      };
      
      axios.put.mockResolvedValue(mockResponse);
      
      const questId = 1;
      const questData = {
        title: 'Updated Quest',
        published: true
      };
      
      const result = await questService.updateQuest(questId, questData);
      
      expect(axios.put).toHaveBeenCalledWith(`/quests/${questId}`, questData);
      expect(result.data).toEqual(mockResponse.data);
    });

    it('should delete quest successfully', async () => {
      axios.delete.mockResolvedValue({ status: 204 });
      
      const questId = 1;
      await questService.deleteQuest(questId);
      
      expect(axios.delete).toHaveBeenCalledWith(`/quests/${questId}`);
    });
  });

  describe('GameService', () => {
    it('should get current level successfully', async () => {
      const mockResponse = {
        data: {
          level: { id: 1, title: 'Level 1', orderIndex: 1 },
          progress: { sectorsClosed: 0, startedAt: '2024-01-01T00:00:00Z' }
        }
      };
      
      axios.get.mockResolvedValue(mockResponse);
      
      const sessionId = 'session-123';
      const result = await gameService.getCurrentLevel(sessionId);
      
      expect(axios.get).toHaveBeenCalledWith(`/game/sessions/${sessionId}/current`);
      expect(result.data).toEqual(mockResponse.data);
    });

    it('should submit code successfully', async () => {
      const mockResponse = {
        data: { result: 'ACCEPTED_NORMAL', message: 'Code accepted' }
      };
      
      axios.post.mockResolvedValue(mockResponse);
      
      const sessionId = 'session-123';
      const codeData = {
        rawCode: 'TEST123',
        userId: 1
      };
      
      const result = await gameService.submitCode(sessionId, codeData);
      
      expect(axios.post).toHaveBeenCalledWith(`/game/sessions/${sessionId}/code`, codeData);
      expect(result.data).toEqual(mockResponse.data);
    });

    it('should auto pass level successfully', async () => {
      const mockResponse = {
        data: {
          level: { id: 2, title: 'Level 2', orderIndex: 2 },
          progress: { sectorsClosed: 5, startedAt: '2024-01-01T00:00:00Z' }
        }
      };
      
      axios.post.mockResolvedValue(mockResponse);
      
      const sessionId = 'session-123';
      const result = await gameService.autoPass(sessionId);
      
      expect(axios.post).toHaveBeenCalledWith(`/game/sessions/${sessionId}/auto-pass`);
      expect(result.data).toEqual(mockResponse.data);
    });
  });

  describe('TeamService', () => {
    it('should get teams successfully', async () => {
      const mockResponse = {
        data: [
          { id: 1, name: 'Team 1', memberCount: 3 },
          { id: 2, name: 'Team 2', memberCount: 5 }
        ]
      };
      
      axios.get.mockResolvedValue(mockResponse);
      
      const params = { page: 1, limit: 10 };
      const result = await teamService.getTeams(params);
      
      expect(axios.get).toHaveBeenCalledWith('/teams', { params });
      expect(result.data).toEqual(mockResponse.data);
    });

    it('should create team successfully', async () => {
      const mockResponse = {
        data: { id: 1, name: 'New Team', description: 'New team description' }
      };
      
      axios.post.mockResolvedValue(mockResponse);
      
      const teamData = {
        name: 'New Team',
        description: 'New team description'
      };
      
      const result = await teamService.createTeam(teamData);
      
      expect(axios.post).toHaveBeenCalledWith('/teams', teamData);
      expect(result.data).toEqual(mockResponse.data);
    });

    it('should join team successfully', async () => {
      const mockResponse = {
        data: { message: 'Successfully joined team' }
      };
      
      axios.post.mockResolvedValue(mockResponse);
      
      const teamId = 1;
      const inviteCode = 'INVITE123';
      
      const result = await teamService.joinTeam(teamId, inviteCode);
      
      expect(axios.post).toHaveBeenCalledWith(`/teams/${teamId}/join`, { inviteCode });
      expect(result.data).toEqual(mockResponse.data);
    });

    it('should get invitation count successfully', async () => {
      const mockResponse = {
        data: 3
      };
      
      axios.get.mockResolvedValue(mockResponse);
      
      const result = await teamService.getInvitationCount();
      
      expect(axios.get).toHaveBeenCalledWith('/teams/invitations/count');
      expect(result.data).toEqual(3);
    });
  });

  describe('FileService', () => {
    it('should upload file successfully', async () => {
      const mockResponse = {
        data: {
          id: 'file-123',
          name: 'test.jpg',
          size: 1024,
          type: 'image/jpeg',
          url: 'http://example.com/files/test.jpg'
        }
      };
      
      axios.post.mockResolvedValue(mockResponse);
      
      const file = new File(['test'], 'test.jpg', { type: 'image/jpeg' });
      const onUploadProgress = vi.fn();
      
      const result = await fileService.uploadFile(file, onUploadProgress);
      
      expect(axios.post).toHaveBeenCalledWith(
        '/files/upload',
        expect.any(FormData),
        {
          headers: { 'Content-Type': 'multipart/form-data' },
          onUploadProgress
        }
      );
      expect(result.data).toEqual(mockResponse.data);
    });

    it('should upload multiple files successfully', async () => {
      const mockResponse = {
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
      
      axios.post.mockResolvedValue(mockResponse);
      
      const files = [
        new File(['test1'], 'test1.jpg', { type: 'image/jpeg' }),
        new File(['test2'], 'test2.jpg', { type: 'image/jpeg' })
      ];
      const onUploadProgress = vi.fn();
      
      const result = await fileService.uploadMultipleFiles(files, onUploadProgress);
      
      expect(axios.post).toHaveBeenCalledWith(
        '/files/batch-upload',
        expect.any(FormData),
        {
          headers: { 'Content-Type': 'multipart/form-data' },
          onUploadProgress
        }
      );
      expect(result.data).toEqual(mockResponse.data);
    });

    it('should delete file successfully', async () => {
      axios.delete.mockResolvedValue({ status: 204 });
      
      const fileId = 'file-123';
      await fileService.deleteFile(fileId);
      
      expect(axios.delete).toHaveBeenCalledWith(`/files/${fileId}`);
    });

    it('should get file info successfully', async () => {
      const mockResponse = {
        data: {
          id: 'file-123',
          name: 'test.jpg',
          size: 1024,
          type: 'image/jpeg',
          uploadedAt: '2024-01-01T00:00:00Z'
        }
      };
      
      axios.get.mockResolvedValue(mockResponse);
      
      const fileId = 'file-123';
      const result = await fileService.getFileInfo(fileId);
      
      expect(axios.get).toHaveBeenCalledWith(`/files/${fileId}/info`);
      expect(result.data).toEqual(mockResponse.data);
    });

    it('should get storage stats successfully', async () => {
      const mockResponse = {
        data: {
          totalFiles: 100,
          totalSize: 1048576,
          usedSpace: 524288,
          availableSpace: 524288
        }
      };
      
      axios.get.mockResolvedValue(mockResponse);
      
      const result = await fileService.getStorageStats();
      
      expect(axios.get).toHaveBeenCalledWith('/files/stats');
      expect(result.data).toEqual(mockResponse.data);
    });
  });
});