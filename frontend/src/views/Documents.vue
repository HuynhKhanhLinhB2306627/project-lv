<template>
  <div class="container-fluid py-2 animate-fade-in documents-page">
    <div class="d-flex justify-content-between align-items-center mb-4 page-toolbar">
      <h3 class="fw-bold text-theme-main"><i class="bi bi-file-earmark-medical text-primary me-2"></i>Kho tài liệu y tế</h3>
      <div class="row g-2 page-toolbar-actions">
        <div class="col-12 col-md-8">
          <div class="profile-selector shadow-sm rounded-pill bg-theme-card ps-3 pe-2 py-1 border border-theme d-flex align-items-center">
            <i class="bi bi-person-circle text-primary me-2"></i>
            <select class="form-select border-0 bg-transparent fw-semibold text-theme-main profile-select py-0" v-model="selectedProfileId" @change="fetchDocuments" style="box-shadow: none;">
              <option v-for="p in profiles" :key="p.id" :value="p.id" class="bg-theme-card text-theme-main">{{ p.fullName }} ({{ p.profileName }})</option>
            </select>
          </div>
        </div>
        <div class="col-12 col-md-4 d-grid">
          <button v-if="canEdit" class="btn btn-primary rounded-pill px-3 shadow-sm fw-bold" @click="showUploadModal = true">
            <i class="bi bi-upload me-1"></i> Tải lên mới
          </button>
        </div>
      </div>
    </div>

    
    <div class="mb-4">
      <div class="input-group shadow-sm rounded-pill overflow-hidden bg-theme-card border border-theme">
        <span class="input-group-text bg-transparent border-0 ps-3"><i class="bi bi-search text-theme-muted"></i></span>
        <input type="text" class="form-control bg-transparent border-0 py-2 text-theme-main shadow-none" v-model="searchQuery" placeholder="Tìm kiếm tên tài liệu, kết quả xét nghiệm...">
      </div>
    </div>

    <div class="bg-theme-card p-4 rounded-4 shadow-sm border border-theme">
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-primary" role="status"></div>
      </div>

      <div v-else-if="filteredDocuments.length === 0" class="text-center py-5 text-theme-muted">
        <div class="empty-state-icon bg-theme-light rounded-circle d-inline-flex p-4 mb-3">
          <i class="bi bi-folder-x display-4 opacity-25 text-theme-muted"></i>
        </div>
        <p class="mt-2 fw-medium text-theme-muted">Không tìm thấy tài liệu nào.</p>
        <button v-if="canEdit" class="btn btn-sm btn-link text-decoration-none fw-bold" @click="showUploadModal = true">Tải tài liệu đầu tiên ngay</button>
      </div>

      <div class="row g-4" v-else>
        <div v-for="doc in filteredDocuments" :key="doc.id" class="col-sm-6 col-md-4 col-lg-3">
          <div class="card bg-theme-card h-100 border border-theme shadow-sm rounded-4 overflow-hidden document-card position-relative">
            
            <button v-if="canEdit" class="btn btn-danger btn-sm rounded-circle position-absolute top-0 end-0 m-2 shadow-sm z-1" 
                    style="width: 28px; height: 28px; padding: 0;" @click.stop="deleteDocument(doc.id)">
              <i class="bi bi-x"></i>
            </button>

            
            <div class="document-preview bg-theme-light d-flex align-items-center justify-content-center border-bottom border-theme" @click="openFile(doc.url)">
              <template v-if="isImage(doc.type)">
                <img :src="doc.url" class="img-fluid w-100 h-100 object-fit-cover">
              </template>
              <template v-else>
                <div class="text-center">
                  <i :class="getFileIcon(doc.type)" class="display-3" :style="{ color: getIconColor(doc.type) }"></i>
                  <div class="small fw-bold text-theme-muted mt-1 text-uppercase">{{ doc.type }}</div>
                </div>
              </template>
              <div class="overlay-hover">
                <i class="bi bi-eye-fill text-white fs-2"></i>
              </div>
            </div>

            
            <div class="card-body p-3 bg-theme-card">
              <h6 class="fw-bold text-theme-main text-truncate mb-1" :title="doc.name">{{ doc.name }}</h6>
              <div class="d-flex justify-content-between align-items-center mt-2">
                <div class="small text-theme-muted"><i class="bi bi-calendar-event me-1"></i> {{ doc.date }}</div>
                <div class="d-flex gap-1">
                  <a :href="doc.url" :download="doc.name" class="btn btn-sm btn-light bg-theme-light text-primary rounded-circle border border-theme">
                    <i class="bi bi-download"></i>
                  </a>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    
    <div v-if="showUploadModal" class="modal-overlay d-flex align-items-center justify-content-center p-3" @click.self="showUploadModal = false">
      <div class="bg-theme-card rounded-4 shadow-lg w-100 overflow-hidden border border-theme" style="max-width: 500px;">
        <div class="p-3 border-bottom border-theme d-flex justify-content-between align-items-center bg-theme-light">
          <h5 class="mb-0 fw-bold text-theme-main">Tải lên tài liệu y tế</h5>
          <button class="btn-close" :class="{'btn-close-white': isDarkMode}" @click="showUploadModal = false"></button>
        </div>
        <form @submit.prevent="submitUpload" class="p-4 bg-theme-card">
          <div class="mb-3">
            <label class="form-label small fw-bold text-theme-main">Tên tài liệu / Ghi chú</label>
            <input type="text" class="form-control bg-theme-light text-theme-main border-theme rounded-3" v-model="uploadForm.documentName" 
                   placeholder="VD: Kết quả xét nghiệm máu, Đơn thuốc BV Chợ Rẫy..." required>
          </div>
          <div class="mb-4">
            <label class="form-label small fw-bold text-theme-main">Chọn tệp tin (PDF, Ảnh)</label>
            <div class="upload-zone rounded-3 p-4 text-center border-dashed border-theme position-relative" :class="{'bg-primary bg-opacity-10 border-primary': uploadForm.file}">
              <input type="file" class="position-absolute top-0 start-0 w-100 h-100 opacity-0 cursor-pointer" 
                     @change="handleFileChange" accept=".pdf,image/*">
              <div v-if="!uploadForm.file">
                <i class="bi bi-cloud-arrow-up display-5 text-primary"></i>
                <p class="small text-theme-muted mb-0 mt-2">Nhấn hoặc kéo thả tệp vào đây</p>
              </div>
              <div v-else class="d-flex align-items-center justify-content-center gap-2">
                <i class="bi bi-check-circle-fill text-success"></i>
                <span class="small fw-bold text-theme-main text-truncate">{{ uploadForm.file.name }}</span>
                <button type="button" class="btn-close" :class="{'btn-close-white': isDarkMode}" style="font-size: 0.7rem;" @click.stop="uploadForm.file = null"></button>
              </div>
            </div>
          </div>
          <div class="d-grid">
            <button type="submit" class="btn btn-primary rounded-pill py-2 fw-bold" :disabled="submitting || !uploadForm.file">
              <span v-if="submitting" class="spinner-border spinner-border-sm me-2"></span>
              <i class="bi bi-upload me-1"></i> Bắt đầu tải lên
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import axios from 'axios';
import { toast } from '@/utils/toast';

