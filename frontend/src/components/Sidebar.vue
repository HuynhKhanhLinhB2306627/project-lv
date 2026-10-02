<template>
  <aside :class="['sidebar', { collapsed: isCollapsed }]">
    <div class="sidebar-header">
      <div class="logo">
        <i class="bi bi-journal-medical text-primary"></i>
        <span v-if="!isCollapsed">HealthRecord</span>
      </div>
      <button class="toggle-btn" @click="toggleSidebar">
        <i :class="isCollapsed ? 'bi bi-chevron-right' : 'bi bi-chevron-left'"></i>
      </button>
    </div>

    <nav class="sidebar-nav">
      <router-link to="/dashboard" class="nav-item" title="Dashboard">
        <i class="bi bi-grid-1x2-fill"></i>
        <span v-if="!isCollapsed">Tổng quan</span>
      </router-link>
      <router-link to="/appointments" class="nav-item" title="Lịch hẹn">
        <i class="bi bi-calendar-check-fill"></i>
        <span v-if="!isCollapsed">Lịch hẹn khám</span>
      </router-link>
      <router-link to="/medications" class="nav-item" title="Thuốc">
        <i class="bi bi-capsule-pill"></i>
        <span v-if="!isCollapsed">Lịch uống thuốc</span>
      </router-link>
      <router-link to="/chat" class="nav-item" title="Trò chuyện">
        <i class="bi bi-chat-dots-fill"></i>
        <span v-if="!isCollapsed">Tin nhắn</span>
      </router-link>
      <router-link to="/documents" class="nav-item" title="Tài liệu">
        <i class="bi bi-file-earmark-medical-fill"></i>
        <span v-if="!isCollapsed">Kho tài liệu</span>
      </router-link>
      <router-link to="/insurance" class="nav-item" title="Bảo hiểm">
        <i class="bi bi-card-checklist"></i>
        <span v-if="!isCollapsed">Bảo hiểm y tế</span>
      </router-link>


      <router-link to="/forum" class="nav-item" title="Diễn đàn">
        <i class="bi bi-people-fill"></i>
        <span v-if="!isCollapsed">Cộng đồng</span>
      </router-link>
      <router-link to="/profiles" class="nav-item" title="Hồ sơ">
        <i class="bi bi-person-vcard-fill"></i>
        <span v-if="!isCollapsed">Quản lý hồ sơ</span>
      </router-link>
      <router-link to="/admin" class="nav-item admin-link" title="Quản trị">
        <i class="bi bi-shield-lock-fill"></i>
        <span v-if="!isCollapsed">Quản trị viên</span>
      </router-link>
    </nav>

    <div class="sidebar-footer">
      <button class="nav-item logout-btn" @click="handleLogout" title="Đăng xuất">
        <i class="bi bi-box-arrow-right"></i>
        <span v-if="!isCollapsed">Đăng xuất</span>
      </button>
    </div>
  </aside>
</template>

<script setup>
import { ref } from 'vue';
import { toast } from '@/utils/toast';

const isCollapsed = ref(false);

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value;
};

const handleLogout = async () => {
  if (await toast.confirm('Xác nhận đăng xuất', 'Bạn chắc chắn muốn đăng xuất?')) {
    window.location.href = '/logout';
  }
};
</script>

<style scoped>
.sidebar {
  width: 260px;
  background-color: #1e293b;
  color: white;
  height: 100vh;
  display: flex;
  flex-direction: column;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: fixed;
  left: 0;
  top: 0;
  z-index: 1000;
  box-shadow: 4px 0 10px rgba(0, 0, 0, 0.1);
}

.sidebar.collapsed {
  width: 80px;
}

.sidebar-header {
  padding: 1.5rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.logo {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 1.25rem;
  font-weight: 700;
  color: white;
  white-space: nowrap;
}

.logo i {
  font-size: 1.5rem;
}

.toggle-btn {
  background: rgba(255, 255, 255, 0.05);
  border: none;
  color: #94a3b8;
  cursor: pointer;
  padding: 5px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  transition: all 0.2s;
}

.toggle-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: white;
}

.sidebar-nav {
  padding: 1rem 0.75rem;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 0.75rem 1rem;
  color: #94a3b8;
  text-decoration: none;
  gap: 1rem;
  transition: all 0.2s;
  cursor: pointer;
  border: none;
  background: none;
  width: 100%;
  text-align: left;
  border-radius: 8px;
}

.nav-item:hover {
  background-color: rgba(255, 255, 255, 0.05);
  color: white;
}

.nav-item.router-link-active {
  background-color: #0d6efd;
  color: white;
  box-shadow: 0 4px 12px rgba(13, 110, 253, 0.2);
}

.nav-item i {
  font-size: 1.25rem;
  min-width: 24px;
  text-align: center;
}

.nav-item span {
  font-weight: 500;
  white-space: nowrap;
}

.sidebar-footer {
  border-top: 1px solid rgba(255, 255, 255, 0.05);
  padding: 1rem 0.75rem;
}

.logout-btn {
  color: #f87171;
}

.logout-btn:hover {
  background-color: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.admin-link {
  color: #fbbf24;
}
</style>
