<template>
  <div class="container-fluid p-4 animate-fade-in d-flex flex-column h-100">
    
    <div class="card border-0 rounded-4 shadow-sm overflow-hidden mb-4 position-relative hover-lift flex-shrink-0">
      <div class="banner-overlay position-absolute top-0 start-0 w-100 h-100" style="background: linear-gradient(to right, rgba(0,0,0,0.8), rgba(0,0,0,0.2)); z-index: 1;"></div>
      <img src="/images/banner1.jpg" alt="Admin Banner" class="w-100 object-fit-cover position-relative z-0" style="height: 140px;">
      <div class="position-absolute bottom-0 start-0 p-4 text-white z-2 w-100 d-flex justify-content-between align-items-end">
        <div>
          <h2 class="fw-bold mb-1 text-white"><i class="bi bi-shield-lock-fill me-2"></i>Trung tâm Quản trị</h2>
          <p class="mb-0 opacity-75 text-white">Quản lý toàn bộ hệ thống HealthRecord</p>
        </div>
        <div class="bg-white bg-opacity-25 px-3 py-2 rounded-3 backdrop-blur d-none d-md-block" style="backdrop-filter: blur(5px);">
          <span class="small fw-bold text-white"><i class="bi bi-clock-history me-1"></i> Quản trị viên</span>
        </div>
      </div>
    </div>

    
    <div class="row g-3 mb-4 flex-shrink-0">
      <div class="col-xl-3 col-sm-6">
        <div class="card bg-theme-card border border-theme shadow-sm rounded-4 p-3 h-100 hover-lift">
          <div class="d-flex align-items-center">
            <div class="bg-primary bg-opacity-10 p-3 rounded-4 me-3 text-primary">
              <i class="bi bi-chat-square-text fs-3"></i>
            </div>
            <div>
              <div class="extra-small text-theme-muted text-uppercase fw-bold tracking-wider">Bài chờ duyệt</div>
              <div class="h3 fw-bold mb-0 text-theme-main">{{ pendingPosts.length }}</div>
            </div>
          </div>
        </div>
      </div>
      <div class="col-xl-3 col-sm-6">
        <div class="card bg-theme-card border border-theme shadow-sm rounded-4 p-3 h-100 hover-lift">
          <div class="d-flex align-items-center">
            <div class="bg-warning bg-opacity-10 p-3 rounded-4 me-3 text-warning">
              <i class="bi bi-capsule fs-3"></i>
            </div>
            <div>
              <div class="extra-small text-theme-muted text-uppercase fw-bold tracking-wider">Thuốc chờ duyệt</div>
              <div class="h3 fw-bold mb-0 text-theme-main">{{ pendingMedicines.length }}</div>
            </div>
          </div>
        </div>
      </div>
      <div class="col-xl-3 col-sm-6">
        <div class="card bg-theme-card border border-theme shadow-sm rounded-4 p-3 h-100 hover-lift">
          <div class="d-flex align-items-center">
            <div class="bg-danger bg-opacity-10 p-3 rounded-4 me-3 text-danger">
              <i class="bi bi-exclamation-octagon fs-3"></i>
            </div>
            <div>
              <div class="extra-small text-theme-muted text-uppercase fw-bold tracking-wider">Báo cáo vi phạm</div>
              <div class="h3 fw-bold mb-0 text-theme-main">{{ reports.length }}</div>
            </div>
          </div>
        </div>
      </div>
      <div class="col-xl-3 col-sm-6">
        <div class="card bg-theme-card border border-theme shadow-sm rounded-4 p-3 h-100 hover-lift">
          <div class="d-flex align-items-center">
            <div class="bg-success bg-opacity-10 p-3 rounded-4 me-3 text-success">
              <i class="bi bi-people fs-3"></i>
            </div>
            <div>
              <div class="extra-small text-theme-muted text-uppercase fw-bold tracking-wider">Người dùng</div>
              <div class="h3 fw-bold mb-0 text-theme-main">{{ adminStats.totalUsers }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="card bg-theme-card border border-theme shadow-sm rounded-4 p-3 mb-4 flex-shrink-0">
      <div class="d-flex justify-content-between align-items-center mb-2">
        <h6 class="fw-bold mb-0 text-theme-main"><i class="bi bi-bar-chart-line me-2 text-primary"></i>Thống kê hệ thống</h6>
        <span class="small text-theme-muted">Cập nhật theo dữ liệu hiện tại</span>
      </div>
      <div style="height: 220px;">
        <Bar :data="chartData" :options="chartOptions" />
      </div>
    </div>

    
    <div class="bg-theme-card rounded-4 shadow-sm border border-theme overflow-hidden flex-grow-1 d-flex flex-column" style="min-height: 500px;">
      
      <div class="nav-scroller bg-theme-light border-bottom border-theme px-2 pt-2 flex-shrink-0">
        <nav class="nav nav-underline gap-2" aria-label="Secondary navigation">
          <button @click="activeTab = 'posts'" :class="['nav-link border-0 bg-transparent fw-bold py-3 px-4 transition-all rounded-top-3', isCommunityTab ? 'active text-primary bg-theme-card shadow-sm' : 'text-theme-muted hover-bg']">
            <i class="bi bi-people-fill me-2"></i>CỘNG ĐỒNG
          </button>
          <button @click="activeTab = 'medicines'" :class="['nav-link border-0 bg-transparent fw-bold py-3 px-4 transition-all rounded-top-3', isMedicalTab ? 'active text-primary bg-theme-card shadow-sm' : 'text-theme-muted hover-bg']">
            <i class="bi bi-heart-pulse-fill me-2"></i>DANH MỤC Y TẾ
          </button>
        </nav>
      </div>

      <div class="animate-slide-up position-relative p-0 flex-grow-1 overflow-y-auto custom-scrollbar bg-theme-card">
        
        <div v-if="isCommunityTab" class="h-100 d-flex flex-column">
          <div class="d-flex bg-theme-card border-bottom border-theme px-4 py-2 gap-4 sticky-top z-1">
            <button @click="subTab = 'pending_posts'" :class="['btn btn-sm border-0 fw-bold rounded-pill transition-all', subTab === 'pending_posts' ? 'btn-primary text-white shadow-sm' : 'btn-light bg-theme-light text-theme-muted hover-bg']">
              Chờ duyệt ({{ pendingPosts.length }})
            </button>
            <button @click="subTab = 'all_posts'" :class="['btn btn-sm border-0 fw-bold rounded-pill transition-all', subTab === 'all_posts' ? 'btn-primary text-white shadow-sm' : 'btn-light bg-theme-light text-theme-muted hover-bg']">
              Tất cả bài viết
            </button>
            <button @click="subTab = 'reports'" :class="['btn btn-sm border-0 fw-bold rounded-pill transition-all', subTab === 'reports' ? 'btn-danger text-white shadow-sm' : 'btn-light bg-theme-light text-theme-muted hover-bg']">
              Báo cáo vi phạm
            </button>
          </div>

          <div class="p-4 flex-grow-1 bg-theme-card">
            
            <transition name="fade" mode="out-in">
              <div v-if="subTab === 'pending_posts'" key="pending_posts">
                <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-4 gap-3">
                  <div class="input-group shadow-sm rounded-pill overflow-hidden bg-theme-light border border-theme" style="max-width: 420px;">
                    <span class="input-group-text bg-transparent border-0 ps-4 pe-2"><i class="bi bi-search text-theme-muted"></i></span>
                    <input type="text" class="form-control border-0 ps-0 shadow-none py-2 text-theme-main bg-transparent" v-model="pendingPostQuery" placeholder="Tìm trong bài chờ duyệt...">
                  </div>
                  <h6 class="fw-bold mb-0 text-theme-muted">
                    <span class="text-primary">{{ filteredPendingPosts.length }}</span> bài đang chờ duyệt
                  </h6>
                </div>

                <div v-if="filteredPendingPosts.length === 0" class="text-center py-5 text-theme-muted bg-theme-light rounded-4 border border-dashed border-theme">
                  <i class="bi bi-check2-all display-4 text-success opacity-25"></i>
                  <p class="mt-3 fw-medium">Không có bài viết nào khớp điều kiện hiện tại.</p>
                </div>
                <div v-else class="row g-4">
                  <div v-for="post in paginatedPendingPosts" :key="post.id" class="col-xxl-4 col-md-6">
                    <div class="card h-100 rounded-4 border border-theme shadow-sm post-card-admin overflow-hidden bg-theme-card">
                      <div class="card-body p-4">
                        <div class="d-flex justify-content-between mb-3 align-items-center">
                          <div class="d-flex align-items-center gap-2 min-width-0 flex-grow-1">
                            <img :src="`https://ui-avatars.com/api/?name=${post.author}&background=random`" class="rounded-circle shadow-sm border border-theme flex-shrink-0" width="36" height="36">
                            <div class="min-width-0">
                              <div class="fw-bold small mb-0 lh-1 text-theme-main text-truncate" style="max-width: 150px;">{{ post.author }}</div>
                              <div class="extra-small text-theme-muted">{{ formatDate(post.date) }}</div>
                            </div>
                          </div>
                          <span class="badge bg-primary bg-opacity-10 text-primary border border-primary border-opacity-25 rounded-pill px-2 flex-shrink-0">{{ post.category }}</span>
                        </div>
                        <h6 class="fw-bold mb-2 text-theme-main text-break-word">{{ post.title }}</h6>
                        <p class="small text-theme-muted line-clamp-3 mb-3 text-break-word">{{ post.content }}</p>
                        <div v-if="post.media" class="rounded-3 overflow-hidden bg-dark position-relative shadow-sm mb-3 hover-lift" style="height: 160px; cursor: pointer;">
                          <a :href="mediaUrl(post.media)" target="_blank" class="d-block h-100 w-100">
                            
                            <img v-if="!isVideoMedia(post.media)" 
                                 :src="mediaUrl(post.media)" 
                                 class="w-100 h-100 object-fit-cover opacity-75 hover-opacity-100 transition-all">
                            
                            <div v-else class="w-100 h-100 d-flex align-items-center justify-content-center text-white flex-column bg-black bg-opacity-50">
                              <i class="bi bi-play-circle-fill fs-1 text-white opacity-75"></i>
                              <span class="small fw-bold mt-2">Xem Video</span>
                            </div>
                          </a>
                        </div>
                      </div>
                      <div class="card-footer bg-theme-light border-top border-theme d-flex gap-2 p-3">
                        <button class="btn btn-outline-primary btn-sm rounded-pill fw-bold transition-all" @click="openPostDetail(post)">
                          Chi tiết
                        </button>
                        <button class="btn btn-outline-danger btn-sm flex-grow-1 rounded-pill fw-bold transition-all" @click="handleDeletePost(post.id)">Từ chối</button>
                        <button class="btn btn-primary btn-sm flex-grow-1 rounded-pill fw-bold transition-all shadow-sm" @click="handleApprove(post.id)">Duyệt ngay</button>
                      </div>
                    </div>
                  </div>
                </div>

                <div v-if="pendingPostsTotalPages > 1" class="d-flex justify-content-end mt-4">
                  <div class="btn-group">
                    <button class="btn btn-sm btn-light bg-theme-light border border-theme" @click="pendingPostPage = Math.max(1, pendingPostPage - 1)" :disabled="pendingPostPage === 1">
                      <i class="bi bi-chevron-left"></i>
                    </button>
                    <button v-for="p in pageRange(pendingPostPage, pendingPostsTotalPages)" :key="`pp-${p}`" class="btn btn-sm border border-theme" :class="p === pendingPostPage ? 'btn-primary text-white' : 'btn-light bg-theme-light text-theme-main'" @click="pendingPostPage = p">
                      {{ p }}
                    </button>
                    <button class="btn btn-sm btn-light bg-theme-light border border-theme" @click="pendingPostPage = Math.min(pendingPostsTotalPages, pendingPostPage + 1)" :disabled="pendingPostPage === pendingPostsTotalPages">
                      <i class="bi bi-chevron-right"></i>
                    </button>
                  </div>
                </div>
              </div>

              
              <div v-else-if="subTab === 'all_posts'" key="all_posts">
                <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-4 gap-3">
                  <div class="input-group shadow-sm rounded-pill overflow-hidden bg-theme-light border border-theme" style="max-width: 420px;">
                    <span class="input-group-text bg-transparent border-0 ps-4 pe-2"><i class="bi bi-search text-theme-muted"></i></span>
                    <input type="text" class="form-control border-0 ps-0 shadow-none py-2 text-theme-main bg-transparent" v-model="allPostQuery" placeholder="Tìm trong tất cả bài viết...">
                  </div>
                  <h6 class="fw-bold mb-0 text-theme-muted">
                    <span class="text-primary">{{ filteredAllPosts.length }}</span> bài viết
                  </h6>
                </div>
                <div class="table-responsive rounded-4 shadow-sm border border-theme">
                  <table class="table table-hover align-middle mb-0">
                    <thead>
                      <tr class="table-theme-header">
                        <th class="ps-4 py-3 text-theme-muted fw-bold extra-small text-uppercase tracking-wider">ID</th>
                        <th class="py-3 text-theme-muted fw-bold extra-small text-uppercase tracking-wider">Media</th>
                        <th class="py-3 text-theme-muted fw-bold extra-small text-uppercase tracking-wider">Tiêu đề</th>
                        <th class="py-3 text-theme-muted fw-bold extra-small text-uppercase tracking-wider">Tác giả</th>
                        <th class="py-3 text-theme-muted fw-bold extra-small text-uppercase tracking-wider">Ngày đăng</th>
                        <th class="py-3 text-theme-muted fw-bold extra-small text-uppercase tracking-wider">Danh mục</th>
                        <th class="text-end pe-4 py-3 text-theme-muted fw-bold extra-small text-uppercase tracking-wider">Thao tác</th>
                      </tr>
                    </thead>
                    <tbody class="bg-theme-card">
                      <tr v-for="post in paginatedAllPosts" :key="post.id" class="transition-all hover-bg">
                        <td class="ps-4 text-theme-muted small fw-medium">#{{ post.id }}</td>
                        <td class="py-2">
                          <div v-if="post.media" class="rounded border border-theme overflow-hidden bg-dark shadow-sm hover-lift" style="width: 50px; height: 40px; cursor: pointer;">
                            <a :href="mediaUrl(post.media)" target="_blank" class="d-block h-100 w-100">
                              <img v-if="!isVideoMedia(post.media)" 
                                   :src="mediaUrl(post.media)" 
                                   class="w-100 h-100 object-fit-cover">
                              <div v-else class="w-100 h-100 d-flex align-items-center justify-content-center">
                                <i class="bi bi-play-circle-fill text-white" style="font-size: 0.8rem;"></i>
                              </div>
                            </a>
                          </div>
                          <span v-else class="text-muted extra-small">Không</span>
                        </td>
                        <td class="fw-bold small text-truncate text-theme-main" style="max-width: 200px;" :title="post.title">{{ post.title }}</td>
                        <td>
                          <div class="d-flex align-items-center gap-2">
                            <img :src="`https://ui-avatars.com/api/?name=${post.author}&background=random`" class="rounded-circle border border-theme flex-shrink-0" width="24" height="24">
                            <span class="small fw-medium text-theme-main text-truncate" style="max-width: 120px;" :title="post.author">{{ post.author }}</span>
                          </div>
                        </td>
                        <td class="small text-theme-muted">{{ formatDate(post.date) }}</td>
                        <td><span class="badge bg-theme-light text-theme-main border border-theme px-2 py-1">{{ post.category }}</span></td>
                        <td class="text-end pe-4">
                          <div class="d-flex justify-content-end gap-2">
                            <button class="btn btn-sm btn-light bg-theme-light text-primary rounded-circle shadow-sm border border-theme" @click="openPostDetail(post)" title="Xem chi tiết">
                              <i class="bi bi-eye-fill"></i>
                            </button>
                            <button class="btn btn-sm btn-light bg-theme-light text-danger rounded-circle shadow-sm border border-theme" @click="handleDeletePost(post.id)" title="Xóa bài viết">
                              <i class="bi bi-trash3-fill"></i>
                            </button>
                          </div>
                        </td>
                      </tr>
                      <tr v-if="filteredAllPosts.length === 0">
                        <td colspan="7" class="text-center py-5 text-theme-muted bg-theme-light border-0">
                          <i class="bi bi-search display-6 d-block mb-3 opacity-25"></i>
                          Không tìm thấy bài viết phù hợp.
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
                <div v-if="allPostsTotalPages > 1" class="d-flex justify-content-end mt-4">
                  <div class="btn-group">
                    <button class="btn btn-sm btn-light bg-theme-light border border-theme" @click="allPostPage = Math.max(1, allPostPage - 1)" :disabled="allPostPage === 1">
                      <i class="bi bi-chevron-left"></i>
                    </button>
                    <button v-for="p in pageRange(allPostPage, allPostsTotalPages)" :key="`ap-${p}`" class="btn btn-sm border border-theme" :class="p === allPostPage ? 'btn-primary text-white' : 'btn-light bg-theme-light text-theme-main'" @click="allPostPage = p">
                      {{ p }}
                    </button>
                    <button class="btn btn-sm btn-light bg-theme-light border border-theme" @click="allPostPage = Math.min(allPostsTotalPages, allPostPage + 1)" :disabled="allPostPage === allPostsTotalPages">
                      <i class="bi bi-chevron-right"></i>
                    </button>
                  </div>
                </div>
              </div>

              
              <div v-else-if="subTab === 'reports'" key="reports">
                <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-4 gap-3">
                  <div class="input-group shadow-sm rounded-pill overflow-hidden bg-theme-light border border-theme" style="max-width: 420px;">
                    <span class="input-group-text bg-transparent border-0 ps-4 pe-2"><i class="bi bi-search text-theme-muted"></i></span>
                    <input type="text" class="form-control border-0 ps-0 shadow-none py-2 text-theme-main bg-transparent" v-model="reportQuery" placeholder="Tìm báo cáo theo người, lý do, bài viết...">
                  </div>
                  <h6 class="fw-bold mb-0 text-theme-muted">
                    <span class="text-danger">{{ filteredReports.length }}</span> báo cáo đang mở
                  </h6>
                </div>

                <div v-if="filteredReports.length === 0" class="text-center py-5 text-theme-muted bg-theme-light rounded-4 border border-dashed border-theme">
                  <i class="bi bi-shield-check display-4 text-success opacity-25"></i>
                  <p class="mt-3 fw-medium">Không có báo cáo nào khớp điều kiện hiện tại.</p>
                </div>
                <div v-else class="table-responsive rounded-4 shadow-sm border border-theme">
                  <table class="table table-hover align-middle mb-0">
                    <thead>
                      <tr class="table-theme-header">
                        <th class="ps-4 py-3 text-theme-muted fw-bold extra-small text-uppercase tracking-wider">Người báo cáo</th>
                        <th class="py-3 text-theme-muted fw-bold extra-small text-uppercase tracking-wider">Media</th>
                        <th class="py-3 text-theme-muted fw-bold extra-small text-uppercase tracking-wider">Bài viết bị tố cáo</th>
                        <th class="py-3 text-theme-muted fw-bold extra-small text-uppercase tracking-wider">Lý do</th>
                        <th class="text-end pe-4 py-3 text-theme-muted fw-bold extra-small text-uppercase tracking-wider">Thao tác</th>
                      </tr>
                    </thead>
                    <tbody class="bg-theme-card">
                      <tr v-for="r in paginatedReports" :key="r.id" class="transition-all hover-bg">
                        <td class="ps-4 fw-bold small text-theme-main">
                          <div class="d-flex align-items-center gap-2">
                            <i class="bi bi-person-badge text-theme-muted"></i> {{ r.reporterName }}
                          </div>
                        </td>
                        <td class="py-2">
                          <div v-if="r.postMedia" class="rounded border border-theme overflow-hidden bg-dark shadow-sm hover-lift" style="width: 50px; height: 40px; cursor: pointer;">
                            <a :href="mediaUrl(r.postMedia)" target="_blank" class="d-block h-100 w-100">
                              <img v-if="!isVideoMedia(r.postMedia)" 
                                   :src="mediaUrl(r.postMedia)" 
                                   class="w-100 h-100 object-fit-cover">
                              <div v-else class="w-100 h-100 d-flex align-items-center justify-content-center">
                                <i class="bi bi-play-circle-fill text-white" style="font-size: 0.8rem;"></i>
                              </div>
                            </a>
                          </div>
                          <span v-else class="text-muted extra-small">Không</span>
                        </td>
                        <td>
                          <div class="small fw-bold text-primary mb-1">#{{ r.postId }}</div>
                          <div class="extra-small text-theme-muted text-truncate" style="max-width: 250px;">{{ r.postTitle }}</div>
                        </td>
                        <td><span class="badge bg-danger text-white border-0 px-2 py-1 shadow-sm"><i class="bi bi-flag-fill me-1"></i> {{ r.reason }}</span></td>
                        <td class="text-end pe-4">
                          <div class="d-flex justify-content-end gap-2">
                            <button class="btn btn-sm btn-light bg-theme-light text-primary fw-bold rounded-pill px-3 shadow-sm border border-theme" @click="openReportDetail(r)">Chi tiết</button>
                            <button class="btn btn-sm btn-light bg-theme-light text-primary fw-bold rounded-pill px-3 shadow-sm border border-theme" @click="handleResolveReport(r.id)">Đã xử lý</button>
                            <button class="btn btn-sm btn-danger fw-bold rounded-pill px-3 shadow-sm hover-lift" @click="handleDeletePost(r.postId)"><i class="bi bi-trash3-fill me-1"></i> Xóa bài</button>
                          </div>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
                <div v-if="reportsTotalPages > 1" class="d-flex justify-content-end mt-4">
                  <div class="btn-group">
                    <button class="btn btn-sm btn-light bg-theme-light border border-theme" @click="reportPage = Math.max(1, reportPage - 1)" :disabled="reportPage === 1">
                      <i class="bi bi-chevron-left"></i>
                    </button>
                    <button v-for="p in pageRange(reportPage, reportsTotalPages)" :key="`rp-${p}`" class="btn btn-sm border border-theme" :class="p === reportPage ? 'btn-primary text-white' : 'btn-light bg-theme-light text-theme-main'" @click="reportPage = p">
                      {{ p }}
                    </button>
                    <button class="btn btn-sm btn-light bg-theme-light border border-theme" @click="reportPage = Math.min(reportsTotalPages, reportPage + 1)" :disabled="reportPage === reportsTotalPages">
                      <i class="bi bi-chevron-right"></i>
                    </button>
                  </div>
                </div>
              </div>
            </transition>
          </div>
        </div>

        
        <div v-if="isMedicalTab" class="h-100 d-flex flex-column bg-theme-card">
          <div class="d-flex bg-theme-card border-bottom border-theme px-4 py-2 gap-4 sticky-top z-1">
            <button @click="subTabMed = 'pending_meds'" :class="['btn btn-sm border-0 fw-bold rounded-pill transition-all', subTabMed === 'pending_meds' ? 'btn-primary text-white shadow-sm' : 'btn-light bg-theme-light text-theme-muted hover-bg']">
              Yêu cầu thêm thuốc ({{ pendingMedicines.length }})
            </button>
            <button @click="subTabMed = 'all_meds'" :class="['btn btn-sm border-0 fw-bold rounded-pill transition-all', subTabMed === 'all_meds' ? 'btn-primary text-white shadow-sm' : 'btn-light bg-theme-light text-theme-muted hover-bg']">
              Danh mục thuốc hệ thống
            </button>
          </div>

          <div class="p-4 flex-grow-1 bg-theme-card">
            <transition name="fade" mode="out-in">
              
              <div v-if="subTabMed === 'pending_meds'" key="pending_meds">
                <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-4 gap-3">
                  <div class="input-group shadow-sm rounded-pill overflow-hidden bg-theme-light border border-theme" style="max-width: 420px;">
                    <span class="input-group-text bg-transparent border-0 ps-4 pe-2"><i class="bi bi-search text-theme-muted"></i></span>
                    <input type="text" class="form-control border-0 ps-0 shadow-none py-2 text-theme-main bg-transparent" v-model="pendingMedQuery" placeholder="Tìm thuốc chờ duyệt...">
                  </div>
                  <h6 class="fw-bold mb-0 text-theme-muted">
                    <span class="text-warning">{{ filteredPendingMedicines.length }}</span> thuốc đang chờ duyệt
                  </h6>
                </div>
                <div v-if="filteredPendingMedicines.length === 0" class="text-center py-5 text-theme-muted bg-theme-light rounded-4 border border-dashed border-theme">
                  <i class="bi bi-clipboard-check display-4 text-success opacity-25"></i>
                  <p class="mt-3 fw-medium">Không có thuốc chờ duyệt khớp điều kiện hiện tại.</p>
                </div>
                <div v-else class="table-responsive rounded-4 shadow-sm border border-theme">
                  <table class="table table-hover align-middle mb-0">
                    <thead>
                      <tr class="table-theme-header">
                        <th class="ps-4 py-3 text-theme-muted fw-bold extra-small text-uppercase tracking-wider">Tên thuốc</th>
                        <th class="py-3 text-theme-muted fw-bold extra-small text-uppercase tracking-wider">Đơn vị</th>
                        <th class="py-3 text-theme-muted fw-bold extra-small text-uppercase tracking-wider">Mô tả / Công dụng</th>
                        <th class="text-end pe-4 py-3 text-theme-muted fw-bold extra-small text-uppercase tracking-wider">Thao tác</th>
                      </tr>
                    </thead>
                    <tbody class="bg-theme-card">
                      <tr v-for="m in paginatedPendingMedicines" :key="m.id" class="transition-all hover-bg">
                        <td class="ps-4 fw-bold text-theme-main d-flex align-items-center gap-2 py-3">
                          <div class="bg-primary bg-opacity-10 p-2 rounded-circle text-primary"><i class="bi bi-capsule-pill"></i></div>
                          {{ m.medicineName }}
                        </td>
                        <td class="small fw-medium"><span class="badge bg-theme-light text-theme-main border border-theme">{{ m.unit }}</span></td>
                        <td class="small text-theme-muted">{{ m.description || 'Không có mô tả' }}</td>
                        <td class="text-end pe-4">
                          <div class="d-flex justify-content-end gap-2">
                            <button class="btn btn-sm btn-outline-primary px-3 rounded-pill fw-bold transition-all" @click="openEditMedicine(m)">Sửa</button>
                            <button class="btn btn-sm btn-outline-danger px-3 rounded-pill fw-bold transition-all" @click="handleDeleteMedicine(m.id)">Từ chối</button>
                            <button class="btn btn-sm btn-success px-4 rounded-pill fw-bold shadow-sm hover-lift transition-all" @click="handleApproveMedicine(m.id)"><i class="bi bi-check2 me-1"></i> Duyệt</button>
                          </div>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
                <div v-if="pendingMedsTotalPages > 1" class="d-flex justify-content-end mt-4">
                  <div class="btn-group">
                    <button class="btn btn-sm btn-light bg-theme-light border border-theme" @click="pendingMedPage = Math.max(1, pendingMedPage - 1)" :disabled="pendingMedPage === 1">
                      <i class="bi bi-chevron-left"></i>
                    </button>
                    <button v-for="p in pageRange(pendingMedPage, pendingMedsTotalPages)" :key="`pm-${p}`" class="btn btn-sm border border-theme" :class="p === pendingMedPage ? 'btn-primary text-white' : 'btn-light bg-theme-light text-theme-main'" @click="pendingMedPage = p">
                      {{ p }}
                    </button>
                    <button class="btn btn-sm btn-light bg-theme-light border border-theme" @click="pendingMedPage = Math.min(pendingMedsTotalPages, pendingMedPage + 1)" :disabled="pendingMedPage === pendingMedsTotalPages">
                      <i class="bi bi-chevron-right"></i>
                    </button>
                  </div>
                </div>
              </div>

              
              <div v-else-if="subTabMed === 'all_meds'" key="all_meds">
                <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-4 gap-3">
                  <div class="input-group shadow-sm rounded-pill overflow-hidden bg-theme-light border border-theme" style="max-width: 400px;">
                    <span class="input-group-text bg-transparent border-0 ps-4 pe-2"><i class="bi bi-search text-theme-muted"></i></span>
                    <input type="text" class="form-control border-0 ps-0 shadow-none py-2 text-theme-main bg-transparent" v-model="medSearchQuery" placeholder="Tìm tên thuốc trong hệ thống...">
                  </div>
                  <div class="d-flex gap-3 align-items-center">
                    <h6 class="fw-bold mb-0 text-theme-muted align-self-center d-none d-lg-block">
                      <span class="text-primary">{{ filteredApprovedMedicines.length }}</span> thuốc đã duyệt
                    </h6>
                    <button class="btn btn-primary rounded-pill px-4 py-2 shadow-sm fw-bold hover-lift transition-all" @click="showAddMedicineModal = true">
                      <i class="bi bi-plus-lg me-1"></i> Thêm thuốc nhanh
                    </button>
                  </div>
                </div>
                
                <div class="table-responsive rounded-4 shadow-sm border border-theme bg-theme-card">
                  <table class="table table-hover align-middle mb-0">
                    <thead>
                      <tr class="table-dark">
                        <th class="ps-4 py-3 fw-medium extra-small text-uppercase tracking-wider border-0">Tên thuốc</th>
                        <th class="py-3 fw-medium extra-small text-uppercase tracking-wider border-0">Đơn vị</th>
                        <th class="py-3 fw-medium extra-small text-uppercase tracking-wider border-0">Mô tả</th>
                        <th class="text-end pe-4 py-3 fw-medium extra-small text-uppercase tracking-wider border-0">Thao tác</th>
                      </tr>
                    </thead>
                    <tbody class="bg-theme-card">
                      <tr v-for="med in paginatedApprovedMedicines" :key="med.id" class="transition-all hover-bg">
                        <td class="ps-4 fw-bold text-primary py-3">
                          <div class="d-flex align-items-center gap-2">
                            <i class="bi bi-prescription text-theme-muted"></i> {{ med.medicineName }}
                          </div>
                        </td>
                        <td class="small fw-medium"><span class="badge bg-theme-light text-theme-main border border-theme">{{ med.unit }}</span></td>
                        <td class="small text-theme-muted" style="max-width: 350px;">{{ med.description }}</td>
                        <td class="text-end pe-4">
                          <div class="d-flex justify-content-end gap-2">
                            <button class="btn btn-sm btn-light bg-theme-light border border-theme text-primary rounded-circle shadow-sm hover-primary transition-all" @click="openEditMedicine(med)" title="Sửa thuốc">
                              <i class="bi bi-pencil-fill"></i>
                            </button>
                            <button class="btn btn-sm btn-light bg-theme-light border border-theme text-danger rounded-circle shadow-sm hover-danger transition-all" @click="handleDeleteMedicine(med.id)" title="Xóa thuốc">
                              <i class="bi bi-trash3-fill"></i>
                            </button>
                          </div>
                        </td>
                      </tr>
                      <tr v-if="filteredApprovedMedicines.length === 0">
                        <td colspan="4" class="text-center py-5 text-theme-muted bg-theme-light border-0">
                          <i class="bi bi-search display-6 d-block mb-3 opacity-25"></i>
                          Không tìm thấy thuốc nào khớp với từ khóa "<span class="text-theme-main fw-bold">{{ medSearchQuery }}</span>"
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
                <div v-if="approvedMedsTotalPages > 1" class="d-flex justify-content-end mt-4">
                  <div class="btn-group">
                    <button class="btn btn-sm btn-light bg-theme-light border border-theme" @click="approvedMedPage = Math.max(1, approvedMedPage - 1)" :disabled="approvedMedPage === 1">
                      <i class="bi bi-chevron-left"></i>
                    </button>
                    <button v-for="p in pageRange(approvedMedPage, approvedMedsTotalPages)" :key="`am-${p}`" class="btn btn-sm border border-theme" :class="p === approvedMedPage ? 'btn-primary text-white' : 'btn-light bg-theme-light text-theme-main'" @click="approvedMedPage = p">
                      {{ p }}
                    </button>
                    <button class="btn btn-sm btn-light bg-theme-light border border-theme" @click="approvedMedPage = Math.min(approvedMedsTotalPages, approvedMedPage + 1)" :disabled="approvedMedPage === approvedMedsTotalPages">
                      <i class="bi bi-chevron-right"></i>
                    </button>
                  </div>
                </div>
              </div>
            </transition>
          </div>
        </div>
      </div>
    </div>

    
    <transition name="fade">
      <div v-if="showAddMedicineModal" class="modal-overlay d-flex align-items-center justify-content-center p-3" @click.self="showAddMedicineModal = false">
        <div class="bg-theme-card border border-theme rounded-4 shadow-lg w-100 overflow-hidden animate-slide-up" style="max-width: 450px;">
          <div class="p-4 border-bottom border-theme d-flex justify-content-between align-items-center bg-primary text-white">
            <h5 class="mb-0 fw-bold"><i class="bi bi-plus-circle me-2"></i>Thêm thuốc mới</h5>
            <button class="btn-close btn-close-white" @click="showAddMedicineModal = false"></button>
          </div>
          <form @submit.prevent="adminAddMedicine" class="p-4 bg-theme-card">
            <div class="mb-3">
              <label class="form-label small fw-bold text-theme-main">Tên thuốc</label>
              <input type="text" class="form-control bg-theme-light text-theme-main rounded-3 shadow-sm border border-theme py-2" v-model="newMedicine.name" placeholder="Ví dụ: Paracetamol 500mg" required>
            </div>
            <div class="mb-3">
              <label class="form-label small fw-bold text-theme-main">Đơn vị tính</label>
              <input type="text" class="form-control bg-theme-light text-theme-main rounded-3 shadow-sm border border-theme py-2" v-model="newMedicine.unit" placeholder="Ví dụ: Viên, Vỉ, Hộp..." required>
            </div>
            <div class="mb-4">
              <label class="form-label small fw-bold text-theme-main">Mô tả / Công dụng</label>
              <textarea class="form-control bg-theme-light text-theme-main rounded-3 shadow-sm border border-theme py-2" v-model="newMedicine.description" rows="3" placeholder="Nhập mô tả ngắn gọn..."></textarea>
            </div>
            <button type="submit" class="btn btn-primary w-100 rounded-pill fw-bold py-2 shadow hover-lift transition-all text-uppercase tracking-wider">
              Lưu & Duyệt ngay
            </button>
          </form>
        </div>
      </div>
    </transition>

    
    <transition name="fade">
      <div v-if="showEditMedicineModal" class="modal-overlay d-flex align-items-center justify-content-center p-3" @click.self="showEditMedicineModal = false">
        <div class="bg-theme-card border border-theme rounded-4 shadow-lg w-100 overflow-hidden animate-slide-up" style="max-width: 500px;">
          <div class="p-4 border-bottom border-theme d-flex justify-content-between align-items-center bg-theme-light">
            <h5 class="mb-0 fw-bold text-theme-main"><i class="bi bi-pencil-square me-2 text-primary"></i>Chỉnh sửa thuốc</h5>
            <button class="btn-close" @click="showEditMedicineModal = false"></button>
          </div>
          <form @submit.prevent="saveEditedMedicine" class="p-4 bg-theme-card">
            <div class="mb-3">
              <label class="form-label small fw-bold text-theme-main">Tên thuốc</label>
              <input type="text" class="form-control bg-theme-light text-theme-main rounded-3 shadow-sm border border-theme py-2" v-model="editingMedicine.name" required>
            </div>
            <div class="mb-3">
              <label class="form-label small fw-bold text-theme-main">Đơn vị</label>
              <input type="text" class="form-control bg-theme-light text-theme-main rounded-3 shadow-sm border border-theme py-2" v-model="editingMedicine.unit" required>
            </div>
            <div class="mb-4">
              <label class="form-label small fw-bold text-theme-main">Mô tả</label>
              <textarea class="form-control bg-theme-light text-theme-main rounded-3 shadow-sm border border-theme py-2" v-model="editingMedicine.description" rows="3"></textarea>
            </div>
            <button type="submit" class="btn btn-primary w-100 rounded-pill fw-bold py-2 shadow hover-lift">
              Lưu thay đổi
            </button>
          </form>
        </div>
      </div>
    </transition>

    
    <transition name="fade">
      <div v-if="showPostDetailModal && selectedPost" class="modal-overlay d-flex align-items-center justify-content-center p-3" @click.self="showPostDetailModal = false">
        <div class="bg-theme-card border border-theme rounded-4 shadow-lg w-100 overflow-hidden animate-slide-up" style="max-width: 760px;">
          <div class="p-4 border-bottom border-theme d-flex justify-content-between align-items-center bg-theme-light">
            <div>
              <h5 class="mb-1 fw-bold text-theme-main">{{ selectedPost.title }}</h5>
              <div class="small text-theme-muted">
                <span class="me-3"><i class="bi bi-person me-1"></i>{{ selectedPost.author }}</span>
                <span class="me-3"><i class="bi bi-clock me-1"></i>{{ formatDate(selectedPost.date) }}</span>
                <span><i class="bi bi-tag me-1"></i>{{ selectedPost.category }}</span>
              </div>
            </div>
            <button class="btn-close" @click="showPostDetailModal = false"></button>
          </div>
          <div class="p-4 bg-theme-card" style="max-height: 70vh; overflow-y: auto;">
            <p class="text-theme-main mb-4 white-space-pre-wrap text-break-word">{{ selectedPost.content || 'Không có nội dung.' }}</p>
            <div v-if="selectedPost.media" class="rounded-4 overflow-hidden border border-theme bg-black">
              <img v-if="!isVideoMedia(selectedPost.media)" :src="mediaUrl(selectedPost.media)" class="w-100" style="max-height: 420px; object-fit: contain;">
              <video v-else controls class="w-100" style="max-height: 420px; background: #000;">
                <source :src="mediaUrl(selectedPost.media)">
              </video>
            </div>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue';
import axios from 'axios';
import { toast } from '@/utils/toast';
import { Bar } from 'vue-chartjs';
import { Chart as ChartJS, Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale } from 'chart.js';

ChartJS.register(Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale);

const activeTab = ref('posts'); 
const subTab = ref('pending_posts'); 
const subTabMed = ref('pending_meds'); 

const pendingPosts = ref([]);
const allPosts = ref([]);
const reports = ref([]);
const allMedicines = ref([]);
const adminStats = ref({
  totalUsers: 0,
  approvedPosts: 0,
  pendingPosts: 0,
  activeReports: 0,
  totalMedicines: 0,
  approvedMedicines: 0,
  pendingMedicines: 0
});

const showAddMedicineModal = ref(false);
const showEditMedicineModal = ref(false);
const showPostDetailModal = ref(false);
const selectedPost = ref(null);
const medSearchQuery = ref('');
const pendingPostQuery = ref('');
const allPostQuery = ref('');
const reportQuery = ref('');
const pendingMedQuery = ref('');

const pendingPostPage = ref(1);
const allPostPage = ref(1);
const reportPage = ref(1);
const pendingMedPage = ref(1);
const approvedMedPage = ref(1);

const PAGE_SIZE = {
  pendingPosts: 6,
  allPosts: 8,
  reports: 8,
  pendingMeds: 8,
  approvedMeds: 8
};

const currentDate = ref(new Date().toLocaleDateString('vi-VN', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' }));

const newMedicine = reactive({
  name: '',
  unit: '',
  description: ''
});

const editingMedicine = reactive({
  id: null,
  name: '',
  unit: '',
  description: ''
});

const isCommunityTab = computed(() => activeTab.value === 'posts');
const isMedicalTab = computed(() => activeTab.value === 'medicines');
const pendingMedicines = computed(() => allMedicines.value.filter(m => !m.isApproved));
const approvedMedicines = computed(() => allMedicines.value.filter(m => m.isApproved));

const normalizeText = (value) => (value ?? '').toString().toLowerCase();

const filteredPendingPosts = computed(() => {
  const q = normalizeText(pendingPostQuery.value).trim();
  if (!q) return pendingPosts.value;
  return pendingPosts.value.filter(post =>
    normalizeText(post.title).includes(q) ||
    normalizeText(post.content).includes(q) ||
    normalizeText(post.author).includes(q) ||
    normalizeText(post.category).includes(q)
  );
});

const filteredAllPosts = computed(() => {
  const q = normalizeText(allPostQuery.value).trim();
  if (!q) return allPosts.value;
  return allPosts.value.filter(post =>
    normalizeText(post.title).includes(q) ||
    normalizeText(post.content).includes(q) ||
    normalizeText(post.author).includes(q) ||
    normalizeText(post.category).includes(q)
  );
});

const filteredReports = computed(() => {
  const q = normalizeText(reportQuery.value).trim();
  if (!q) return reports.value;
  return reports.value.filter(report =>
    normalizeText(report.reporterName).includes(q) ||
    normalizeText(report.reason).includes(q) ||
    normalizeText(report.postTitle).includes(q) ||
    normalizeText(report.postAuthor).includes(q) ||
    normalizeText(report.postContent).includes(q)
  );
});

const filteredPendingMedicines = computed(() => {
  const q = normalizeText(pendingMedQuery.value).trim();
  if (!q) return pendingMedicines.value;
  return pendingMedicines.value.filter(m =>
    normalizeText(m.medicineName).includes(q) ||
    normalizeText(m.description).includes(q) ||
    normalizeText(m.unit).includes(q)
  );
});

const filteredApprovedMedicines = computed(() => {
  if (!medSearchQuery.value) return approvedMedicines.value;
  const q = normalizeText(medSearchQuery.value);
  return approvedMedicines.value.filter(m => 
    normalizeText(m.medicineName).includes(q) || 
    normalizeText(m.description).includes(q) ||
    normalizeText(m.unit).includes(q)
  );
});

const paginate = (items, page, pageSize) => {
  const start = (page - 1) * pageSize;
  return items.slice(start, start + pageSize);
};

const totalPages = (total, size) => Math.max(1, Math.ceil(total / size));

const pendingPostsTotalPages = computed(() => totalPages(filteredPendingPosts.value.length, PAGE_SIZE.pendingPosts));
const allPostsTotalPages = computed(() => totalPages(filteredAllPosts.value.length, PAGE_SIZE.allPosts));
const reportsTotalPages = computed(() => totalPages(filteredReports.value.length, PAGE_SIZE.reports));
const pendingMedsTotalPages = computed(() => totalPages(filteredPendingMedicines.value.length, PAGE_SIZE.pendingMeds));
const approvedMedsTotalPages = computed(() => totalPages(filteredApprovedMedicines.value.length, PAGE_SIZE.approvedMeds));

const paginatedPendingPosts = computed(() => paginate(filteredPendingPosts.value, pendingPostPage.value, PAGE_SIZE.pendingPosts));
const paginatedAllPosts = computed(() => paginate(filteredAllPosts.value, allPostPage.value, PAGE_SIZE.allPosts));
const paginatedReports = computed(() => paginate(filteredReports.value, reportPage.value, PAGE_SIZE.reports));
const paginatedPendingMedicines = computed(() => paginate(filteredPendingMedicines.value, pendingMedPage.value, PAGE_SIZE.pendingMeds));
const paginatedApprovedMedicines = computed(() => paginate(filteredApprovedMedicines.value, approvedMedPage.value, PAGE_SIZE.approvedMeds));

const pageRange = (current, total) => {
  const maxVisible = 5;
  let start = Math.max(1, current - Math.floor(maxVisible / 2));
  let end = Math.min(total, start + maxVisible - 1);
  start = Math.max(1, end - maxVisible + 1);
  return Array.from({ length: end - start + 1 }, (_, i) => start + i);
};

const mediaUrl = (media) => {
  if (!media) return '';
  if (media.startsWith('http')) return media;
  const baseUrl = axios.defaults.baseURL || '';
  return `${baseUrl}/uploads/${media}`;
};

const isVideoMedia = (media) => {
  if (!media) return false;
  const normalized = media.toLowerCase();
  return normalized.includes('.mp4') || normalized.includes('video/upload');
};

const openPostDetail = (post) => {
  selectedPost.value = post;
  showPostDetailModal.value = true;
};

const openReportDetail = (report) => {
  selectedPost.value = {
    id: report.postId,
    title: report.postTitle,
    content: report.postContent,
    media: report.postMedia,
    author: report.postAuthor,
    date: report.postCreatedAt,
    category: 'Bị báo cáo'
  };
  showPostDetailModal.value = true;
};

const chartData = computed(() => ({
  labels: ['Người dùng', 'Bài viết duyệt', 'Bài chờ duyệt', 'Báo cáo mở', 'Thuốc duyệt', 'Thuốc chờ duyệt'],
  datasets: [
    {
      label: 'Số lượng',
      data: [
        adminStats.value.totalUsers,
        adminStats.value.approvedPosts,
        adminStats.value.pendingPosts,
        adminStats.value.activeReports,
        adminStats.value.approvedMedicines,
        adminStats.value.pendingMedicines
      ],
      backgroundColor: ['#0d6efd', '#198754', '#ffc107', '#dc3545', '#20c997', '#fd7e14'],
      borderRadius: 8
    }
  ]
}));

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false }
  },
  scales: {
    x: { grid: { display: false } },
    y: { beginAtZero: true, ticks: { precision: 0 } }
  }
};