const documents = ref([]);
const profiles = ref([]);
const selectedProfileId = ref(null);
const canEdit = ref(false);
const loading = ref(true);
const searchQuery = ref('');
const showUploadModal = ref(false);
const submitting = ref(false);

const isDarkMode = computed(() => document.documentElement.getAttribute('data-bs-theme') === 'dark');

const uploadForm = reactive({
  documentName: '',
  file: null
});

const isImage = (type) => ['jpg', 'png', 'jpeg', 'webp', 'gif'].includes(type);

const getFileIcon = (type) => {
  if (type === 'pdf') return 'bi-filetype-pdf';
  if (isImage(type)) return 'bi-file-image';
  if (['doc', 'docx'].includes(type)) return 'bi-file-word';
  return 'bi-file-earmark-medical';
};

const getIconColor = (type) => {
  if (type === 'pdf') return '#dc3545';
  if (isImage(type)) return '#198754';
  return '#6c757d';
};

const filteredDocuments = computed(() => {
  if (!searchQuery.value.trim()) return documents.value;
  const q = searchQuery.value.toLowerCase();
  return documents.value.filter(d => d.name.toLowerCase().includes(q));
});

const fetchDocuments = async () => {
  loading.value = true;
  try {
    const url = selectedProfileId.value ? `/api/documents?profileId=${selectedProfileId.value}` : '/api/documents';
    const res = await axios.get(url);
    const baseUrl = axios.defaults.baseURL || '';
    documents.value = (res.data.documents || []).map(d => ({
      id: d.id,
      name: d.documentName,
      type: d.filePath.split('.').pop().toLowerCase(),
      date: new Date(d.uploadDate).toLocaleDateString('vi-VN'),
      url: d.filePath.startsWith('http') ? d.filePath : `${baseUrl}/uploads/${d.filePath}`
    }));
    profiles.value = res.data.profiles || [];
    selectedProfileId.value = res.data.currentProfileId;
    canEdit.value = res.data.canEdit;
  } catch (err) {
    console.error("Lỗi tải tài liệu:", err);
  } finally {
    loading.value = false;
  }
};

