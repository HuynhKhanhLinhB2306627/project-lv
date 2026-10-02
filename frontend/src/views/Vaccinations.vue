<template>
  <div class="container-fluid py-2 animate-fade-in vaccinations-page">
    <div class="d-flex justify-content-between align-items-center mb-4 page-toolbar">
      <h3 class="fw-bold mb-0 text-theme-main"><i class="bi bi-shield-plus text-primary me-2"></i>Lịch sử tiêm chủng</h3>
      <div class="row g-2 page-toolbar-actions">
        <div class="col-12 col-md-8">
          <div class="profile-selector shadow-sm rounded-pill bg-theme-card ps-3 pe-2 py-1 border border-theme d-flex align-items-center">
            <i class="bi bi-person-circle text-primary me-2"></i>
            <select class="form-select border-0 bg-transparent fw-semibold text-theme-main profile-select py-0" v-model="selectedProfileId" @change="fetchVaccinations" style="box-shadow: none;">
              <option v-for="p in profiles" :key="p.id" :value="p.id" class="bg-theme-card text-theme-main">{{ p.fullName }} ({{ p.profileName }})</option>
            </select>
          </div>
        </div>
        <div class="col-12 col-md-4 d-grid">
          <button v-if="canEdit" class="btn btn-primary shadow-sm rounded-pill px-3 fw-bold" @click="openCreateModal">
            <i class="bi bi-plus-circle me-1"></i> Thêm mũi tiêm
          </button>
        </div>
      </div>
    </div>

    
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status"></div>
    </div>

    <div v-else-if="vaccinations.length === 0" class="text-center py-5 bg-theme-card rounded-4 shadow-sm border border-dashed border-theme">
      <i class="bi bi-droplet-half display-4 text-theme-muted opacity-25"></i>
      <p class="mt-3 text-theme-muted">Chưa có dữ liệu tiêm chủng cho hồ sơ này.</p>
    </div>

    <div v-else class="row row-cols-1 row-cols-md-2 row-cols-lg-3 g-4">
      <div class="col" v-for="vac in vaccinations" :key="vac.id">
        <div class="card bg-theme-card h-100 shadow-sm border border-theme rounded-4 overflow-hidden hover-lift">
          <div class="card-body p-4">
            <div class="d-flex justify-content-between align-items-start mb-3">
              <div class="bg-primary bg-opacity-10 p-3 rounded-3">
                <i class="bi bi-capsule text-primary fs-4"></i>
              </div>
              <div v-if="canEdit" class="dropdown">
                <button class="btn btn-light bg-theme-light text-theme-main btn-sm rounded-circle border border-theme" data-bs-toggle="dropdown"><i class="bi bi-three-dots-vertical"></i></button>
                <ul class="dropdown-menu dropdown-menu-end shadow-lg border border-theme p-2 bg-theme-card">
                  <li><button class="dropdown-item rounded-2 text-theme-main" @click="editVaccination(vac)"><i class="bi bi-pencil me-2"></i>Sửa</button></li>
                  <li><button class="dropdown-item text-danger rounded-2" @click="deleteVaccination(vac.id)"><i class="bi bi-trash me-2"></i>Xóa</button></li>
                </ul>
              </div>
            </div>
            
            <h5 class="fw-bold text-theme-main mb-1 text-truncate" :title="vac.vaccineName">{{ vac.vaccineName }}</h5>
            <p class="badge bg-info bg-opacity-10 text-info rounded-pill px-3 mb-3 border border-info border-opacity-25">Mũi thứ: {{ vac.doseNumber }}</p>
            
            <div class="info-item d-flex align-items-center gap-2 mb-2 text-theme-muted small">
              <i class="bi bi-calendar3 flex-shrink-0"></i> <span>{{ formatDate(vac.vaccinationDate) }}</span>
            </div>
            <div class="info-item d-flex align-items-center gap-2 text-theme-muted small">
              <i class="bi bi-geo-alt flex-shrink-0"></i> <span class="text-truncate" :title="vac.location || 'Chưa cập nhật địa điểm'">{{ vac.location || 'Chưa cập nhật địa điểm' }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    
    <div v-if="showModal" class="modal-overlay d-flex align-items-center justify-content-center p-3" @click.self="showModal = false">
      <div class="bg-theme-card rounded-4 shadow-lg w-100 overflow-hidden border border-theme" style="max-width: 500px;">
        <div class="p-3 border-bottom border-theme d-flex justify-content-between align-items-center bg-theme-light">
          <h5 class="mb-0 fw-bold text-theme-main">{{ editingId ? 'Cập nhật thông tin' : 'Thêm mũi tiêm mới' }}</h5>
          <button class="btn-close" :class="{'btn-close-white': isDarkMode}" @click="showModal = false"></button>
        </div>
        <form @submit.prevent="submitForm" class="p-4 bg-theme-card">
          <div class="mb-3">
            <label class="form-label small fw-bold text-theme-main">Tên loại Vắc-xin</label>
            <input type="text" class="form-control bg-theme-light text-theme-main border-theme" v-model="form.vaccineName" placeholder="VD: AstraZeneca, Pfizer, Moderna..." required>
          </div>
          <div class="row mb-3">
            <div class="col-12 col-md-6">
              <label class="form-label small fw-bold text-theme-main">Mũi thứ mấy?</label>
              <input type="number" class="form-control bg-theme-light text-theme-main border-theme" v-model="form.doseNumber" min="1" required>
            </div>
            <div class="col-12 col-md-6">
              <label class="form-label small fw-bold text-theme-main">Ngày tiêm</label>
              <SmartDateInput class="form-control bg-theme-light text-theme-main border-theme" v-model="form.vaccinationDate" required />
            </div>
          </div>
          <div class="mb-3">
            <label class="form-label small fw-bold text-theme-main">Địa điểm tiêm</label>
            <input type="text" class="form-control bg-theme-light text-theme-main border-theme" v-model="form.location" placeholder="VD: Bệnh viện Đa khoa, Trạm y tế...">
          </div>
          <div class="d-grid mt-4">
            <button type="submit" class="btn btn-primary rounded-pill py-2 fw-bold" :disabled="submitting">
              <span v-if="submitting" class="spinner-border spinner-border-sm me-2"></span>
              {{ editingId ? 'Cập nhật ngay' : 'Lưu thông tin' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import axios from 'axios';
import { toast } from '@/utils/toast';
import SmartDateInput from '@/components/common/SmartDateInput.vue';

const vaccinations = ref([]);
const profiles = ref([]);
const selectedProfileId = ref(null);
const canEdit = ref(false);
const loading = ref(true);
const showModal = ref(false);
const submitting = ref(false);
const editingId = ref(null);

const isDarkMode = computed(() => document.documentElement.getAttribute('data-bs-theme') === 'dark');

const form = reactive({
  vaccineName: '',
  doseNumber: 1,
  vaccinationDate: '',
  location: ''
});

const fetchVaccinations = async () => {
  loading.value = true;
  try {
    const url = selectedProfileId.value ? `/api/vaccinations?profileId=${selectedProfileId.value}` : '/api/vaccinations';
    const res = await axios.get(url);
    vaccinations.value = res.data.vaccinations || [];
    profiles.value = res.data.profiles || [];
    selectedProfileId.value = res.data.currentProfileId;
    canEdit.value = res.data.canEdit;
  } catch (err) {
    console.error("Lỗi tải lịch tiêm chủng", err);
    toast.error("Không thể tải dữ liệu tiêm chủng. Vui lòng kiểm tra lại kết nối.");
  } finally {
    loading.value = false;
  }
};

const openCreateModal = () => {
  editingId.value = null;
  form.vaccineName = '';
  form.doseNumber = 1;
  form.vaccinationDate = '';
  form.location = '';
  showModal.value = true;
};

const editVaccination = (vac) => {
  editingId.value = vac.id;
  form.vaccineName = vac.vaccineName;
  form.doseNumber = vac.doseNumber;
  form.vaccinationDate = vac.vaccinationDate;
  form.location = vac.location;
  showModal.value = true;
};

const submitForm = async () => {
  submitting.value = true;
  try {
    const payload = {
      ...form,
      id: editingId.value,
      profileId: selectedProfileId.value
    };
    const res = await axios.post('/api/vaccinations', payload);
    if (res.data.success) {
      toast.success(res.data.message);
      showModal.value = false;
      fetchVaccinations();
    }
  } catch (err) {
    toast.error(err.response?.data?.message || err.message);
  } finally {
    submitting.value = false;
  }
};

const deleteVaccination = async (id) => {
  if (!await toast.confirm("Xác nhận xóa", "Bạn có chắc chắn muốn xóa mũi tiêm này?")) return;
  try {
    const res = await axios.delete(`/api/vaccinations/${id}`);
    if (res.data.success) {
      fetchVaccinations();
    }
  } catch (err) {
    toast.error(err.response?.data?.message || err.message);
  }
};

const formatDate = (dateStr) => {
  if (!dateStr) return '---';
  return new Date(dateStr).toLocaleDateString('vi-VN');
};

onMounted(fetchVaccinations);
</script>

<style scoped>
.hover-lift { transition: transform 0.2s, box-shadow 0.2s; }
.hover-lift:hover { transform: translateY(-5px); box-shadow: 0 10px 20px rgba(0,0,0,0.08) !important; }
.border-dashed { border-style: dashed !important; border-width: 2px !important; }
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.6); backdrop-filter: blur(4px); z-index: 2000; }
.animate-fade-in { animation: fadeIn 0.4s ease-out; }
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
.border-theme { border-color: var(--border-color) !important; }
.page-toolbar-actions { width: min(100%, 680px); }
.profile-selector { width: 100%; }
.profile-select { width: 100%; min-width: 0; max-width: 100%; }

@media (max-width: 767.98px) {
  .vaccinations-page {
    padding-left: 0.6rem !important;
    padding-right: 0.6rem !important;
  }

  .page-toolbar {
    flex-direction: column;
    align-items: stretch !important;
    gap: 0.75rem;
    margin-bottom: 1rem !important;
  }

  .page-toolbar h3 {
    font-size: 1.15rem;
    margin-bottom: 0;
  }

  .page-toolbar-actions { width: 100%; }

  .profile-select,
  .page-toolbar-actions .btn {
    width: 100%;
    min-width: 0;
  }

  .vaccinations-page .p-4 {
    padding: 0.9rem !important;
  }
}
</style>
