<template>
  <div class="ai-prediction container-fluid py-4 animate-fade-in">
    <div class="row g-4">
      
      <div class="col-lg-7">
        <div class="card bg-theme-card border border-theme shadow-sm rounded-4 overflow-hidden h-100">
          <div class="card-header bg-theme-card border-0 pt-4 pb-0 px-4">
            <div class="d-flex align-items-center gap-3">
              <div class="icon-box bg-primary bg-opacity-10 text-primary rounded-4 p-3 shadow-sm">
                <i class="bi bi-robot fs-2"></i>
              </div>
              <div>
                <h3 class="fw-bold mb-1 text-theme-main">Dự đoán Nguy cơ Tim mạch</h3>
                <p class="text-theme-muted small mb-0">Hệ thống AI dựa trên thuật toán XGBoost tiên tiến</p>
              </div>
            </div>
          </div>

          <div class="card-body p-4 bg-theme-card">
            <div class="mb-4">
              <div class="d-flex justify-content-between align-items-center mb-1">
                <label class="form-label small fw-bold text-theme-main mb-0">Hồ sơ áp dụng</label>
                <router-link :to="{ path: '/documents', query: { profileId: selectedProfileId } }" class="btn btn-sm btn-outline-primary rounded-pill py-0 px-2 extra-small fw-bold">
                  <i class="bi bi-magic me-1"></i> Quét từ tài liệu xét nghiệm
                </router-link>
              </div>
              <div class="d-flex gap-2 align-items-center">
                <select
                  class="form-select bg-theme-light text-theme-main border-theme shadow-none"
                  v-model="selectedProfileId"
                  @change="handleProfileChange"
                  :disabled="profilesLoading || profiles.length === 0"
                >
                  <option
                    v-for="p in profiles"
                    :key="p.id"
                    :value="p.id"
                    class="bg-theme-card"
                  >
                    {{ p.fullName }} ({{ p.profileName }})
                  </option>
                </select>
                <span v-if="profilesLoading" class="spinner-border spinner-border-sm text-primary"></span>
              </div>
              <small class="text-theme-muted extra-small d-block mt-1">
                Mặc định lấy hồ sơ hiện tại. Bạn có thể đổi sang hồ sơ khác hoặc quét phiếu xét nghiệm để tự động điền chỉ số.
              </small>
            </div>

            <form @submit.prevent="handlePredict">
              <h6 class="fw-bold text-primary mb-3 border-bottom border-theme pb-2">1. Thông tin thể chất</h6>
              <div class="row g-3 mb-4">
                <div class="col-md-6">
                  <label class="form-label small fw-bold text-theme-main">Tuổi</label>
                  <div class="input-group shadow-sm">
                    <span class="input-group-text bg-theme-light border-theme text-theme-muted"><i class="bi bi-person"></i></span>
                    <input type="number" class="form-control bg-theme-light text-theme-main border-theme shadow-none" v-model.number="form.age" placeholder="Nhập tuổi" required min="1" max="120" />
                  </div>
                </div>
                <div class="col-md-6">
                  <label class="form-label small fw-bold text-theme-main">Giới tính</label>
                  <div class="input-group shadow-sm">
                    <span class="input-group-text bg-theme-light border-theme text-theme-muted"><i class="bi bi-gender-ambiguous"></i></span>
                    <select class="form-select bg-theme-light text-theme-main border-theme shadow-none" v-model.number="form.gender" required>
                      <option value="1" class="bg-theme-card">Nam</option>
                      <option value="0" class="bg-theme-card">Nữ</option>
                    </select>
                  </div>
                </div>
                <div class="col-md-6">
                  <label class="form-label small fw-bold text-theme-main">Chiều cao (cm)</label>
                  <div class="input-group shadow-sm">
                    <span class="input-group-text bg-theme-light border-theme text-theme-muted"><i class="bi bi-arrows-vertical"></i></span>
                    <input type="number" class="form-control bg-theme-light text-theme-main border-theme shadow-none" v-model.number="form.height" placeholder="Ví dụ: 170" required min="50" max="250" />
                  </div>
                </div>
                <div class="col-md-6">
                  <label class="form-label small fw-bold text-theme-main">Cân nặng (kg)</label>
                  <div class="input-group shadow-sm">
                    <span class="input-group-text bg-theme-light border-theme text-theme-muted"><i class="bi bi-speedometer"></i></span>
                    <input type="number" class="form-control bg-theme-light text-theme-main border-theme shadow-none" v-model.number="form.weight" placeholder="Ví dụ: 70" required min="10" max="400" />
                  </div>
                </div>
              </div>

              <h6 class="fw-bold text-primary mb-3 border-bottom border-theme pb-2">2. Chỉ số lâm sàng</h6>
              <div class="row g-3 mb-4">
                <div class="col-md-6">
                  <label class="form-label small fw-bold text-theme-main">Huyết áp tâm thu (mmHg)</label>
                  <div class="input-group shadow-sm">
                    <span class="input-group-text bg-theme-light border-theme text-theme-muted"><i class="bi bi-activity"></i></span>
                    <input type="number" class="form-control bg-theme-light text-theme-main border-theme shadow-none" v-model.number="form.systolic_bp" placeholder="Ví dụ: 120" required min="60" max="300" />
                  </div>
                  <div class="form-text extra-small text-theme-muted">Số đo phía trên (lớn hơn)</div>
                </div>
                <div class="col-md-6">
                  <label class="form-label small fw-bold text-theme-main">Huyết áp tâm trương (mmHg)</label>
                  <div class="input-group shadow-sm">
                    <span class="input-group-text bg-theme-light border-theme text-theme-muted"><i class="bi bi-activity"></i></span>
                    <input type="number" class="form-control bg-theme-light text-theme-main border-theme shadow-none" v-model.number="form.diastolic_bp" placeholder="Ví dụ: 80" required min="30" max="200" />
                  </div>
                  <div class="form-text extra-small text-theme-muted">Số đo phía dưới (nhỏ hơn)</div>
                </div>
                <div class="col-md-6">
                  <label class="form-label small fw-bold text-theme-main">Cholesterol</label>
                  <div class="input-group shadow-sm">
                    <span class="input-group-text bg-theme-light border-theme text-theme-muted"><i class="bi bi-droplet"></i></span>
                    <select class="form-select bg-theme-light text-theme-main border-theme shadow-none" v-model.number="form.cholesterol" required>
                      <option value="1" class="bg-theme-card">Bình thường</option>
                      <option value="2" class="bg-theme-card">Trên bình thường</option>
                      <option value="3" class="bg-theme-card">Cao</option>
                    </select>
                  </div>
                </div>
                <div class="col-md-6">
                  <label class="form-label small fw-bold text-theme-main">Đường huyết (Glucose)</label>
                  <div class="input-group shadow-sm">
                    <span class="input-group-text bg-theme-light border-theme text-theme-muted"><i class="bi bi-droplet-half"></i></span>
                    <select class="form-select bg-theme-light text-theme-main border-theme shadow-none" v-model.number="form.glucose" required>
                      <option value="1" class="bg-theme-card">Bình thường</option>
                      <option value="2" class="bg-theme-card">Trên bình thường</option>
                      <option value="3" class="bg-theme-card">Cao</option>
                    </select>
                  </div>
                </div>
              </div>

              <h6 class="fw-bold text-primary mb-3 border-bottom border-theme pb-2">3. Lối sống</h6>
              <div class="row g-3">
                <div class="col-md-4">
                  <label class="form-label small fw-bold text-theme-main">Hút thuốc</label>
                  <div class="input-group shadow-sm">
                    <select class="form-select bg-theme-light text-theme-main border-theme shadow-none" v-model.number="form.smoke" required>
                      <option value="0" class="bg-theme-card">Không</option>
                      <option value="1" class="bg-theme-card">Có</option>
                    </select>
                  </div>
                </div>
                <div class="col-md-4">
                  <label class="form-label small fw-bold text-theme-main">Rượu bia</label>
                  <div class="input-group shadow-sm">
                    <select class="form-select bg-theme-light text-theme-main border-theme shadow-none" v-model.number="form.alco" required>
                      <option value="0" class="bg-theme-card">Không</option>
                      <option value="1" class="bg-theme-card">Có</option>
                    </select>
                  </div>
                </div>
                <div class="col-md-4">
                  <label class="form-label small fw-bold text-theme-main">Hoạt động thể lực</label>
                  <div class="input-group shadow-sm">
                    <select class="form-select bg-theme-light text-theme-main border-theme shadow-none" v-model.number="form.active" required>
                      <option value="1" class="bg-theme-card">Có vận động</option>
                      <option value="0" class="bg-theme-card">Ít vận động</option>
                    </select>
                  </div>
                </div>
              </div>
              
              <div v-if="errorMsg" class="alert alert-danger mt-4 py-2 small border-0 shadow-sm">
                <i class="bi bi-exclamation-triangle-fill me-2"></i> {{ errorMsg }}
              </div>

              <button type="submit" class="btn btn-primary btn-lg w-100 mt-4 rounded-pill shadow fw-bold hover-lift" :disabled="loading">
                <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
                <i v-else class="bi bi-magic me-2"></i>
                {{ loading ? 'AI đang phân tích...' : 'Phân tích & Dự đoán' }}
              </button>
            </form>
          </div>
        </div>
      </div>

      
      <div class="col-lg-5">
        <div class="card border-0 shadow-sm rounded-4 h-100 result-card bg-primary text-white overflow-hidden position-relative border border-theme">
          <div class="position-absolute opacity-10 end-0 bottom-0" style="transform: translate(20%, 20%); pointer-events: none;">
            <i class="bi bi-heart-pulse-fill" style="font-size: 15rem;"></i>
          </div>
          
          <div class="card-body p-4 p-lg-5 position-relative z-1 h-100 d-flex flex-column">
            <div v-if="!result" class="d-flex flex-column align-items-center justify-content-center flex-grow-1">
              <div class="bg-white bg-opacity-10 p-4 rounded-circle mb-4 d-inline-block shadow-sm">
                <i class="bi bi-cpu fs-1"></i>
              </div>
              <h4 class="fw-bold mb-3">AI đang chờ dữ liệu</h4>
              <p class="opacity-75">Vui lòng nhập đầy đủ thông tin bên trái. AI sẽ phân tích các chỉ số sức khỏe để đánh giá rủi ro tim mạch của bạn.</p>
            </div>
            
            
            <div v-else class="w-100 animate-slide-up overflow-auto custom-scrollbar pe-2" style="max-height: calc(100vh - 110px); padding-right: 8px; padding-bottom: 5.5rem;">
              <div class="result-content-wrapper">
              <h4 class="text-center fw-bold mb-4">Kết Quả Phân Tích</h4>
              
              <div class="risk-gauge mb-4 mx-auto bg-theme-card rounded-circle shadow-lg d-flex flex-column align-items-center justify-content-center" style="width: 180px; height: 180px; border: 6px solid var(--border-color);">
                <div class="gauge-value" :class="riskColorClass" style="font-size: 3rem; font-weight: 800; line-height: 1;">
                  {{ result.risk_percentage }}<span style="font-size: 1.5rem;">%</span>
                </div>
                <div class="risk-label fw-bold mt-1 text-theme-main text-uppercase small">MỨC ĐỘ: {{ result.risk_level }}</div>
              </div>
              
              <div class="alert bg-white bg-opacity-10 text-white border-0 shadow-sm mb-4">
                <i class="bi bi-info-circle-fill me-2"></i> {{ result.message }}
              </div>

              
              <div v-if="result.confidence_score || result.urgency_level" class="row g-2 mb-4">
                <div v-if="result.confidence_score" class="col-6">
                  <div class="bg-white bg-opacity-10 rounded-3 p-3 text-center">
                    <div class="small opacity-75 mb-1">Độ tin cậy</div>
                    <div class="fs-5 fw-bold">{{ result.confidence_score }}%</div>
                  </div>
                </div>
                <div v-if="result.urgency_level" class="col-6">
                  <div class="bg-white bg-opacity-10 rounded-3 p-3 text-center">
                    <div class="small opacity-75 mb-1">Mức độ</div>
                    <div class="fs-5 fw-bold">{{ result.urgency_level }}</div>
                  </div>
                </div>
              </div>

              
              <div v-if="result.explanation && Object.keys(result.explanation).length > 0" class="bg-theme-card text-theme-main rounded-4 p-4 shadow-sm border border-theme mb-3">
                <h6 class="fw-bold border-bottom border-theme pb-2 mb-3 text-primary">
                  <i class="bi bi-lightbulb me-2"></i> Phân tích chi tiết:
                </h6>
                <div class="explanation-list">
                  <div v-for="(text, feature) in result.explanation" :key="feature" class="mb-3">
                    <div class="fw-bold text-primary small mb-1">
                      <i class="bi bi-dot"></i> {{ feature }}
                    </div>
                    <div class="extra-small text-theme-muted ps-3">
                      {{ text }}
                    </div>
                  </div>
                </div>
              </div>
              
              <div class="bg-theme-card text-theme-main rounded-4 p-4 shadow-sm border border-theme">
                <h6 class="fw-bold border-bottom border-theme pb-2 mb-3 text-primary"><i class="bi bi-shield-check me-2"></i> Lời khuyên từ AI:</h6>
                <ul class="list-unstyled mb-0">
                  <li v-for="(rec, idx) in result.recommendations" :key="idx" class="mb-2 small d-flex">
                    <i class="bi bi-check2-circle text-success me-2 mt-1 flex-shrink-0"></i>
                    <span>{{ rec }}</span>
                  </li>
                </ul>
                
                <div v-if="result.warning_signs && result.warning_signs.length > 0" class="mt-3 pt-3 border-top border-theme">
                  <h6 class="fw-bold text-danger mb-2 small"><i class="bi bi-exclamation-triangle me-1"></i> Dấu hiệu cần chú ý:</h6>
                  <ul class="list-unstyled mb-0">
                    <li v-for="(warn, idx) in result.warning_signs" :key="idx" class="mb-1 extra-small text-danger d-flex">
                      <i class="bi bi-dot flex-shrink-0"></i> <span>{{ warn }}</span>
                    </li>
                  </ul>
                </div>
              </div>
              
              <div class="text-center mt-4 mb-2">
                <small class="opacity-75"><i>* Đây chỉ là kết quả dự đoán của AI mang tính tham khảo, không thay thế chẩn đoán của bác sĩ.</i></small>
              </div>
              </div>
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

