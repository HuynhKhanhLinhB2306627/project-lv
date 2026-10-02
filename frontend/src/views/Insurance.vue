<template>
  <div class="container-fluid py-2 animate-fade-in insurance-page">
    <div class="d-flex justify-content-between align-items-center mb-4 page-toolbar">
      <h3 class="fw-bold mb-0 text-theme-main"><i class="bi bi-card-checklist text-primary me-2"></i>Quản lý thẻ BHYT</h3>
      <div class="row g-2 page-toolbar-actions">
        <div class="col-12 col-md-8">
          <div class="profile-selector shadow-sm rounded-pill bg-theme-card ps-3 pe-2 py-1 border border-theme d-flex align-items-center">
            <i class="bi bi-person-circle text-primary me-2"></i>
            <select class="form-select border-0 bg-transparent fw-semibold text-theme-main profile-select py-0" v-model="selectedProfileId" @change="fetchCards" style="box-shadow: none;">
              <option v-for="p in profiles" :key="p.id" :value="p.id" class="bg-theme-card text-theme-main">{{ p.fullName }} ({{ p.profileName }})</option>
            </select>
          </div>
        </div>
        <div class="col-12 col-md-4 d-grid">
          <button v-if="canEdit" class="btn btn-primary shadow-sm rounded-pill px-3 fw-bold" @click="openCreateModal">
            <i class="bi bi-plus-circle me-1"></i> Thêm thẻ mới
          </button>
        </div>
      </div>
    </div>

    
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status"></div>
    </div>

    <div v-else-if="cards.length === 0" class="text-center py-5 bg-theme-card rounded-4 shadow-sm border border-dashed border-theme">
      <i class="bi bi-credit-card-2-front display-4 text-theme-muted opacity-25"></i>
      <p class="mt-3 text-theme-muted">Chưa có thông tin thẻ BHYT cho hồ sơ này.</p>
    </div>

    <div v-else class="row row-cols-1 row-cols-md-2 row-cols-lg-3 g-4">
      <div class="col" v-for="card in cards" :key="card.id">
        <div class="card bg-theme-card h-100 shadow-sm border border-theme rounded-4 overflow-hidden hover-lift insurance-card" :class="{'expired': isExpired(card.expiryDate)}">
          <div class="card-body p-4">
            <div class="d-flex justify-content-between align-items-start mb-3">
              <div class="bg-primary bg-opacity-10 p-3 rounded-3">
                <i class="bi bi-vignette text-primary fs-4"></i>
              </div>
              <div v-if="canEdit" class="dropdown">
                <button class="btn btn-light bg-theme-light text-theme-main border border-theme btn-sm rounded-circle shadow-sm" data-bs-toggle="dropdown"><i class="bi bi-three-dots-vertical"></i></button>
                <ul class="dropdown-menu dropdown-menu-end shadow-lg border border-theme p-2 bg-theme-card">
                  <li><button class="dropdown-item rounded-2 text-theme-main" @click="editCard(card)"><i class="bi bi-pencil me-2"></i>Sửa</button></li>
                  <li><button class="dropdown-item text-danger rounded-2" @click="deleteCard(card.id)"><i class="bi bi-trash me-2"></i>Xóa</button></li>
                </ul>
              </div>
            </div>
            
            <h5 class="fw-bold text-theme-main mb-1 text-truncate" :title="card.cardNumber">{{ card.cardNumber }}</h5>
            <p class="badge rounded-pill px-3 mb-3 border border-opacity-25" :class="isExpired(card.expiryDate) ? 'bg-danger bg-opacity-10 text-danger border-danger' : 'bg-success bg-opacity-10 text-success border-success'">
              {{ isExpired(card.expiryDate) ? 'Đã hết hạn' : 'Còn hiệu lực' }}
            </p>
            
            <div class="info-item mb-2 text-theme-muted small">
              <div class="fw-bold text-theme-main mb-1">Nơi ĐKKCBBĐ:</div>
              <div class="d-flex align-items-center gap-2">
                <i class="bi bi-hospital flex-shrink-0"></i> 
                <span class="text-truncate" :title="card.registrationPlace">{{ card.registrationPlace }}</span>
              </div>
            </div>
            
            <div class="row mt-3 pt-3 border-top border-theme g-2">
              <div class="col-6">
                <div class="text-theme-muted extra-small uppercase fw-bold">Ngày cấp</div>
                <div class="small fw-bold text-theme-main">{{ formatDate(card.issueDate) }}</div>
              </div>
              <div class="col-6">
                <div class="text-theme-muted extra-small uppercase fw-bold">Ngày hết hạn</div>
                <div class="small fw-bold" :class="isExpired(card.expiryDate) ? 'text-danger' : 'text-theme-main'">{{ formatDate(card.expiryDate) }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    
    <div v-if="showModal" class="modal-overlay d-flex align-items-center justify-content-center p-3" @click.self="showModal = false">
      <div class="bg-theme-card rounded-4 shadow-lg w-100 overflow-hidden border border-theme" style="max-width: 500px;">
        <div class="p-3 border-bottom border-theme d-flex justify-content-between align-items-center bg-theme-light">
          <h5 class="mb-0 fw-bold text-theme-main">{{ editingId ? 'Cập nhật thông tin thẻ' : 'Thêm thẻ BHYT mới' }}</h5>
          <button class="btn-close" :class="{'btn-close-white': isDarkMode}" @click="showModal = false"></button>
        </div>
        <form @submit.prevent="submitForm" class="p-4 bg-theme-card">
          <div class="mb-3">
            <label class="form-label small fw-bold text-theme-main">Số thẻ BHYT</label>
            <input type="text" class="form-control bg-theme-light text-theme-main border-theme rounded-3" v-model="form.cardNumber" placeholder="VD: GD4797921200021" required>
          </div>
          <div class="mb-3">
            <label class="form-label small fw-bold text-theme-main">Nơi đăng ký KCB ban đầu</label>
            <input type="text" class="form-control bg-theme-light text-theme-main border-theme rounded-3" v-model="form.registrationPlace" placeholder="VD: Bệnh viện Đa khoa tỉnh" required>
          </div>
            <div class="row mb-3">
              <div class="col-12 col-md-6">
                <label class="form-label small fw-bold text-theme-main">Ngày cấp</label>
                <SmartDateInput class="form-control bg-theme-light text-theme-main border-theme rounded-3" v-model="form.issueDate" required />
              </div>
              <div class="col-12 col-md-6">
                <label class="form-label small fw-bold text-theme-main">Ngày hết hạn</label>
                <SmartDateInput class="form-control bg-theme-light text-theme-main border-theme rounded-3" v-model="form.expiryDate" required />
              </div>
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

const cards = ref([]);
const profiles = ref([]);
const selectedProfileId = ref(null);
const canEdit = ref(false);
const loading = ref(true);
const showModal = ref(false);
const submitting = ref(false);
const editingId = ref(null);

const isDarkMode = computed(() => document.documentElement.getAttribute('data-bs-theme') === 'dark');

const form = reactive({
  cardNumber: '',
  registrationPlace: '',
  issueDate: '',
  expiryDate: ''
});

const fetchCards = async () => {
  loading.value = true;
  try {
    const url = selectedProfileId.value ? `/api/insurance?profileId=${selectedProfileId.value}` : '/api/insurance';
    const res = await axios.get(url);
    cards.value = res.data.cards || [];
    profiles.value = res.data.profiles || [];
    selectedProfileId.value = res.data.currentProfileId;
    canEdit.value = res.data.canEdit;
  } catch (err) {
    console.error("Lỗi tải thông tin thẻ BHYT", err);
    toast.error("Không thể tải dữ liệu thẻ BHYT. Vui lòng kiểm tra lại kết nối.");
  } finally {
    loading.value = false;
  }
};

const openCreateModal = () => {
  editingId.value = null;
  form.cardNumber = '';
  form.registrationPlace = '';
  form.issueDate = '';
  form.expiryDate = '';
  showModal.value = true;
};

const editCard = (card) => {
  editingId.value = card.id;
  form.cardNumber = card.cardNumber;
  form.registrationPlace = card.registrationPlace;
  form.issueDate = card.issueDate;
  form.expiryDate = card.expiryDate;
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
    const res = await axios.post('/api/insurance', payload);
    if (res.data.success) {
      toast.success(res.data.message);
      showModal.value = false;
      fetchCards();
    }
  } catch (err) {
    toast.error(err.response?.data?.message || err.message);
  } finally {
    submitting.value = false;
  }
};

