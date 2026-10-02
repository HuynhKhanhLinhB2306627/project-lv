<template>
  <footer class="app-footer bg-theme-card border-top border-theme py-3 px-4">
    <div class="d-flex flex-column flex-md-row justify-content-between align-items-center gap-2">
      <div class="small text-theme-muted order-2 order-md-1">
        © 2026 <strong>HealthRecord</strong>. All rights reserved.
      </div>
      <div class="d-flex align-items-center gap-4 order-1 order-md-2">
        <a href="#" class="text-decoration-none small text-theme-muted hover-text-primary">Hướng dẫn</a>
        <a href="#" class="text-decoration-none small text-theme-muted hover-text-primary">Chính sách bảo mật</a>
        <a href="#" class="text-decoration-none small text-theme-muted hover-text-primary">Hỗ trợ</a>
        <div class="vr bg-theme-muted opacity-25 d-none d-md-block" style="height: 15px;"></div>
        <div class="d-flex gap-3 fs-5">
          <a href="#" class="text-theme-muted hover-text-primary"><i class="bi bi-facebook"></i></a>
          <a href="#" class="text-theme-muted hover-text-primary"><i class="bi bi-github"></i></a>
        </div>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.app-footer {
  width: 100%;
  z-index: 10;
  background-color: var(--bg-card) !important;
}
.hover-text-primary:hover {
  color: var(--primary-color) !important;
}
</style>
