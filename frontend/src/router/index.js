import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layouts/MainLayout.vue'
import Home from '../views/Home.vue'
import Dashboard from '../views/Dashboard.vue'
import Appointments from '../views/Appointments.vue'

import Profile from '../views/Profile.vue'
import Forum from '../views/Forum.vue'
import Chat from '../views/Chat.vue'
import Documents from '../views/Documents.vue'
import AdminDashboard from '../views/admin/AdminDashboard.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'


const routes = [
  
  
  
  {
    path: '/',
    name: 'Home',
    component: Home,
    meta: { title: 'Trang chủ - Health Record System', public: true }
  },
  {
    
    path: '/home',
    name: 'HomeAlways',
    component: Home,
    meta: { title: 'Trang chủ - Health Record System', public: true, isHomePage: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { title: 'Đăng nhập', public: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: { title: 'Đăng ký tài khoản', public: true }
  },
  
  
  
  
  {
    path: '/',
    component: MainLayout,
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: Dashboard,
        meta: { title: 'Tổng quan sức khỏe' }
      },
      {
        path: 'appointments',
        name: 'Appointments',
        component: Appointments,
        meta: { title: 'Lịch hẹn khám' }
      },
      {
        path: 'medications',
        name: 'Medications',
        component: () => import('../views/Medications.vue'),
        meta: { title: 'Lịch uống thuốc' }
      },

      {
        path: 'forum',
        name: 'Forum',
        component: Forum,
        meta: { title: 'Cộng đồng sức khỏe' }
      },
      {
        path: 'chat',
        name: 'Chat',
        component: Chat,
        meta: { title: 'Trò chuyện & Hỗ trợ' }
      },
      {
        path: 'documents',
        name: 'Documents',
        component: Documents,
        meta: { title: 'Kho tài liệu y tế' }
      },
      {
        path: 'vaccinations',
        name: 'Vaccinations',
        component: () => import('../views/Vaccinations.vue'),
        meta: { title: 'Lịch sử tiêm chủng' }
      },
      {
        path: 'insurance',
        name: 'Insurance',
        component: () => import('../views/Insurance.vue'),
        meta: { title: 'Quản lý thẻ BHYT' }
      },
      {
        path: 'body-metrics',
        name: 'BodyMetrics',
        component: () => import('../views/BodyMetrics.vue'),
        meta: { title: 'Theo dõi chỉ số cơ thể' }
      },


      {
        path: 'profiles',
        name: 'Profiles',
        component: () => import('../views/Profiles.vue'),
        meta: { title: 'Quản lý hồ sơ sức khỏe' }
      },
      {
        path: 'profiles/:id',
        name: 'ProfileDetail',
        component: () => import('../views/Profile.vue'),
        meta: { title: 'Chi tiết hồ sơ' }
      },
      {
        path: 'admin',
        name: 'Admin',
        component: AdminDashboard,
        meta: { title: 'Quản trị hệ thống', requiresAdmin: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})



export default router
