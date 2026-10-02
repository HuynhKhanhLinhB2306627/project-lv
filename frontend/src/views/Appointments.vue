<template>
  <div class="container-fluid py-2 animate-fade-in appointments-page">
    <div class="d-flex justify-content-between align-items-center mb-4 page-toolbar">
      <h3 class="fw-bold text-theme-main"><i class="bi bi-calendar-check text-primary me-2"></i>Lịch hẹn khám</h3>
      <div class="row g-2 page-toolbar-actions">
        <div class="col-12 col-md-8">
          <div class="profile-selector shadow-sm rounded-pill bg-theme-card ps-3 pe-2 py-1 border border-theme d-flex align-items-center">
            <i class="bi bi-person-circle text-primary me-2"></i>
            <select class="form-select border-0 bg-transparent fw-semibold text-theme-main profile-select py-0" v-model="selectedProfileId" @change="fetchAppointments" style="box-shadow: none;">
              <option v-for="p in profiles" :key="p.id" :value="p.id" class="bg-theme-card text-theme-main">{{ p.fullName }} ({{ p.profileName }})</option>
            </select>
          </div>
        </div>
        <div class="col-12 col-md-4 d-grid">
          <button v-if="canEdit" class="btn btn-primary shadow-sm rounded-pill px-3 fw-bold" @click="openCreateModal">
            <i class="bi bi-plus-circle me-1"></i> Đặt lịch mới
          </button>
        </div>
      </div>
    </div>

    <div class="row g-4">
      
      <div class="col-lg-8">
        <div class="bg-theme-card p-4 rounded-4 shadow-sm border border-theme">
          
          
          <ul class="nav nav-pills mb-4 bg-theme-light p-1 rounded-pill d-inline-flex border border-theme" role="tablist">
            <li class="nav-item" role="presentation">
              <button class="nav-link rounded-pill px-4 active fw-bold" id="upcoming-tab" data-bs-toggle="pill" data-bs-target="#upcoming" type="button" role="tab">
                Sắp tới ({{ upcoming.length }})
              </button>
            </li>
            <li class="nav-item" role="presentation">
              <button class="nav-link rounded-pill px-4 fw-bold" id="past-tab" data-bs-toggle="pill" data-bs-target="#past" type="button" role="tab">
                Lịch sử ({{ past.length }})
              </button>
            </li>
          </ul>

          <div class="tab-content">
            
            <div class="tab-pane fade show active" id="upcoming" role="tabpanel">
              <div v-if="loading" class="text-center py-5">
                <div class="spinner-border text-primary" role="status"></div>
              </div>
              
              <div v-else-if="upcoming.length === 0" class="text-center py-5 text-theme-muted bg-theme-light rounded-4 border border-dashed border-theme">
                <i class="bi bi-calendar-x display-4 opacity-25 text-theme-muted"></i>
                <p class="mt-2 mb-0 text-theme-muted">Bạn chưa có lịch hẹn nào sắp tới.</p>
              </div>

              <div v-for="item in upcoming" :key="item.id" class="p-3 border-start border-4 border-primary bg-theme-light rounded-3 mb-3 shadow-sm hover-lift border border-theme border-start-4">
                <div class="d-flex justify-content-between align-items-start">
                  <div class="min-width-0 flex-grow-1 me-2">
                    <h6 class="fw-bold mb-1 text-theme-main text-truncate" :title="item.reason">{{ item.reason }}</h6>
                    <div class="small text-theme-muted text-truncate" :title="item.location"><i class="bi bi-hospital me-1"></i> {{ item.location }}</div>
                    <div class="small text-primary fw-bold mt-1 text-truncate" :title="`BS. ${item.doctor}`"><i class="bi bi-person-badge me-1"></i> BS. {{ item.doctor }}</div>
                  </div>
                  <div class="d-flex flex-column align-items-end flex-shrink-0">
                    <span class="badge bg-primary px-3 py-2 mb-2 shadow-sm">{{ item.day }} {{ item.month }}</span>
                    <div v-if="canEdit" class="dropdown">
                      <button class="btn btn-sm btn-light bg-theme-light text-theme-main border border-theme rounded-circle" data-bs-toggle="dropdown"><i class="bi bi-three-dots-vertical"></i></button>
                      <ul class="dropdown-menu dropdown-menu-end shadow-lg border border-theme bg-theme-card">
                        <li><button class="dropdown-item text-theme-main" @click="editAppointment(item)"><i class="bi bi-pencil me-2"></i>Sửa</button></li>
                        <li><button class="dropdown-item text-danger" @click="deleteAppointment(item.id)"><i class="bi bi-trash me-2"></i>Xóa</button></li>
                      </ul>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            
            <div class="tab-pane fade" id="past" role="tabpanel">
              <div class="table-responsive">
                <table class="table table-hover align-middle">
                  <thead>
                    <tr class="table-dark-custom">
                      <th class="py-3 px-3 text-theme-main">Ngày khám</th>
                      <th class="text-theme-main">Bác sĩ</th>
                      <th class="text-theme-main">Nơi khám</th>
                      <th class="text-theme-main">Kết luận</th>
                      <th v-if="canEdit"></th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="item in past" :key="item.id">
                      <td class="py-3 px-3">
                        <div class="fw-bold text-theme-main">{{ item.date }}</div>
                        <small class="text-theme-muted">{{ item.time }}</small>
                      </td>
                      <td class="small text-theme-main text-truncate" style="max-width: 150px;" :title="item.doctor">{{ item.doctor }}</td>
                      <td class="small text-theme-muted text-truncate" style="max-width: 200px;" :title="item.location">{{ item.location }}</td>
                      <td><span class="badge bg-theme-light text-theme-main border border-theme fw-normal text-truncate d-inline-block" style="max-width: 150px;" :title="item.diagnosis || 'Chưa cập nhật'">{{ item.diagnosis || 'Chưa cập nhật' }}</span></td>
                      <td v-if="canEdit" class="text-end">
                        <button class="btn btn-sm btn-light bg-theme-light text-primary me-1 border border-theme" @click="editAppointment(item)"><i class="bi bi-pencil"></i></button>
                        <button class="btn btn-sm btn-light bg-theme-light text-danger border border-theme" @click="deleteAppointment(item.id)"><i class="bi bi-trash"></i></button>
                      </td>
                    </tr>
                    <tr v-if="past.length === 0">
                      <td colspan="5" class="text-center py-5 text-theme-muted italic">Chưa có dữ liệu lịch sử khám bệnh</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </div>

      
      <div class="col-lg-4">
        <div class="bg-theme-card p-4 rounded-4 shadow-sm text-center border border-theme mb-4 position-sticky notes-card" style="top: 20px;">
          <div class="bg-primary bg-opacity-10 p-4 rounded-circle d-inline-block mb-3">
            <i class="bi bi-heart-pulse text-primary display-5"></i>
          </div>
          <h6 class="fw-bold text-theme-main">Ghi chú sức khỏe</h6>
          <p class="small text-theme-muted mb-4">Theo dõi lịch khám định kỳ giúp bạn chủ động bảo vệ sức khỏe bản thân và gia đình.</p>
          <ul class="list-unstyled text-start small text-theme-muted">
            <li class="mb-3 d-flex gap-2">
              <i class="bi bi-check-circle-fill text-success"></i> 
              <span class="text-theme-muted">Luôn mang theo sổ khám bệnh và các kết quả xét nghiệm cũ.</span>
            </li>
            <li class="mb-3 d-flex gap-2">
              <i class="bi bi-check-circle-fill text-success"></i> 
              <span class="text-theme-muted">Ghi chú các triệu chứng bất thường trước khi gặp bác sĩ.</span>
            </li>
            <li class="d-flex gap-2">
              <i class="bi bi-check-circle-fill text-success"></i> 
              <span class="text-theme-muted">Tuân thủ đúng giờ hẹn để tránh việc chờ đợi lâu.</span>
            </li>
          </ul>
        </div>
      </div>
    </div>

    
    <div v-if="showModal" class="modal-overlay d-flex align-items-center justify-content-center p-3" @click.self="showModal = false">
      <div class="bg-theme-card rounded-4 shadow-lg w-100 overflow-hidden border border-theme" style="max-width: 550px;">
        <div class="p-3 border-bottom border-theme d-flex justify-content-between align-items-center bg-theme-light">
          <h5 class="mb-0 fw-bold text-theme-main">{{ editingId ? 'Cập nhật lịch hẹn' : 'Đặt lịch hẹn mới' }}</h5>
          <button class="btn-close" :class="{'btn-close-white': isDarkMode}" @click="showModal = false"></button>
        </div>
        <form @submit.prevent="submitForm" class="p-4 bg-theme-card">
          <div class="mb-3">
            <label class="form-label small fw-bold text-theme-main">Lý do khám / Nội dung</label>
            <input type="text" class="form-control bg-theme-light text-theme-main border-theme" v-model="form.reason" placeholder="VD: Khám tổng quát, Đau đầu..." required>
          </div>
          <div class="row mb-3">
            <div class="col-md-6 mb-3 mb-md-0">
              <label class="form-label small fw-bold text-theme-main">Ngày và giờ hẹn</label>
              <SmartDateInput class="form-control bg-theme-light text-theme-main border-theme" v-model="form.appointmentDate" mode="datetime" required />
            </div>
            <div class="col-md-6">
              <label class="form-label small fw-bold text-theme-main">Bác sĩ phụ trách</label>
              <input type="text" class="form-control bg-theme-light text-theme-main border-theme" v-model="form.doctorName" placeholder="VD: BS. Nguyễn Văn A">
            </div>
          </div>
          <div class="mb-3">
            <label class="form-label small fw-bold text-theme-main">Địa điểm / Cơ sở y tế</label>
            <input type="text" class="form-control bg-theme-light text-theme-main border-theme" v-model="form.location" placeholder="VD: Bệnh viện Đa khoa Trung ương..." required>
          </div>
          <div class="mb-3" v-if="editingId">
            <label class="form-label small fw-bold text-theme-main">Kết luận / Chẩn đoán (Nếu có)</label>
            <textarea class="form-control bg-theme-light text-theme-main border-theme" v-model="form.diagnosis" rows="2" placeholder="Nhập kết luận của bác sĩ sau khi khám xong..."></textarea>
          </div>
          <div class="d-grid mt-4">
            <button type="submit" class="btn btn-primary rounded-pill py-2 fw-bold" :disabled="submitting">
              <span v-if="submitting" class="spinner-border spinner-border-sm me-2"></span>
              {{ editingId ? 'Cập nhật ngay' : 'Lưu lịch hẹn' }}
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

