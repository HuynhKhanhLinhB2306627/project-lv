<template>
  <div class="container-fluid py-2">
    <div class="row justify-content-center">
      <div class="col-lg-10">
        <div class="bg-theme-card p-4 p-md-5 rounded-4 shadow-sm border border-theme position-relative overflow-hidden">
          
          
          <div class="position-absolute top-0 start-0 w-100 bg-primary opacity-10" style="height: 120px;"></div>

          <div class="row position-relative z-1 mt-4">
            <div class="col-md-4 text-center border-end-md border-theme">
              
              <div class="position-relative d-inline-block mb-4 group">
                <img v-if="user.avatar" :src="avatarUrl(user.avatar)" 
                     class="rounded-circle shadow-lg border border-4 border-theme object-fit-cover bg-theme-light" 
                     width="160" height="160">
                <img v-else :src="`https://ui-avatars.com/api/?name=${encodeURIComponent(user.fullName || 'User')}&background=0d6efd&color=fff&size=160&bold=true`" 
                     class="rounded-circle shadow-lg border border-4 border-theme object-fit-cover bg-theme-light" 
                     width="160" height="160">
                <label v-if="canEdit" class="btn btn-primary btn-sm rounded-circle position-absolute bottom-0 end-0 shadow cursor-pointer p-2" style="width: 38px; height: 38px;">
                  <i class="bi bi-camera-fill"></i>
                  <input type="file" class="d-none" @change="handleAvatarUpload" accept="image/*">
                </label>
                <div v-if="uploadingAvatar" class="position-absolute top-50 start-50 translate-middle">
                  <div class="spinner-border text-primary" role="status"></div>
                </div>
              </div>
              
              <h4 class="fw-bold mb-1 text-theme-main text-truncate" style="max-width: 300px;" :title="user.fullName || 'Người dùng'">{{ user.fullName || 'Người dùng' }}</h4>
              <p class="badge bg-primary bg-opacity-10 text-primary rounded-pill px-3 mb-4 border border-primary border-opacity-10">Mã số: #{{ user.id || '---' }}</p>
              
              
              <div class="bg-theme-light p-3 rounded-4 mb-4 text-start border border-theme">
                <div class="d-flex justify-content-between align-items-center mb-2">
                  <span class="small fw-bold text-theme-muted uppercase">Chỉ số BMI</span>
                  <span class="badge" :class="bmiClass">{{ bmiStatus }}</span>
                </div>
                <div class="d-flex align-items-end gap-2 mb-2">
                  <h3 class="mb-0 fw-bold text-theme-main">{{ bmi }}</h3>
                  <small class="text-theme-muted mb-1">kg/m²</small>
                </div>
                <div class="progress bg-theme-card" style="height: 6px; border-radius: 10px;">
                  <div class="progress-bar" :class="bmiBg" :style="{width: Math.min(bmi*2.5, 100) + '%'}"></div>
                </div>
                <p class="extra-small text-theme-muted mt-2 mb-0">Dựa trên cân nặng: <strong class="text-theme-main">{{ user.weight }}kg</strong> và chiều cao: <strong class="text-theme-main">{{ user.height }}cm</strong></p>
              </div>
            </div>

            <div class="col-md-8 ps-md-5 mt-4 mt-md-0">
              <div class="d-flex justify-content-between align-items-center mb-4">
                <h5 class="fw-bold mb-0 d-flex align-items-center gap-2 text-theme-main">
                  <i class="bi bi-person-badge text-primary"></i> Thông tin hồ sơ
                </h5>
                <button v-if="canEdit" class="btn btn-sm fw-bold shadow-sm" :class="isEditing ? 'btn-danger px-4' : 'btn-primary px-3'" @click="toggleEdit">
                  <i class="bi me-1" :class="isEditing ? 'bi-x-lg' : 'bi-pencil-square'"></i>
                  {{ isEditing ? 'Hủy bỏ' : 'Chỉnh sửa' }}
                </button>
              </div>

              
              <div class="row g-4" v-if="!isEditing">
                <div class="col-md-6 info-group">
                  <label class="small fw-bold text-theme-muted text-uppercase mb-1">Họ và tên</label>
                  <p class="fw-bold text-theme-main fs-6 text-truncate" :title="user.fullName">{{ user.fullName }}</p>
                </div>
                <div class="col-md-6 info-group">
                  <label class="small fw-bold text-theme-muted text-uppercase mb-1">Email liên hệ</label>
                  <p class="text-theme-main">{{ user.email }}</p>
                </div>
                <div class="col-md-6 info-group">
                  <label class="small fw-bold text-theme-muted text-uppercase mb-1">Số điện thoại</label>
                  <p class="text-theme-main fw-medium">{{ user.phone || '---' }}</p>
                </div>
                <div class="col-md-6 info-group">
                  <label class="small fw-bold text-theme-muted text-uppercase mb-1">Ngày sinh</label>
                  <p class="text-theme-main">{{ formatDate(user.dob) }}</p>
                </div>
                <div class="col-md-6 info-group">
                  <label class="small fw-bold text-theme-muted text-uppercase mb-1">Nhóm máu</label>
                  <p><span class="badge bg-danger rounded-pill px-3 shadow-sm">{{ user.bloodType || 'Chưa rõ' }}</span></p>
                </div>
                <div class="col-md-6 info-group">
                  <label class="small fw-bold text-theme-muted text-uppercase mb-1">Giới tính</label>
                  <p class="text-theme-main">{{ user.gender || '---' }}</p>
                </div>
                <div class="col-md-12 info-group">
                  <label class="small fw-bold text-theme-muted text-uppercase mb-1">Tình trạng bệnh lý mãn tính</label>
                  <div class="d-flex flex-wrap gap-2 mt-1">
                    <span v-for="tag in medicalHistoryTags" :key="tag" class="badge bg-theme-light text-theme-main border border-theme fw-normal px-3 py-2 rounded-3 shadow-sm">
                      <i class="bi bi-check2-circle text-success me-1"></i> {{ tag }}
                    </span>
                    <span v-if="medicalHistoryTags.length === 0" class="text-theme-muted small italic">Không có dữ liệu bệnh lý</span>
                  </div>
                </div>
              </div>

              
              <form @submit.prevent="saveProfile" class="row g-3" v-else>
                <div class="col-md-6">
                  <label class="form-label small fw-bold text-theme-main">Họ và tên</label>
                  <input type="text" class="form-control bg-theme-light text-theme-main border-theme" v-model="editForm.fullName" required>
                </div>
                <div class="col-md-6">
                  <label class="form-label small fw-bold text-theme-main">Ngày sinh</label>
                  <SmartDateInput class="form-control bg-theme-light text-theme-main border-theme" v-model="editForm.dob" />
                </div>
                <div class="col-md-6">
                  <label class="form-label small fw-bold text-theme-main">Số điện thoại</label>
                  <input type="tel" class="form-control bg-theme-light text-theme-main border-theme" v-model="editForm.phone">
                </div>
                <div class="col-md-3">
                  <label class="form-label small fw-bold text-theme-main">Giới tính</label>
                  <select class="form-select bg-theme-light text-theme-main border-theme" v-model="editForm.gender">
                    <option value="Nam">Nam</option>
                    <option value="Nữ">Nữ</option>
                    <option value="Khác">Khác</option>
                  </select>
                </div>
                <div class="col-md-3">
                  <label class="form-label small fw-bold text-theme-main">Nhóm máu</label>
                  <select class="form-select bg-theme-light text-theme-main border-theme" v-model="editForm.bloodType">
                    <option v-for="type in ['A+', 'A-', 'B+', 'B-', 'O+', 'O-', 'AB+', 'AB-']" :key="type" :value="type">{{ type }}</option>
                  </select>
                </div>
                <div class="col-12">
                  <label class="form-label small fw-bold text-theme-main">Bệnh mãn tính (cách nhau bởi dấu phẩy)</label>
                  <textarea class="form-control bg-theme-light text-theme-main border-theme" rows="3" v-model="editForm.chronicConditions" placeholder="VD: Tiểu đường, Cao huyết áp..."></textarea>
                </div>
                <div class="col-12 mt-4">
                  <button type="submit" class="btn btn-primary w-100 rounded-pill py-2 shadow-sm fw-bold" :disabled="saving">
                    <span v-if="saving" class="spinner-border spinner-border-sm me-2"></span>
                    Lưu thay đổi
                  </button>
                </div>
              </form>

            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import axios from 'axios';