const loading = ref(false);
const result = ref(null);
const errorMsg = ref('');
const profiles = ref([]);
const selectedProfileId = ref(null);
const profilesLoading = ref(false);
const route = useRoute();

const defaultForm = {
  age: '',
  gender: 1,
  height: 170,
  weight: 70,
  systolic_bp: 120,
  diastolic_bp: 80,
  cholesterol: 1,
  glucose: 1,
  smoke: 0,
  alco: 0,
  active: 1
};

const form = reactive({
  ...defaultForm
});

const resetFormToDefault = () => {
  Object.assign(form, defaultForm);
};

const toBinaryNumber = (value, fallback) => {
  if (value === true || value === 1 || value === '1') return 1;
  if (value === false || value === 0 || value === '0') return 0;
  return fallback;
};

const calculateAgeFromDob = (dobText) => {
  if (!dobText) return null;
  const dob = new Date(dobText);
  if (Number.isNaN(dob.getTime())) return null;

  const today = new Date();
  let age = today.getFullYear() - dob.getFullYear();
  const monthDiff = today.getMonth() - dob.getMonth();
  const dayDiff = today.getDate() - dob.getDate();
  if (monthDiff < 0 || (monthDiff === 0 && dayDiff < 0)) {
    age -= 1;
  }
  return age > 0 ? age : null;
};

