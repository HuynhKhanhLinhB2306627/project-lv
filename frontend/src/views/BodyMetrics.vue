<template>
  <div class="container-fluid py-2 body-metrics-page">
    <div class="d-flex justify-content-between align-items-center mb-4 page-toolbar">
      <h3 class="fw-bold text-theme-main"><i class="bi bi-activity text-primary me-2"></i>Chỉ số cơ thể</h3>
      <div class="row g-2 page-toolbar-actions">
        <div class="col-12 col-md-8">
          <div class="profile-selector shadow-sm rounded-pill bg-theme-card ps-3 pe-2 py-1 border border-theme d-flex align-items-center">
            <i class="bi bi-person-circle text-primary me-2"></i>
            <select class="form-select border-0 bg-transparent fw-semibold text-theme-main profile-select py-0" v-model="selectedProfileId" @change="fetchMetrics" style="box-shadow: none;">
              <option v-for="p in profiles" :key="p.id" :value="p.id" class="bg-theme-card text-theme-main">{{ p.fullName }} ({{ p.profileName }})</option>
            </select>
          </div>
        </div>
        <div class="col-12 col-md-4 d-grid">
          <button v-if="canEdit" class="btn btn-primary shadow-sm rounded-pill px-3" @click="openCreateModal">
            <i class="bi bi-plus-circle me-1"></i> Ghi nhận mới
          </button>
        </div>
      </div>
    </div>

    
    <div class="row g-4 mb-4" v-if="metrics.length > 0">
      <div class="col-6 col-md-3">
        <div class="bg-theme-card border border-theme shadow-sm rounded-4 p-3 text-center h-100">
          <small class="text-theme-muted fw-bold text-uppercase">Cân nặng</small>
          <h2 class="fw-bold mt-1 mb-0 text-theme-main">{{ metrics[0].weight }} <span class="fs-6 fw-normal">kg</span></h2>
        </div>
      </div>
      <div class="col-6 col-md-3">
        <div class="bg-theme-card border border-theme shadow-sm rounded-4 p-3 text-center h-100">
          <small class="text-theme-muted fw-bold text-uppercase">BMI hiện tại</small>
          <h2 class="fw-bold text-primary mt-1 mb-0">{{ metrics[0].bmi }}</h2>
        </div>
      </div>
      <div class="col-6 col-md-3">
        <div class="bg-theme-card border border-theme shadow-sm rounded-4 p-3 text-center h-100">
          <small class="text-theme-muted fw-bold text-uppercase">Huyết áp mới nhất</small>
          <h2 class="fw-bold text-danger mt-1 mb-0" v-if="metrics[0].systolic && metrics[0].diastolic">
            {{ metrics[0].systolic }}/{{ metrics[0].diastolic }}
          </h2>
          <h2 class="fw-bold text-theme-muted mt-1 mb-0" v-else>--/--</h2>
        </div>
      </div>
      <div class="col-6 col-md-3">
        <div class="bg-theme-card border border-theme shadow-sm rounded-4 p-3 text-center h-100">
          <small class="text-theme-muted fw-bold text-uppercase">Cập nhật lần cuối</small>
          <h4 class="fw-bold text-theme-muted mt-2 mb-0">{{ metrics[0].formattedDate }}</h4>
          <small class="text-theme-muted opacity-75">Lúc {{ metrics[0].formattedTime }}</small>
        </div>
      </div>
    </div>

    <div class="row g-4">
      
      <div class="col-lg-8">
        
        <div class="bg-theme-card border border-theme shadow-sm rounded-4 p-4 mb-4" v-if="metrics.length > 1">
          <h5 class="fw-bold mb-4 d-flex justify-content-between align-items-center text-theme-main">
            <span><i class="bi bi-graph-up text-primary me-2"></i>Biểu đồ xu hướng</span>
            <div class="btn-group btn-group-sm shadow-none">
              <button class="btn btn-outline-primary" :class="{active: chartType==='weight'}" @click="chartType='weight'">Cân nặng</button>
              <button class="btn btn-outline-primary" :class="{active: chartType==='bmi'}" @click="chartType='bmi'">Chỉ số BMI</button>
            </div>
          </h5>
          <div class="body-metrics-chart">
            <Line v-if="chartData" :data="chartData" :options="chartOptions" />
          </div>
        </div>

        <div class="bg-theme-card p-4 rounded-4 shadow-sm border border-theme">
          <h5 class="fw-bold mb-4 text-theme-main">Lịch sử đo lường</h5>
          
          <div v-if="loading" class="text-center py-5">
            <div class="spinner-border text-primary" role="status"></div>
          </div>

          <div v-else-if="metrics.length === 0" class="text-center py-5 text-theme-muted bg-theme-light rounded-4 border-dashed border-theme">
            <i class="bi bi-clipboard-x display-4 opacity-25"></i>
            <p class="mt-2 mb-0 text-theme-muted">Chưa có dữ liệu đo lường.</p>
          </div>

          <div class="table-responsive" v-else>
            <table class="table table-hover align-middle">
              <thead>
                <tr class="table-dark-custom">
                  <th class="py-3 px-3 text-theme-main">Thời gian đo</th>
                  <th class="text-theme-main">Cao (cm)</th>
                  <th class="text-theme-main">Nặng (kg)</th>
                  <th class="text-theme-main">BMI</th>
                  <th class="text-theme-main">Huyết áp</th>
                  <th v-if="canEdit"></th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="m in metrics" :key="m.id">
                  <td class="py-3 px-3">
                    <div class="fw-bold text-theme-main">{{ m.formattedDate }}</div>
                    <small class="text-theme-muted opacity-75">Lúc {{ m.formattedTime }}</small>
                  </td>
                  <td class="text-theme-main">{{ m.height }}</td>
                  <td class="text-theme-main">{{ m.weight }}</td>
                  <td><span class="badge" :class="getBmiBadgeClass(m.bmi)">{{ m.bmi }}</span></td>
                  <td class="small text-theme-main">
                    <span v-if="m.systolic && m.diastolic">{{ m.systolic }}/{{ m.diastolic }}</span>
                    <span v-else class="text-theme-muted italic">--</span>
                  </td>
                  <td v-if="canEdit" class="text-end">
                    <button class="btn btn-sm btn-light bg-theme-light text-primary me-1 border-theme" @click="editMetric(m)"><i class="bi bi-pencil"></i></button>
                    <button class="btn btn-sm btn-light bg-theme-light text-danger border-theme" @click="deleteMetric(m.id)"><i class="bi bi-trash"></i></button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      
      <div class="col-lg-4">
        <div class="bg-theme-card p-4 rounded-4 shadow-sm border border-theme mb-4">
          <h6 class="fw-bold mb-3 text-theme-main">Phân loại BMI (WHO)</h6>
          <div class="small text-theme-main">
            <div class="d-flex justify-content-between mb-2"><span>Dưới 18.5</span> <span class="badge bg-info">Gầy</span></div>
            <div class="d-flex justify-content-between mb-2"><span>18.5 - 24.9</span> <span class="badge bg-success">Bình thường</span></div>
            <div class="d-flex justify-content-between mb-2"><span>25.0 - 29.9</span> <span class="badge bg-warning text-dark">Tiền béo phì</span></div>
            <div class="d-flex justify-content-between mb-2"><span>Trên 30.0</span> <span class="badge bg-danger">Béo phì</span></div>
          </div>
        </div>


      </div>
    </div>

    
    <div v-if="showModal" class="modal-overlay d-flex align-items-center justify-content-center p-3" @click.self="showModal = false">
      <div class="bg-theme-card rounded-4 shadow-lg w-100 overflow-hidden border border-theme" style="max-width: 550px;">
        <div class="p-3 border-bottom d-flex justify-content-between align-items-center bg-theme-light border-theme">
          <h5 class="mb-0 fw-bold text-theme-main">{{ editingId ? 'Cập nhật chỉ số' : 'Ghi nhận chỉ số mới' }}</h5>
          <button class="btn-close" :class="{'btn-close-white': isDarkMode}" @click="showModal = false"></button>
        </div>
        <form @submit.prevent="submitForm" class="p-4 bg-theme-card">
          <div class="mb-3">
            <label class="form-label small fw-bold text-theme-main">Thời gian đo</label>
            <SmartDateInput class="form-control bg-theme-light text-theme-main border-theme" v-model="form.logDate" mode="datetime" required />
          </div>
          <div class="row mb-3">
            <div class="col-12 col-md-6">
              <label class="form-label small fw-bold text-theme-main">Chiều cao (cm)</label>
              <input type="number" class="form-control bg-theme-light text-theme-main border-theme" v-model="form.heightInCm" step="0.1" required>
            </div>
            <div class="col-12 col-md-6">
              <label class="form-label small fw-bold text-theme-main">Cân nặng (kg)</label>
              <input type="number" class="form-control bg-theme-light text-theme-main border-theme" v-model="form.weightInKg" step="0.1" required>
            </div>
          </div>
          <div class="row mb-3">
            <div class="col-12 col-md-6">
              <label class="form-label small fw-bold text-theme-main">Huyết áp tâm thu <span class="text-theme-muted fw-normal">(Tùy chọn)</span></label>
              <input type="number" class="form-control bg-theme-light text-theme-main border-theme" v-model="form.systolicBp" placeholder="VD: 120">
            </div>
            <div class="col-12 col-md-6">
              <label class="form-label small fw-bold text-theme-main">Huyết áp tâm trương <span class="text-theme-muted fw-normal">(Tùy chọn)</span></label>
              <input type="number" class="form-control bg-theme-light text-theme-main border-theme" v-model="form.diastolicBp" placeholder="VD: 80">
            </div>

          </div>
          <div class="row mb-3">
            <div class="col-12 col-md-6">
              <label class="form-label small fw-bold text-theme-main">Mỡ máu (1-3)</label>
              <select class="form-select bg-theme-light text-theme-main border-theme" v-model="form.cholesterol">
                <option value="1">Bình thường (1)</option>
                <option value="2">Trên bình thường (2)</option>
                <option value="3">Cao (3)</option>
              </select>
            </div>
            <div class="col-12 col-md-6">
              <label class="form-label small fw-bold text-theme-main">Đường huyết (1-3)</label>
              <select class="form-select bg-theme-light text-theme-main border-theme" v-model="form.glucose">
                <option value="1">Bình thường (1)</option>
                <option value="2">Trên bình thường (2)</option>
                <option value="3">Cao (3)</option>
              </select>
            </div>
          </div>
          <div class="row mb-3">
            <div class="col-12 mb-2"><label class="small fw-bold text-theme-main">Thói quen sinh hoạt</label></div>
            <div class="col-12 col-md-4">
              <div class="form-check small">
                <input class="form-check-input border-theme" type="checkbox" v-model="form.smoking" id="smokeCheck">
                <label class="form-check-label text-theme-main" for="smokeCheck">Hút thuốc</label>
              </div>
            </div>
            <div class="col-12 col-md-4">
              <div class="form-check small">
                <input class="form-check-input border-theme" type="checkbox" v-model="form.alcohol" id="alcoholCheck">
                <label class="form-check-label text-theme-main" for="alcoholCheck">Rượu bia</label>
              </div>
            </div>
            <div class="col-12 col-md-4">
              <div class="form-check small">
                <input class="form-check-input border-theme" type="checkbox" v-model="form.physicalActivity" id="exerciseCheck">
                <label class="form-check-label text-theme-main" for="exerciseCheck">Vận động</label>
              </div>
            </div>
          </div>
          <div class="d-grid mt-4">
            <button type="submit" class="btn btn-primary rounded-pill py-2 fw-bold" :disabled="submitting">
              <span v-if="submitting" class="spinner-border spinner-border-sm me-2"></span>
              {{ editingId ? 'Cập nhật ngay' : 'Lưu chỉ số' }}
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
import { Line } from 'vue-chartjs';
import { Chart as ChartJS, Title, Tooltip, Legend, LineElement, PointElement, CategoryScale, LinearScale } from 'chart.js';
import { toast } from '@/utils/toast';
import SmartDateInput from '@/components/common/SmartDateInput.vue';

