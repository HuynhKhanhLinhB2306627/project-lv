<template>
  <div class="container-fluid py-4 px-3 px-md-4 animate-fade-in dashboard-page">
    
    <div class="row mb-4">
      <div class="col-12">
        <div class="card border-0 rounded-4 shadow-sm overflow-hidden position-relative hover-lift dashboard-hero-card">
          <div class="position-absolute top-0 start-0 w-100 h-100" style="background: linear-gradient(135deg, rgba(13,110,253,0.85) 0%, rgba(0,163,191,0.6) 100%); z-index: 1;"></div>
          <img src="/images/banner2.jpg" alt="Dashboard Banner" class="w-100 h-100 object-fit-cover position-absolute top-0 start-0 z-0">
          <div class="p-4 p-md-5 text-white position-relative z-2">
            <div class="row align-items-center">
              <div class="col-md-8">
                <h1 class="display-5 fw-bold mb-2 dashboard-hero-title">Chào mừng trở lại, {{ userName }}! 👋</h1>
                <p class="lead opacity-75 mb-0 dashboard-hero-subtitle">Hôm nay là một ngày tuyệt vời để theo dõi và cải thiện sức khỏe của bạn.</p>
                <div class="d-md-none mt-3 d-flex gap-2 flex-wrap">
                  <span class="badge bg-white bg-opacity-25 px-3 py-2">Nhịp tim: {{ stats.latestMetric?.heartRate || '--' }}</span>
                  <span class="badge bg-white bg-opacity-25 px-3 py-2">HA: {{ stats.latestMetric?.systolicBP || '--' }}/{{ stats.latestMetric?.diastolicBP || '--' }}</span>
                </div>
              </div>
              <div class="col-md-4 d-none d-md-block text-end">
                <div class="d-inline-flex gap-3 bg-white bg-opacity-25 p-3 rounded-4 backdrop-blur shadow-sm border border-white border-opacity-25">
                  <div class="text-center px-2">
                    <div class="h4 fw-bold mb-0 text-white">{{ stats.latestMetric?.heartRate || '--' }}</div>
                    <small class="text-white-50 small text-uppercase fw-bold">Nhịp tim</small>
                  </div>
                  <div class="vr bg-white opacity-50"></div>
                  <div class="text-center px-2">
                    <div class="h4 fw-bold mb-0 text-white">
                      {{ stats.latestMetric?.systolicBP || '--' }}/{{ stats.latestMetric?.diastolicBP || '--' }}
                    </div>
                    <small class="text-white-50 small text-uppercase fw-bold">Huyết áp</small>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    
    <div class="row mb-4" v-if="profiles.length > 1">
      <div class="col-12">
        <div class="bg-theme-card p-3 rounded-3 shadow-sm d-flex align-items-center border border-theme profile-filter-wrap">
          <i class="bi bi-person-badge-fill text-primary me-3 fs-5 flex-shrink-0"></i>
          <span class="fw-bold text-theme-muted me-3 flex-shrink-0 profile-filter-label">Đang xem dữ liệu của:</span>
          <select class="form-select border-0 bg-theme-light fw-bold text-theme-main shadow-none text-truncate"
                  style="max-width: 300px;"
                  v-model="selectedProfile" @change="fetchDashboardData(selectedProfile)">
            <option v-for="p in profiles" :key="p.id" :value="p.id" class="bg-theme-card">{{ p.profileName }} ({{ p.fullName }})</option>
          </select>
        </div>
      </div>
    </div>

    
    <div class="row g-4">
      
      <div class="col-lg-8">
        <div class="bg-theme-card p-4 rounded-4 shadow-sm mb-4 border border-theme">
          <h5 class="fw-bold mb-4 d-flex align-items-center text-theme-main">
            <i class="bi bi-activity text-primary me-2"></i> Tóm tắt hoạt động
          </h5>
            <div class="row g-2 text-center">
              <div class="col-6 col-md-4 col-xl">
                <router-link to="/appointments" class="text-decoration-none">
                  <div class="p-2 bg-primary bg-opacity-10 rounded-4 hover-lift">
                    <h3 class="fw-bold text-primary mb-0">{{ stats.appointmentCount }}</h3>
                  <small class="text-theme-muted text-uppercase fw-bold extra-small">Lịch hẹn</small>
                </div>
              </router-link>
            </div>
              <div class="col-6 col-md-4 col-xl">
                <router-link to="/medications" class="text-decoration-none">
                  <div class="p-2 bg-danger bg-opacity-10 rounded-4 hover-lift">
                    <h3 class="fw-bold text-danger mb-0">{{ stats.medicationCount }}</h3>
                  <small class="text-theme-muted text-uppercase fw-bold extra-small">Uống thuốc</small>
                </div>
              </router-link>
            </div>
              <div class="col-6 col-md-4 col-xl">
                <router-link to="/vaccinations" class="text-decoration-none">
                  <div class="p-2 bg-success bg-opacity-10 rounded-4 hover-lift">
                    <h3 class="fw-bold text-success mb-0">{{ stats.vaccinationCount }}</h3>
                  <small class="text-theme-muted text-uppercase fw-bold extra-small">Tiêm chủng</small>
                </div>
              </router-link>
            </div>
              <div class="col-6 col-md-4 col-xl">
                <router-link to="/documents" class="text-decoration-none">
                  <div class="p-2 bg-info bg-opacity-10 rounded-4 hover-lift">
                    <h3 class="fw-bold text-info mb-0">{{ stats.documentCount }}</h3>
                  <small class="text-theme-muted text-uppercase fw-bold extra-small">Tài liệu</small>
                </div>
              </router-link>
            </div>
              <div class="col-6 col-md-4 col-xl">
                <router-link to="/insurance" class="text-decoration-none">
                  <div class="p-2 bg-warning bg-opacity-10 rounded-4 hover-lift">
                    <h3 class="fw-bold text-warning mb-0">{{ stats.insuranceCount }}</h3>
                  <small class="text-theme-muted text-uppercase fw-bold extra-small">BHYT</small>
                </div>
              </router-link>
            </div>
          </div>

          
          <div class="mt-5">
            <div class="d-flex justify-content-between align-items-center mb-4">
              <h5 class="fw-bold mb-0 text-theme-main"><i class="bi bi-graph-up text-success me-2"></i>Chỉ số BMI & Cân nặng</h5>
              <router-link to="/body-metrics" class="btn btn-sm btn-link text-decoration-none p-0 text-primary fw-bold">Xem chi tiết <i class="bi bi-arrow-right"></i></router-link>
            </div>
            <div class="dashboard-chart-wrap">
              <Line v-if="chartData" :data="chartData" :options="chartOptions" />
              <div v-else class="p-5 text-center bg-theme-light rounded-4 border-dashed border-2 border-theme">
                <i class="bi bi-bar-chart display-4 opacity-25 text-theme-muted"></i>
                <p class="mt-2 text-theme-muted">Chưa có đủ dữ liệu để vẽ biểu đồ.</p>
              </div>
            </div>
          </div>
        </div>

        <div class="row g-4 mb-4">
          <div class="col-md-6">
            <div class="bg-theme-card p-4 rounded-4 shadow-sm h-100 text-center border-bottom border-primary border-4 border-start border-end border-top border-theme">
              <i class="bi bi-heart-pulse text-primary display-5 mb-2 d-block"></i>
              <h6 class="text-theme-muted text-uppercase fw-bold mb-2">Huyết áp trung bình</h6>
              <h3 class="fw-bold text-theme-main">{{ stats.avgSystolic || '--' }}/{{ stats.avgDiastolic || '--' }} <small class="fw-normal text-theme-muted fs-6">mmHg</small></h3>
            </div>
          </div>
          <div class="col-md-6">
            <div class="bg-theme-card p-4 rounded-4 shadow-sm h-100 text-center border-bottom border-success border-4 border-start border-end border-top border-theme">
              <i class="bi bi-speedometer2 text-success display-5 mb-2 d-block"></i>
              <h6 class="text-theme-muted text-uppercase fw-bold mb-2">BMI trung bình</h6>
              <h3 class="fw-bold text-theme-main">{{ stats.avgBmi || '--' }}</h3>
              <span class="badge" :class="getBmiClass(stats.avgBmi)">{{ getBmiLabel(stats.avgBmi) }}</span>
            </div>
          </div>
        </div>
      </div>

      
      <div class="col-lg-4">
        <div class="bg-theme-card p-4 rounded-4 shadow-sm h-100 border border-theme">
          <h5 class="fw-bold mb-4 d-flex align-items-center text-theme-main">
            <i class="bi bi-calendar-day text-primary me-2"></i> Lịch biểu hôm nay
          </h5>
          
          <div v-if="stats.todayAppointments && stats.todayAppointments.length > 0">
            <div v-for="app in stats.todayAppointments" :key="app.id" class="d-flex gap-3 mb-4 p-3 bg-theme-light rounded-3 border-start border-primary border-4 shadow-sm today-appointment-item">
              <div class="text-center">
                <div class="h5 fw-bold text-primary mb-0">{{ app.time }}</div>
                <small class="text-theme-muted fw-bold extra-small">Hôm nay</small>
              </div>
              <div class="vr opacity-10"></div>
              <div>
                <h6 class="fw-bold mb-0 text-theme-main">{{ app.reason }}</h6>
                <small class="text-theme-muted d-block mb-1"><i class="bi bi-geo-alt me-1"></i> {{ app.location }}</small>
                <small class="text-theme-muted"><i class="bi bi-person-doctor me-1"></i> {{ app.doctorName }}</small>
              </div>
            </div>
          </div>
          
          <div v-if="stats.appointmentCount > 0" class="d-flex gap-3 mb-4 p-3 bg-theme-light rounded-3 border border-theme dashboard-info-box">
            <div class="icon-circle bg-primary bg-opacity-10 text-primary">
              <i class="bi bi-clipboard2-check"></i>
            </div>
            <div>
              <h6 class="fw-bold mb-0 text-theme-main">Tổng {{ stats.appointmentCount }} cuộc hẹn</h6>
              <p class="small text-theme-muted mb-0">Bạn có các cuộc hẹn khám đã lên lịch trong hệ thống.</p>
            </div>
          </div>


        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';
