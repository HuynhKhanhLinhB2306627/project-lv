<template>
  <div class="container-fluid py-2 animate-fade-in medications-page">
    <!-- Toolbar đồng bộ với Vaccinations, Insurance, BodyMetrics -->
    <div class="d-flex justify-content-between align-items-center mb-4 page-toolbar">
      <div>
        <h3 class="fw-bold mb-1 text-theme-main">
          <i class="bi bi-capsule-pill text-primary me-2"></i>Lịch nhắc uống thuốc
        </h3>
        <p class="text-theme-muted small mb-0">Theo dõi cữ uống trong ngày và quản lý các liệu trình điều trị</p>
      </div>

      <div class="row g-2 page-toolbar-actions">
        <div class="col-12 col-md-8">
          <div class="profile-selector shadow-sm rounded-pill bg-theme-card ps-3 pe-2 py-1 border border-theme d-flex align-items-center">
            <i class="bi bi-person-circle text-primary me-2"></i>
            <select 
              class="form-select border-0 bg-transparent fw-semibold text-theme-main profile-select py-0" 
              v-model="selectedProfileId" 
              @change="fetchData" 
              style="box-shadow: none;"
            >
              <option v-for="p in profiles" :key="p.id" :value="p.id" class="bg-theme-card text-theme-main">
                {{ p.fullName }} ({{ p.profileName }})
              </option>
            </select>
          </div>
        </div>
        <div class="col-12 col-md-4 d-grid">
          <button v-if="canEdit" class="btn btn-primary shadow-sm rounded-pill px-3 fw-bold text-nowrap" @click="openCreateModal">
            <i class="bi bi-plus-circle me-1"></i> Thêm liệu trình
          </button>
        </div>
      </div>
    </div>

    <!-- Thông báo kết nối Telegram (Thiết kế thực tế, gọn gàng, không màu mè) -->
    <div v-if="telegramStatus.connected" class="alert alert-success d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3 rounded-4 shadow-sm border border-success border-opacity-25 bg-theme-card mb-4 py-3 px-3.5">
      <div class="d-flex align-items-center gap-3">
        <div class="bg-success bg-opacity-10 text-success p-2.5 rounded-3 d-flex align-items-center justify-content-center flex-shrink-0" style="width: 42px; height: 42px;">
          <i class="bi bi-telegram fs-4"></i>
        </div>
        <div>
          <div class="d-flex align-items-center gap-2">
            <span class="fw-bold text-theme-main">Đã bật nhắc nhở qua Telegram</span>
            <span class="badge bg-success bg-opacity-10 text-success border border-success border-opacity-25 rounded-pill extra-small">Đang kích hoạt</span>
          </div>
          <div class="small text-theme-muted mt-0.5">
            Tin nhắn tự động sẽ gửi đến Telegram <strong>(#{{ telegramStatus.chatId }})</strong> trước 30 phút mỗi cữ uống.
          </div>
        </div>
      </div>
      <div class="d-flex align-items-center gap-2 flex-shrink-0">
        <button class="btn btn-sm btn-outline-success rounded-pill px-3 fw-bold" @click="sendTelegramTest" :disabled="testingTelegram">
          <span v-if="testingTelegram" class="spinner-border spinner-border-sm me-1"></span>
          <i v-else class="bi bi-send-check me-1"></i> Gửi thử tin nhắn
        </button>
        <button class="btn btn-sm btn-outline-secondary rounded-pill px-3" @click="disconnectTelegram">
          Hủy liên kết
        </button>
      </div>
    </div>

    <div v-else class="alert alert-light d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3 rounded-4 shadow-sm border border-theme bg-theme-card mb-4 py-3 px-3.5">
      <div class="d-flex align-items-center gap-3">
        <div class="bg-primary bg-opacity-10 text-primary p-2.5 rounded-3 d-flex align-items-center justify-content-center flex-shrink-0" style="width: 42px; height: 42px;">
          <i class="bi bi-telegram fs-4"></i>
        </div>
        <div>
          <div class="fw-bold text-theme-main">Nhận thông báo uống thuốc qua Telegram</div>
          <div class="small text-theme-muted mt-0.5">
            Tự động nhận tin nhắn nhắc nhở trực tiếp trên điện thoại trước giờ uống, không lo quên cữ thuốc.
          </div>
        </div>
      </div>
      <button class="btn btn-sm btn-outline-primary rounded-pill px-3.5 py-1.5 fw-bold flex-shrink-0" @click="openTelegramModal">
        <i class="bi bi-link-45deg me-1"></i> Kết nối Telegram
      </button>
    </div>

    <!-- Thanh chuyển Tab tiêu chuẩn -->
    <ul class="nav nav-pills mb-4 bg-theme-light p-1.5 rounded-pill border border-theme d-inline-flex">
      <li class="nav-item">
        <button 
          class="nav-link rounded-pill px-3.5 py-2 fw-semibold text-theme-main d-flex align-items-center gap-2" 
          :class="{ 'active bg-primary text-white': activeTab === 'reminders' }" 
          @click="activeTab = 'reminders'"
        >
          <i class="bi bi-clock-history"></i>
          <span>Lịch uống hôm nay</span>
          <span 
            class="badge rounded-pill ms-1 extra-small"
            :class="activeTab === 'reminders' ? 'bg-white text-primary' : 'bg-secondary bg-opacity-25 text-theme-main'"
          >
            {{ todayReminders.length }}
          </span>
        </button>
      </li>
      <li class="nav-item">
        <button 
          class="nav-link rounded-pill px-3.5 py-2 fw-semibold text-theme-main d-flex align-items-center gap-2" 
          :class="{ 'active bg-primary text-white': activeTab === 'courses' }" 
          @click="activeTab = 'courses'"
        >
          <i class="bi bi-list-check"></i>
          <span>Danh sách liệu trình</span>
          <span 
            class="badge rounded-pill ms-1 extra-small"
            :class="activeTab === 'courses' ? 'bg-white text-primary' : 'bg-secondary bg-opacity-25 text-theme-main'"
          >
            {{ courses.length }}
          </span>
        </button>
      </li>
    </ul>

    <!-- ===================== TAB 1: LỊCH UỐNG HÔM NAY ===================== -->
    <div v-if="activeTab === 'reminders'">
      <div v-if="remindersLoading" class="text-center py-5">
        <div class="spinner-border text-primary" role="status"></div>
        <p class="text-theme-muted mt-2 small">Đang tải lịch nhắc...</p>
      </div>

      <div v-else-if="todayReminders.length === 0" class="text-center py-5 bg-theme-card rounded-4 shadow-sm border border-dashed border-theme">
        <i class="bi bi-calendar2-check display-4 text-theme-muted opacity-25"></i>
        <h5 class="fw-bold mt-3 text-theme-main">Không có cữ thuốc nào cho hôm nay</h5>
        <p class="text-theme-muted small mb-3">Tất cả cữ thuốc đã hoàn thành hoặc hồ sơ này chưa có liệu trình đang chạy.</p>
        <button v-if="canEdit" class="btn btn-outline-primary rounded-pill px-4 btn-sm fw-bold" @click="openCreateModal">
          <i class="bi bi-plus-lg me-1"></i> Thêm liệu trình mới
        </button>
      </div>

      <!-- Danh sách cữ thuốc hôm nay dạng bảng/thẻ rõ ràng -->
      <div v-else class="d-flex flex-column gap-3">
        <div 
          v-for="rem in todayReminders" 
          :key="rem.id" 
          class="card bg-theme-card border border-theme shadow-sm rounded-4 overflow-hidden"
          :class="{ 'border-success border-opacity-30': rem.status === 'TAKEN', 'opacity-75': rem.status === 'SKIPPED' }"
        >
          <div class="card-body p-3 p-md-4 d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3">
            <!-- Cột giờ & Thông tin thuốc -->
            <div class="d-flex align-items-center gap-3 min-width-0 flex-grow-1">
              <!-- Giờ uống -->
              <div 
                class="time-badge px-3 py-2 rounded-3 text-center flex-shrink-0"
                :class="rem.status === 'TAKEN' ? 'bg-success bg-opacity-10 text-success' : (rem.status === 'SKIPPED' ? 'bg-secondary bg-opacity-10 text-secondary' : 'bg-primary bg-opacity-10 text-primary')"
              >
                <div class="extra-small fw-semibold text-uppercase opacity-75">Giờ</div>
                <div class="fs-5 fw-bold lh-1">{{ rem.time }}</div>
              </div>

              <!-- Chi tiết -->
              <div class="min-width-0">
                <div class="d-flex align-items-center gap-2 mb-1 flex-wrap">
                  <h6 class="fw-bold text-theme-main mb-0 text-truncate" :title="rem.medicineName">
                    {{ rem.medicineName }}
                  </h6>
                  <span v-if="rem.status === 'TAKEN'" class="badge bg-success bg-opacity-10 text-success border border-success border-opacity-25 rounded-pill extra-small">
                    <i class="bi bi-check-circle-fill me-1"></i>Đã uống
                  </span>
                  <span v-else-if="rem.status === 'SKIPPED'" class="badge bg-secondary bg-opacity-10 text-secondary border border-secondary border-opacity-25 rounded-pill extra-small">
                    Đã bỏ qua
                  </span>
                  <span v-else class="badge bg-warning bg-opacity-10 text-warning border border-warning border-opacity-25 rounded-pill extra-small">
                    Chờ uống
                  </span>
                </div>

                <div class="small text-theme-muted">
                  <span>Liều dùng: <strong class="text-theme-main">{{ rem.dosage }}</strong></span>
                  <span class="mx-2">•</span>
                  <span>Thời điểm: <strong class="text-theme-main">{{ rem.timing }}</strong></span>
                  <span class="mx-2">•</span>
                  <span>Bệnh nhân: <strong class="text-primary">{{ rem.profileName }}</strong></span>
                </div>
              </div>
            </div>

            <!-- Nút hành động -->
            <div class="d-flex align-items-center gap-2 flex-shrink-0 justify-content-end">
              <template v-if="rem.status === 'PENDING'">
                <button 
                  class="btn btn-sm btn-success rounded-pill px-3 fw-bold shadow-sm"
                  @click="updateStatus(rem.id, 'TAKEN')"
                >
                  <i class="bi bi-check-lg me-1"></i>Đã uống
                </button>
                <button 
                  class="btn btn-sm btn-outline-secondary rounded-pill px-3"
                  @click="updateStatus(rem.id, 'SKIPPED')"
                >
                  Bỏ qua
                </button>
              </template>

              <template v-else>
                <button 
                  class="btn btn-sm btn-outline-secondary rounded-pill px-2.5 py-1 extra-small text-muted"
                  @click="updateStatus(rem.id, 'PENDING')"
                  title="Hoàn tác trạng thái"
                >
                  <i class="bi bi-arrow-counterclockwise me-1"></i>Đặt lại
                </button>
              </template>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ===================== TAB 2: DANH SÁCH LIỆU TRÌNH ===================== -->
    <div v-if="activeTab === 'courses'">
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-primary" role="status"></div>
      </div>

      <div v-else-if="courses.length === 0" class="text-center py-5 bg-theme-card rounded-4 shadow-sm border border-dashed border-theme">
        <i class="bi bi-clipboard-plus display-4 text-theme-muted opacity-25"></i>
        <p class="mt-3 text-theme-muted">Chưa có liệu trình nào cho hồ sơ này.</p>
        <button v-if="canEdit" class="btn btn-primary rounded-pill px-4 btn-sm fw-bold" @click="openCreateModal">
          <i class="bi bi-plus-lg me-1"></i> Thêm liệu trình đầu tiên
        </button>
      </div>

      <!-- Lưới thẻ liệu trình tương đồng với Vaccinations.vue & Insurance.vue -->
      <div v-else class="row row-cols-1 row-cols-md-2 row-cols-lg-3 g-4">
        <div class="col" v-for="course in courses" :key="course.id">
          <div class="card bg-theme-card h-100 shadow-sm border border-theme rounded-4 overflow-hidden hover-lift">
            <div class="card-body p-4 d-flex flex-column justify-content-between">
              <div>
                <!-- Đầu thẻ: Icon + Menu 3 chấm -->
                <div class="d-flex justify-content-between align-items-start mb-3">
                  <div class="bg-primary bg-opacity-10 p-3 rounded-3 text-primary">
                    <i class="bi bi-capsule fs-4"></i>
                  </div>
                  <div v-if="canEdit" class="dropdown">
                    <button class="btn btn-light bg-theme-light text-theme-main btn-sm rounded-circle border border-theme" data-bs-toggle="dropdown">
                      <i class="bi bi-three-dots-vertical"></i>
                    </button>
                    <ul class="dropdown-menu dropdown-menu-end shadow-lg border border-theme p-2 bg-theme-card">
                      <li>
                        <button class="dropdown-item rounded-2 text-theme-main small py-1.5" @click="editCourse(course)">
                          <i class="bi bi-pencil me-2 text-primary"></i>Chỉnh sửa
                        </button>
                      </li>
                      <li><hr class="dropdown-divider border-theme my-1"></li>
                      <li>
                        <button class="dropdown-item text-danger rounded-2 small py-1.5" @click="deleteCourse(course.id)">
                          <i class="bi bi-trash me-2"></i>Xóa liệu trình
                        </button>
                      </li>
                    </ul>
                  </div>
                </div>

                <!-- Tên thuốc & Trạng thái -->
                <h5 class="fw-bold text-theme-main mb-1 text-truncate" :title="course.medicineName">
                  {{ course.medicineName }}
                </h5>
                <p class="badge rounded-pill px-3 mb-3 border border-opacity-25" :class="getAutoStatusClass(course)">
                  {{ formatAutoStatus(course) }}
                </p>

                <!-- Thông tin chi tiết -->
                <div class="info-item d-flex align-items-center gap-2 mb-2 text-theme-muted small">
                  <i class="bi bi-droplet flex-shrink-0 text-primary"></i>
                  <span>Liều dùng: <strong class="text-theme-main">{{ course.dosage }} ({{ course.unit }})</strong></span>
                </div>
                <div class="info-item d-flex align-items-center gap-2 mb-2 text-theme-muted small">
                  <i class="bi bi-repeat flex-shrink-0 text-primary"></i>
                  <span>Tần suất: <strong class="text-theme-main">{{ course.frequency }}</strong> ({{ course.timing }})</span>
                </div>
                <div class="info-item d-flex align-items-center gap-2 mb-2 text-theme-muted small">
                  <i class="bi bi-calendar3 flex-shrink-0 text-primary"></i>
                  <span>{{ formatDate(course.startDate) }} - {{ formatDate(course.endDate) }} ({{ course.duration }} ngày)</span>
                </div>

                <!-- Ghi chú nếu có -->
                <div v-if="course.notes" class="mt-2 p-2 rounded-2 bg-theme-light border border-theme small text-theme-muted">
                  <i class="bi bi-info-circle me-1 text-primary"></i>{{ course.notes }}
                </div>
              </div>

              <!-- Thanh tiến độ ở đáy thẻ -->
              <div class="mt-3 pt-3 border-top border-theme">
                <div class="d-flex justify-content-between align-items-center text-theme-muted extra-small mb-1.5">
                  <span>Tiến độ liệu trình</span>
                  <span class="fw-bold text-primary">{{ getCourseProgress(course) }}%</span>
                </div>
                <div class="progress bg-theme-light border border-theme" style="height: 6px;">
                  <div 
                    class="progress-bar rounded-pill" 
                    :class="getCourseProgress(course) === 100 ? 'bg-success' : 'bg-primary'"
                    :style="{ width: getCourseProgress(course) + '%' }"
                  ></div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ===================== MODAL: THÊM / SỬA LIỆU TRÌNH ===================== -->
    <div v-if="showModal" class="modal-overlay d-flex align-items-center justify-content-center p-3" @click.self="showModal = false">
      <div class="bg-theme-card rounded-4 shadow-lg w-100 overflow-hidden border border-theme" style="max-width: 540px;">
        <div class="p-3 border-bottom border-theme d-flex justify-content-between align-items-center bg-theme-light">
          <h5 class="mb-0 fw-bold text-theme-main">
            <i class="bi bi-capsule-pill text-primary me-2"></i>{{ editingId ? 'Cập nhật liệu trình' : 'Thêm liệu trình thuốc mới' }}
          </h5>
          <button class="btn-close" :class="{'btn-close-white': isDarkMode}" @click="showModal = false"></button>
        </div>

        <form @submit.prevent="submitForm" class="p-4 bg-theme-card">
          <!-- Chọn thuốc -->
          <div class="mb-3 position-relative">
            <div class="d-flex justify-content-between align-items-center mb-1">
              <label class="form-label small fw-bold text-theme-main mb-0">Thuốc điều trị <span class="text-danger">*</span></label>
              <button type="button" class="btn btn-link btn-sm text-primary p-0 extra-small text-decoration-none" @click="showAddMedicineModal = true">
                + Thêm thuốc mới vào danh mục
              </button>
            </div>

            <div v-if="!selectedMedicineName" class="input-group">
              <span class="input-group-text bg-theme-light border-theme text-theme-muted"><i class="bi bi-search"></i></span>
              <input 
                type="text" 
                class="form-control bg-theme-light text-theme-main border-theme" 
                v-model="medicineSearchQuery" 
                placeholder="Gõ tên thuốc để tìm kiếm..." 
                @focus="showMedicineSuggestions = true"
              >
            </div>

            <ul 
              v-if="!selectedMedicineName && showMedicineSuggestions && filteredMedicines.length > 0" 
              class="list-group position-absolute w-100 shadow-lg z-3 mt-1 overflow-auto border border-theme" 
              style="max-height: 200px;"
            >
              <li 
                v-for="m in filteredMedicines" 
                :key="m.id" 
                class="list-group-item list-group-item-action bg-theme-card text-theme-main border-theme d-flex justify-content-between align-items-center py-2" 
                @click="selectMedicine(m)"
                style="cursor: pointer;"
              >
                <div>
                  <span class="fw-bold">{{ m.name }}</span>
                  <span class="text-theme-muted ms-2 small">({{ m.unit }})</span>
                </div>
                <i class="bi bi-plus-circle text-primary"></i>
              </li>
            </ul>

            <div v-if="selectedMedicineName" class="p-2.5 bg-primary bg-opacity-10 border border-primary border-opacity-25 rounded-3 d-flex justify-content-between align-items-center">
              <div>
                <span class="small text-muted me-1">Đã chọn:</span>
                <strong class="text-primary">{{ selectedMedicineName }}</strong>
              </div>
              <button type="button" class="btn btn-sm btn-outline-danger rounded-pill extra-small py-0.5 px-2" @click="clearSelectedMedicine">
                Chọn lại
              </button>
            </div>
          </div>

          <!-- Liều lượng & Tần suất -->
          <div class="row g-3 mb-3">
            <div class="col-12 col-md-6">
              <label class="form-label small fw-bold text-theme-main">Liều dùng mỗi lần <span class="text-danger">*</span></label>
              <input type="text" class="form-control bg-theme-light text-theme-main border-theme" v-model="form.dosage" placeholder="VD: 1 viên, 5ml..." required>
            </div>
            <div class="col-12 col-md-6">
              <label class="form-label small fw-bold text-theme-main">Tần suất uống <span class="text-danger">*</span></label>
              <select class="form-select bg-theme-light text-theme-main border-theme" v-model="form.frequency" @change="initReminderTimes" required>
                <option value="1 lần/ngày">1 lần / ngày</option>
                <option value="2 lần/ngày">2 lần / ngày</option>
                <option value="3 lần/ngày">3 lần / ngày</option>
              </select>
            </div>
          </div>

          <!-- Khung giờ nhắc nhở -->
          <div class="mb-3">
            <label class="form-label small fw-bold text-theme-main mb-1">Giờ nhắc nhở trong ngày <span class="text-danger">*</span></label>
            <div class="row g-2">
              <div v-for="(time, index) in form.reminderTimes" :key="index" class="col-12 col-sm-4">
                <div class="input-group input-group-sm">
                  <span class="input-group-text bg-theme-light border-theme text-muted extra-small">Cữ {{ index + 1 }}</span>
                  <input type="time" class="form-control bg-theme-light text-theme-main border-theme" v-model="form.reminderTimes[index]" required>
                </div>
              </div>
            </div>
          </div>

          <!-- Thời điểm uống -->
          <div class="mb-3">
            <label class="form-label small fw-bold text-theme-main mb-1">Thời điểm uống</label>
            <div class="row g-2">
              <div class="col-6 col-sm-3" v-for="t in timingOptions" :key="t.value">
                <input class="btn-check" type="radio" :value="t.value" :id="'opt_' + t.value" v-model="form.timing">
                <label class="btn btn-outline-secondary btn-sm w-100 text-truncate" :for="'opt_' + t.value">
                  {{ t.label }}
                </label>
              </div>
            </div>
          </div>

          <!-- Ngày bắt đầu & Số ngày -->
          <div class="row g-3 mb-3">
            <div class="col-12 col-md-6">
              <label class="form-label small fw-bold text-theme-main">Ngày bắt đầu <span class="text-danger">*</span></label>
              <SmartDateInput class="form-control bg-theme-light text-theme-main border-theme" v-model="form.startDate" required />
            </div>
            <div class="col-12 col-md-6">
              <label class="form-label small fw-bold text-theme-main">Thời gian (ngày) <span class="text-danger">*</span></label>
              <input type="number" class="form-control bg-theme-light text-theme-main border-theme" v-model="form.durationInDays" min="1" max="365" required>
            </div>
          </div>

          <!-- Ghi chú -->
          <div class="mb-4">
            <label class="form-label small fw-bold text-theme-main">Ghi chú thêm</label>
            <textarea class="form-control bg-theme-light text-theme-main border-theme" v-model="form.notes" rows="2" placeholder="Uống nhiều nước ấm, tránh uống cùng sữa..."></textarea>
          </div>

          <!-- Submit -->
          <div class="d-grid">
            <button type="submit" class="btn btn-primary rounded-pill py-2 fw-bold" :disabled="submitting">
              <span v-if="submitting" class="spinner-border spinner-border-sm me-2"></span>
              {{ editingId ? 'Lưu thay đổi' : 'Lưu liệu trình' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- ===================== MODAL: YÊU CẦU THÊM THUỐC MỚI ===================== -->
    <div v-if="showAddMedicineModal" class="modal-overlay d-flex align-items-center justify-content-center p-3" @click.self="showAddMedicineModal = false" style="z-index: 2150;">
      <div class="bg-theme-card rounded-4 shadow-lg w-100 overflow-hidden border border-theme" style="max-width: 440px;">
        <div class="p-3 border-bottom border-theme d-flex justify-content-between align-items-center bg-theme-light">
          <h6 class="mb-0 fw-bold text-theme-main">Yêu cầu thêm thuốc mới</h6>
          <button class="btn-close" :class="{'btn-close-white': isDarkMode}" @click="showAddMedicineModal = false"></button>
        </div>
        <form @submit.prevent="submitMedicineRequest" class="p-4 bg-theme-card">
          <div class="mb-3">
            <label class="form-label small fw-bold text-theme-main">Tên thuốc <span class="text-danger">*</span></label>
            <input type="text" class="form-control bg-theme-light text-theme-main border-theme" v-model="medicineRequest.name" placeholder="VD: Panadol, Paracetamol..." required>
          </div>
          <div class="mb-3">
            <label class="form-label small fw-bold text-theme-main">Đơn vị tính <span class="text-danger">*</span></label>
            <input type="text" class="form-control bg-theme-light text-theme-main border-theme" v-model="medicineRequest.unit" placeholder="VD: Viên, Gói, Chai..." required>
          </div>
          <div class="mb-3">
            <label class="form-label small fw-bold text-theme-main">Mô tả / Công dụng</label>
            <textarea class="form-control bg-theme-light text-theme-main border-theme" v-model="medicineRequest.description" rows="2" placeholder="Thuốc giảm đau, hạ sốt..."></textarea>
          </div>
          <button type="submit" class="btn btn-primary w-100 rounded-pill py-2 fw-bold" :disabled="requesting">
            <span v-if="requesting" class="spinner-border spinner-border-sm me-2"></span> Gửi yêu cầu
          </button>
        </form>
      </div>
    </div>

    <!-- ===================== MODAL: KẾT NỐI TELEGRAM ===================== -->
    <div v-if="showTelegramModal" class="modal-overlay d-flex align-items-center justify-content-center p-3" @click.self="showTelegramModal = false" style="z-index: 2200;">
      <div class="bg-theme-card rounded-4 shadow-lg w-100 overflow-hidden border border-theme" style="max-width: 480px;">
        <div class="p-3 border-bottom border-theme d-flex justify-content-between align-items-center bg-theme-light">
          <div class="d-flex align-items-center gap-2">
            <i class="bi bi-telegram fs-5 text-primary"></i>
            <h6 class="mb-0 fw-bold text-theme-main">Kết nối Telegram nhận thông báo</h6>
          </div>
          <button class="btn-close" :class="{'btn-close-white': isDarkMode}" @click="showTelegramModal = false"></button>
        </div>

        <div class="p-4 bg-theme-card">
          <div class="p-3 rounded-3 bg-theme-light border border-theme mb-3">
            <div class="d-flex align-items-start gap-2">
              <span class="badge bg-primary rounded-circle px-2 py-1">1</span>
              <div>
                <div class="fw-bold small text-theme-main">Mở Bot Telegram & bấm nút START</div>
                <p class="extra-small text-theme-muted mb-2">Bấm nút bên dưới để mở ứng dụng Telegram, sau đó bấm <strong>START</strong> để kích hoạt bot.</p>
                <a :href="telegramStatus.botInviteLink" target="_blank" class="btn btn-sm btn-primary rounded-pill px-3 fw-bold">
                  <i class="bi bi-box-arrow-up-right me-1"></i> Mở Telegram ngay
                </a>
              </div>
            </div>
          </div>

          <div class="p-3 rounded-3 bg-theme-light border border-theme mb-3">
            <div class="d-flex align-items-start gap-2">
              <span class="badge bg-success rounded-circle px-2 py-1">2</span>
              <div class="flex-grow-1">
                <div class="fw-bold small text-theme-main">Xác nhận hoàn tất</div>
                <p class="extra-small text-theme-muted mb-2">Sau khi đã bấm nút START trên Telegram, bấm nút bên dưới để hệ thống nhận diện:</p>
                <button class="btn btn-sm btn-success rounded-pill px-3 py-1.5 fw-bold w-100" @click="connectTelegramAuto" :disabled="connectingTelegram">
                  <span v-if="connectingTelegram" class="spinner-border spinner-border-sm me-1"></span>
                  <i v-else class="bi bi-check2-circle me-1"></i> Tôi đã bấm START rồi (Hoàn tất)
                </button>
              </div>
            </div>
          </div>

          <div class="border-top border-theme pt-3">
            <a href="#" class="extra-small text-theme-muted text-decoration-none d-flex align-items-center justify-content-between" @click.prevent="showManualInput = !showManualInput">
              <span>Hoặc tự nhập mã Chat ID thủ công</span>
              <i class="bi" :class="showManualInput ? 'bi-chevron-up' : 'bi-chevron-down'"></i>
            </a>
            <div v-if="showManualInput" class="mt-2">
              <div class="input-group input-group-sm">
                <input type="text" class="form-control bg-theme-light text-theme-main border-theme" v-model="manualChatId" placeholder="VD: 7942650831">
                <button class="btn btn-outline-primary fw-bold" @click="connectTelegramManual" :disabled="!manualChatId.trim() || connectingTelegram">
                  Lưu ID
                </button>
              </div>
            </div>
          </div>
        </div>
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

// Telegram State
const telegramStatus = ref({
  connected: false,
  chatId: null,
  botUsername: 'Suc_khoe_dien_tu_Bot',
  botInviteLink: 'https://t.me/Suc_khoe_dien_tu_Bot'
});
const showTelegramModal = ref(false);
const showManualInput = ref(false);
const manualChatId = ref('');
const connectingTelegram = ref(false);
const testingTelegram = ref(false);

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
  { value: 'Trước ăn', label: 'Trước ăn' },
  { value: 'Sau ăn', label: 'Sau ăn' },
  { value: 'Trong khi ăn', label: 'Trong bữa' },
  { value: 'Trước khi đi ngủ', label: 'Trước ngủ' }
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
  selectedMedicineName.value = '';
  medicineSearchQuery.value = '';
  form.dosage = '';
  form.frequency = '2 lần/ngày';
  form.timing = 'Sau ăn';
  form.startDate = new Date().toISOString().split('T')[0];
  form.durationInDays = 7;
  form.reminderTimes = ['08:00', '20:00'];
  form.notes = '';
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
  form.notes = course.notes || '';
  initReminderTimes();
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

  if (now < start) return 'bg-info bg-opacity-10 text-info border-info';
  if (now > end) return 'bg-secondary bg-opacity-10 text-secondary border-secondary';
  return 'bg-success bg-opacity-10 text-success border-success';
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

const getCourseProgress = (course) => {
  const start = new Date(course.startDate);
  start.setHours(0, 0, 0, 0);
  const end = new Date(course.endDate);
  end.setHours(23, 59, 59, 999);
  const now = new Date();

  if (now < start) return 0;
  if (now > end) return 100;

  const total = end.getTime() - start.getTime();
  const current = now.getTime() - start.getTime();
  return Math.round((current / total) * 100);
};

// Telegram Methods
const fetchTelegramStatus = async () => {
  try {
    const res = await axios.get('/api/telegram/status');
    if (res.data.success) {
      telegramStatus.value = res.data;
    }
  } catch (err) {
    console.error('Lỗi lấy trạng thái Telegram', err);
  }
};

const openTelegramModal = () => {
  showTelegramModal.value = true;
  manualChatId.value = '';
  showManualInput.value = false;
};

const connectTelegramAuto = async () => {
  connectingTelegram.value = true;
  try {
    const res = await axios.post('/api/telegram/connect', {});
    if (res.data.success) {
      toast.success(res.data.message || 'Kết nối Telegram thành công!');
      showTelegramModal.value = false;
      await fetchTelegramStatus();
    }
  } catch (err) {
    toast.error(err.response?.data?.message || 'Chưa tìm thấy tin nhắn START từ bạn trên Telegram.');
  } finally {
    connectingTelegram.value = false;
  }
};

const connectTelegramManual = async () => {
  if (!manualChatId.value.trim()) return;
  connectingTelegram.value = true;
  try {
    const res = await axios.post('/api/telegram/connect', { chatId: manualChatId.value.trim() });
    if (res.data.success) {
      toast.success(res.data.message || 'Kết nối Telegram thành công!');
      showTelegramModal.value = false;
      manualChatId.value = '';
      await fetchTelegramStatus();
    }
  } catch (err) {
    toast.error(err.response?.data?.message || 'Lỗi khi lưu Telegram Chat ID.');
  } finally {
    connectingTelegram.value = false;
  }
};

const disconnectTelegram = async () => {
  if (!confirm('Bạn có chắc muốn hủy nhận thông báo qua Telegram không?')) return;
  try {
    const res = await axios.post('/api/telegram/disconnect');
    if (res.data.success) {
      toast.success('Đã hủy liên kết Telegram.');
      await fetchTelegramStatus();
    }
  } catch (err) {
    toast.error('Lỗi khi hủy liên kết.');
  }
};

const sendTelegramTest = async () => {
  testingTelegram.value = true;
  try {
    const res = await axios.post('/api/telegram/test');
    if (res.data.success) {
      toast.success(res.data.message || 'Đã gửi thông báo kiểm tra đến Telegram!');
    }
  } catch (err) {
    toast.error(err.response?.data?.message || 'Lỗi khi gửi thông báo test.');
  } finally {
    testingTelegram.value = false;
  }
};

onMounted(() => {
  fetchData();
  fetchTelegramStatus();
});
</script>

<style scoped>
.extra-small {
  font-size: 0.75rem;
}

.hover-lift {
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.hover-lift:hover {
  transform: translateY(-2px);
  box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.08) !important;
}

.time-badge {
  min-width: 76px;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  z-index: 2100;
}

.page-toolbar-actions {
  width: min(100%, 640px);
}
.profile-selector {
  width: 100%;
}
.profile-select {
  min-width: 0;
  width: 100%;
}

@media (max-width: 767.98px) {
  .page-toolbar-actions {
    width: 100%;
  }
}
</style>
