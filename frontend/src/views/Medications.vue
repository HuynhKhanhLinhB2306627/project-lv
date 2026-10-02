<template>
  <div class="container-fluid py-4 animate-fade-in medications-page">
    
    <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-4 gap-3 page-toolbar">
      <div>
        <h2 class="fw-bold text-theme-main mb-1">
          <i class="bi bi-capsule-pill text-primary me-2"></i>Lịch nhắc uống thuốc
        </h2>
        <p class="text-theme-muted mb-0">Quản lý và theo dõi lịch uống thuốc cho từng hồ sơ sức khỏe</p>
      </div>
      <div class="row g-2 page-toolbar-actions">
        <div class="col-12 col-md-8">
          <div class="profile-selector shadow-sm rounded-pill bg-theme-card ps-3 pe-2 py-1 border border-theme d-flex align-items-center">
            <i class="bi bi-person-circle text-primary me-2"></i>
            <select class="form-select border-0 bg-transparent fw-semibold text-theme-main profile-select py-0" v-model="selectedProfileId" @change="fetchData" style="box-shadow: none;">
              <option v-for="p in profiles" :key="p.id" :value="p.id" class="bg-theme-card text-theme-main">{{ p.fullName }} ({{ p.profileName }})</option>
            </select>
          </div>
        </div>
        <div class="col-12 col-md-4 d-grid">
          <button v-if="canEdit" class="btn btn-primary rounded-pill px-3 shadow-sm hover-scale fw-bold" @click="openCreateModal">
            <i class="bi bi-plus-lg me-1"></i> Thêm liệu trình
          </button>
        </div>
      </div>
    </div>

    
    <div class="bg-theme-card border border-theme shadow-sm rounded-4 overflow-hidden mb-4">
      <div class="card-header bg-theme-card border-0 p-0">
        <ul class="nav nav-tabs nav-justified border-0 custom-tabs border-bottom border-theme">
          <li class="nav-item">
            <button class="nav-link bg-theme-card text-theme-muted" :class="{ active: activeTab === 'reminders' }" @click="activeTab = 'reminders'">
              <i class="bi bi-clock-history me-2"></i>Lịch uống hôm nay
            </button>
          </li>
          <li class="nav-item">
            <button class="nav-link bg-theme-card text-theme-muted" :class="{ active: activeTab === 'courses' }" @click="activeTab = 'courses'">
              <i class="bi bi-list-check me-2"></i>Danh sách liệu trình
            </button>
          </li>
        </ul>
      </div>

      <div class="card-body p-4 bg-theme-light bg-opacity-50">
        
        <div v-if="activeTab === 'reminders'" class="animate-slide-up">
          <div v-if="remindersLoading" class="text-center py-5">
            <div class="spinner-grow text-primary" role="status"></div>
            <p class="text-theme-muted mt-2">Đang tải lịch nhắc...</p>
          </div>
          
          <div v-else-if="todayReminders.length === 0" class="text-center py-5">
            <div class="mb-3">
              <i class="bi bi-calendar-x display-1 text-theme-muted opacity-25"></i>
            </div>
            <h5 class="text-theme-muted">Không có lịch nhắc uống thuốc cho hôm nay</h5>
            <p class="small text-theme-muted">Hãy thêm liệu trình mới để nhận được thông báo nhắc nhở.</p>
          </div>
          
          <div v-else class="row g-4">
            
            <div class="col-12">
              <div class="timeline-container border-theme">
                <div v-for="rem in todayReminders" :key="rem.id" class="timeline-item mb-4" :class="getReminderStatusClass(rem.status)">
                  <div class="timeline-time fw-bold text-theme-main">{{ rem.time }}</div>
                  <div class="bg-theme-card timeline-card border border-theme shadow-sm hover-lift rounded-3">
                    <div class="card-body p-3">
                      <div class="d-flex justify-content-between align-items-center">
                        <div class="d-flex gap-3 align-items-center min-width-0 flex-grow-1">
                          <div class="reminder-icon-box rounded-3 text-white shadow-sm flex-shrink-0" :class="getIconBgClass(rem.status)">
                            <i class="bi bi-prescription fs-5"></i>
                          </div>
                          <div class="min-width-0">
                            <h6 class="fw-bold mb-1 text-theme-main text-truncate" :title="rem.medicineName">{{ rem.medicineName }}</h6>
                            <div class="d-flex flex-wrap gap-2 small">
                              <span class="badge bg-theme-light text-theme-main border border-theme"><i class="bi bi-droplet me-1"></i>{{ rem.dosage }}</span>
                              <span class="badge bg-theme-light text-theme-main border border-theme"><i class="bi bi-clock me-1"></i>{{ rem.timing }}</span>
                              <span class="text-primary fw-semibold text-truncate" style="max-width: 150px;"><i class="bi bi-person me-1"></i>{{ rem.profileName }}</span>
                            </div>
                          </div>
                        </div>
                        <div class="action-buttons flex-shrink-0">
                          <div v-if="rem.status === 'PENDING'" class="d-flex gap-2">
                            <button class="btn btn-outline-success btn-sm rounded-pill px-3 shadow-sm fw-bold" @click="updateStatus(rem.id, 'TAKEN')">
                              <i class="bi bi-check2 me-1"></i>Đã uống
                            </button>
                            <button class="btn btn-outline-danger btn-sm rounded-pill px-3 shadow-sm fw-bold" @click="updateStatus(rem.id, 'SKIPPED')">
                              <i class="bi bi-x-lg me-1"></i>Bỏ qua
                            </button>
                          </div>
                          <div v-else class="status-indicator d-flex align-items-center gap-2">
                            <span :class="rem.status === 'TAKEN' ? 'text-success' : 'text-danger'" class="fw-bold small">
                              {{ rem.status === 'TAKEN' ? 'Đã uống' : 'Đã bỏ qua' }}
                            </span>
                            <i :class="rem.status === 'TAKEN' ? 'bi bi-check-circle-fill text-success' : 'bi bi-x-circle-fill text-danger'"></i>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        
        <div v-if="activeTab === 'courses'" class="animate-slide-up">
          <div v-if="loading" class="text-center py-5">
            <div class="spinner-grow text-primary" role="status"></div>
          </div>

          <div v-else-if="courses.length === 0" class="text-center py-5 bg-theme-card rounded-4 border border-dashed border-theme">
            <i class="bi bi-clipboard-x display-4 text-theme-muted opacity-25"></i>
            <h5 class="mt-3 text-theme-muted">Chưa có liệu trình thuốc nào cho hồ sơ này.</h5>
          </div>

          <div v-else class="row g-4">
            <div class="col-xl-6" v-for="course in courses" :key="course.id">
              <div class="bg-theme-card h-100 border border-theme shadow-sm rounded-4 overflow-hidden course-card hover-lift">
                <div class="card-body p-4">
                  <div class="d-flex justify-content-between align-items-start mb-3">
                    <div class="min-width-0 flex-grow-1">
                      <h5 class="fw-bold mb-2 text-theme-main text-truncate" :title="course.medicineName">{{ course.medicineName }}</h5>
                      <div class="d-flex flex-wrap gap-2 mb-2">
                        <span class="badge bg-primary bg-opacity-10 text-primary border border-primary border-opacity-25 rounded-pill">{{ course.frequency }}</span>
                        <span class="badge bg-info bg-opacity-10 text-info border border-info border-opacity-25 rounded-pill">{{ course.timing }}</span>
                      </div>
                    </div>
                    <div class="flex-shrink-0">
                      <div v-if="canEdit" class="dropdown">
                        <button class="btn btn-light bg-theme-light text-theme-main btn-sm rounded-circle shadow-sm border border-theme" data-bs-toggle="dropdown"><i class="bi bi-three-dots-vertical"></i></button>
                        <ul class="dropdown-menu dropdown-menu-end shadow border border-theme p-2 bg-theme-card">
                          <li><button class="dropdown-item text-theme-main rounded-2" @click="editCourse(course)"><i class="bi bi-pencil me-2"></i>Chỉnh sửa</button></li>
                          <li><hr class="dropdown-divider border-theme"></li>
                          <li><button class="dropdown-item text-danger rounded-2" @click="deleteCourse(course.id)"><i class="bi bi-trash me-2"></i>Xóa liệu trình</button></li>
                        </ul>
                      </div>
                    </div>
                  </div>
                  
                  <div class="course-details bg-theme-light bg-opacity-50 rounded-3 p-3 mb-3 border border-theme">
                    <div class="row g-3">
                      <div class="col-6 border-end border-theme">
                        <div class="text-theme-muted small mb-1"><i class="bi bi-droplet me-1"></i> Liều lượng</div>
                        <div class="fw-bold text-theme-main">{{ course.dosage }} ({{ course.unit }})</div>
                      </div>
                      <div class="col-6">
                        <div class="text-theme-muted small mb-1"><i class="bi bi-calendar-range me-1"></i> Thời gian</div>
                        <div class="fw-bold text-theme-main">{{ course.duration }} ngày</div>
                      </div>
                    </div>
                  </div>
                  
                  <div class="d-flex justify-content-between align-items-center small">
                    <div class="text-theme-muted">
                      <i class="bi bi-calendar3 me-1"></i> {{ formatDate(course.startDate) }} - {{ formatDate(course.endDate) }}
                    </div>
                    <div class="progress flex-grow-1 mx-3 bg-theme-light border border-theme" style="height: 6px;">
                      <div class="progress-bar bg-primary rounded-pill shadow-sm" :style="{ width: getCourseProgress(course) + '%' }"></div>
                    </div>
                    <div class="fw-bold text-primary">{{ getCourseProgress(course) }}%</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    
    <div v-if="showModal" class="modal-overlay d-flex align-items-center justify-content-center p-3" @click.self="showModal = false">
      <div class="bg-theme-card rounded-4 shadow-lg w-100 overflow-hidden border border-theme d-flex flex-column" style="max-width: 600px; max-height: 90vh;">
        <div class="modal-header border-0 bg-primary text-white p-4 flex-shrink-0">
          <h5 class="modal-title fw-bold mb-0">
            <i class="bi bi-plus-circle me-2"></i>{{ editingId ? 'Cập nhật liệu trình' : 'Thêm liệu trình thuốc mới' }}
          </h5>
          <button class="btn-close btn-close-white" @click="showModal = false"></button>
        </div>
        <div class="overflow-auto custom-scrollbar flex-grow-1 bg-theme-card">
          <form @submit.prevent="submitForm" class="p-4">
            <div class="mb-4 position-relative">
              <label class="form-label fw-bold text-theme-main">Chọn thuốc</label>
              <div v-if="!selectedMedicineName" class="input-group shadow-sm rounded-3">
                <span class="input-group-text bg-theme-light border-theme border-end-0"><i class="bi bi-search text-theme-muted"></i></span>
                <input type="text" class="form-control bg-theme-light text-theme-main border-theme border-start-0" v-model="medicineSearchQuery" placeholder="Tìm tên thuốc trong danh mục..." @focus="showMedicineSuggestions = true">
                <button class="btn btn-outline-primary border-theme" type="button" @click="showAddMedicineModal = true">
                  <i class="bi bi-plus-lg"></i> Mới
                </button>
              </div>
              
              <ul v-if="!selectedMedicineName && showMedicineSuggestions && filteredMedicines.length > 0" class="list-group position-absolute w-100 shadow-lg z-3 mt-1 overflow-auto border border-theme" style="max-height: 200px;">
                <li v-for="m in filteredMedicines" :key="m.id" class="list-group-item list-group-item-action bg-theme-card text-theme-main border-theme cursor-pointer d-flex justify-content-between align-items-center" @click="selectMedicine(m)">
                  <div class="min-width-0 flex-grow-1">
                    <div class="fw-bold text-truncate" :title="m.name">{{ m.name }}</div>
                    <small class="text-theme-muted">{{ m.unit }}</small>
                  </div>
                  <i class="bi bi-plus-circle text-primary"></i>
                </li>
              </ul>
              <div v-if="selectedMedicineName" class="mt-2 p-3 bg-theme-light border border-theme rounded-3 d-flex justify-content-between align-items-center shadow-sm">
                <div class="fw-bold text-primary fs-5"><i class="bi bi-capsule-pill me-2"></i>{{ selectedMedicineName }}</div>
                <button type="button" class="btn btn-sm btn-outline-danger rounded-pill fw-bold" @click="clearSelectedMedicine">
                  <i class="bi bi-arrow-counterclockwise me-1"></i> Chọn lại
                </button>
              </div>
            </div>
            
            <div class="row g-3 mb-4">
              <div class="col-md-6">
                <label class="form-label fw-bold text-theme-main">Liều dùng mỗi lần</label>
                <div class="input-group shadow-sm rounded-3">
                  <span class="input-group-text bg-theme-light border-theme"><i class="bi bi-droplet text-primary"></i></span>
                  <input type="text" class="form-control bg-theme-light text-theme-main border-theme" v-model="form.dosage" placeholder="VD: 1 viên, 5ml..." required>
                </div>
              </div>
              <div class="col-md-6">
                <label class="form-label fw-bold text-theme-main">Tần suất uống</label>
                <div class="input-group shadow-sm rounded-3">
                  <span class="input-group-text bg-theme-light border-theme"><i class="bi bi-repeat text-primary"></i></span>
                  <select class="form-select bg-theme-light text-theme-main border-theme" v-model="form.frequency" @change="initReminderTimes" required>
                    <option value="1 lần/ngày">1 lần/ngày</option>
                    <option value="2 lần/ngày">2 lần/ngày</option>
                    <option value="3 lần/ngày">3 lần/ngày</option>
                  </select>
                </div>
              </div>
            </div>

            <div class="mb-4">
              <label class="form-label fw-bold text-theme-main">Đặt giờ uống thuốc</label>
              <div class="row g-2">
                <div v-for="(time, index) in form.reminderTimes" :key="index" class="col-12 col-sm-4">
                  <div class="input-group input-group-sm shadow-sm border border-theme rounded-2 overflow-hidden">
                    <span class="input-group-text bg-theme-light border-0 text-theme-muted small">Lần {{ index + 1 }}</span>
                    <input type="time" class="form-control bg-theme-card border-0 text-theme-main" v-model="form.reminderTimes[index]" required>
                  </div>
                </div>
              </div>
              <div class="form-text small mt-2 text-primary fw-medium"><i class="bi bi-info-circle me-1"></i>Hệ thống sẽ gửi email nhắc nhở trước 30 phút.</div>
            </div>

            <div class="mb-4">
              <label class="form-label fw-bold text-theme-main">Thời điểm lý tưởng</label>
              <div class="row g-3">
                <div class="col-6" v-for="t in timingOptions" :key="t.value">
                  <div class="form-check custom-radio p-0">
                    <input class="btn-check" type="radio" :value="t.value" :id="'timing' + t.value" v-model="form.timing">
                    <label class="btn btn-outline-theme w-100 border border-theme text-start p-2 shadow-sm text-theme-main" :for="'timing' + t.value">
                      <i :class="'bi bi-' + t.icon + ' me-2 text-primary'"></i> {{ t.label }}
                    </label>
                  </div>
                </div>
              </div>
            </div>

            <div class="row g-3 mb-4">
              <div class="col-md-6">
                <label class="form-label fw-bold text-theme-main">Ngày bắt đầu</label>
                <div class="input-group shadow-sm rounded-3">
                  <span class="input-group-text bg-theme-light border-theme"><i class="bi bi-calendar-event text-primary"></i></span>
                  <SmartDateInput class="form-control bg-theme-light text-theme-main border-theme" v-model="form.startDate" required />
                </div>
              </div>
              <div class="col-md-6">
                <label class="form-label fw-bold text-theme-main">Thời gian (ngày)</label>
                <div class="input-group shadow-sm rounded-3">
                  <span class="input-group-text bg-theme-light border-theme"><i class="bi bi-hourglass-split text-primary"></i></span>
                  <input type="number" class="form-control bg-theme-light text-theme-main border-theme" v-model="form.durationInDays" min="1" required>
                </div>
              </div>
            </div>

            <div class="mb-4">
              <label class="form-label fw-bold text-theme-main">Ghi chú thêm</label>
              <div class="input-group shadow-sm rounded-3">
                <span class="input-group-text bg-theme-light border-theme"><i class="bi bi-sticky text-primary"></i></span>
                <textarea class="form-control bg-theme-light text-theme-main border-theme" v-model="form.notes" rows="2" placeholder="VD: Uống sau ăn no, tránh sữa..."></textarea>
              </div>
            </div>

            <div class="d-grid mt-2">
              <button type="submit" class="btn btn-primary btn-lg rounded-pill fw-bold shadow-sm" :disabled="submitting">
                <span v-if="submitting" class="spinner-border spinner-border-sm me-2"></span>
                {{ editingId ? 'Cập nhật liệu trình' : 'Lưu liệu trình ngay' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    
    <div v-if="showAddMedicineModal" class="modal-overlay d-flex align-items-center justify-content-center p-3" @click.self="showAddMedicineModal = false" style="z-index: 2100;">
      <div class="bg-theme-card rounded-4 shadow-lg w-100 border border-theme" style="max-width: 400px;">
        <div class="modal-header border-0 bg-info text-white p-3">
          <h6 class="modal-title fw-bold">Yêu cầu thêm thuốc mới</h6>
          <button class="btn-close btn-close-white" @click="showAddMedicineModal = false"></button>
        </div>
        <form @submit.prevent="submitMedicineRequest" class="p-4 bg-theme-card">
          <div class="mb-3">
            <label class="form-label small fw-bold text-theme-main">Tên thuốc</label>
            <input type="text" class="form-control bg-theme-light text-theme-main border-theme" v-model="medicineRequest.name" placeholder="VD: Panadol, Paracetamol..." required>
          </div>
          <div class="mb-3">
            <label class="form-label small fw-bold text-theme-main">Đơn vị tính</label>
            <input type="text" class="form-control bg-theme-light text-theme-main border-theme" v-model="medicineRequest.unit" placeholder="VD: Viên, Gói, Chai..." required>
          </div>
          <div class="mb-4">
            <label class="form-label small fw-bold text-theme-main">Công dụng / Mô tả</label>
            <textarea class="form-control bg-theme-light text-theme-main border-theme" v-model="medicineRequest.description" rows="2" placeholder="Thuốc giảm đau..."></textarea>
          </div>
          <button type="submit" class="btn btn-info text-white w-100 rounded-pill fw-bold shadow-sm" :disabled="requesting">
            <span v-if="requesting" class="spinner-border spinner-border-sm me-2"></span> Gửi yêu cầu duyệt
          </button>
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

const activeTab = ref('reminders');
const courses = ref([]);
const profiles = ref([]);
const todayReminders = ref([]);
const medicines = ref([]);
const selectedProfileId = ref(null);
const canEdit = ref(false);
const loading = ref(true);
const remindersLoading = ref(true);
const showModal = ref(false);
const showAddMedicineModal = ref(false);
const submitting = ref(false);
const requesting = ref(false);
const editingId = ref(null);

const isDarkMode = computed(() => document.documentElement.getAttribute('data-bs-theme') === 'dark');

const medicineSearchQuery = ref('');
const showMedicineSuggestions = ref(false);
const selectedMedicineName = ref('');

const filteredMedicines = computed(() => {
  if (!medicineSearchQuery.value) return medicines.value;
  const q = medicineSearchQuery.value.toLowerCase();
  return medicines.value.filter(m => m.name.toLowerCase().includes(q));
});

const selectMedicine = (m) => {
  form.medicineId = m.id;
  selectedMedicineName.value = m.name;
  medicineSearchQuery.value = m.name;
  showMedicineSuggestions.value = false;
};

const clearSelectedMedicine = () => {
  form.medicineId = '';
  selectedMedicineName.value = '';
  medicineSearchQuery.value = '';
};

const timingOptions = [
  { value: 'Trước ăn', label: 'Trước khi ăn', icon: 'clock-history' },
  { value: 'Sau ăn', label: 'Sau khi ăn', icon: 'clock-history' },
  { value: 'Trong khi ăn', label: 'Trong bữa ăn', icon: 'utensils' },
  { value: 'Trước khi đi ngủ', label: 'Trước khi ngủ', icon: 'moon-stars' }
];

const form = reactive({
  medicineId: '',
  dosage: '',
  frequency: '2 lần/ngày',
  timing: 'Sau ăn',
  startDate: new Date().toISOString().split('T')[0],
  durationInDays: 7,
  reminderTimes: ['08:00', '20:00'],
  notes: ''
});

const initReminderTimes = () => {
  const count = parseInt(form.frequency);
  if (count === 1) form.reminderTimes = ['08:00'];
  else if (count === 2) form.reminderTimes = ['08:00', '20:00'];
  else if (count === 3) form.reminderTimes = ['08:00', '12:00', '20:00'];
};

const medicineRequest = reactive({
  name: '',
  unit: '',
  description: ''
});

const fetchData = async () => {
  loading.value = true;
  try {
    const url = selectedProfileId.value ? `/api/medications?profileId=${selectedProfileId.value}` : '/api/medications';
    const res = await axios.get(url);
    courses.value = res.data.courses || [];
    profiles.value = res.data.profiles || [];
    medicines.value = res.data.medicines || [];
    selectedProfileId.value = res.data.currentProfileId;
    canEdit.value = res.data.canEdit;
    fetchReminders();
  } catch (err) {
    console.error("Lỗi tải dữ liệu", err);
  } finally {
    loading.value = false;
  }
};

const fetchReminders = async () => {
  remindersLoading.value = true;
  try {
    const res = await axios.get(`/api/medications/today-reminders?profileId=${selectedProfileId.value || ''}`);
    todayReminders.value = res.data;
  } catch (err) {
    console.error("Lỗi tải lịch nhắc", err);
  } finally {
    remindersLoading.value = false;
  }
};

const submitMedicineRequest = async () => {
  requesting.value = true;
  try {
    const res = await axios.post('/api/medicines/request', medicineRequest);
    if (res.data.success) {
      toast.success(res.data.message);
      showAddMedicineModal.value = false;
      medicineRequest.name = '';
      medicineRequest.unit = '';
      medicineRequest.description = '';
    }
  } catch (err) {
    toast.error(err.response?.data?.message || "Lỗi gửi yêu cầu");
  } finally {
    requesting.value = false;
  }
};

const updateStatus = async (reminderId, status) => {
  try {
    await axios.post(`/api/medications/reminders/${reminderId}/status`, { status });
    fetchReminders();
  } catch (err) {
    toast.error(err.response?.data?.message || err.message);
  }
};

const openCreateModal = () => {
  editingId.value = null;
  form.medicineId = '';
  form.dosage = '';
  form.frequency = '2 lần/ngày';
  form.timing = 'Sau ăn';
  form.startDate = new Date().toISOString().split('T')[0];
  form.durationInDays = 7;
  showModal.value = true;
};

const editCourse = (course) => {
  editingId.value = course.id;
  form.medicineId = course.medicineId;
  selectedMedicineName.value = course.medicineName;
  medicineSearchQuery.value = course.medicineName;
  form.dosage = course.dosage;
  form.frequency = course.frequency;
  form.timing = course.timing;
  form.startDate = course.startDate;
  form.durationInDays = course.duration;
  showModal.value = true;
};

const submitForm = async () => {
  if (!form.medicineId) {
    toast.error("Vui lòng tìm và chọn một loại thuốc từ danh sách!");
    return;
  }
  submitting.value = true;
  try {
    const payload = {
      ...form,
      id: editingId.value,
      profileId: selectedProfileId.value
    };
    const res = await axios.post('/api/medications', payload);
    if (res.data.success) {
      showModal.value = false;
      fetchData();
    }
  } catch (err) {
    toast.error(err.response?.data?.message || err.message);
  } finally {
    submitting.value = false;
  }
};

const deleteCourse = async (id) => {
  if (!await toast.confirm("Xác nhận xóa", "Xóa liệu trình sẽ xóa toàn bộ lịch nhắc liên quan. Tiếp tục?")) return;
  try {
    const res = await axios.delete(`/api/medications/${id}`);
    if (res.data.success) {
      fetchData();
    }
  } catch (err) {
    toast.error(err.response?.data?.message || err.message);
  }
};

const formatDate = (dateStr) => {
  if (!dateStr) return '---';
  return new Date(dateStr).toLocaleDateString('vi-VN');
};

const getAutoStatusClass = (course) => {
  const now = new Date();
  now.setHours(0, 0, 0, 0);
  const start = new Date(course.startDate);
  const end = new Date(course.endDate);

  if (now < start) return 'bg-info bg-opacity-10 text-info border border-info border-opacity-25';
  if (now > end) return 'bg-secondary bg-opacity-10 text-secondary border border-secondary border-opacity-25';
  return 'bg-success bg-opacity-10 text-success border border-success border-opacity-25';
};

const formatAutoStatus = (course) => {
  const now = new Date();
  now.setHours(0, 0, 0, 0);
  const start = new Date(course.startDate);
  const end = new Date(course.endDate);

  if (now < start) return 'Chưa bắt đầu';
  if (now > end) return 'Đã kết thúc';
  return 'Đang điều trị';
};

const getStatusClass = (status) => {
  switch(status) {
    case 'ACTIVE': return 'bg-success bg-opacity-10 text-success border border-success border-opacity-25';
    case 'COMPLETED': return 'bg-secondary bg-opacity-10 text-secondary border border-secondary border-opacity-25';
    case 'STOPPED': return 'bg-danger bg-opacity-10 text-danger border border-danger border-opacity-25';
    default: return 'bg-theme-light text-theme-main border border-theme';
  }
};

const formatStatus = (status) => {
  switch(status) {
    case 'ACTIVE': return 'Đang điều trị';
    case 'COMPLETED': return 'Đã kết thúc';
    case 'STOPPED': return 'Tạm dừng';
    default: return status;
  }
};

const getReminderStatusClass = (status) => {
  switch(status) {
    case 'TAKEN': return 'status-taken';
    case 'SKIPPED': return 'status-skipped';
    default: return 'status-pending';
  }
};

const getIconBgClass = (status) => {
  switch(status) {
    case 'TAKEN': return 'bg-success';
    case 'SKIPPED': return 'bg-danger';
    default: return 'bg-primary';
  }
};

const getCourseProgress = (course) => {
  // Parse date and adjust to local timezone start/end of day
  const start = new Date(course.startDate);
  start.setHours(0, 0, 0, 0); // Bắt đầu từ 00:00:00 ngày bắt đầu
  
  const end = new Date(course.endDate);
  end.setHours(23, 59, 59, 999); // Kết thúc vào 23:59:59 ngày kết thúc
  
  const now = new Date();
  
  if (now < start) return 0;
  if (now > end) return 100;
  
  const total = end.getTime() - start.getTime();
  const current = now.getTime() - start.getTime();
  return Math.round((current / total) * 100);
};

onMounted(fetchData);
</script>

<style scoped>
.animate-fade-in { animation: fadeIn 0.4s ease-out; }
.animate-slide-up { animation: slideUp 0.4s ease-out; }

@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
@keyframes slideUp { from { transform: translateY(20px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }

.custom-tabs .nav-link {
  border: none;
  padding: 1rem;
  font-weight: 600;
  color: var(--text-muted);
  transition: all 0.3s;
  background: var(--bg-card);
}

.custom-tabs .nav-link:hover { color: var(--primary-color); background: var(--hover-bg); }
.custom-tabs .nav-link.active {
  color: var(--primary-color);
  border-bottom: 3px solid var(--primary-color);
  background: var(--bg-card);
}

.hover-lift { transition: all 0.3s cubic-bezier(0.165, 0.84, 0.44, 1); }
.hover-lift:hover { transform: translateY(-5px); box-shadow: 0 1rem 3rem rgba(0,0,0,.1) !important; }

.hover-scale { transition: transform 0.2s; }
.hover-scale:hover { transform: scale(1.05); }

.bg-primary.bg-opacity-10 { background-color: rgba(13, 110, 253, 0.1) !important; }

.course-card { border-radius: 1.25rem; }

.timeline-container {
  position: relative;
  padding-left: 2rem;
  border-left: 2px solid var(--border-color);
  margin-left: 4.5rem;
}

.timeline-item { position: relative; }
.timeline-item::before {
  content: '';
  position: absolute;
  left: -2.4rem;
  top: 10px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: var(--bg-card);
  border: 4px solid var(--primary-color);
  z-index: 1;
  transition: all 0.3s;
}

.timeline-time {
  position: absolute;
  left: -6.5rem;
  top: 8px;
  width: 4rem;
  text-align: right;
  font-size: 0.9rem;
}

.status-taken::before { border-color: var(--success-color); background: var(--success-color); }
.status-skipped::before { border-color: var(--danger-color); background: var(--danger-color); }
.status-pending::before { border-color: var(--primary-color); animation: pulse 2s infinite; }

@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(13, 110, 253, 0.4); }
  70% { box-shadow: 0 0 0 10px rgba(13, 110, 253, 0); }
  100% { box-shadow: 0 0 0 0 rgba(13, 110, 253, 0); }
}

.reminder-icon-box {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.medicine-icon {
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.6);
  backdrop-filter: blur(4px);
  z-index: 2000;
}

.custom-radio .btn-check:checked + label {
  background-color: var(--primary-color) !important;
  color: white !important;
  border-color: var(--primary-color) !important;
}

.btn-outline-theme:hover {
  background-color: var(--hover-bg);
}

.course-details { border: 1px solid var(--border-color); }
.border-theme { border-color: var(--border-color) !important; }
.page-toolbar-actions { width: min(100%, 680px); }
.profile-selector { width: 100%; }
.profile-select { min-width: 0; width: 100%; }

@media (max-width: 767.98px) {
  .medications-page {
    padding-top: 0.75rem !important;
    padding-left: 0.6rem !important;
    padding-right: 0.6rem !important;
  }

  .page-toolbar h2 {
    font-size: 1.15rem;
  }

  .page-toolbar p {
    font-size: 0.82rem;
  }

  .page-toolbar-actions {
    width: 100%;
  }

  .profile-selector {
    width: 100%;
  }

  .profile-select {
    min-width: 0;
    width: 100%;
  }

  .page-toolbar-actions .btn {
    width: 100%;
  }

  .medications-page .p-4 {
    padding: 0.9rem !important;
  }

  .timeline-container {
    padding-left: 0.8rem;
    margin-left: 0.4rem;
  }

  .timeline-time {
    position: static !important;
    margin-bottom: 0.35rem;
    display: inline-block;
    font-size: 0.78rem;
    background: var(--bg-light);
    padding: 0.1rem 0.45rem;
    border-radius: 999px;
  }

  .timeline-item::before {
    left: -0.85rem !important;
  }

  .timeline-card .card-body {
    padding: 0.75rem !important;
  }

  .action-buttons .btn {
    padding: 0.25rem 0.5rem;
    font-size: 0.72rem;
  }

  .course-card .card-body {
    padding: 0.85rem !important;
  }
}
</style>