const applyAgeFromSelectedProfile = () => {
  const selected = profiles.value.find(p => Number(p.id) === Number(selectedProfileId.value));
  const calculatedAge = calculateAgeFromDob(selected?.dob);
  form.age = calculatedAge ?? '';
};

const applyLatestMetricToForm = (metric) => {
  resetFormToDefault();
  if (!metric) return;

  form.height = Number(metric.height) || defaultForm.height;
  form.weight = Number(metric.weight) || defaultForm.weight;
  form.systolic_bp = Number(metric.systolic) || defaultForm.systolic_bp;
  form.diastolic_bp = Number(metric.diastolic) || defaultForm.diastolic_bp;
  form.cholesterol = Number(metric.cholesterol) || defaultForm.cholesterol;
  form.glucose = Number(metric.glucose) || defaultForm.glucose;
  form.smoke = toBinaryNumber(metric.smoke, defaultForm.smoke);
  form.alco = toBinaryNumber(metric.alco, defaultForm.alco);
  form.active = toBinaryNumber(metric.active, defaultForm.active);
};

const loadProfileData = async (profileId = null) => {
  profilesLoading.value = true;
  try {
    const url = profileId ? `/api/body-metrics?profileId=${profileId}` : '/api/body-metrics';
    const res = await axios.get(url);

    profiles.value = res.data?.profiles || [];
    selectedProfileId.value = res.data?.currentProfileId ?? null;

    const latestMetric = Array.isArray(res.data?.metrics) && res.data.metrics.length > 0
      ? res.data.metrics[0]
      : null;
    applyLatestMetricToForm(latestMetric);
    applyAgeFromSelectedProfile();
  } catch (err) {
    console.error(err);
    resetFormToDefault();
    errorMsg.value = 'Không tải được dữ liệu hồ sơ để điền mặc định.';
  } finally {
    profilesLoading.value = false;
  }
};

