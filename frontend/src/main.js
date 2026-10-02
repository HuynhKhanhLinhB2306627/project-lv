import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import axios from 'axios'


import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap-icons/font/bootstrap-icons.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'

import './assets/css/main.css'
import './style.css'


axios.defaults.timeout = 60000


const API_URL = (window.location.hostname === 'linhdz.id.vn' || window.location.hostname === 'api.linhdz.id.vn')
  ? 'https://api.linhdz.id.vn'
  : (window.location.protocol + '//' + window.location.hostname + ':8080');

axios.defaults.baseURL = API_URL;


axios.defaults.withCredentials = true;

console.log("HealthRecord: API Base URL is set to:", axios.defaults.baseURL);


window.onerror = function(msg, url, line, col, error) {
  console.error("LỖI HỆ THỐNG VUE:", msg, error);
};




const app = createApp(App)
const pinia = createPinia()


app.use(pinia)


import('./router').then(({ default: router }) => {
  
  setupRouterGuard(router)

  app.use(router)
  app.mount('#app')
  console.log("HealthRecord: Vue App Started!");

  
  setupAxiosInterceptor()
}).catch(err => {
  console.error("HealthRecord: KHÔNG THỂ TẢI ROUTER (Lỗi nghiêm trọng):", err);
  
  
});




import { useAuthStore } from './stores/auth'

function setupRouterGuard(router) {
  router.beforeEach(async (to, from, next) => {
    const authStore = useAuthStore();
    
    
    document.title = to.meta.title ? `${to.meta.title} - HealthRecord` : 'HealthRecord';

    const isPublicPage = to.meta.public;
    const isAuthenticated = authStore.isAuthenticated;

    
    if ((to.path === '/login' || to.path === '/register') && isAuthenticated) {
      return next('/dashboard');
    }

    
    if (!isPublicPage && !isAuthenticated) {
      return next('/login');
    }

    
    if (to.meta.requiresAdmin && !authStore.isAdmin) {
      console.warn("Truy cập bị từ chối: Cần quyền Admin");
      return next('/dashboard');
    }

    
    if (isAuthenticated && !authStore.user) {
      try {
        const user = await authStore.fetchCurrentUser();
        if (!user) {
          console.warn("Phiên đăng nhập không hợp lệ, yêu cầu đăng nhập lại");
          return next('/login');
        }
      } catch (err) {
        console.error("Lỗi khi lấy thông tin user:", err);
        return next('/login');
      }
    }

    next();
  });
}




function setupAxiosInterceptor() {
  let isRefreshing = false;
  let failedQueue = [];

  const processQueue = (error, token = null) => {
    failedQueue.forEach(prom => {
      if (error) {
        prom.reject(error);
      } else {
        prom.resolve(token);
      }
    });
    failedQueue = [];
  };

  
  axios.interceptors.request.use(config => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  }, error => {
    return Promise.reject(error);
  });

  
  axios.interceptors.response.use(
    (response) => response,
    async (error) => {
      const originalRequest = error.config;

      
      if (error.response && error.response.status === 401 && !originalRequest._retry && !originalRequest.url.includes('/api/auth/login') && !originalRequest.url.includes('/api/auth/refresh')) {
        
        if (isRefreshing) {
          return new Promise((resolve, reject) => {
            failedQueue.push({ resolve, reject });
          }).then(token => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            return axios(originalRequest);
          }).catch(err => {
            return Promise.reject(err);
          });
        }

        originalRequest._retry = true;
        isRefreshing = true;

        
        const { useAuthStore } = await import('./stores/auth');
        const authStore = useAuthStore();
        
        try {
          const newToken = await authStore.refreshAccessToken();
          if (newToken) {
            processQueue(null, newToken);
            originalRequest.headers.Authorization = `Bearer ${newToken}`;
            return axios(originalRequest);
          } else {
            processQueue(new Error('Failed to refresh token'), null);
            authStore.clearAuthData();
            window.location.href = '/login';
            return Promise.reject(error);
          }
        } catch (refreshError) {
          processQueue(refreshError, null);
          authStore.clearAuthData();
          window.location.href = '/login';
          return Promise.reject(refreshError);
        } finally {
          isRefreshing = false;
        }
      }

      return Promise.reject(error);
    }
  );
}