import { Line } from 'vue-chartjs';
import { Chart as ChartJS, Title, Tooltip, Legend, LineElement, PointElement, CategoryScale, LinearScale } from 'chart.js';

ChartJS.register(Title, Tooltip, Legend, LineElement, PointElement, CategoryScale, LinearScale);

const userName = ref('Bạn');
const profiles = ref([]);
const selectedProfile = ref(null);
const stats = ref({
  appointmentCount: 0,
  medicationCount: 0,
  vaccinationCount: 0,
  documentCount: 0,
  insuranceCount: 0,
  latestMetric: null,
  avgBmi: 0,
  avgSystolic: 0,
  avgDiastolic: 0,
  chartLabels: [],
  chartBmis: [],
  chartWeights: [],
  todayAppointments: []
});

const getComputedStyleValue = (varName) => {
  return getComputedStyle(document.documentElement).getPropertyValue(varName).trim();
};

const chartData = computed(() => {
  if (!stats.value.chartLabels || stats.value.chartLabels.length === 0) return null;
  
  const labels = [...stats.value.chartLabels].reverse();
  const bmiData = [...stats.value.chartBmis].reverse();
  const weightData = [...stats.value.chartWeights].reverse();

  return {
    labels: labels,
    datasets: [
      {
        label: 'BMI',
        borderColor: '#198754',
        backgroundColor: '#198754',
        data: bmiData,
        tension: 0.3,
        pointRadius: 4,
        yAxisID: 'y'
      },
      {
        label: 'Cân nặng (kg)',
        borderColor: '#0d6efd',
        backgroundColor: '#0d6efd',
        data: weightData,
        tension: 0.3,
        pointRadius: 4,
        yAxisID: 'y1'
      }
    ]
  };
});

