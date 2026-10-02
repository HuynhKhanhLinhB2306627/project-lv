<template>
  <header class="app-header bg-theme-card shadow-sm px-4 d-flex justify-content-between align-items-center sticky-top">
    
    <div class="d-flex align-items-center gap-3">
      <button class="btn btn-light d-lg-none rounded-circle bg-theme-light text-theme-main border-theme" @click="$emit('toggle-mobile-menu')">
        <i class="bi bi-list fs-5"></i>
      </button>
      <h5 class="mb-0 fw-bold text-theme-main d-none d-md-block">{{ pageTitle }}</h5>
    </div>
    
    <div class="d-flex align-items-center gap-2">
      
      <button class="btn btn-light rounded-circle hover-bg shadow-sm d-flex align-items-center justify-content-center transition-all border-theme bg-theme-light" 
              style="width: 40px; height: 40px;" @click="toggleTheme">
        <i class="bi" :class="isDarkMode ? 'bi-moon-stars-fill text-warning' : 'bi-sun-fill text-warning'"></i>
      </button>

      
      <div class="dropdown">
        <div class="notification-bell p-2 rounded-circle hover-bg cursor-pointer position-relative" 
             id="notiDropdown" data-bs-toggle="dropdown" aria-expanded="false">
          <i class="bi bi-bell fs-5 text-theme-muted"></i>
          <span v-if="unreadCount > 0" class="badge-count shadow-sm animate-bounce">
            {{ unreadCount > 9 ? '9+' : unreadCount }}
          </span>
        </div>
        
        <ul class="dropdown-menu dropdown-menu-end shadow-lg border-theme mt-2 p-0 overflow-hidden bg-theme-card" 
            aria-labelledby="notiDropdown" style="width: 320px; max-height: 400px;">
          <li class="p-3 border-bottom border-theme d-flex justify-content-between align-items-center bg-theme-light">
            <span class="fw-bold small text-uppercase tracking-wider text-theme-muted opacity-75">Thông báo</span>
            <button v-if="notifications.length > 0" 
                    class="btn btn-link btn-sm p-0 text-decoration-none small fw-bold text-primary" 
                    @click="markAllAsRead">Đánh dấu đã đọc</button>
          </li>
          
          <div class="notification-list custom-scrollbar overflow-auto bg-theme-card" style="max-height: 330px;">
            <li v-if="notifications.length === 0" class="p-4 text-center text-theme-muted small">
              <i class="bi bi-bell-slash fs-3 d-block mb-2 opacity-25"></i>
              Chưa có thông báo nào
            </li>
            <li v-for="n in notifications" :key="n.id" 
                class="notification-item border-bottom border-theme position-relative"
                :class="{ 'unread-bg': !n.read }">
              <a :href="n.link" class="dropdown-item p-3 d-flex gap-3 text-theme-main" @click.prevent="handleNotiClick(n)">
                <div class="noti-icon bg-primary bg-opacity-10 text-primary rounded-circle d-flex align-items-center justify-content-center flex-shrink-0" style="width: 35px; height: 35px;">
                  <i class="bi bi-chat-left-dots" v-if="n.link.includes('chat')"></i>
                  <i class="bi bi-people" v-else-if="n.link.includes('forum')"></i>
                  <i class="bi bi-person-badge" v-else-if="n.link.includes('profile')"></i>
                  <i class="bi bi-bell" v-else></i>
                </div>
                <div class="flex-grow-1 overflow-hidden">
                  <p class="mb-1 small text-theme-main fw-medium text-wrap" style="line-height: 1.3;">{{ n.message }}</p>
                  <div class="d-flex justify-content-between align-items-center mt-1">
                    <small class="text-theme-muted" style="font-size: 0.7rem;">{{ formatTime(n.time) }}</small>
                    <span v-if="!n.read" class="unread-dot"></span>
                  </div>
                </div>
              </a>
            </li>
          </div>
        </ul>
      </div>

      
      <div class="dropdown">
        <div class="user-profile-header d-flex align-items-center gap-2 ps-2 border-start border-theme text-decoration-none text-theme-main cursor-pointer rounded-pill px-2 py-1 hover-bg transition-all" 
             id="userDropdown" data-bs-toggle="dropdown" aria-expanded="false" data-bs-offset="0,10">
          <img v-if="avatar" :src="avatarUrl" class="rounded-circle border border-theme shadow-sm object-fit-cover flex-shrink-0" width="36" height="36">
          <img v-else :src="`https://ui-avatars.com/api/?name=${encodeURIComponent(userName)}&background=0d6efd&color=fff&bold=true`" class="rounded-circle border border-theme shadow-sm flex-shrink-0" width="36" height="36">
          <span class="fw-bold small d-none d-sm-block">{{ userName }}</span>
          <i class="bi bi-chevron-down small text-theme-muted flex-shrink-0"></i>
        </div>
        <ul class="dropdown-menu dropdown-menu-end shadow-lg border-theme p-2 rounded-4 bg-theme-card" aria-labelledby="userDropdown" style="min-width: 220px;">
          <li class="px-3 py-3 border-bottom border-theme mb-2 bg-theme-light rounded-top-4">
            <div class="d-flex align-items-center gap-2">
              <img v-if="avatar" :src="avatarUrl" class="rounded-circle border border-theme shadow-sm object-fit-cover flex-shrink-0" width="40" height="40">
              <img v-else :src="`https://ui-avatars.com/api/?name=${encodeURIComponent(userName)}&background=0d6efd&color=fff&bold=true`" class="rounded-circle border border-theme shadow-sm flex-shrink-0" width="40" height="40">
              <div class="overflow-hidden min-width-0">
                <span class="fw-bold d-block text-truncate text-theme-main">{{ userName }}</span>
                <span class="extra-small text-theme-muted d-block text-truncate">{{ userRole }}</span>
              </div>
            </div>
          </li>
          <li>
            <router-link to="/home" class="dropdown-item rounded-3 py-2 d-flex align-items-center gap-2 text-theme-main">
              <i class="bi bi-house-heart-fill text-info fs-5"></i> <span class="fw-medium">Trang chủ</span>
            </router-link>
          </li>
          <li>
            <router-link to="/profiles/me" class="dropdown-item rounded-3 py-2 d-flex align-items-center gap-2 text-theme-main">
              <i class="bi bi-person-circle text-primary fs-5"></i> <span class="fw-medium">Hồ sơ của tôi</span>
            </router-link>
          </li>
          <li>
            <router-link to="/body-metrics" class="dropdown-item rounded-3 py-2 d-flex align-items-center gap-2 text-theme-main">
              <i class="bi bi-activity text-success fs-5"></i> <span class="fw-medium">Chỉ số sức khỏe</span>
            </router-link>
          </li>
          <li><hr class="dropdown-divider border-theme my-2"></li>
          <li>
            <button class="dropdown-item rounded-3 py-2 text-danger d-flex align-items-center gap-2 fw-medium" @click="handleLogout">
              <i class="bi bi-box-arrow-left fs-5"></i> Đăng xuất
            </button>
          </li>
        </ul>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import axios from 'axios';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';