const deleteCard = async (id) => {
  if (!await toast.confirm("Xác nhận xóa", "Bạn có chắc chắn muốn xóa thẻ BHYT này?")) return;
  try {
    const res = await axios.delete(`/api/insurance/${id}`);
    if (res.data.success) {
      fetchCards();
    }
  } catch (err) {
    toast.error(err.response?.data?.message || err.message);
  }
};

const formatDate = (dateStr) => {
  if (!dateStr) return '---';
  return new Date(dateStr).toLocaleDateString('vi-VN');
};

const isExpired = (expiryDate) => {
  if (!expiryDate) return false;
  return new Date(expiryDate) < new Date();
};

onMounted(fetchCards);
</script>

<style scoped>
.hover-lift { transition: transform 0.2s, box-shadow 0.2s; }
.hover-lift:hover { transform: translateY(-5px); box-shadow: 0 10px 20px rgba(0,0,0,0.08) !important; }
.border-dashed { border-style: dashed !important; border-width: 2px !important; }
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.6); backdrop-filter: blur(4px); z-index: 2000; }
.insurance-card { position: relative; }
.insurance-card.expired { border-left: 4px solid #dc3545 !important; }
.extra-small { font-size: 0.65rem; }
.uppercase { text-transform: uppercase; }
.border-theme { border-color: var(--border-color) !important; }
.animate-fade-in { animation: fadeIn 0.4s ease-out; }
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
.page-toolbar-actions { width: min(100%, 680px); }
.profile-selector { width: 100%; }
.profile-select { width: 100%; min-width: 0; max-width: 100%; }

@media (max-width: 767.98px) {
  .insurance-page {
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

  .insurance-page .p-4 {
    padding: 0.9rem !important;
  }
}
</style>
