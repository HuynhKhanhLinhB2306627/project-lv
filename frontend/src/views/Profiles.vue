<template>
  <div class="container py-2 animate-fade-in">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h3 class="fw-bold mb-0 text-theme-main"><i class="bi bi-person-lines-fill text-primary me-2"></i>Quản lý hồ sơ sức khỏe</h3>
      <button class="btn btn-primary rounded-pill px-4 shadow-sm fw-bold" @click="showModal = true">
        <i class="bi bi-plus-lg me-2"></i>Thêm hồ sơ
      </button>
    </div>

    
    <div class="mb-5">
      <h5 class="text-theme-muted fw-bold mb-3 d-flex align-items-center">
        <span class="me-2">Hồ sơ của tôi</span>
        <hr class="flex-grow-1 border-theme opacity-50">
      </h5>
      
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-primary" role="status"></div>
      </div>
      
      <div v-else-if="myProfiles.length === 0" class="text-center py-5 bg-theme-card rounded-4 shadow-sm border border-dashed border-theme mb-5">
        <i class="bi bi-person-vcard display-1 text-theme-muted opacity-25"></i>
        <h5 class="mt-3 text-theme-muted">Bạn chưa có hồ sơ sức khỏe nào</h5>
        <button class="btn btn-link text-primary fw-bold" @click="showModal = true">Tạo hồ sơ đầu tiên ngay</button>
      </div>

      <div v-else class="row g-4">
        <div v-for="profile in myProfiles" :key="profile.id" class="col-md-6 col-lg-4">
          <div class="card bg-theme-card h-100 shadow-sm border border-theme rounded-4 overflow-hidden position-relative hover-lift">
            <div class="card-body p-4">
              <div class="d-flex justify-content-between align-items-start mb-3">
                <div class="profile-avatar-lg bg-primary bg-opacity-10 text-primary rounded-circle d-flex align-items-center justify-content-center fw-bold fs-3 flex-shrink-0">
                  {{ profile.fullName.charAt(0) }}
                </div>
                <div class="dropdown">
                  <button class="btn btn-light bg-theme-light text-theme-main btn-sm rounded-circle border-theme" data-bs-toggle="dropdown"><i class="bi bi-three-dots-vertical"></i></button>
                  <ul class="dropdown-menu dropdown-menu-end shadow border-theme p-2 bg-theme-card">
                    <li><button class="dropdown-item rounded-2 text-theme-main" @click="editProfile(profile)"><i class="bi bi-pencil me-2"></i>Chỉnh sửa</button></li>
                    <li><button class="dropdown-item rounded-2 text-theme-main" @click="openShareModal(profile)"><i class="bi bi-share me-2"></i>Chia sẻ hồ sơ</button></li>
                    <li><hr class="dropdown-divider border-theme"></li>
                    <li><button class="dropdown-item text-danger rounded-2" @click="deleteProfile(profile.id)"><i class="bi bi-trash me-2"></i>Xóa hồ sơ</button></li>
                  </ul>
                </div>
              </div>
              <h5 class="card-title fw-bold text-theme-main mb-1 text-truncate">{{ profile.fullName }}</h5>
              <p class="text-theme-muted small mb-3 text-truncate"><i class="bi bi-tag me-1"></i> {{ profile.profileName }}</p>
              <div class="d-flex gap-2 flex-wrap mb-4">
                <span class="badge bg-theme-light text-theme-main border border-theme">{{ profile.gender || 'Chưa rõ' }}</span>
                <span class="badge bg-theme-light text-theme-main border border-theme">{{ formatDate(profile.dob) }}</span>
              </div>
              <router-link :to="'/body-metrics?profileId=' + profile.id" class="btn btn-outline-primary rounded-pill w-100 fw-bold py-2">
                Xem chi tiết chỉ số
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </div>

    
    <div class="mb-4" v-if="sharedProfiles.length > 0">
      <h5 class="text-theme-muted fw-bold mb-3 d-flex align-items-center">
        <span class="me-2">Hồ sơ được chia sẻ với tôi</span>
        <hr class="flex-grow-1 border-theme opacity-50">
      </h5>
      <div class="row g-4">
        <div v-for="shared in sharedProfiles" :key="shared.id" class="col-md-6 col-lg-4">
          <div class="card bg-theme-card h-100 shadow-sm border border-theme border-top border-4 border-success rounded-4 overflow-hidden hover-lift">
            <div class="card-body p-4">
              <div class="d-flex align-items-center gap-3 mb-3">
                <div class="profile-avatar bg-success bg-opacity-10 text-success rounded-circle d-flex align-items-center justify-content-center fw-bold flex-shrink-0">
                  {{ shared.fullName.charAt(0) }}
                </div>
                <div class="overflow-hidden min-width-0 flex-grow-1">
                  <h5 class="card-title fw-bold text-theme-main mb-1 text-truncate">{{ shared.fullName }}</h5>
                  <p class="text-theme-muted small mb-0 text-truncate">Chủ sở hữu: {{ shared.ownerName }}</p>
                </div>
              </div>
              <div class="bg-theme-light p-3 rounded-3 mb-3 border border-theme">
                <div class="small text-theme-muted mb-1">Quyền hạn của bạn:</div>
                <div class="fw-bold text-theme-main">
                  <i class="bi" :class="shared.accessLevel === 'EDITOR' ? 'bi-pencil-square text-primary' : 'bi-eye text-success'"></i>
                  {{ shared.accessLevel === 'EDITOR' ? ' Có quyền chỉnh sửa' : ' Chỉ quyền xem' }}
                </div>
              </div>
              <router-link :to="'/body-metrics?profileId=' + shared.id" class="btn btn-success rounded-pill w-100 fw-bold py-2">
                Xem dữ liệu sức khỏe
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </div>

    
    <div v-if="showModal" class="modal-overlay d-flex align-items-center justify-content-center p-3" @click.self="showModal = false">
      <div class="bg-theme-card rounded-4 shadow-lg w-100 overflow-hidden border border-theme" style="max-width: 500px;">
        <div class="p-3 border-bottom border-theme d-flex justify-content-between align-items-center bg-theme-light">
          <h5 class="mb-0 fw-bold text-theme-main">Thêm hồ sơ người thân</h5>
          <button class="btn-close" :class="{'btn-close-white': isDarkMode}" @click="showModal = false"></button>
        </div>
        <form @submit.prevent="submitProfile" class="p-4 bg-theme-card">
          <div class="mb-3">
            <label class="form-label small fw-bold text-theme-main">Họ và tên</label>
            <input type="text" class="form-control bg-theme-light text-theme-main border-theme" v-model="form.fullName" required placeholder="Nguyễn Văn A">
          </div>
          <div class="mb-3">
            <label class="form-label small fw-bold text-theme-main">Tên gợi nhớ (Ví dụ: Bố, Mẹ, Vợ...)</label>
            <input type="text" class="form-control bg-theme-light text-theme-main border-theme" v-model="form.profileName" required placeholder="Con trai">
          </div>
          <div class="row mb-3">
            <div class="col-6">
              <label class="form-label small fw-bold text-theme-main">Ngày sinh</label>
              <SmartDateInput class="form-control bg-theme-light text-theme-main border-theme" v-model="form.dob" required />
            </div>
            <div class="col-6">
              <label class="form-label small fw-bold text-theme-main">Giới tính</label>
              <select class="form-select bg-theme-light text-theme-main border-theme" v-model="form.gender">
                <option value="Nam">Nam</option>
                <option value="Nữ">Nữ</option>
                <option value="Khác">Khác</option>
              </select>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-label small fw-bold text-theme-main">Nhóm máu (Tùy chọn)</label>
            <select class="form-select bg-theme-light text-theme-main border-theme" v-model="form.bloodType">
              <option value="">Chưa rõ</option>
              <option value="A">A</option>
              <option value="B">B</option>
              <option value="AB">AB</option>
              <option value="O">O</option>
            </select>
          </div>
          <div class="d-grid mt-4">
            <button type="submit" class="btn btn-primary rounded-pill py-2 fw-bold" :disabled="submitting">
              <span v-if="submitting" class="spinner-border spinner-border-sm me-2"></span>
              {{ editingId ? 'Cập nhật hồ sơ' : 'Tạo hồ sơ mới' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    
    <div v-if="showShareModal" class="modal-overlay d-flex align-items-center justify-content-center p-3" @click.self="showShareModal = false">
      <div class="bg-theme-card rounded-4 shadow-lg w-100 overflow-hidden flex-column d-flex border border-theme" style="max-width: 600px; max-height: 90vh;">
        <div class="p-3 border-bottom border-theme d-flex justify-content-between align-items-center bg-theme-light">
          <h5 class="mb-0 fw-bold text-theme-main">Chia sẻ hồ sơ: {{ selectedProfileForShare?.fullName }}</h5>
          <button class="btn-close" :class="{'btn-close-white': isDarkMode}" @click="showShareModal = false"></button>
        </div>
        
        <div class="p-4 overflow-auto custom-scrollbar bg-theme-card">
          
          <div class="bg-theme-light p-3 rounded-4 mb-4 border border-theme">
            <h6 class="fw-bold mb-3 text-theme-main"><i class="bi bi-person-plus me-2"></i>Chia sẻ cho người dùng mới</h6>
            <div class="row g-2">
              <div class="col-md-6">
                <input type="email" class="form-control bg-theme-card text-theme-main border-theme" placeholder="Email người nhận..." v-model="shareForm.email">
              </div>
              <div class="col-md-4">
                <select class="form-select bg-theme-card text-theme-main border-theme" v-model="shareForm.accessLevel">
                  <option value="VIEWER">Chỉ xem</option>
                  <option value="EDITOR">Có thể chỉnh sửa</option>
                </select>
              </div>
              <div class="col-md-2">
                <button class="btn btn-primary w-100 shadow-sm" @click="addShare" :disabled="sharing">
                  <i class="bi bi-send" v-if="!sharing"></i>
                  <span v-else class="spinner-border spinner-border-sm"></span>
                </button>
              </div>
            </div>
            <small class="text-theme-muted mt-2 d-block">Lưu ý: Người nhận phải có tài khoản trên hệ thống.</small>
          </div>

          
          <h6 class="fw-bold mb-3 text-theme-main">Đang có quyền truy cập ({{ currentShares.length }})</h6>
          <div class="list-group border-0 gap-2">
            <div v-for="access in currentShares" :key="access.id" class="list-group-item bg-theme-card rounded-4 border border-theme p-3 d-flex align-items-center justify-content-between hover-bg">
              <div class="d-flex align-items-center gap-3 min-width-0 flex-grow-1">
                <div class="avatar-sm bg-theme-light text-theme-main rounded-circle border border-theme d-flex align-items-center justify-content-center fw-bold flex-shrink-0">
                  {{ access.userEmail.charAt(0).toUpperCase() }}
                </div>
                <div class="min-width-0">
                  <div class="fw-bold text-theme-main text-truncate" :title="access.userEmail">{{ access.userEmail }}</div>
                  <span class="badge bg-theme-light text-theme-main border border-theme" style="font-size: 0.65rem;">{{ access.accessLevel === 'EDITOR' ? 'Chỉnh sửa' : 'Chỉ xem' }}</span>
                </div>
              </div>
              <button class="btn btn-outline-danger btn-sm rounded-circle border-0 flex-shrink-0" @click="removeShare(access.id)" title="Thu hồi quyền">
                <i class="bi bi-x-lg"></i>
              </button>
            </div>
            <div v-if="currentShares.length === 0" class="text-center py-4 text-theme-muted bg-theme-light rounded-4 border-dashed border-theme">
              Hồ sơ này chưa được chia sẻ cho ai.
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import axios from 'axios';
import { toast } from '@/utils/toast';
import SmartDateInput from '@/components/common/SmartDateInput.vue';

const loading = ref(true);
const myProfiles = ref([]);
const sharedProfiles = ref([]);
const showModal = ref(false);
const submitting = ref(false);
const editingId = ref(null);

const isDarkMode = computed(() => document.documentElement.getAttribute('data-bs-theme') === 'dark');

const form = reactive({
  fullName: '',
  profileName: '',
  dob: '',
  gender: 'Nam',
  bloodType: ''
});


const showShareModal = ref(false);
const selectedProfileForShare = ref(null);
const currentShares = ref([]);
const sharing = ref(false);
const shareForm = reactive({
  email: '',
  accessLevel: 'VIEWER'
});

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await axios.get('/api/user-profile/list');
    myProfiles.value = res.data.myProfiles || [];
    sharedProfiles.value = res.data.sharedProfiles || [];
  } catch (err) {
    console.error("Lỗi fetch profiles:", err);
  } finally {
    loading.value = false;
  }
};