const handleProfileChange = async () => {
  result.value = null;
  errorMsg.value = '';
  await loadProfileData(selectedProfileId.value);
};

const handlePredict = async () => {
  loading.value = true;
  errorMsg.value = '';
  result.value = null;
  
  if (form.systolic_bp <= form.diastolic_bp) {
    errorMsg.value = "Huyết áp tâm thu phải lớn hơn huyết áp tâm trương.";
    loading.value = false;
    return;
  }
  
  try {
    const res = await axios.post('/api/predict-heart-disease', form);
    result.value = res.data;
  } catch (err) {
    console.error(err);
    errorMsg.value = err.response?.data?.message || 'Không thể kết nối đến máy chủ AI. Vui lòng kiểm tra xem AI server đã chạy chưa.';
  } finally {
    loading.value = false;
  }
};

const riskColorClass = computed(() => {
  if (!result.value) return '';
  if (result.value.risk_level === 'CAO') return 'text-danger';
  if (result.value.risk_level === 'TRUNG BÌNH') return 'text-warning';
  return 'text-success';
});

onMounted(async () => {
  const queryProfileId = route.query.profileId ? Number(route.query.profileId) : null;
  await loadProfileData(Number.isFinite(queryProfileId) && queryProfileId > 0 ? queryProfileId : null);
});
</script>