const fetchData = async () => {
  try {
    const [pendingRes, allPostsRes, repRes, medRes, statsRes] = await Promise.all([
      axios.get('/api/admin/posts/pending'),
      axios.get('/api/admin/posts/all'),
      axios.get('/api/admin/reports'),
      axios.get('/api/admin/medicines'),
      axios.get('/api/admin/stats')
    ]);
    pendingPosts.value = pendingRes.data;
    allPosts.value = allPostsRes.data;
    reports.value = repRes.data;
    allMedicines.value = medRes.data;
    adminStats.value = {
      ...adminStats.value,
      ...(statsRes.data || {})
    };
  } catch (err) { console.error("Lỗi tải dữ liệu quản trị:", err); }
};

const handleApprove = async (id) => {
  if (!await toast.confirm('Xác nhận duyệt', 'Duyệt bài viết này lên diễn đàn?')) return;
  try { 
    await axios.post(`/api/admin/posts/approve/${id}`); 
    fetchData(); 
  } catch (err) { toast.error("Lỗi khi duyệt bài."); }
};

const handleApproveMedicine = async (id) => {
  try {
    await axios.post(`/api/admin/medicines/${id}/approve`);
    fetchData();
  } catch (err) { toast.error("Lỗi khi duyệt thuốc."); }
};