import { useAuthStore } from '../stores/auth';
import { toast } from '@/utils/toast';
import SmartDateInput from '@/components/common/SmartDateInput.vue';

const route = useRoute();
const authStore = useAuthStore();
const isEditing = ref(false);
const saving = ref(false);
const uploadingAvatar = ref(false);
const canEdit = ref(true); 

const isDarkMode = computed(() => document.documentElement.getAttribute('data-bs-theme') === 'dark');

const avatarUrl = (avatar) => {
  if (!avatar) return null;
  if (avatar.startsWith('http')) return avatar;
  const baseUrl = axios.defaults.baseURL || '';
  if (avatar.startsWith('/uploads/')) return baseUrl + avatar;
  if (avatar.startsWith('uploads/')) return baseUrl + '/' + avatar;
  return baseUrl + '/uploads/' + avatar;
};

const user = reactive({
  id: null, profileName: '', fullName: '', dob: '', email: '', phone: '', height: 0, weight: 0, 
  gender: '', bloodType: '', chronicConditions: '', avatar: null, isOwner: true
});

const editForm = reactive({
  profileName: '', fullName: '', dob: '', phone: '', gender: '', bloodType: '', chronicConditions: ''
});

const fetchProfile = async () => {
  try {
    const profileId = route.params.id;
    let url = '/api/user-profile/me';
    if (profileId && profileId !== 'me') {
      url = `/api/user-profile/${profileId}`;
    }
    
    const response = await axios.get(url);
    const data = response.data;
    Object.assign(user, data);
    user.dob = data.dob || data.dateOfBirth || '';
    user.phone = data.phone || data.emergencyContactPhone || '';
    
    
    canEdit.value = data.canEdit !== false; 

    
    if (!profileId || profileId === 'me') {
      authStore.updateUser({
        fullName: data.fullName,
        avatar: data.avatar
      });
    }
  } catch (err) { console.error("Lỗi tải hồ sơ:", err); }
};

