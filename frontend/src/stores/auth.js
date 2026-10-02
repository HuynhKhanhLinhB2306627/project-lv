import { defineStore } from 'pinia';
import axios from 'axios';

export const useAuthStore = defineStore('auth', {
  state: () => {
    let savedUser = null;
    try {
      const userStr = localStorage.getItem('user');
      savedUser = userStr ? JSON.parse(userStr) : null;
    } catch (e) {
      console.error("[AuthStore] Dữ liệu user trong localStorage bị hỏng, đang xóa...", e);
      localStorage.removeItem('user');
    }

    return {
      user: savedUser,
      accessToken: localStorage.getItem('accessToken') || null,
      loading: false,
      error: null,
    };
  },
  getters: {
    isAuthenticated: (state) => !!state.accessToken,
    isAdmin: (state) => state.user?.role === 'ADMIN',
  },
  actions: {
    async login(username, password) {
      this.loading = true;
      this.error = null;
      try {
        
        const response = await axios.post('/api/auth/login', { username, password }, { withCredentials: true });
        
        const { accessToken, user } = response.data;
        
        this.accessToken = accessToken;
        this.user = user;
        
        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('user', JSON.stringify(user));
        
        return { success: true };
      } catch (err) {
        console.error('[AuthStore] Lỗi đăng nhập:', err.response || err);
        this.error = err.response?.data?.message || 'Đăng nhập thất bại. Kiểm tra lại kết nối hoặc tài khoản.';
        return { success: false, message: this.error };
      } finally {
        this.loading = false;
      }
    },
    
    async register(userData) {
      this.loading = true;
      this.error = null;
      try {
        await axios.post('/api/auth/register', userData);
        return { success: true };
      } catch (err) {
        this.error = err.response?.data?.message || 'Đăng ký thất bại';
        return { success: false, message: this.error };
      } finally {
        this.loading = false;
      }
    },
    
    async refreshAccessToken() {
      try {
        
        const response = await axios.post('/api/auth/refresh', {}, { withCredentials: true });
        
        const { accessToken } = response.data;
        this.accessToken = accessToken;
        localStorage.setItem('accessToken', accessToken);
        return accessToken;
      } catch (err) {
        console.error('Không thể làm mới token:', err);
        this.clearAuthData();
        return null;
      }
    },
    
    async fetchCurrentUser() {
      if (!this.accessToken) return null;
      try {
        const response = await axios.get('/api/auth/me');
        this.user = response.data;
        localStorage.setItem('user', JSON.stringify(this.user));
        return this.user;
      } catch (err) {
        console.error('Không thể lấy thông tin người dùng:', err);
        if (err.response?.status === 401) {
          this.clearAuthData();
        }
        return null;
      }
    },

    updateUser(userData) {
      if (this.user) {
        this.user = { ...this.user, ...userData };
        localStorage.setItem('user', JSON.stringify(this.user));
      }
    },
    
    async logout() {
      try {
        
        await axios.post('/api/auth/logout', {}, { withCredentials: true });
      } catch (err) {
        console.error('Lỗi khi đăng xuất phía server:', err);
      } finally {
        this.clearAuthData();
        window.location.href = '/login';
      }
    },

    clearAuthData() {
      this.accessToken = null;
      this.user = null;
      localStorage.removeItem('accessToken');
      localStorage.removeItem('user');
      
    }
  }
});