const handleDeletePost = async (id) => {
  if (!await toast.confirm('Xác nhận xóa', 'Bạn chắc chắn muốn xóa bài viết này? Thao tác không thể hoàn tác.')) return;
  try { 
    const res = await axios.delete(`/api/admin/posts/${id}`);
    if (res.data.success) {
      toast.success("Đã xóa bài viết thành công.");
      fetchData();
    }
  } catch (err) { 
    console.error("Lỗi xóa bài viết:", err);
    toast.error(err.response?.data?.message || err.response?.data?.error || "Lỗi khi xóa bài viết."); 
  }
};

const handleResolveReport = async (id) => {
  try { 
    await axios.post(`/api/admin/reports/${id}/resolve`); 
    fetchData(); 
  } catch (err) { console.error(err); }
};

const handleDeleteMedicine = async (id) => {
  if (!await toast.confirm('Xác nhận xóa', 'Xác nhận xóa thuốc này khỏi hệ thống?')) return;
  try { 
    await axios.delete(`/api/admin/medicines/${id}`); 
    fetchData(); 
  } catch (err) { toast.error("Lỗi khi xóa thuốc."); }
};

const openEditMedicine = (medicine) => {
  editingMedicine.id = medicine.id;
  editingMedicine.name = medicine.medicineName || '';
  editingMedicine.unit = medicine.unit || '';
  editingMedicine.description = medicine.description || '';
  showEditMedicineModal.value = true;
};