ChartJS.register(Title, Tooltip, Legend, LineElement, PointElement, CategoryScale, LinearScale);

const metrics = ref([]);
const profiles = ref([]);
const selectedProfileId = ref(null);
const canEdit = ref(false);
const loading = ref(true);
const showModal = ref(false);
const submitting = ref(false);
const editingId = ref(null);
const chartType = ref('weight'); 

const isDarkMode = computed(() => document.documentElement.getAttribute('data-bs-theme') === 'dark');

const chartLabels = ref([]);
const chartWeights = ref([]);
const chartBmis = ref([]);

const form = reactive({
  heightInCm: '',
  weightInKg: '',
  systolicBp: '',
  diastolicBp: '',
  cholesterol: 1,
  glucose: 1,
  smoking: false,
  alcohol: false,
  physicalActivity: true,
  logDate: ''
});

const getComputedStyleValue = (varName) => {
  return getComputedStyle(document.documentElement).getPropertyValue(varName).trim();
};

const chartData = computed(() => {
  if (chartLabels.value.length === 0) return null;
  
  const labels = [...chartLabels.value].reverse();
  const data = chartType.value === 'weight' ? [...chartWeights.value].reverse() : [...chartBmis.value].reverse();
  const label = chartType.value === 'weight' ? 'Cân nặng (kg)' : 'Chỉ số BMI';
  const color = chartType.value === 'weight' ? '#0d6efd' : '#198754';

  return {
    labels: labels,
    datasets: [{
      label: label,
      backgroundColor: color,
      borderColor: color,
      data: data,
      tension: 0.3,
      pointRadius: 5,
      pointHoverRadius: 8
    }]
  };
});