const chartOptions = computed(() => {
  const gridColor = getComputedStyleValue('--chart-grid') || 'rgba(0,0,0,0.05)';
  const textColor = getComputedStyleValue('--chart-text') || '#94a3b8';
  
  return {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { position: 'top', labels: { usePointStyle: true, font: { weight: 'bold' }, color: textColor } }
    },
    scales: {
      y: {
        type: 'linear',
        display: true,
        position: 'left',
        grid: { color: gridColor },
        ticks: { color: textColor }
      },
      y1: {
        type: 'linear',
        display: true,
        position: 'right',
        grid: { drawOnChartArea: false },
        ticks: { color: textColor }
      },
      x: {
        grid: { display: false },
        ticks: { color: textColor }
      }
    }
  };
});

const fetchDashboardData = async (profileId = null) => {
  try {
    const url = profileId ? `/api/dashboard/stats?profileId=${profileId}` : '/api/dashboard/stats';
    const res = await axios.get(url);
    stats.value = res.data;
    selectedProfile.value = res.data.currentProfileId;
  } catch (err) {
    console.error("Lỗi lấy dữ liệu dashboard:", err);
  }
};

const fetchProfiles = async () => {
  try {
    const res = await axios.get('/api/user-profile/list');
    profiles.value = res.data.myProfiles || [];
  } catch (err) {
    console.error("Lỗi lấy danh sách hồ sơ:", err);
  }
};