const saveEditedMedicine = async () => {
  try {
    await axios.put(`/api/admin/medicines/${editingMedicine.id}`, {
      name: editingMedicine.name,
      unit: editingMedicine.unit,
      description: editingMedicine.description
    });
    toast.success('Cập nhật thuốc thành công.');
    showEditMedicineModal.value = false;
    await fetchData();
  } catch (err) {
    toast.error(err.response?.data?.error || 'Lỗi khi cập nhật thuốc.');
  }
};

const adminAddMedicine = async () => {
  try {
    await axios.post('/api/medicines/request', newMedicine);
    toast.success("Đã thêm thuốc mới.");
    showAddMedicineModal.value = false;
    newMedicine.name = ''; newMedicine.unit = ''; newMedicine.description = '';
    fetchData();
  } catch (err) { toast.error(err.response?.data?.message || "Lỗi khi thêm thuốc."); }
};

const formatDate = (dateStr) => {
  if (!dateStr) return '---';
  return new Date(dateStr).toLocaleString('vi-VN');
};

watch([pendingPostQuery, allPostQuery, reportQuery, pendingMedQuery, medSearchQuery], () => {
  pendingPostPage.value = 1;
  allPostPage.value = 1;
  reportPage.value = 1;
  pendingMedPage.value = 1;
  approvedMedPage.value = 1;
});