const chartOptions = computed(() => {
  const gridColor = getComputedStyleValue('--chart-grid') || 'rgba(0,0,0,0.05)';
  const textColor = getComputedStyleValue('--chart-text') || '#64748b';
  
  return {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false }
    },
    scales: {
      y: {
        beginAtZero: false,
        grid: { color: gridColor },
        ticks: { color: textColor }
      },
      x: {
        grid: { display: false },
        ticks: { color: textColor }
      }
    }
  };
});

const fetchMetrics = async () => {
  loading.value = true;
  try {
    const url = selectedProfileId.value ? `/api/body-metrics?profileId=${selectedProfileId.value}` : '/api/body-metrics';
    const res = await axios.get(url);
    metrics.value = res.data.metrics || [];
    profiles.value = res.data.profiles || [];
    selectedProfileId.value = res.data.currentProfileId;
    canEdit.value = res.data.canEdit;
    
    chartLabels.value = res.data.chartLabels || [];
    chartWeights.value = res.data.chartWeights || [];
    chartBmis.value = res.data.chartBmis || [];
  } catch (err) {
    console.error("Lỗi tải chỉ số cơ thể", err);
  } finally {
    loading.value = false;
  }
};

const openCreateModal = () => {
  editingId.value = null;
  
  
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  const hours = String(now.getHours()).padStart(2, '0');
  const minutes = String(now.getMinutes()).padStart(2, '0');
  form.logDate = `${year}-${month}-${day}T${hours}:${minutes}`;

  if (metrics.value.length > 0) {
    const last = metrics.value[0];
    form.heightInCm = last.height;
    form.weightInKg = last.weight;
    form.systolicBp = last.systolic;
    form.diastolicBp = last.diastolic;
    form.cholesterol = last.cholesterol || 1;
    form.glucose = last.glucose || 1;
  } else {
    form.heightInCm = ''; form.weightInKg = ''; form.systolicBp = ''; form.diastolicBp = '';
    form.cholesterol = 1; form.glucose = 1;
  }
  showModal.value = true;
};