import { useAuthStore } from '../../stores/auth';
import { toast } from '@/utils/toast';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();

defineEmits(['toggle-mobile-menu']);

const userName = computed(() => authStore.user?.fullName || 'Bạn');
const userRole = computed(() => authStore.user?.role || 'USER');
const avatar = computed(() => authStore.user?.avatar || null);
const avatarUrl = computed(() => {
  const value = avatar.value;
  if (!value) return null;
  if (value.startsWith('http')) return value;
  
  const baseUrl = axios.defaults.baseURL || '';
  if (value.startsWith('/uploads/')) return baseUrl + value;
  if (value.startsWith('uploads/')) return baseUrl + '/' + value;
  return baseUrl + '/uploads/' + value;
});
const currentUserId = computed(() => authStore.user?.id || null);

const unreadCount = ref(0);
const notifications = ref([]);
let stompClient = null;


const isDarkMode = ref(false);

const applyTheme = (dark) => {
  if (dark) {
    document.documentElement.setAttribute('data-bs-theme', 'dark');
    localStorage.setItem('theme', 'dark');
  } else {
    document.documentElement.removeAttribute('data-bs-theme');
    localStorage.setItem('theme', 'light');
  }
};

const toggleTheme = () => {
  isDarkMode.value = !isDarkMode.value;
  applyTheme(isDarkMode.value);
};

const initTheme = () => {
  const savedTheme = localStorage.getItem('theme');
  if (savedTheme) {
    isDarkMode.value = savedTheme === 'dark';
  } else {
    isDarkMode.value = window.matchMedia('(prefers-color-scheme: dark)').matches;
  }
  applyTheme(isDarkMode.value);
};

const pageTitle = computed(() => {
  return route.meta.title || 'Sức khỏe Cộng đồng';
});

const fetchNotifications = async () => {
  if (!authStore.isAuthenticated) return;
  try {
    const res = await axios.get('/api/notifications');
    notifications.value = res.data;
    unreadCount.value = notifications.value.filter(n => !n.read).length;
  } catch (err) {
    console.error("Lỗi fetch noti:", err);
  }
};