const handleFileChange = (e) => {
  const file = e.target.files[0];
  if (file) {
    uploadForm.file = file;
    
    if (!uploadForm.documentName) {
      uploadForm.documentName = file.name.split('.').shift();
    }
  }
};

const submitUpload = async () => {
  if (!uploadForm.file || !selectedProfileId.value) return;
  submitting.value = true;
  
  try {
    const formData = new FormData();
    formData.append('profileId', selectedProfileId.value);
    formData.append('documentName', uploadForm.documentName);
    formData.append('file', uploadForm.file);

    const res = await axios.post('/api/documents/upload', formData);

    if (res.data.success) {
      toast.success("Tải lên thành công!");
      showUploadModal.value = false;
      uploadForm.documentName = '';
      uploadForm.file = null;
      fetchDocuments();
    }
  } catch (err) {
    toast.error(err.response?.data?.message || err.message);
  } finally {
    submitting.value = false;
  }
};

const deleteDocument = async (id) => {
  if (!await toast.confirm("Xác nhận xóa", "Bạn muốn xóa tài liệu này vĩnh viễn?")) return;
  try {
    const res = await axios.delete(`/api/documents/${id}`);
    if (res.data.success) {
      fetchDocuments();
    }
  } catch (err) {
    toast.error(err.response?.data?.message || err.message);
  }
};

const openFile = (url) => {
  window.open(url, '_blank');
};

onMounted(fetchDocuments);
</script>

<style scoped>
.document-card { transition: all 0.3s; cursor: pointer; }
.document-card:hover { transform: translateY(-5px); box-shadow: 0 12px 25px rgba(0,0,0,0.1) !important; }

.document-preview { height: 180px; position: relative; overflow: hidden; }
.overlay-hover { 
  position: absolute; top: 0; left: 0; width: 100%; height: 100%; 
  background: rgba(0,0,0,0.3); opacity: 0; transition: 0.3s; 
  display: flex; align-items: center; justify-content: center;
}
.document-card:hover .overlay-hover { opacity: 1; }

.upload-zone { border: 2px dashed var(--border-color); transition: 0.2s; }
.upload-zone:hover { border-color: var(--primary-color); background: rgba(13, 110, 253, 0.05); }

.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.6); backdrop-filter: blur(4px); z-index: 2000; }
.object-fit-cover { object-fit: cover; }
.z-1 { z-index: 1; }
.border-theme { border-color: var(--border-color) !important; }
.animate-fade-in { animation: fadeIn 0.4s ease-out; }
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
.page-toolbar-actions { width: min(100%, 680px); }
.profile-selector { width: 100%; }
.profile-select { width: 100%; min-width: 0; max-width: 100%; }

@media (max-width: 767.98px) {
  .documents-page {
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

  .documents-page .p-4 {
    padding: 0.9rem !important;
  }

  .document-preview {
    height: 140px;
  }
}
</style>