const submitProfile = async () => {
  submitting.value = true;
  try {
    const payload = { ...form, id: editingId.value };
    await axios.post('/api/user-profile/save', payload);
    showModal.value = false;
    fetchData();
    resetForm();
  } catch (err) {
    toast.error(err.response?.data?.message || err.message);
  } finally {
    submitting.value = false;
  }
};

const editProfile = (p) => {
  editingId.value = p.id;
  form.fullName = p.fullName;
  form.profileName = p.profileName;
  form.dob = p.dob;
  form.gender = p.gender || 'Nam';
  form.bloodType = p.bloodType || '';
  showModal.value = true;
};

const deleteProfile = async (id) => {
  if (!await toast.confirm("Xác nhận xóa", "Bạn muốn xóa hồ sơ này? Mọi dữ liệu đo lường liên quan cũng sẽ bị xóa.")) return;
  try {
    await axios.delete(`/api/user-profile/${id}`);
    fetchData();
  } catch (err) {
    toast.error(err.response?.data?.message || err.message);
  }
};

const resetForm = () => {
  editingId.value = null;
  form.fullName = '';
  form.profileName = '';
  form.dob = '';
  form.gender = 'Nam';
  form.bloodType = '';
};


const openShareModal = async (profile) => {
  selectedProfileForShare.value = profile;
  showShareModal.value = true;
  shareForm.email = '';
  fetchShares(profile.id);
};