const fetchUser = async () => {
  try {
    const res = await axios.get('/api/auth/me');
    userName.value = res.data.fullName;
  } catch (err) {
    console.error("Lỗi lấy thông tin user:", err);
  }
};

const getBmiClass = (bmi) => {
  if (!bmi) return 'bg-secondary';
  if (bmi < 18.5) return 'bg-info';
  if (bmi < 25) return 'bg-success';
  if (bmi < 30) return 'bg-warning text-dark';
  return 'bg-danger';
};

const getBmiLabel = (bmi) => {
  if (!bmi) return 'Chưa có dữ liệu';
  if (bmi < 18.5) return 'Gầy';
  if (bmi < 25) return 'Bình thường';
  if (bmi < 30) return 'Tiền béo phì';
  return 'Béo phì';
};

onMounted(() => {
  fetchUser();
  fetchProfiles();
  fetchDashboardData();
});
</script>

<style scoped>
.hover-lift { transition: transform 0.25s ease, box-shadow 0.25s ease; cursor: pointer; }
.hover-lift:hover { transform: translateY(-5px); box-shadow: 0 10px 20px rgba(0,0,0,0.1) !important; }
.backdrop-blur { backdrop-filter: blur(8px); -webkit-backdrop-filter: blur(8px); }
.extra-small { font-size: 0.65rem; }
.border-dashed { border-style: dashed !important; }
.icon-circle { width: 45px; height: 45px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 1.25rem; flex-shrink: 0; }
.animate-fade-in { animation: fadeIn 0.6s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
.dashboard-chart-wrap { height: 280px; position: relative; }


.custom-scrollbar::-webkit-scrollbar { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: var(--border-color); border-radius: 10px; }

@media (max-width: 991.98px) {
  .profile-filter-wrap {
    flex-direction: column;
    align-items: stretch !important;
    gap: 0.5rem;
  }

  .profile-filter-label {
    margin-right: 0 !important;
  }

  .profile-filter-wrap .form-select {
    max-width: 100% !important;
  }
}

@media (max-width: 767.98px) {
  .dashboard-page {
    padding-top: 0.75rem !important;
    padding-left: 0.6rem !important;
    padding-right: 0.6rem !important;
  }

  .dashboard-hero-card .p-4 {
    padding: 1rem !important;
  }

  .dashboard-hero-title {
    font-size: 1.35rem !important;
    line-height: 1.25;
  }

  .dashboard-hero-subtitle {
    font-size: 0.92rem;
  }

  .dashboard-page .bg-theme-card.p-4 {
    padding: 0.9rem !important;
  }

  .dashboard-page .row.g-4 {
    --bs-gutter-x: 0.75rem;
    --bs-gutter-y: 0.75rem;
  }

  .dashboard-page .h3 {
    font-size: 1.2rem;
  }

  .dashboard-chart-wrap {
    height: 220px;
  }

  .today-appointment-item {
    flex-direction: column;
    gap: 0.5rem !important;
  }

  .today-appointment-item .vr {
    display: none;
  }

  .dashboard-info-box {
    align-items: flex-start !important;
  }

  .dashboard-quick-action {
    padding: 1rem !important;
  }

  .dashboard-quick-action .display-4 {
    font-size: 2rem;
    margin-bottom: 0.5rem !important;
  }

  .dashboard-quick-action h5 {
    font-size: 1rem;
  }

  .dashboard-quick-action .btn {
    min-height: 42px;
  }
}
</style>
