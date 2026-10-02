<template>
  <aside class="app-sidebar shadow-sm d-none d-lg-flex flex-column transition-all" :class="{ 'collapsed': isCollapsed }">
    
    <div class="logo-area px-4 py-3 d-flex align-items-center justify-content-between border-bottom">
      <router-link to="/dashboard" class="d-flex align-items-center gap-2 text-decoration-none">
        <i class="bi bi-heart-pulse-fill text-primary fs-3 shadow-sm rounded-circle p-1 bg-primary bg-opacity-10"></i>
        <h4 class="mb-0 fw-bold tracking-tight" v-if="!isCollapsed">HealthRecord</h4>
      </router-link>
      <button class="btn btn-sm btn-light rounded-circle d-none d-xl-flex" @click="toggleSidebar">
        <i class="bi" :class="isCollapsed ? 'bi-list' : 'bi-chevron-left'"></i>
      </button>
    </div>
    
    
    <div class="sidebar-nav-container flex-grow-1 overflow-y-auto custom-scrollbar py-3 px-2">
      <nav class="nav flex-column gap-1">
        
        <router-link to="/home" class="nav-link-custom home-link" active-class="active" title="Về trang chủ">
          <i class="bi bi-house-heart-fill"></i> <span v-if="!isCollapsed">Trang chủ</span>
        </router-link>
        
        <div class="nav-label text-muted small fw-bold px-3 mb-2 mt-2" v-if="!isCollapsed">CÁ NHÂN</div>
        <router-link to="/dashboard" class="nav-link-custom" active-class="active" title="Tổng quan">
          <i class="bi bi-grid-1x2-fill"></i> <span v-if="!isCollapsed">Tổng quan</span>
        </router-link>
        <router-link to="/profiles" class="nav-link-custom" active-class="active" title="Hồ sơ sức khỏe">
          <i class="bi bi-person-vcard-fill"></i> <span v-if="!isCollapsed">Hồ sơ sức khỏe</span>
        </router-link>
        <router-link to="/body-metrics" class="nav-link-custom" active-class="active" title="Chỉ số cơ thể">
          <i class="bi bi-activity"></i> <span v-if="!isCollapsed">Chỉ số cơ thể</span>
        </router-link>
        
        <div class="nav-label text-muted small fw-bold px-3 mb-2 mt-3" v-if="!isCollapsed">Y TẾ & LỊCH TRÌNH</div>
        <router-link to="/appointments" class="nav-link-custom" active-class="active" title="Lịch hẹn khám">
          <i class="bi bi-calendar-check-fill"></i> <span v-if="!isCollapsed">Lịch hẹn khám</span>
        </router-link>
        <router-link to="/medications" class="nav-link-custom" active-class="active" title="Lịch uống thuốc">
          <i class="bi bi-capsule-pill"></i> <span v-if="!isCollapsed">Lịch uống thuốc</span>
        </router-link>
        <router-link to="/vaccinations" class="nav-link-custom" active-class="active" title="Sổ tiêm chủng">
          <i class="bi bi-shield-plus"></i> <span v-if="!isCollapsed">Sổ tiêm chủng</span>
        </router-link>

        <div class="nav-label text-muted small fw-bold px-3 mb-2 mt-3" v-if="!isCollapsed">KẾT NỐI & TIỆN ÍCH</div>
        <router-link to="/forum" class="nav-link-custom" active-class="active" title="Cộng đồng">
          <i class="bi bi-people-fill"></i> <span v-if="!isCollapsed">Cộng đồng</span>
        </router-link>
        <router-link to="/chat" class="nav-link-custom" active-class="active" title="Trò chuyện">
          <i class="bi bi-chat-dots-fill"></i> <span v-if="!isCollapsed">Trò chuyện</span>
        </router-link>

        <router-link to="/documents" class="nav-link-custom" active-class="active" title="Tài liệu">
          <i class="bi bi-file-earmark-medical-fill"></i> <span v-if="!isCollapsed">Kho tài liệu</span>
        </router-link>
        <router-link to="/insurance" class="nav-link-custom" active-class="active" title="Bảo hiểm">
          <i class="bi bi-card-checklist"></i> <span v-if="!isCollapsed">Bảo hiểm y tế</span>
        </router-link>

      </nav>
    </div>

    
    <div class="sidebar-footer p-3 border-top mt-auto">
      <div v-if="authStore.isAdmin" class="mb-2">
        <router-link to="/admin" class="nav-link-custom text-warning bg-warning bg-opacity-10 admin-link py-2" title="Quản trị viên">
          <i class="bi bi-shield-lock-fill"></i> <span v-if="!isCollapsed">Quản trị viên</span>
        </router-link>
      </div>
      <button class="logout-link-custom w-100" @click="handleLogout" title="Đăng xuất">
        <i class="bi bi-box-arrow-left"></i> <span v-if="!isCollapsed">Đăng xuất</span>
      </button>
    </div>
  </aside>
