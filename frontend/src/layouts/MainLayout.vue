<template>
  <div class="app-wrapper d-flex w-100 h-100 overflow-hidden">
    
    <div v-if="showMobileMenu" class="mobile-overlay d-lg-none" @click="showMobileMenu = false"></div>

    
    <AppSidebar :user-role="userRole" :class="{ 'mobile-show': showMobileMenu }" @click="showMobileMenu = false" />

    
    <div class="main-container flex-grow-1 d-flex flex-column overflow-hidden position-relative">
      <AppHeader @toggle-mobile-menu="showMobileMenu = !showMobileMenu" />
      
      
      <main class="content-area flex-grow-1 overflow-y-auto overflow-x-hidden p-0 position-relative">
        <router-view v-slot="{ Component, route }">
          <transition name="page-fade" mode="out-in">
            <div :key="route.path" class="route-wrapper flex-grow-1 d-flex flex-column">
              <component :is="Component" />
            </div>
          </transition>
        </router-view>
        <AppFooter />
      </main>
    </div>

    

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import AppSidebar from '../components/layout/AppSidebar.vue';
import AppHeader from '../components/layout/AppHeader.vue';
import AppFooter from '../components/layout/AppFooter.vue';


const showMobileMenu = ref(false);
const userRole = ref('USER');

const fetchUserInfo = async () => {
  try {
    const res = await axios.get('/api/auth/me');
    userRole.value = res.data.role;
  } catch (err) {
    console.error("Lỗi lấy thông tin user role:", err);
  }
};

onMounted(fetchUserInfo);
</script>

<style scoped>
.app-wrapper {
  
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  height: 100dvh;
}

.main-container {
  min-width: 0; 
}


.content-area {
  scroll-behavior: smooth;
  display: flex;
  flex-direction: column;
}


@media (max-width: 991.98px) {
  .app-wrapper {
    position: relative;
    min-height: 100dvh;
    height: auto;
  }

  .app-sidebar {
    position: fixed;
    top: 0;
    left: -260px;
    width: 260px;
    height: 100dvh;
    z-index: 1060;
    transition: transform 0.3s ease-in-out;
  }
  .app-sidebar.mobile-show {
    transform: translateX(260px);
    display: flex !important;
  }

  .content-area {
    min-height: calc(100dvh - var(--header-height));
  }
  .mobile-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    z-index: 1055;
    backdrop-filter: blur(2px);
  }
}


.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}
.page-fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}
.page-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
