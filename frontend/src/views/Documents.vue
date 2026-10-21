<template>
  <div class="container-fluid py-2 animate-fade-in documents-page">
    <div class="d-flex justify-content-between align-items-center mb-4 page-toolbar flex-wrap gap-3">
      <div>
        <h3 class="fw-bold text-theme-main mb-1">
          <i class="bi bi-file-earmark-medical text-primary me-2"></i>Kho tài liệu y tế
        </h3>
        <p class="text-theme-muted small mb-0">Lưu trữ và theo dõi kết quả xét nghiệm, đơn thuốc, phiếu khám</p>
      </div>

      <div class="d-flex align-items-center gap-2 flex-wrap">
        <div class="profile-selector shadow-sm rounded-pill bg-theme-card px-3 py-1 border border-theme d-flex align-items-center">
          <i class="bi bi-person-circle text-primary me-2"></i>
          <select class="form-select border-0 bg-transparent fw-semibold text-theme-main profile-select py-1" v-model="selectedProfileId" @change="fetchDocuments" style="box-shadow: none; width: auto; min-width: 190px;">
            <option v-for="p in profiles" :key="p.id" :value="p.id" class="bg-theme-card text-theme-main">{{ p.fullName }} ({{ p.profileName }})</option>
          </select>
        </div>

        <button v-if="canEdit" class="btn btn-primary rounded-pill px-3 py-2 shadow-sm fw-bold d-inline-flex align-items-center" @click="showUploadModal = true">
          <i class="bi bi-cloud-arrow-up-fill me-2 fs-6"></i>
          <span>Tải lên tài liệu</span>
        </button>
      </div>
    </div>

    <!-- Search & Filter Bar -->
    <div class="row g-3 mb-4">
      <div class="col-md-8">
        <div class="input-group shadow-sm rounded-pill overflow-hidden bg-theme-card border border-theme">
          <span class="input-group-text bg-transparent border-0 ps-3"><i class="bi bi-search text-theme-muted"></i></span>
          <input type="text" class="form-control bg-transparent border-0 py-2 text-theme-main shadow-none" v-model="searchQuery" placeholder="Tìm kiếm tên tài liệu, kết quả xét nghiệm, đơn thuốc...">
        </div>
      </div>
      <div class="col-md-4">
        <select class="form-select rounded-pill bg-theme-card text-theme-main border-theme shadow-sm py-2" v-model="filterType">
          <option value="ALL">Tất cả loại tài liệu</option>
          <option value="LAB_TEST">Phiếu xét nghiệm</option>
          <option value="PRESCRIPTION">Đơn thuốc / Toa thuốc</option>
          <option value="MEDICAL_EXAM">Phiếu khám bệnh / Giấy ra viện</option>
          <option value="VACCINATION">Sổ / Giấy tiêm chủng</option>
          <option value="OTHER">Tài liệu khác</option>
        </select>
      </div>
    </div>

    <!-- Documents List / Grid -->
    <div class="bg-theme-card p-4 rounded-4 shadow-sm border border-theme">
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-primary" role="status"></div>
        <p class="text-theme-muted mt-2 small">Đang tải danh sách tài liệu...</p>
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
          <div class="card bg-theme-card h-100 border border-theme shadow-sm rounded-4 overflow-hidden document-card position-relative d-flex flex-column">
            
            <!-- Delete Button -->
            <button v-if="canEdit" class="btn btn-danger btn-sm rounded-circle position-absolute top-0 end-0 m-2 shadow-sm z-2" 
                    style="width: 28px; height: 28px; padding: 0;" @click.stop="deleteDocument(doc.id)" title="Xóa tài liệu">
              <i class="bi bi-x"></i>
            </button>

            <!-- Type Badge on top-left -->
            <div class="position-absolute top-0 start-0 m-2 z-2">
              <span class="badge rounded-pill shadow-sm" :class="getDocumentTypeBadgeClass(doc.documentType)">
                {{ getDocumentTypeLabel(doc.documentType) }}
              </span>
            </div>

            <!-- Preview Thumbnail (Click to open split review modal) -->
            <div class="document-preview bg-theme-light d-flex align-items-center justify-content-center border-bottom border-theme" @click="openReviewModal(doc)">
              <template v-if="isImage(doc.type)">
                <img :src="doc.url" class="img-fluid w-100 h-100 object-fit-cover" alt="Document preview">
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

            <!-- Card Body -->
            <div class="card-body p-3 bg-theme-card d-flex flex-column flex-grow-1 justify-content-between">
              <div>
                <h6 class="fw-bold text-theme-main text-truncate mb-1 cursor-pointer" :title="doc.name" @click="openReviewModal(doc)">
                  {{ doc.name }}
                </h6>
                <div class="small text-theme-muted mb-2">
                  <i class="bi bi-calendar-event me-1"></i> {{ doc.date }}
                </div>

                <!-- Status Pill -->
                <div class="mb-2">
                  <span v-if="doc.extractionStatus === 'COMPLETED'" class="badge bg-success bg-opacity-10 text-success border border-success border-opacity-25 small">
                    <i class="bi bi-check-circle me-1"></i>Đã trích xuất
                    <i v-if="doc.isApplied" class="bi bi-check-all ms-1 text-primary" title="Đã lưu vào hồ sơ"></i>
                  </span>
                  <span v-else-if="doc.extractionStatus === 'PROCESSING'" class="badge bg-primary bg-opacity-10 text-primary border border-primary border-opacity-25 small">
                    <span class="spinner-border spinner-border-sm me-1" style="width: 0.7rem; height: 0.7rem;"></span>Đang phân tích...
                  </span>
                  <span v-else-if="doc.extractionStatus === 'FAILED'" class="badge bg-danger bg-opacity-10 text-danger border border-danger border-opacity-25 small" :title="doc.errorMessage">
                    <i class="bi bi-exclamation-triangle me-1"></i>Lỗi phân tích
                  </span>
                  <span v-else class="badge bg-secondary bg-opacity-10 text-theme-muted border border-secondary border-opacity-25 small">
                    <i class="bi bi-hourglass-split me-1"></i>Chưa trích xuất
                  </span>
                </div>

                <!-- Summary snippet -->
                <p v-if="doc.summary" class="extra-small text-theme-muted text-truncate-2 mb-2">
                  {{ doc.summary }}
                </p>
              </div>

              <!-- Action buttons -->
              <div class="pt-2 border-top border-theme d-flex gap-2">
                <button v-if="doc.extractionStatus === 'COMPLETED'" 
                        class="btn btn-sm btn-primary rounded-pill flex-grow-1 fw-bold shadow-sm"
                        @click.stop="openReviewModal(doc)">
                  <i class="bi bi-file-earmark-medical me-1"></i> Xem chi tiết
                </button>
                <button v-else-if="doc.extractionStatus === 'PROCESSING'" 
                        class="btn btn-sm btn-outline-primary rounded-pill flex-grow-1 disabled">
                  <span class="spinner-border spinner-border-sm me-1"></span> Đang xử lý
                </button>
                <button v-else 
                        class="btn btn-sm btn-outline-primary rounded-pill flex-grow-1 fw-bold"
                        @click.stop="triggerExtract(doc)">
                  <i class="bi bi-file-text me-1"></i> Trích xuất
                </button>

                <a :href="doc.url" :download="doc.name" class="btn btn-sm btn-light bg-theme-light text-primary rounded-circle border border-theme" title="Tải tệp tin">
                  <i class="bi bi-download"></i>
                </a>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Upload Modal -->
    <div v-if="showUploadModal" class="modal-overlay d-flex align-items-center justify-content-center p-3" @click.self="showUploadModal = false">
      <div class="bg-theme-card rounded-4 shadow-lg w-100 overflow-hidden border border-theme" style="max-width: 520px;">
        <div class="p-3 border-bottom border-theme d-flex justify-content-between align-items-center bg-theme-light">
          <h5 class="mb-0 fw-bold text-theme-main">
            <i class="bi bi-cloud-arrow-up text-primary me-2"></i>Tải lên tài liệu y tế
          </h5>
          <button class="btn-close" :class="{'btn-close-white': isDarkMode}" @click="showUploadModal = false"></button>
        </div>
        <form @submit.prevent="submitUpload" class="p-4 bg-theme-card">
          <div class="mb-3">
            <label class="form-label small fw-bold text-theme-main">Tên tài liệu / Ghi chú</label>
            <input type="text" class="form-control bg-theme-light text-theme-main border-theme rounded-3" v-model="uploadForm.documentName" 
                   placeholder="VD: Kết quả xét nghiệm máu, Đơn thuốc BV Chợ Rẫy..." required>
          </div>

          <div class="mb-3">
            <label class="form-label small fw-bold text-theme-main">Chọn tệp tin (Ảnh hoặc PDF)</label>
            <div class="upload-zone rounded-3 p-4 text-center border-dashed border-theme position-relative" :class="{'bg-primary bg-opacity-10 border-primary': uploadForm.file}">
              <input type="file" class="position-absolute top-0 start-0 w-100 h-100 opacity-0 cursor-pointer" 
                     @change="handleFileChange" accept=".pdf,image/*">
              <div v-if="!uploadForm.file">
                <i class="bi bi-cloud-arrow-up display-5 text-primary"></i>
                <p class="small text-theme-muted mb-0 mt-2">Nhấn hoặc kéo thả tệp vào đây</p>
                <small class="text-theme-muted extra-small">Hỗ trợ JPG, PNG, WEBP, PDF (Tối đa 50MB)</small>
              </div>
              <div v-else class="d-flex align-items-center justify-content-center gap-2">
                <i class="bi bi-check-circle-fill text-success fs-4"></i>
                <div class="text-start">
                  <div class="small fw-bold text-theme-main text-truncate" style="max-width: 280px;">{{ uploadForm.file.name }}</div>
                  <small class="text-theme-muted">{{ (uploadForm.file.size / 1024 / 1024).toFixed(2) }} MB</small>
                </div>
                <button type="button" class="btn-close ms-2" :class="{'btn-close-white': isDarkMode}" style="font-size: 0.7rem;" @click.stop="uploadForm.file = null"></button>
              </div>
            </div>
          </div>

          <!-- Auto OCR Toggle Option -->
          <div class="form-check form-switch mb-4 p-3 bg-theme-light rounded-3 border border-theme">
            <input class="form-check-input ms-0 me-3 cursor-pointer" type="checkbox" id="autoExtractSwitch" v-model="uploadForm.autoExtract">
            <label class="form-check-label text-theme-main small fw-bold cursor-pointer" for="autoExtractSwitch">
              <i class="bi bi-magic text-primary me-1"></i> Tự động OCR & Trích xuất dữ liệu AI
              <div class="text-theme-muted fw-normal extra-small mt-1">Hệ thống sẽ tự động đọc chỉ số, đơn thuốc và phân loại tài liệu ngay sau khi tải lên.</div>
            </label>
          </div>

          <div class="d-grid">
            <button type="submit" class="btn btn-primary rounded-pill py-2 fw-bold" :disabled="submitting || !uploadForm.file">
              <span v-if="submitting" class="spinner-border spinner-border-sm me-2"></span>
              <i v-else class="bi bi-upload me-1"></i>
              {{ submitting ? 'Đang tải lên & xử lý...' : 'Bắt đầu tải lên' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Split-Screen Review & Action Modal (Clean & Professional Medical View) -->
    <div v-if="showReviewModal && activeDoc" class="modal-overlay d-flex align-items-center justify-content-center p-2 p-md-3" @click.self="showReviewModal = false">
      <div class="bg-theme-card rounded-4 shadow-lg w-100 overflow-hidden border border-theme review-modal-container d-flex flex-column">
        
        <!-- Modal Header (Compact & Clean with Full Summary) -->
        <div class="px-3 py-2 border-bottom border-theme d-flex justify-content-between align-items-start bg-theme-light flex-shrink-0">
          <div class="flex-grow-1 min-w-0 me-3">
            <div class="d-flex align-items-center gap-2">
              <span class="badge rounded-pill" :class="getDocumentTypeBadgeClass(activeDoc.documentType)">
                {{ getDocumentTypeLabel(activeDoc.documentType) }}
              </span>
              <h5 class="mb-0 fw-bold text-theme-main">{{ activeDoc.name }}</h5>
            </div>
            <div v-if="activeDoc.summary" class="small text-theme-muted mt-1 lh-base">
              <i class="bi bi-info-circle me-1 text-primary"></i>{{ activeDoc.summary }}
            </div>
          </div>
          <button class="btn-close mt-1" :class="{'btn-close-white': isDarkMode}" @click="showReviewModal = false" title="Đóng"></button>
        </div>

        <!-- Modal Body (Split Screen: Left = Document Original, Right = Structured Medical Data) -->
        <div class="row g-0 flex-grow-1 overflow-hidden">
          
          <!-- LEFT COLUMN: Document Original Preview (Image / PDF Card) -->
          <div class="col-lg-5 border-end border-theme bg-theme-light d-flex flex-column h-100">
            <div class="px-3 py-2 border-bottom border-theme d-flex justify-content-between align-items-center bg-white bg-opacity-25">
              <small class="fw-bold text-theme-muted"><i class="bi bi-file-earmark-medical me-1"></i>Tài liệu gốc đối chiếu</small>
              <a :href="activeDoc.url" target="_blank" class="btn btn-sm btn-link text-decoration-none py-0 small">
                <i class="bi bi-box-arrow-up-right me-1"></i>Mở toàn màn hình
              </a>
            </div>
            <div class="preview-scroll-area flex-grow-1 p-2 text-center overflow-auto d-flex align-items-center justify-content-center">
              <template v-if="isImage(activeDoc.type)">
                <img :src="activeDoc.url" class="img-fluid rounded-3 shadow-sm preview-image" alt="Original document">
              </template>
              <template v-else-if="activeDoc.type === 'pdf'">
                <div class="pdf-preview-box w-100 h-100 d-flex flex-column align-items-center justify-content-center p-4 bg-white rounded-3 shadow-sm border border-theme text-center" style="min-height: 480px;">
                  <div class="p-4 bg-danger bg-opacity-10 rounded-circle mb-3 shadow-sm">
                    <i class="bi bi-file-earmark-pdf-fill text-danger display-3"></i>
                  </div>
                  <h6 class="fw-bold text-theme-main mb-1 text-break">{{ activeDoc.name }}.pdf</h6>
                  <p class="small text-theme-muted mb-4">Tài liệu y tế định dạng PDF</p>

                  <div class="d-flex flex-column gap-2 w-100" style="max-width: 280px;">
                    <a :href="activeDoc.url" target="_blank" class="btn btn-danger rounded-pill fw-bold shadow-sm py-2">
                      <i class="bi bi-file-earmark-text me-1"></i> Mở xem toàn bộ file PDF
                    </a>
                    <a :href="activeDoc.url" :download="activeDoc.name" class="btn btn-outline-secondary rounded-pill py-2 small">
                      <i class="bi bi-download me-1"></i> Tải tệp PDF về máy
                    </a>
                  </div>
                </div>
              </template>
              <template v-else>
                <div class="py-5 text-theme-muted text-center">
                  <i class="bi bi-file-earmark-medical display-1 text-primary"></i>
                  <p class="mt-2">Định dạng file không hỗ trợ xem trực tiếp.<br>Vui lòng mở toàn màn hình để xem.</p>
                  <a :href="activeDoc.url" target="_blank" class="btn btn-sm btn-outline-primary rounded-pill">Mở file</a>
                </div>
              </template>
            </div>
          </div>

          <!-- RIGHT COLUMN: Structured Medical Data & Direct Editing -->
          <div class="col-lg-7 d-flex flex-column h-100 bg-theme-card">
            
            <!-- Clean Profile Bar & Tabs Header -->
            <div class="px-3 pt-2 pb-0 bg-theme-light border-bottom border-theme flex-shrink-0">
              <div class="d-flex align-items-center justify-content-between flex-wrap gap-2 mb-2">
                <div class="d-flex align-items-center gap-2 flex-wrap">
                  <span class="small fw-bold text-theme-main"><i class="bi bi-folder2-open text-primary me-1"></i>Hồ sơ:</span>
                  <select class="form-select form-select-sm bg-theme-card text-theme-main border-theme rounded-pill py-1 px-3 fw-semibold shadow-none" 
                          v-model="targetProfileId" style="width: auto; min-width: 200px;">
                    <option v-for="p in profiles" :key="p.id" :value="p.id">
                      {{ p.fullName }} ({{ p.profileName }})
                    </option>
                  </select>
                  <button v-if="Number(targetProfileId) !== Number(activeDoc.profileId)" 
                          class="btn btn-sm btn-primary rounded-pill fw-bold px-3 shadow-sm"
                          @click="transferDocumentProfile" title="Chuyển tài liệu này sang hồ sơ được chọn">
                    <i class="bi bi-arrow-left-right me-1"></i> Chuyển sang hồ sơ này
                  </button>
                  <span v-else class="badge bg-secondary bg-opacity-10 text-theme-muted border border-theme small">
                    <i class="bi bi-check2 me-1"></i>Hồ sơ hiện tại
                  </span>
                </div>
                <span v-if="activeDoc.isApplied" class="badge bg-success bg-opacity-10 text-success border border-success border-opacity-25 small">
                  <i class="bi bi-check-all me-1"></i>Đã lưu vào hồ sơ
                </span>
              </div>

              <!-- Tab Navigation -->
              <ul class="nav nav-tabs border-0 gap-1">
                <li class="nav-item">
                  <button class="nav-link py-2 px-3 fw-bold small" :class="{active: activeTab === 'metrics'}" @click="activeTab = 'metrics'">
                    <i class="bi bi-activity text-danger me-1"></i> Chỉ số sinh hóa
                  </button>
                </li>
                <li class="nav-item">
                  <button class="nav-link py-2 px-3 fw-bold small" :class="{active: activeTab === 'medications'}" @click="activeTab = 'medications'">
                    <i class="bi bi-capsule text-success me-1"></i> Toa thuốc ({{ editableMedications.length }})
                  </button>
                </li>
                <li class="nav-item">
                  <button class="nav-link py-2 px-3 fw-bold small" :class="{active: activeTab === 'appointment'}" @click="activeTab = 'appointment'">
                    <i class="bi bi-calendar2-check text-primary me-1"></i> Lịch khám
                  </button>
                </li>
                <li class="nav-item">
                  <button class="nav-link py-2 px-3 fw-bold small" :class="{active: activeTab === 'raw'}" @click="activeTab = 'raw'">
                    <i class="bi bi-file-text text-theme-muted me-1"></i> Văn bản gốc
                  </button>
                </li>
              </ul>
            </div>

            <!-- Tab Contents (Spacious & Scrollable) -->
            <div class="p-3 p-md-4 flex-grow-1 overflow-auto">
              
              <!-- TAB 1: METRICS & LAB TESTS (EDITABLE FORM) -->
              <div v-if="activeTab === 'metrics'" class="animate-fade-in">
                <div class="d-flex justify-content-between align-items-center mb-3">
                  <div>
                    <h6 class="fw-bold text-theme-main mb-1"><i class="bi bi-heart-pulse text-danger me-2"></i>Chỉ số cơ thể & Xét nghiệm</h6>
                    <small class="text-theme-muted">Kiểm tra và sửa lại nếu cần trước khi lưu.</small>
                  </div>
                </div>

                <!-- Editable Metric Form Grid -->
                <div class="card p-3 rounded-4 border border-theme bg-theme-light shadow-sm mb-4">
                  <div class="row g-3">
                    
                    <!-- Huyết áp -->
                    <div class="col-md-6">
                      <label class="form-label small fw-bold text-theme-main">
                        <i class="bi bi-speedometer2 text-danger me-1"></i> Huyết áp (Tâm thu / Tâm trương)
                      </label>
                      <div class="input-group">
                        <input type="number" class="form-control" placeholder="Tâm thu (120)" v-model.number="editableMetrics.systolicBp">
                        <span class="input-group-text bg-theme-card border-theme text-theme-muted">/</span>
                        <input type="number" class="form-control" placeholder="Tâm trương (80)" v-model.number="editableMetrics.diastolicBp">
                        <span class="input-group-text bg-theme-card border-theme text-theme-muted">mmHg</span>
                      </div>
                      <small class="extra-small text-theme-muted">Ví dụ: 120/80 mmHg</small>
                    </div>

                    <!-- Đường huyết -->
                    <div class="col-md-6">
                      <label class="form-label small fw-bold text-theme-main">
                        <i class="bi bi-droplet-half text-primary me-1"></i> Đường huyết (Glucose)
                      </label>
                      <div class="input-group">
                        <input type="text" class="form-control" placeholder="Trị số đo (vd: 5.4)" v-model="editableMetrics.glucoseValue">
                        <select class="form-select" v-model.number="editableMetrics.glucose" style="max-width: 140px;">
                          <option :value="1">1: Bình thường</option>
                          <option :value="2">2: Tiền ĐTĐ</option>
                          <option :value="3">3: ĐTĐ (Cao)</option>
                        </select>
                      </div>
                      <small class="extra-small text-theme-muted">Trị số & Mức phân loại nguy cơ (1-3)</small>
                    </div>

                    <!-- Cholesterol -->
                    <div class="col-md-6">
                      <label class="form-label small fw-bold text-theme-main">
                        <i class="bi bi-pie-chart text-warning me-1"></i> Mỡ máu (Cholesterol toàn phần)
                      </label>
                      <div class="input-group">
                        <input type="text" class="form-control" placeholder="Trị số đo (vd: 4.8)" v-model="editableMetrics.cholesterolValue">
                        <select class="form-select" v-model.number="editableMetrics.cholesterol" style="max-width: 140px;">
                          <option :value="1">1: Bình thường</option>
                          <option :value="2">2: Cận cao</option>
                          <option :value="3">3: Cao</option>
                        </select>
                      </div>
                      <small class="extra-small text-theme-muted">Trị số & Mức phân loại nguy cơ (1-3)</small>
                    </div>

                    <!-- Thể trạng: Chiều cao & Cân nặng -->
                    <div class="col-md-6">
                      <label class="form-label small fw-bold text-theme-main">
                        <i class="bi bi-person text-success me-1"></i> Thể trạng (Chiều cao & Cân nặng)
                      </label>
                      <div class="input-group">
                        <input type="number" step="0.1" class="form-control" placeholder="Cao (cm)" v-model.number="editableMetrics.heightInCm">
                        <span class="input-group-text bg-theme-card border-theme text-theme-muted">cm</span>
                        <input type="number" step="0.1" class="form-control" placeholder="Nặng (kg)" v-model.number="editableMetrics.weightInKg">
                        <span class="input-group-text bg-theme-card border-theme text-theme-muted">kg</span>
                      </div>
                      <small class="extra-small text-theme-muted">Dùng để tính BMI</small>
                    </div>

                    <!-- Ngày đo -->
                    <div class="col-md-6">
                      <label class="form-label small fw-bold text-theme-main">
                        <i class="bi bi-calendar-event me-1"></i> Ngày ghi nhận chỉ số
                      </label>
                      <input type="date" class="form-control" v-model="editableMetrics.logDate">
                    </div>

                  </div>
                </div>

                <!-- Action Buttons For Metrics (Clean & Right Aligned) -->
                <div class="d-flex align-items-center justify-content-end gap-2 mb-4 p-3 bg-theme-light rounded-4 border border-theme">
                  <button class="btn btn-outline-primary rounded-pill fw-bold px-3 shadow-sm" :disabled="applying" @click="goToCardioPrediction">
                    <i class="bi bi-heart-pulse me-1"></i> Lưu & Dự đoán tim mạch
                  </button>
                  <button class="btn btn-success rounded-pill fw-bold px-4 shadow-sm" :disabled="applying" @click="saveAndApplyMetrics">
                    <span v-if="applying" class="spinner-border spinner-border-sm me-1"></span>
                    <i v-else class="bi bi-check2-circle me-1"></i> Lưu vào Chỉ số cơ thể
                  </button>
                </div>

                <!-- Other Lab Results Table -->
                <div v-if="parsedJson.metrics?.otherLabResults?.length" class="mt-3">
                  <h6 class="fw-bold text-theme-main small mb-2">Các kết quả xét nghiệm cận lâm sàng khác:</h6>
                  <div class="table-responsive rounded-3 border border-theme">
                    <table class="table table-hover table-sm mb-0 align-middle">
                      <thead class="table-light">
                        <tr>
                          <th>Tên xét nghiệm</th>
                          <th>Giá trị</th>
                          <th>Đơn vị</th>
                          <th>Khoảng chuẩn</th>
                          <th>Đánh giá</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-for="(lab, idx) in parsedJson.metrics.otherLabResults" :key="idx">
                          <td class="fw-bold">{{ lab.name }}</td>
                          <td>{{ lab.value }}</td>
                          <td><small class="text-theme-muted">{{ lab.unit }}</small></td>
                          <td><small class="text-theme-muted">{{ lab.normalRange }}</small></td>
                          <td>
                            <span class="badge" :class="getLabStatusClass(lab.status)">{{ lab.status || 'Bình thường' }}</span>
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>

              <!-- TAB 2: MEDICATIONS (EDITABLE LIST) -->
              <div v-if="activeTab === 'medications'" class="animate-fade-in">
                <div class="d-flex justify-content-between align-items-center mb-3">
                  <div>
                    <h6 class="fw-bold text-theme-main mb-1"><i class="bi bi-capsule text-success me-2"></i>Danh sách thuốc trong toa</h6>
                    <small class="text-theme-muted">Chỉnh sửa tên, hàm lượng, số lượng hoặc xóa bớt dòng không cần thiết.</small>
                  </div>
                  <button class="btn btn-sm btn-outline-primary rounded-pill fw-bold px-3" @click="addMedicationRow">
                    <i class="bi bi-plus-lg me-1"></i> Thêm thuốc
                  </button>
                </div>

                <div v-if="!editableMedications.length" class="text-center py-5 text-theme-muted bg-theme-light rounded-4 border border-theme mb-3">
                  <i class="bi bi-capsule display-4 opacity-25"></i>
                  <p class="mt-2 mb-3">Hiện chưa có thuốc nào trong danh sách.</p>
                  <button class="btn btn-sm btn-outline-primary rounded-pill px-3" @click="addMedicationRow">
                    <i class="bi bi-plus-lg me-1"></i> Thêm thuốc thủ công
                  </button>
                </div>

                <div class="row g-3 mb-4" v-else>
                  <div v-for="(med, idx) in editableMedications" :key="idx" class="col-12">
                    <div class="card p-3 rounded-4 border border-theme bg-theme-light shadow-sm position-relative">
                      
                      <!-- Header row: Med name & Delete btn -->
                      <div class="d-flex justify-content-between align-items-center gap-2 mb-2">
                        <div class="d-flex align-items-center gap-2 flex-grow-1">
                          <span class="badge bg-secondary rounded-pill">{{ idx + 1 }}</span>
                          <input type="text" class="form-control form-control-sm fw-bold text-theme-main" 
                                 placeholder="Tên thuốc (ví dụ: Paracetamol 500mg)" v-model="med.medicineName">
                        </div>
                        <button class="btn btn-sm btn-outline-danger border-0 rounded-circle" title="Xóa thuốc này" @click="removeMedicationRow(idx)">
                          <i class="bi bi-trash fs-6"></i>
                        </button>
                      </div>

                      <!-- Row details: Dosage, Quantity, Unit, Duration -->
                      <div class="row g-2 mb-2">
                        <div class="col-6 col-md-3">
                          <label class="extra-small text-theme-muted d-block">Hàm lượng</label>
                          <input type="text" class="form-control form-control-sm" placeholder="vd: 500mg" v-model="med.dosage">
                        </div>
                        <div class="col-3 col-md-3">
                          <label class="extra-small text-theme-muted d-block">Số lượng</label>
                          <input type="number" class="form-control form-control-sm" placeholder="vd: 20" v-model.number="med.quantity">
                        </div>
                        <div class="col-3 col-md-3">
                          <label class="extra-small text-theme-muted d-block">Đơn vị</label>
                          <input type="text" class="form-control form-control-sm" placeholder="viên/gói" v-model="med.unit">
                        </div>
                        <div class="col-6 col-md-3">
                          <label class="extra-small text-theme-muted d-block">Số ngày dùng</label>
                          <input type="number" class="form-control form-control-sm" placeholder="vd: 7" v-model.number="med.durationInDays">
                        </div>
                      </div>

                      <!-- Row Doses: Morning, Noon, Afternoon, Evening -->
                      <div class="row g-2 mb-2 text-center">
                        <div class="col-3">
                          <div class="p-1 bg-theme-card rounded border border-theme">
                            <small class="extra-small text-theme-muted d-block">Sáng</small>
                            <input type="text" class="form-control form-control-sm text-center border-0 p-0 fw-bold" v-model="med.morningDose" placeholder="0">
                          </div>
                        </div>
                        <div class="col-3">
                          <div class="p-1 bg-theme-card rounded border border-theme">
                            <small class="extra-small text-theme-muted d-block">Trưa</small>
                            <input type="text" class="form-control form-control-sm text-center border-0 p-0 fw-bold" v-model="med.noonDose" placeholder="0">
                          </div>
                        </div>
                        <div class="col-3">
                          <div class="p-1 bg-theme-card rounded border border-theme">
                            <small class="extra-small text-theme-muted d-block">Chiều</small>
                            <input type="text" class="form-control form-control-sm text-center border-0 p-0 fw-bold" v-model="med.afternoonDose" placeholder="0">
                          </div>
                        </div>
                        <div class="col-3">
                          <div class="p-1 bg-theme-card rounded border border-theme">
                            <small class="extra-small text-theme-muted d-block">Tối</small>
                            <input type="text" class="form-control form-control-sm text-center border-0 p-0 fw-bold" v-model="med.eveningDose" placeholder="0">
                          </div>
                        </div>
                      </div>

                      <!-- Timing and Instructions -->
                      <div class="row g-2">
                        <div class="col-md-5">
                          <input type="text" class="form-control form-control-sm" placeholder="Thời điểm (vd: Sau ăn 30p)" v-model="med.timing">
                        </div>
                        <div class="col-md-7">
                          <input type="text" class="form-control form-control-sm" placeholder="Lời dặn / Cách dùng" v-model="med.instructions">
                        </div>
                      </div>

                    </div>
                  </div>
                </div>

                <!-- Action Buttons For Medications (Right Aligned) -->
                <div class="d-flex align-items-center justify-content-end gap-2 p-3 bg-theme-light rounded-4 border border-theme">
                  <button class="btn btn-success rounded-pill fw-bold px-4 shadow-sm" 
                          :disabled="applying || !editableMedications.length" @click="saveAndApplyMedications">
                    <span v-if="applying" class="spinner-border spinner-border-sm me-1"></span>
                    <i v-else class="bi bi-capsule me-1"></i> Lưu vào Danh mục thuốc & Lịch uống ({{ editableMedications.length }} thuốc)
                  </button>
                </div>
              </div>

              <!-- TAB 3: APPOINTMENT (EDITABLE FORM) -->
              <div v-if="activeTab === 'appointment'" class="animate-fade-in">
                <div class="d-flex justify-content-between align-items-center mb-3">
                  <div>
                    <h6 class="fw-bold text-theme-main mb-1"><i class="bi bi-hospital text-primary me-2"></i>Thông tin lần khám bệnh</h6>
                    <small class="text-theme-muted">Kiểm tra thông tin chẩn đoán và nơi khám trước khi lưu vào Lịch sử khám bệnh.</small>
                  </div>
                </div>

                <div class="card p-3 rounded-4 border border-theme bg-theme-light shadow-sm mb-4">
                  <div class="row g-3">
                    <div class="col-md-6">
                      <label class="form-label small fw-bold text-theme-main">Ngày khám</label>
                      <input type="date" class="form-control" v-model="editableAppointment.appointmentDate">
                    </div>
                    <div class="col-md-6">
                      <label class="form-label small fw-bold text-theme-main">Bác sĩ khám</label>
                      <input type="text" class="form-control" placeholder="Tên bác sĩ" v-model="editableAppointment.doctorName">
                    </div>
                    <div class="col-12">
                      <label class="form-label small fw-bold text-theme-main">Cơ sở khám / Bệnh viện</label>
                      <input type="text" class="form-control" placeholder="Tên bệnh viện, phòng khám" v-model="editableAppointment.location">
                    </div>
                    <div class="col-12">
                      <label class="form-label small fw-bold text-theme-main">Lý do khám</label>
                      <input type="text" class="form-control" placeholder="Lý do đến khám" v-model="editableAppointment.reason">
                    </div>
                    <div class="col-12">
                      <label class="form-label small fw-bold text-theme-main">Chẩn đoán của bác sĩ</label>
                      <input type="text" class="form-control fw-bold text-primary" placeholder="Chẩn đoán bệnh (nếu có)" v-model="editableAppointment.diagnosis">
                      <small class="extra-small text-theme-muted">Nếu là bệnh mạn tính (Tiểu đường, Tăng huyết áp...), hệ thống sẽ tự động cập nhật vào hồ sơ bệnh mạn tính.</small>
                    </div>
                    <div class="col-12">
                      <label class="form-label small fw-bold text-theme-main">Lời dặn của bác sĩ</label>
                      <textarea class="form-control" rows="2" placeholder="Lời dặn, kiêng cử, hẹn tái khám..." v-model="editableAppointment.doctorNote"></textarea>
                    </div>
                  </div>
                </div>

                <!-- Action Buttons For Appointment (Right Aligned) -->
                <div class="d-flex align-items-center justify-content-end gap-2 p-3 bg-theme-light rounded-4 border border-theme">
                  <button class="btn btn-primary rounded-pill fw-bold px-4 shadow-sm" :disabled="applying" @click="saveAndApplyAppointment">
                    <span v-if="applying" class="spinner-border spinner-border-sm me-1"></span>
                    <i v-else class="bi bi-calendar2-check me-1"></i> Lưu vào Lịch khám bệnh
                  </button>
                </div>
              </div>

              <!-- TAB 4: RAW OCR TEXT (EXPANDED TO FULL HEIGHT) -->
              <div v-if="activeTab === 'raw'" class="animate-fade-in d-flex flex-column h-100">
                <div class="d-flex justify-content-between align-items-center mb-2 flex-shrink-0">
                  <h6 class="fw-bold text-theme-main mb-0"><i class="bi bi-file-text me-1"></i>Văn bản đọc được từ tài liệu:</h6>
                  <button class="btn btn-sm btn-outline-secondary rounded-pill" @click="copyOcrText">
                    <i class="bi bi-clipboard me-1"></i> Sao chép văn bản
                  </button>
                </div>
                <div class="p-3 bg-theme-light rounded-3 border border-theme text-theme-main font-monospace small flex-grow-1" 
                     style="white-space: pre-wrap; min-height: 500px; height: 100%; overflow-y: auto;">
                  {{ activeDoc.ocrRawText || parsedJson.ocrRawText || 'Chưa có văn bản trích xuất.' }}
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
import { useRouter } from 'vue-router';
import axios from 'axios';
import { toast } from '@/utils/toast';

const router = useRouter();

const documents = ref([]);
const profiles = ref([]);
const selectedProfileId = ref(null);
const canEdit = ref(false);
const loading = ref(true);
const searchQuery = ref('');
const filterType = ref('ALL');
const showUploadModal = ref(false);
const submitting = ref(false);
const applying = ref(false);

// Modal Review states
const showReviewModal = ref(false);
const activeDoc = ref(null);
const activeTab = ref('metrics');
const targetProfileId = ref(null);

// Editable Form States for Human-in-the-Loop review
const editableMetrics = reactive({
  systolicBp: null,
  diastolicBp: null,
  glucose: 1,
  glucoseValue: '',
  cholesterol: 1,
  cholesterolValue: '',
  heightInCm: null,
  weightInKg: null,
  logDate: ''
});

const editableMedications = ref([]);

const editableAppointment = reactive({
  appointmentDate: '',
  doctorName: '',
  location: '',
  reason: '',
  diagnosis: '',
  doctorNote: ''
});

const currentTargetProfileName = computed(() => {
  const p = profiles.value.find(x => Number(x.id) === Number(targetProfileId.value));
  return p ? p.fullName : '';
});

const onProfileChange = () => {
  // Khi chọn hồ sơ khác trong dropdown, chỉ cần cập nhật targetProfileId
};

const transferDocumentProfile = async () => {
  if (!activeDoc.value || !targetProfileId.value) return;
  try {
    const res = await axios.post(`/api/documents/${activeDoc.value.id}/change-profile?targetProfileId=${targetProfileId.value}`);
    if (res.data.success) {
      activeDoc.value.profileId = targetProfileId.value;
      activeDoc.value.profileName = currentTargetProfileName.value;
      toast.success(`Đã chuyển tài liệu thành công sang hồ sơ: ${currentTargetProfileName.value}`);
      await fetchDocuments();
    }
  } catch (err) {
    toast.error(err.response?.data?.message || "Không thể chuyển hồ sơ");
  }
};

const isDarkMode = computed(() => document.documentElement.getAttribute('data-bs-theme') === 'dark');

const uploadForm = reactive({
  documentName: '',
  file: null,
  autoExtract: true
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

const getDocumentTypeLabel = (type) => {
  switch (type) {
    case 'LAB_TEST': return 'Phiếu xét nghiệm';
    case 'PRESCRIPTION': return 'Đơn thuốc';
    case 'MEDICAL_EXAM': return 'Phiếu khám bệnh';
    case 'VACCINATION': return 'Tiêm chủng';
    case 'INSURANCE': return 'Thẻ BHYT';
    default: return 'Tài liệu y tế';
  }
};

const getDocumentTypeBadgeClass = (type) => {
  switch (type) {
    case 'LAB_TEST': return 'bg-info text-dark';
    case 'PRESCRIPTION': return 'bg-success text-white';
    case 'MEDICAL_EXAM': return 'bg-primary text-white';
    case 'VACCINATION': return 'bg-warning text-dark';
    default: return 'bg-secondary text-white';
  }
};

const getLabStatusClass = (status) => {
  if (!status) return 'bg-secondary';
  const s = status.toLowerCase();
  if (s.includes('cao') || s.includes('tăng')) return 'bg-danger';
  if (s.includes('thấp') || s.includes('giảm')) return 'bg-warning text-dark';
  return 'bg-success';
};

const parsedJson = computed(() => {
  if (!activeDoc.value || !activeDoc.value.extractedJson) return {};
  try {
    return JSON.parse(activeDoc.value.extractedJson);
  } catch (e) {
    return {};
  }
});

const filteredDocuments = computed(() => {
  let list = documents.value;
  if (filterType.value !== 'ALL') {
    list = list.filter(d => d.documentType === filterType.value);
  }
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.toLowerCase();
    list = list.filter(d => (d.name && d.name.toLowerCase().includes(q)) || 
                           (d.summary && d.summary.toLowerCase().includes(q)) ||
                           (d.ocrRawText && d.ocrRawText.toLowerCase().includes(q)));
  }
  return list;
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
      url: d.filePath.startsWith('http') ? d.filePath : `${baseUrl}/uploads/${d.filePath}`,
      documentType: d.documentType || 'OTHER',
      extractionStatus: d.extractionStatus || 'NONE',
      summary: d.summary,
      extractedJson: d.extractedJson,
      isApplied: d.isApplied,
      errorMessage: d.errorMessage,
      ocrRawText: d.ocrRawText,
      profileId: d.profileId,
      profileName: d.profileName
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
    formData.append('autoExtract', uploadForm.autoExtract);

    const res = await axios.post('/api/documents/upload', formData);

    if (res.data.success) {
      toast.success(uploadForm.autoExtract ? "Tải lên và quét AI thành công!" : "Tải lên tài liệu thành công!");
      showUploadModal.value = false;
      uploadForm.documentName = '';
      uploadForm.file = null;
      await fetchDocuments();

      // Nếu vừa autoExtract thành công, mở luôn modal xem kết quả
      if (uploadForm.autoExtract && res.data.document && res.data.document.extractionStatus === 'COMPLETED') {
        const doc = documents.value.find(d => d.id === res.data.document.id);
        if (doc) openReviewModal(doc);
      }
    }
  } catch (err) {
    toast.error(err.response?.data?.message || err.message);
  } finally {
    submitting.value = false;
  }
};

const triggerExtract = async (doc) => {
  doc.extractionStatus = 'PROCESSING';
  toast.info("Đang kích hoạt AI đọc tài liệu...");
  try {
    const res = await axios.post(`/api/documents/${doc.id}/extract`);
    if (res.data.success) {
      toast.success("AI đã đọc và phân tích tài liệu thành công!");
      await fetchDocuments();
      const updatedDoc = documents.value.find(d => d.id === doc.id);
      if (updatedDoc) openReviewModal(updatedDoc);
    } else {
      toast.error(res.data.message || "Quét tài liệu thất bại.");
      doc.extractionStatus = 'FAILED';
    }
  } catch (err) {
    doc.extractionStatus = 'FAILED';
    toast.error(err.response?.data?.message || err.message);
  }
};

const openReviewModal = (doc) => {
  activeDoc.value = doc;
  targetProfileId.value = doc.profileId || selectedProfileId.value;

  // Điền dữ liệu vào form chỉnh sửa từ JSON trích xuất
  let pj = {};
  if (doc.extractedJson) {
    try {
      pj = JSON.parse(doc.extractedJson);
    } catch (e) {
      pj = {};
    }
  }

  // Khởi tạo Editable Metrics
  editableMetrics.systolicBp = pj.metrics?.systolicBp ?? null;
  editableMetrics.diastolicBp = pj.metrics?.diastolicBp ?? null;
  editableMetrics.glucose = pj.metrics?.glucose ?? 1;
  editableMetrics.glucoseValue = pj.metrics?.glucoseValue ?? '';
  editableMetrics.cholesterol = pj.metrics?.cholesterol ?? 1;
  editableMetrics.cholesterolValue = pj.metrics?.cholesterolValue ?? '';
  editableMetrics.heightInCm = pj.metrics?.heightInCm ?? null;
  editableMetrics.weightInKg = pj.metrics?.weightInKg ?? null;
  editableMetrics.logDate = pj.documentDate || new Date().toISOString().substring(0, 10);

  // Khởi tạo Editable Medications
  editableMedications.value = (pj.medications || []).map(m => ({
    medicineName: m.medicineName || '',
    dosage: m.dosage || '',
    quantity: m.quantity || null,
    unit: m.unit || 'viên',
    durationInDays: m.durationInDays || 7,
    morningDose: m.morningDose || '1',
    noonDose: m.noonDose || '0',
    afternoonDose: m.afternoonDose || '0',
    eveningDose: m.eveningDose || '1',
    timing: m.timing || 'Sau ăn',
    instructions: m.instructions || ''
  }));

  // Khởi tạo Editable Appointment
  editableAppointment.appointmentDate = pj.appointment?.appointmentDate || pj.documentDate || new Date().toISOString().substring(0, 10);
  editableAppointment.doctorName = pj.appointment?.doctorName || pj.doctorName || '';
  editableAppointment.location = pj.appointment?.location || pj.facilityName || '';
  editableAppointment.reason = pj.appointment?.reason || ('Khám theo tài liệu: ' + doc.name);
  editableAppointment.diagnosis = pj.appointment?.diagnosis || '';
  editableAppointment.doctorNote = pj.appointment?.doctorNote || '';

  // Tự chọn tab phù hợp với loại tài liệu
  if (doc.documentType === 'PRESCRIPTION') {
    activeTab.value = 'medications';
  } else if (doc.documentType === 'MEDICAL_EXAM') {
    activeTab.value = 'appointment';
  } else {
    activeTab.value = 'metrics';
  }
  showReviewModal.value = true;
};

const closeReviewModal = () => {
  showReviewModal.value = false;
};

const addMedicationRow = () => {
  editableMedications.value.push({
    medicineName: '',
    dosage: '',
    quantity: 10,
    unit: 'viên',
    durationInDays: 7,
    morningDose: '1',
    noonDose: '0',
    afternoonDose: '0',
    eveningDose: '1',
    timing: 'Sau ăn',
    instructions: ''
  });
};

const removeMedicationRow = (idx) => {
  editableMedications.value.splice(idx, 1);
};

const saveAndApplyMetrics = async () => {
  if (!activeDoc.value) return;
  applying.value = true;
  try {
    const res = await axios.post(
      `/api/documents/${activeDoc.value.id}/apply-to-metrics?targetProfileId=${targetProfileId.value}`,
      editableMetrics
    );
    if (res.data.success) {
      toast.success(res.data.message || "Đã lưu chỉ số cơ thể thành công!");
      activeDoc.value.isApplied = true;
      await fetchDocuments();
    }
  } catch (err) {
    toast.error(err.response?.data?.message || err.message);
  } finally {
    applying.value = false;
  }
};

const saveAndApplyMedications = async () => {
  if (!activeDoc.value) return;
  if (!editableMedications.value.length) {
    toast.warning("Chưa có thuốc nào trong danh sách!");
    return;
  }
  applying.value = true;
  try {
    const res = await axios.post(
      `/api/documents/${activeDoc.value.id}/apply-to-medications?targetProfileId=${targetProfileId.value}`,
      editableMedications.value
    );
    if (res.data.success) {
      toast.success(res.data.message || "Đã tạo liệu trình dùng thuốc thành công!");
      activeDoc.value.isApplied = true;
      await fetchDocuments();
    }
  } catch (err) {
    toast.error(err.response?.data?.message || err.message);
  } finally {
    applying.value = false;
  }
};

const saveAndApplyAppointment = async () => {
  if (!activeDoc.value) return;
  applying.value = true;
  try {
    const res = await axios.post(
      `/api/documents/${activeDoc.value.id}/apply-to-appointment?targetProfileId=${targetProfileId.value}`,
      editableAppointment
    );
    if (res.data.success) {
      toast.success(res.data.message || "Đã lưu lịch khám bệnh thành công!");
      activeDoc.value.isApplied = true;
      await fetchDocuments();
    }
  } catch (err) {
    toast.error(err.response?.data?.message || err.message);
  } finally {
    applying.value = false;
  }
};

// Aliases for compatibility
const applyMetrics = saveAndApplyMetrics;
const applyMedications = saveAndApplyMedications;
const applyAppointment = saveAndApplyAppointment;

const goToCardioPrediction = async () => {
  // Lưu metrics đã chỉnh sửa trước khi chuyển trang
  await saveAndApplyMetrics();
  showReviewModal.value = false;
  router.push({
    path: '/ai-prediction',
    query: { profileId: targetProfileId.value }
  });
};

const copyOcrText = () => {
  const text = activeDoc.value?.ocrRawText || parsedJson.value?.ocrRawText || '';
  if (text) {
    navigator.clipboard.writeText(text);
    toast.success("Đã sao chép văn bản OCR vào bộ nhớ tạm!");
  }
};

const deleteDocument = async (id) => {
  if (!await toast.confirm("Xác nhận xóa", "Bạn muốn xóa tài liệu này vĩnh viễn?")) return;
  try {
    const res = await axios.delete(`/api/documents/${id}`);
    if (res.data.success) {
      toast.success("Đã xóa tài liệu!");
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

.document-preview { height: 160px; position: relative; overflow: hidden; }
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
.z-2 { z-index: 2; }
.border-theme { border-color: var(--border-color) !important; }
.animate-fade-in { animation: fadeIn 0.35s ease-out; }
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }

.extra-small { font-size: 0.75rem; }
.text-truncate-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.review-modal-container {
  max-width: 1200px;
  height: 90vh;
}

.preview-scroll-area {
  background: #f8f9fa;
}
[data-bs-theme="dark"] .preview-scroll-area {
  background: #1a1d20;
}

.preview-image {
  max-height: 80vh;
  object-fit: contain;
}

.nav-tabs .nav-link {
  border: none;
  color: var(--text-muted);
  border-bottom: 2px solid transparent;
}
.nav-tabs .nav-link.active {
  color: var(--primary-color);
  background: transparent;
  border-bottom: 2px solid var(--primary-color);
}

@media (max-width: 991.98px) {
  .review-modal-container {
    height: 96vh;
    overflow-y: auto !important;
  }
  .review-modal-container .row {
    flex-direction: column;
    overflow-y: visible !important;
  }
  .preview-scroll-area {
    min-height: 250px;
  }
}
</style>