const upcoming = ref([]);
const past = ref([]);
const profiles = ref([]);
const selectedProfileId = ref(null);
const canEdit = ref(false);
const loading = ref(true);
const showModal = ref(false);
const submitting = ref(false);
const editingId = ref(null);

const isDarkMode = computed(() => document.documentElement.getAttribute('data-bs-theme') === 'dark');

const form = reactive({
  reason: '',
  appointmentDate: '',
  doctorName: '',
  location: '',
  diagnosis: ''
});

const fetchAppointments = async () => {
  loading.value = true;
  try {
    const url = selectedProfileId.value ? `/api/appointments?profileId=${selectedProfileId.value}` : '/api/appointments';
    const res = await axios.get(url);
    const data = res.data.appointments || [];
    profiles.value = res.data.profiles || [];
    selectedProfileId.value = res.data.currentProfileId;
    canEdit.value = res.data.canEdit;

    const now = new Date();
    
    upcoming.value = [];
    past.value = [];

    data.forEach(a => {
      const appDate = new Date(a.appointmentDate);
      if (appDate >= now) {
        upcoming.value.push({
          id: a.id,
          day: appDate.getDate(),
          month: 'T' + (appDate.getMonth() + 1),
          reason: a.reason,
          location: a.location,
          doctor: a.doctorName || 'Chưa rõ',
          rawDate: a.appointmentDate,
          diagnosis: a.diagnosis
        });
      } else {
        past.value.push({
          id: a.id,
          date: appDate.toLocaleDateString('vi-VN'),
          time: appDate.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }),
          doctor: a.doctorName || '---',
          location: a.location,
          diagnosis: a.diagnosis,
          rawDate: a.appointmentDate,
          reason: a.reason
        });
      }
    });

    
    upcoming.value.sort((a, b) => new Date(a.rawDate) - new Date(b.rawDate));
    past.value.sort((a, b) => new Date(b.rawDate) - new Date(a.rawDate));

  } catch (err) {
    console.error("Lỗi tải lịch hẹn:", err);
  } finally {
    loading.value = false;
  }
};