const editMetric = (m) => {
  editingId.value = m.id;
  form.heightInCm = m.height;
  form.weightInKg = m.weight;
  form.systolicBp = m.systolic;
  form.diastolicBp = m.diastolic;
  form.cholesterol = m.cholesterol || 1;
  form.glucose = m.glucose || 1;
  
  
  if (m.date) {
    const d = new Date(m.date);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    const hours = String(d.getHours()).padStart(2, '0');
    const minutes = String(d.getMinutes()).padStart(2, '0');
    form.logDate = `${year}-${month}-${day}T${hours}:${minutes}`;
  }
  
  showModal.value = true;
};

const submitForm = async () => {
  submitting.value = true;
  try {
    const payload = { ...form, id: editingId.value, profileId: selectedProfileId.value };
    const res = await axios.post('/api/body-metrics', payload);
    if (res.data.success) {
      showModal.value = false;
      fetchMetrics();
    }
  } catch (err) {
    toast.error(err.response?.data?.message || err.message);
  } finally {
    submitting.value = false;
  }
};

const deleteMetric = async (id) => {
  if (!await toast.confirm("Xác nhận xóa", "Bạn muốn xóa bản ghi chỉ số này?")) return;
  try {
    const res = await axios.delete(`/api/body-metrics/${id}`);
    if (res.data.success) fetchMetrics();
  } catch (err) {
    toast.error(err.response?.data?.message || err.message);
  }
};