onMounted(fetchData);
</script>

<style scoped>
.container-fluid {
  height: calc(100vh - var(--header-height) - 60px); 
}

.extra-small { font-size: 0.65rem; }
.tracking-wider { letter-spacing: 0.05em; }
.line-clamp-3 { display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden; }

.post-card-admin { transition: all 0.3s cubic-bezier(0.165, 0.84, 0.44, 1); }
.post-card-admin:hover { transform: translateY(-5px); box-shadow: 0 15px 30px rgba(0,0,0,0.1) !important; }

.hover-lift { transition: transform 0.2s cubic-bezier(0.165, 0.84, 0.44, 1), box-shadow 0.2s; }
.hover-lift:hover { transform: translateY(-3px); box-shadow: 0 10px 20px rgba(0,0,0,0.08) !important; }

.hover-bg { transition: background-color 0.2s; }
.hover-bg:hover { background-color: var(--hover-bg); }

.hover-danger:hover { background-color: #dc3545 !important; color: white !important; }
.hover-primary:hover { background-color: #0d6efd !important; color: white !important; }

.transition-all { transition: all 0.2s ease-in-out; }

.object-fit-cover { object-fit: cover; }
.hover-opacity-100:hover { opacity: 1 !important; }

.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.6); backdrop-filter: blur(4px); z-index: 2000; }
.nav-underline .active { color: var(--primary-color); border-bottom: 3px solid var(--primary-color) !important; }

.backdrop-blur { backdrop-filter: blur(5px); }


.animate-fade-in { animation: fadeIn 0.4s ease-out; }
.animate-slide-up { animation: slideUp 0.4s ease-out; }

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
@keyframes slideUp {
  from { opacity: 0; transform: translateY(15px); }
  to { opacity: 1; transform: translateY(0); }
}


.custom-scrollbar::-webkit-scrollbar { width: 6px; height: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: var(--border-color); border-radius: 10px; }
.custom-scrollbar::-webkit-scrollbar-thumb:hover { background: var(--secondary-color); }

.border-theme { border-color: var(--border-color) !important; }
.table-theme-header th { background-color: var(--bg-light) !important; color: var(--text-muted) !important; border-bottom: 1px solid var(--border-color) !important; }
</style>