const openCreateModal = () => {
  editingId.value = null;
  form.reason = '';
  form.appointmentDate = '';
  form.doctorName = '';
  form.location = '';
  form.diagnosis = '';
  showModal.value = true;
};

const editAppointment = (item) => {
  editingId.value = item.id;
  form.reason = item.reason;
  form.appointmentDate = item.rawDate;
  form.doctorName = item.doctor === '---' || item.doctor === 'Chưa rõ' ? '' : item.doctor;
  form.location = item.location;
  form.diagnosis = item.diagnosis || '';
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
    const res = await axios.post('/api/appointments', payload);
    if (res.data.success) {
      toast.success(res.data.message);
      showModal.value = false;
      fetchAppointments();
    }
  } catch (err) {
    toast.error(err.response?.data?.message || err.message);
  } finally {
    submitting.value = false;
  }
};

const deleteAppointment = async (id) => {
  if (!await toast.confirm("Xác nhận xóa", "Bạn có chắc chắn muốn xóa lịch hẹn này?")) return;
  try {
    const res = await axios.delete(`/api/appointments/${id}`);
    if (res.data.success) {
      fetchAppointments();
    }
  } catch (err) {
    toast.error(err.response?.data?.message || err.message);
  }
};

const formatDate = (dateStr) => {
  if (!dateStr) return '---';
  return new Date(dateStr).toLocaleDateString('vi-VN');
};