const getBmiBadgeClass = (bmi) => {
  if (bmi < 18.5) return 'bg-info';
  if (bmi < 25) return 'bg-success';
  if (bmi < 30) return 'bg-warning text-dark';
  return 'bg-danger';
};

const formatDate = (dateStr) => {
  if (!dateStr) return '---';
  return new Date(dateStr).toLocaleDateString('vi-VN');
};

onMounted(fetchMetrics);
</script>

<style scoped>
.hover-lift { transition: transform 0.2s, box-shadow 0.2s; }
.hover-lift:hover { transform: translateY(-3px); box-shadow: 0 8px 15px rgba(0,0,0,0.05) !important; }
.border-dashed { border: 2px dashed var(--border-color) !important; }
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.6); backdrop-filter: blur(4px); z-index: 2000; }
.btn-group .btn { padding: 0.25rem 0.75rem; font-size: 0.75rem; font-weight: 600; }
.border-theme { border-color: var(--border-color) !important; }
.page-toolbar-actions { width: min(100%, 680px); }
.profile-selector { width: 100%; }
.profile-select { width: 100%; min-width: 0; max-width: 100%; }
.body-metrics-chart { height: 300px; position: relative; }


.table-responsive::-webkit-scrollbar { height: 6px; }
.table-responsive::-webkit-scrollbar-thumb { background: var(--border-color); border-radius: 10px; }

@media (max-width: 767.98px) {
  .body-metrics-page {
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

  .body-metrics-page .rounded-4 {
    border-radius: 12px !important;
  }

  .body-metrics-page .p-4 {
    padding: 0.9rem !important;
  }

  .body-metrics-chart {
    height: 220px;
  }

  .btn-group {
    width: 100%;
  }

  .btn-group .btn {
    flex: 1 1 auto;
    padding: 0.45rem 0.5rem;
    font-size: 0.72rem;
  }

  .table-responsive table {
    font-size: 0.86rem;
  }

  .table-responsive th,
  .table-responsive td {
    white-space: nowrap;
    padding-left: 0.5rem !important;
    padding-right: 0.5rem !important;
  }

  .bmi-ai-card {
    position: static !important;
    margin-top: 0.75rem;
  }

  .modal-overlay .p-4 {
    padding: 0.9rem !important;
  }
}
</style>