const toggleEdit = () => {
  if (!isEditing.value) {
    
    Object.assign(editForm, {
      profileName: user.profileName,
      fullName: user.fullName,
      dob: user.dob,
      phone: user.phone,
      gender: user.gender,
      bloodType: user.bloodType,
      chronicConditions: user.chronicConditions
    });
  }
  isEditing.value = !isEditing.value;
};

const saveProfile = async () => {
  saving.value = true;
  try {
    const profileId = route.params.id;
    const payload = {
      profileName: editForm.profileName,
      fullName: editForm.fullName,
      dateOfBirth: editForm.dob || null,
      gender: editForm.gender,
      bloodType: editForm.bloodType,
      chronicConditions: editForm.chronicConditions,
      emergencyContactPhone: editForm.phone
    };
    let url = '/api/user-profile/update';
    if (profileId && profileId !== 'me') {
      url = `/api/user-profile/${profileId}`;
      const res = await axios.put(url, payload);
      if (res.data.success) {
        await fetchProfile();
        isEditing.value = false;
        toast.success("Đã cập nhật hồ sơ thành công!");
      }
    } else {
      const res = await axios.post(url, payload);
      if (res.data.success) {
        await fetchProfile();
        isEditing.value = false;
        
        
        authStore.updateUser({ fullName: editForm.fullName });
        
        toast.success("Đã cập nhật hồ sơ thành công!");
      }
    }
  } catch (err) {
    toast.error(err.response?.data?.message || err.message);
  } finally { saving.value = false; }
};

const handleAvatarUpload = async (e) => {
  const file = e.target.files[0];
  if (!file) return;

  const formData = new FormData();
  formData.append('file', file);

  uploadingAvatar.value = true;
  try {
    const profileId = route.params.id;
    let url = '/api/user-profile/upload-avatar';
    
    if (profileId && profileId !== 'me') {
      toast.warning("Tính năng cập nhật ảnh cho hồ sơ phụ đang được hoàn thiện.");
      uploadingAvatar.value = false;
      return;
    }
    
    const res = await axios.post(url, formData);
    if (res.data.success) {
      user.avatar = res.data.avatar;
      
      authStore.updateUser({ avatar: res.data.avatar });
    }
  } catch (err) {
    toast.error("Lỗi tải ảnh: " + err.message);
  } finally { uploadingAvatar.value = false; }
};

const medicalHistoryTags = computed(() => {
  if (!user.chronicConditions) return [];
  return user.chronicConditions.split(',').map(s => s.trim()).filter(s => s);
});

const bmi = computed(() => {
  if (!user.height || !user.weight) return 0;
  const h = user.height / 100;
  return (user.weight / (h * h)).toFixed(1);
});

const bmiStatus = computed(() => {
  const val = parseFloat(bmi.value);
  if (val < 18.5) return 'Gầy';
  if (val < 25) return 'Bình thường';
  if (val < 30) return 'Tiền béo phì';
  return 'Béo phì';
});

const bmiClass = computed(() => {
  const val = parseFloat(bmi.value);
  if (val < 18.5) return 'bg-info';
  if (val < 25) return 'bg-success';
  if (val < 30) return 'bg-warning text-dark';
  return 'bg-danger';
});

const bmiBg = computed(() => {
  const val = parseFloat(bmi.value);
  if (val < 18.5) return 'bg-info';
  if (val < 25) return 'bg-success';
  if (val < 30) return 'bg-warning';
  return 'bg-danger';
});

const formatDate = (dateStr) => {
  if (!dateStr) return '---';
  const d = new Date(dateStr);
  return d.toLocaleDateString('vi-VN');
};

onMounted(fetchProfile);
</script>

<style scoped>
.border-end-md { border-right: 1px solid var(--border-color); }
@media (max-width: 767.98px) {
  .border-end-md { border-right: none; border-bottom: 1px solid var(--border-color); padding-bottom: 2rem; margin-bottom: 2rem; }
}
.group:hover .btn-primary { transform: scale(1.1); }
.btn-primary { transition: 0.2s transform; }
.extra-small { font-size: 0.75rem; }
.info-group label { letter-spacing: 0.05em; }
.italic { font-style: italic; }
.uppercase { text-transform: uppercase; }
.cursor-pointer { cursor: pointer; }
.object-fit-cover { object-fit: cover; }
.z-1 { z-index: 1; }
.border-theme { border-color: var(--border-color) !important; }
</style>