const connectWebSocket = () => {
  if (!currentUserId.value) return;
  
  
  const baseUrl = axios.defaults.baseURL || '';
  const socket = new SockJS(baseUrl + '/ws');
  stompClient = Stomp.over(socket);
  stompClient.debug = null; 

  stompClient.connect({}, () => {
    stompClient.subscribe(`/topic/notifications/${currentUserId.value}`, (msg) => {
      const payload = JSON.parse(msg.body);
      notifications.value.unshift({
        id: payload.id,
        message: payload.message,
        link: payload.link,
        read: false,
        time: payload.time || new Date().toISOString()
      });
      if (payload.unreadCount !== undefined) {
        unreadCount.value = payload.unreadCount;
      } else {
        unreadCount.value++;
      }
    });
  }, (err) => {
    console.error("WS Error:", err);
    if (authStore.isAuthenticated) {
      setTimeout(connectWebSocket, 5000);
    }
  });
};

const markAllAsRead = async () => {
  try {
    await axios.post('/api/notifications/mark-all-read');
    notifications.value.forEach(n => n.read = true);
    unreadCount.value = 0;
  } catch (err) { console.error(err); }
};

const handleNotiClick = async (noti) => {
  if (!noti.read) {
    try {
      await axios.post(`/api/notifications/mark-read/${noti.id}`);
      noti.read = true;
      unreadCount.value = Math.max(0, unreadCount.value - 1);
    } catch (err) { console.error(err); }
  }
  router.push(noti.link);
};

const formatTime = (timeStr) => {
  if (!timeStr) return 'Vừa xong';
  const d = new Date(timeStr);
  const now = new Date();
  const diff = Math.floor((now - d) / 1000);
  
  if (diff < 60) return 'Vừa xong';
  if (diff < 3600) return `${Math.floor(diff/60)} phút trước`;
  if (diff < 86400) return `${Math.floor(diff/3600)} giờ trước`;
  return d.toLocaleDateString('vi-VN');
};

const handleLogout = async () => {
  if (await toast.confirm('Xác nhận đăng xuất', 'Bạn muốn đăng xuất?')) {
    authStore.logout();
  }
};


watch(currentUserId, (newId) => {
  if (newId) {
    fetchNotifications();
    connectWebSocket();
  } else if (stompClient) {
    stompClient.disconnect();
  }
});

onMounted(() => {
  initTheme();
  if (authStore.isAuthenticated) {
    authStore.fetchCurrentUser(); 
    fetchNotifications();
    connectWebSocket();
  }
});

onUnmounted(() => {
  if (stompClient) stompClient.disconnect();
});
</script>

<style scoped>

.app-header {
  height: var(--header-height);
  z-index: 1040;
  background-color: var(--bg-card) !important;
  border-bottom: 1px solid var(--border-color);
}

.badge-count { 
  position: absolute; 
  top: 0; 
  right: 0; 
  background: var(--danger-color); 
  color: white; 
  font-size: 0.65rem; 
  font-weight: bold;
  padding: 1px 5px; 
  border-radius: 20px; 
  border: 2px solid white;
}

.unread-bg { background-color: rgba(13, 110, 253, 0.05); }
[data-bs-theme="dark"] .unread-bg { background-color: rgba(59, 130, 246, 0.1); }

.unread-dot { width: 8px; height: 8px; background-color: var(--primary-color); border-radius: 50%; }

.animate-bounce {
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% {transform: translateY(0);}
  40% {transform: translateY(-3px);}
  60% {transform: translateY(-2px);}
}

.dropdown-menu { 
  display: block !important;
  visibility: hidden;
  opacity: 0;
  transform: translateY(15px);
  transition: all 0.25s cubic-bezier(0.165, 0.84, 0.44, 1);
  pointer-events: none;
  border: 1px solid var(--border-color);
}

.dropdown-menu.show {
  visibility: visible;
  opacity: 1;
  transform: translateY(0);
  pointer-events: auto;
}

.transition-all { transition: all 0.2s ease-in-out; }

.notification-bell:hover { background-color: var(--hover-bg); }
.user-profile-header:hover { background-color: var(--hover-bg); }

.dropdown-item:hover {
  background-color: var(--hover-bg);
}

@media (max-width: 767.98px) {
  .app-header {
    padding-left: 0.75rem !important;
    padding-right: 0.75rem !important;
    height: 56px;
  }

  .app-header .dropdown-menu {
    width: min(92vw, 320px) !important;
    max-height: 70vh !important;
  }

  .user-profile-header {
    padding-left: 0 !important;
    border-left: none !important;
    gap: 0.35rem !important;
  }

  .notification-bell {
    padding: 0.4rem !important;
  }
}
</style>