<style scoped>
.animate-fade-in { animation: fadeIn 0.4s ease-out; }
.animate-slide-up { animation: slideUp 0.4s ease-out; }

@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
@keyframes slideUp { from { transform: translateY(20px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }

.extra-small { font-size: 0.75rem; }

.hover-lift { transition: transform 0.2s cubic-bezier(0.165, 0.84, 0.44, 1), box-shadow 0.2s; }
.hover-lift:hover { transform: translateY(-3px); box-shadow: 0 10px 20px rgba(13, 110, 253, 0.2) !important; }

.result-card {
  background: linear-gradient(135deg, #0d6efd 0%, #0a58ca 100%);
}

.text-danger.gauge-value { color: #dc3545 !important; }
.text-warning.gauge-value { color: #ffc107 !important; }
.text-success.gauge-value { color: #198754 !important; }
.border-theme { border-color: var(--border-color) !important; }


.custom-scrollbar::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 10px;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.3);
  border-radius: 10px;
  transition: background 0.2s;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.5);
}


.custom-scrollbar {
  scrollbar-width: thin;
  scrollbar-color: rgba(255, 255, 255, 0.3) rgba(255, 255, 255, 0.1);
}

@media (max-width: 767.98px) {
  .ai-prediction {
    padding-top: 0.8rem !important;
    padding-left: 0.6rem !important;
    padding-right: 0.6rem !important;
  }

  .ai-prediction .card-header,
  .ai-prediction .card-body {
    padding: 0.9rem !important;
  }

  .ai-prediction h3 {
    font-size: 1.1rem;
  }

  .ai-prediction .icon-box {
    padding: 0.6rem !important;
  }

  .ai-prediction .icon-box i {
    font-size: 1.35rem !important;
  }

  .ai-prediction .row.g-3 {
    --bs-gutter-x: 0.65rem;
    --bs-gutter-y: 0.65rem;
  }

  .ai-prediction .btn-lg {
    font-size: 0.95rem;
    padding-top: 0.55rem;
    padding-bottom: 0.55rem;
  }

  .result-card .card-body {
    padding: 1rem !important;
  }

  .risk-gauge {
    width: 145px !important;
    height: 145px !important;
  }

  .gauge-value {
    font-size: 2.2rem !important;
  }

  .result-content-wrapper {
    padding-bottom: 0.3rem;
  }

  .result-card .overflow-auto {
    max-height: none !important;
    overflow: visible !important;
    padding-right: 0 !important;
  }
}
</style>