onMounted(fetchAppointments);
</script>

<style scoped>
.hover-lift { transition: transform 0.2s, box-shadow 0.2s; cursor: pointer; }
.hover-lift:hover { transform: translateY(-3px); box-shadow: 0 8px 15px rgba(0,0,0,0.05) !important; }
.border-dashed { border: 2px dashed var(--border-color) !important; }
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.6); backdrop-filter: blur(4px); z-index: 2000; }
.nav-pills .nav-link { color: var(--text-muted); font-weight: 500; }
.nav-pills .nav-link.active { background-color: var(--bg-card); color: var(--primary-color); box-shadow: 0 2px 5px rgba(0,0,0,0.1); }
.italic { font-style: italic; }
.border-theme { border-color: var(--border-color) !important; }
.animate-fade-in { animation: fadeIn 0.4s ease-out; }
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
.page-toolbar-actions { width: min(100%, 680px); }
.profile-selector { width: 100%; }
.profile-select { width: 100%; min-width: 0; max-width: 100%; }

@media (max-width: 767.98px) {
  .appointments-page {
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

  .page-toolbar-actions {
    width: 100%;
  }

  .profile-select,
  .page-toolbar-actions .btn {
    width: 100%;
    min-width: 0;
  }

  .appointments-page .p-4 {
    padding: 0.9rem !important;
  }

  .appointments-page .nav.nav-pills {
    width: 100%;
    display: flex !important;
  }

  .appointments-page .nav.nav-pills .nav-item {
    flex: 1 1 50%;
  }

  .appointments-page .nav.nav-pills .nav-link {
    width: 100%;
    padding-left: 0.6rem !important;
    padding-right: 0.6rem !important;
    font-size: 0.85rem;
  }

  .notes-card {
    position: static !important;
  }
}
</style>