</template>

<script setup>
import { ref } from 'vue';
import { useAuthStore } from '../../stores/auth';
import { toast } from '@/utils/toast';

const authStore = useAuthStore();
const isCollapsed = ref(false);

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value;
};

const handleLogout = async () => {
  if (await toast.confirm('Xác nhận đăng xuất', 'Bạn chắc chắn muốn đăng xuất?')) {
    authStore.logout();
  }
};
</script>

<style scoped>
.app-sidebar {
  width: var(--sidebar-width);
  height: 100vh;
  z-index: 1050;
  flex-shrink: 0;
  border-right: 1px solid var(--border-color);
  background-color: var(--bg-card) !important;
}

.app-sidebar.collapsed {
  width: 80px;
}

.transition-all {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.sidebar-nav-container {
  overflow-y: auto;
  overflow-x: hidden;
}

.nav-label {
  font-size: 0.7rem;
  letter-spacing: 1px;
}

.nav-link-custom {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  border-radius: 10px;
  color: var(--secondary-color);
  text-decoration: none;
  font-weight: 500;
  transition: all 0.2s;
  white-space: nowrap;
  margin-bottom: 2px;
}

.nav-link-custom i {
  font-size: 1.25rem;
  min-width: 24px;
  text-align: center;
}

.nav-link-custom:hover {
  background-color: var(--hover-bg);
  color: var(--primary-color);
}

.nav-link-custom.active {
  background-color: rgba(13, 110, 253, 0.1);
  color: var(--primary-color);
  font-weight: 700;
  position: relative;
}

.nav-link-custom.active::before {
  content: "";
  position: absolute;
  left: 0;
  top: 20%;
  bottom: 20%;
  width: 4px;
  background-color: var(--primary-color);
  border-radius: 0 4px 4px 0;
}

.nav-link-custom.active i {
  color: var(--primary-color);
}

.admin-link.active {
  background-color: rgba(255, 193, 7, 0.1) !important;
  color: var(--warning-color) !important;
}

.admin-link.active::before {
  background-color: var(--warning-color) !important;
}

.logout-link-custom {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  border-radius: 10px;
  color: var(--danger-color);
  background: transparent;
  border: none;
  font-weight: 500;
  transition: all 0.2s;
  cursor: pointer;
  white-space: nowrap;
}

.logout-link-custom:hover {
  background-color: rgba(220, 53, 69, 0.1);
}

.logout-link-custom i {
  font-size: 1.25rem;
  min-width: 24px;
  text-align: center;
}

.app-sidebar.collapsed .nav-link-custom,
.app-sidebar.collapsed .logout-link-custom {
  justify-content: center;
  padding: 12px 0;
}
.app-sidebar.collapsed .nav-link-custom i,
.app-sidebar.collapsed .logout-link-custom i {
  margin: 0;
}

.sidebar-footer {
  background-color: var(--bg-card) !important;
  border-top-color: var(--border-color) !important;
}


.home-link {
  background: transparent;
  border: 1px solid transparent;
  margin-bottom: 8px !important;
}

.home-link:hover {
  background-color: var(--hover-bg);
  color: var(--primary-color);
}

.home-link i {
  color: var(--primary-color);
}

@media (max-width: 991.98px) {
  .app-sidebar {
    width: 260px;
  }

  .nav-link-custom,
  .logout-link-custom {
    padding: 12px 14px;
    font-size: 0.95rem;
  }

  .nav-link-custom i,
  .logout-link-custom i {
    font-size: 1.1rem;
  }

  .sidebar-nav-container {
    padding-left: 0.5rem !important;
    padding-right: 0.5rem !important;
  }
}
</style>