const fetchShares = async (profileId) => {
  try {
    const res = await axios.get(`/api/user-profile/shares/${profileId}`);
    currentShares.value = res.data;
  } catch (err) {
    console.error("Lỗi lấy DS chia sẻ:", err);
  }
};

const addShare = async () => {
  if (!shareForm.email) return;
  sharing.value = true;
  try {
    const payload = {
      profileId: selectedProfileForShare.value.id,
      email: shareForm.email,
      accessLevel: shareForm.accessLevel
    };
    await axios.post('/api/user-profile/share', payload);
    shareForm.email = '';
    fetchShares(selectedProfileForShare.value.id);
  } catch (err) {
    toast.error(err.response?.data?.message || "Lỗi khi chia sẻ hồ sơ");
  } finally {
    sharing.value = false;
  }
};

const removeShare = async (shareId) => {
  if (!await toast.confirm("Xác nhận thu hồi", "Thu hồi quyền truy cập của người dùng này?")) return;
  try {
    await axios.delete(`/api/user-profile/share/${shareId}`);
    fetchShares(selectedProfileForShare.value.id);
  } catch (err) {
    console.error(err);
  }
};

const formatDate = (dateStr) => {
  if (!dateStr) return '---';
  return new Date(dateStr).toLocaleDateString('vi-VN');
};

onMounted(fetchData);
</script>

<style scoped>
.profile-avatar-lg { width: 64px; height: 64px; }
.profile-avatar { width: 48px; height: 48px; }
.avatar-sm { width: 36px; height: 36px; font-size: 0.9rem; }
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.6); backdrop-filter: blur(4px); z-index: 2000; }
.animate-fade-in { animation: fadeIn 0.5s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
.border-dashed { border-style: dashed !important; }
.extra-small { font-size: 0.65rem; }
</style>
