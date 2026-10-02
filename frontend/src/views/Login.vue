<template>
  <div class="d-flex align-items-center justify-content-center vh-100 bg-theme-light animate-fade-in">
    <div class="card bg-theme-card shadow-lg border border-theme" style="width: 100%; max-width: 450px; border-radius: 1rem;">
      <div class="card-body p-5 bg-theme-card" style="border-radius: 1rem;">
        <div class="text-center mb-4">
          <div class="bg-primary bg-opacity-10 d-inline-block p-3 rounded-circle mb-3 shadow-sm">
            <i class="bi bi-journal-medical text-primary" style="font-size: 3rem; line-height: 1;"></i>
          </div>
          <h2 class="fw-bold mb-1 text-theme-main">Đăng nhập</h2>
          <p class="text-theme-muted">Hệ thống Sổ tay Sức khỏe</p>
        </div>
        
        
        <div v-if="authStore.error" class="alert alert-danger py-2 px-3 small d-flex align-items-center mb-3 border-0 shadow-sm">
          <i class="bi bi-exclamation-triangle-fill me-2 fs-5"></i> {{ authStore.error }}
        </div>

        
        <div v-if="logoutMsg" class="alert alert-success py-2 px-3 small d-flex align-items-center mb-3 border-0 shadow-sm">
          <i class="bi bi-check-circle-fill me-2 fs-5"></i> {{ logoutMsg }}
        </div>

        <form @submit.prevent="handleLogin">
          <div class="mb-3">
            <label class="form-label fw-bold small text-theme-muted">Email hoặc Tên đăng nhập</label>
            <div class="input-group shadow-sm rounded-3 overflow-hidden">
              <span class="input-group-text bg-theme-light border-theme border-end-0"><i class="bi bi-person text-theme-muted"></i></span>
              <input type="text" v-model="form.username" class="form-control form-control-lg bg-theme-light text-theme-main fs-6 border-theme border-start-0 shadow-none" placeholder="Nhập tài khoản" required>
            </div>
          </div>
          <div class="mb-4">
            <div class="d-flex justify-content-between">
              <label class="form-label fw-bold small text-theme-muted">Mật khẩu</label>
              <a href="#" class="small text-decoration-none fw-bold">Quên mật khẩu?</a>
            </div>
            <div class="input-group shadow-sm rounded-3 overflow-hidden">
              <span class="input-group-text bg-theme-light border-theme border-end-0"><i class="bi bi-lock text-theme-muted"></i></span>
              <input :type="showPassword ? 'text' : 'password'" v-model="form.password" class="form-control form-control-lg bg-theme-light text-theme-main fs-6 border-theme border-start-0 border-end-0 shadow-none" placeholder="Nhập mật khẩu" required>
              <span class="input-group-text bg-theme-light border-theme border-start-0 cursor-pointer" @click="showPassword = !showPassword">
                <i class="bi" :class="showPassword ? 'bi-eye-slash' : 'bi-eye'"></i>
              </span>
            </div>
          </div>
          <button type="submit" class="btn btn-primary btn-lg w-100 fw-bold fs-6 py-2 shadow-sm hover-lift" :disabled="authStore.loading">
            <span v-if="authStore.loading" class="spinner-border spinner-border-sm me-2"></span>
            {{ authStore.loading ? 'Đang xử lý...' : 'Đăng nhập ngay' }}
          </button>
        </form>
        
        <div class="text-center mt-4 pt-3 border-top border-theme">
          <p class="small text-theme-muted mb-0">
            Chưa có tài khoản? <router-link to="/register" class="text-decoration-none fw-bold text-primary">Đăng ký tại đây</router-link>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '../stores/auth';

const authStore = useAuthStore();
const router = useRouter();
const route = useRoute();
const logoutMsg = ref('');
const showPassword = ref(false);
const form = reactive({ username: '', password: '' });

onMounted(() => {
  if (route.query.logout !== undefined) {
    logoutMsg.value = 'Bạn đã đăng xuất thành công.';
  }
});

const handleLogin = async () => {
  const result = await authStore.login(form.username, form.password);
  if (result.success) {
    router.push('/dashboard');
  }
};
</script>

<style scoped>
.cursor-pointer { cursor: pointer; }
.form-control:focus { border-color: var(--primary-color); }
.hover-lift { transition: transform 0.2s; }
.hover-lift:hover { transform: translateY(-2px); }
.animate-fade-in { animation: fadeIn 0.5s ease-out; }
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
</style>
