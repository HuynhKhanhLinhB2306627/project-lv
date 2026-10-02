<template>
  <div class="home-page" :class="{ 'dark-mode': isDarkMode }">
    



    <header class="home-header" :class="{ 'scrolled': isScrolled }">
      <div class="container">
        <nav class="navbar navbar-expand-lg py-3">
          <router-link to="/home" class="navbar-brand d-flex align-items-center">
            <div class="brand-icon me-2">
              <i class="bi bi-heart-pulse-fill"></i>
            </div>
            <span class="brand-text fw-bold">Health<span class="text-primary">Record</span></span>
          </router-link>
          
          <button class="navbar-toggler border-0" type="button" @click="showMobileNav = !showMobileNav">
            <i class="bi bi-list fs-3"></i>
          </button>

          <div class="navbar-collapse" :class="{ 'show-mobile': showMobileNav }">
            <ul class="navbar-nav ms-auto mb-2 mb-lg-0 gap-1">
              <li class="nav-item">
                <a href="#features" class="nav-link" @click="showMobileNav = false">Tính năng</a>
              </li>
              <li class="nav-item">
                <a href="#ai" class="nav-link" @click="showMobileNav = false">AI Dự đoán</a>
              </li>
              <li class="nav-item">
                <a href="#about" class="nav-link" @click="showMobileNav = false">Về chúng tôi</a>
              </li>
            </ul>
            <div class="navbar-actions d-flex align-items-center gap-2 ms-lg-4">
              <button class="btn btn-icon" @click="toggleTheme" :title="isDarkMode ? 'Chế độ sáng' : 'Chế độ tối'">
                <i :class="isDarkMode ? 'bi bi-sun-fill' : 'bi bi-moon-fill'"></i>
              </button>
              
              
              <template v-if="isAuthenticated">
                <router-link to="/dashboard" class="btn btn-primary rounded-pill px-4 shadow-sm">
                  <i class="bi bi-speedometer2 me-1"></i> Vào Dashboard
                </router-link>
              </template>
              <template v-else>
                <router-link to="/login" class="btn btn-outline-primary rounded-pill px-3">
                  <i class="bi bi-box-arrow-in-right me-1"></i> Đăng nhập
                </router-link>
                <router-link to="/register" class="btn btn-primary rounded-pill px-3">
                  <i class="bi bi-person-plus me-1"></i> Đăng ký
                </router-link>
              </template>
            </div>
          </div>
        </nav>
      </div>
    </header>

    



    <section class="hero-section position-relative overflow-hidden">
      <div class="carousel-container">
        
        <div class="carousel-slides" :style="{ transform: `translateX(-${currentSlide * 100}%)` }">
          <div 
            v-for="(slide, index) in bannerSlides" 
            :key="index"
            class="carousel-slide"
          >
            <div class="slide-overlay"></div>
            <img :src="slide.image" :alt="slide.title" class="slide-image">
            <div class="slide-content">
              <div class="container">
                <div class="row align-items-center min-vh-60">
                  <div class="col-lg-7 col-md-8">
                    <span class="badge bg-white text-primary px-3 py-2 rounded-pill mb-3 animate-fade-down">
                      <i :class="slide.icon" class="me-1"></i> {{ slide.badge }}
                    </span>
                    <h1 class="display-4 fw-bold text-white mb-4 animate-fade-up">
                      {{ slide.title }}
                    </h1>
                    <p class="lead text-white-50 mb-4 animate-fade-up-delay">
                      {{ slide.description }}
                    </p>
                    <div class="d-flex flex-wrap gap-3 animate-fade-up-delay-2">
                      <router-link :to="slide.primaryLink" class="btn btn-light btn-lg rounded-pill px-4 shadow-sm hover-lift">
                        <i :class="slide.primaryIcon" class="me-2"></i> {{ slide.primaryText }}
                      </router-link>
                      <router-link :to="slide.secondaryLink" class="btn btn-outline-light btn-lg rounded-pill px-4">
                        {{ slide.secondaryText }} <i class="bi bi-arrow-right ms-2"></i>
                      </router-link>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        
        <button class="carousel-nav carousel-prev" @click="prevSlide" aria-label="Previous">
          <i class="bi bi-chevron-left"></i>
        </button>
        <button class="carousel-nav carousel-next" @click="nextSlide" aria-label="Next">
          <i class="bi bi-chevron-right"></i>
        </button>

        
        <div class="carousel-dots">
          <button 
            v-for="(_, index) in bannerSlides" 
            :key="index"
            :class="['carousel-dot', { active: currentSlide === index }]"
            @click="goToSlide(index)"
            :aria-label="`Go to slide ${index + 1}`"
          ></button>
        </div>
      </div>

      
      <div class="scroll-indicator">
        <div class="mouse">
          <div class="wheel"></div>
        </div>
        <span>Cuộn xuống</span>
      </div>
    </section>

    



    <section class="stats-section py-4 bg-theme-card border-bottom border-theme">
      <div class="container">
        <div class="row g-4 text-center">
          <div class="col-6 col-md-3" v-for="stat in quickStats" :key="stat.label">
            <div class="stat-item">
              <div class="stat-icon mb-2">
                <i :class="stat.icon" :style="{ color: stat.color }"></i>
              </div>
              <div class="stat-value fw-bold text-theme-main">{{ stat.value }}</div>
              <div class="stat-label text-theme-muted small">{{ stat.label }}</div>
            </div>
          </div>
        </div>
      </div>
    </section>

    



    <section id="features" class="features-section py-5 bg-theme-light">
      <div class="container">
        <div class="text-center mb-5">
          <span class="badge bg-primary bg-opacity-10 text-primary px-3 py-2 rounded-pill mb-3">
            <i class="bi bi-grid-3x3-gap me-1"></i> Tính năng nổi bật
          </span>
          <h2 class="display-6 fw-bold text-theme-main mb-3">
            Quản lý sức khỏe toàn diện
          </h2>
          <p class="text-theme-muted mx-auto" style="max-width: 600px;">
            Hệ thống Health Record cung cấp đầy đủ công cụ giúp bạn theo dõi và quản lý hồ sơ sức khỏe một cách hiệu quả
          </p>
          <div class="d-flex flex-wrap justify-content-center gap-2 mt-3">
            <span class="badge rounded-pill text-bg-primary px-3 py-2"><i class="bi bi-robot me-1"></i> Trợ lý AI</span>
            <span class="badge rounded-pill text-bg-success px-3 py-2"><i class="bi bi-geo-alt me-1"></i> Cơ sở y tế</span>
            <span class="badge rounded-pill text-bg-danger px-3 py-2"><i class="bi bi-people me-1"></i> Cộng đồng</span>
          </div>
        </div>

        <div class="row g-4">
          
          <div class="col-xl-3 col-md-6">
            <div class="feature-card h-100 bg-theme-card rounded-4 shadow-sm overflow-hidden border border-theme hover-lift-lg">
              <div class="feature-image position-relative">
                <img src="/images/cn1.jpg" alt="Hồ sơ bệnh án" class="w-100">
                <div class="feature-overlay">
                  <span class="badge bg-primary px-3 py-2 rounded-pill">
                    <i class="bi bi-file-medical me-1"></i> Hồ sơ y tế
                  </span>
                </div>
              </div>
              <div class="p-4">
                <h5 class="fw-bold text-theme-main mb-2">
                  <i class="bi bi-journal-medical text-primary me-2"></i>
                  Hồ sơ bệnh án điện tử
                </h5>
                <p class="text-theme-muted small mb-3">
                  Lưu trữ và quản lý toàn bộ hồ sơ y tế, kết quả xét nghiệm, phim X-quang một cách an toàn và tiện lợi.
                </p>
                <router-link to="/documents" class="btn btn-sm btn-outline-primary rounded-pill">
                  Xem chi tiết <i class="bi bi-arrow-right ms-1"></i>
                </router-link>
              </div>
            </div>
          </div>

          
          <div class="col-xl-3 col-md-6">
            <div class="feature-card h-100 bg-theme-card rounded-4 shadow-sm overflow-hidden border border-theme hover-lift-lg">
              <div class="feature-image position-relative">
                <img src="/images/cn2.jpg" alt="Lịch sử tiêm chủng" class="w-100">
                <div class="feature-overlay">
                  <span class="badge bg-success px-3 py-2 rounded-pill">
                    <i class="bi bi-shield-check me-1"></i> Y tế dự phòng
                  </span>
                </div>
              </div>
              <div class="p-4">
                <h5 class="fw-bold text-theme-main mb-2">
                  <i class="bi bi-clipboard2-pulse text-success me-2"></i>
                  Lịch sử tiêm chủng
                </h5>
                <p class="text-theme-muted small mb-3">
                  Theo dõi lịch tiêm vaccine, nhận thông báo nhắc lịch tiêm và quản lý sổ tiêm chủng điện tử.
                </p>
                <router-link to="/vaccinations" class="btn btn-sm btn-outline-success rounded-pill">
                  Xem chi tiết <i class="bi bi-arrow-right ms-1"></i>
                </router-link>
              </div>
            </div>
          </div>

          
          <div class="col-xl-3 col-md-6">
            <div class="feature-card h-100 bg-theme-card rounded-4 shadow-sm overflow-hidden border border-theme hover-lift-lg">
              <div class="feature-image position-relative">
                <img src="/images/cn3.jpg" alt="Quản lý lịch hẹn" class="w-100">
                <div class="feature-overlay">
                  <span class="badge bg-info px-3 py-2 rounded-pill">
                    <i class="bi bi-calendar-check me-1"></i> Lịch hẹn
                  </span>
                </div>
              </div>
              <div class="p-4">
                <h5 class="fw-bold text-theme-main mb-2">
                  <i class="bi bi-laptop text-info me-2"></i>
                  Cổng thông tin bệnh nhân
                </h5>
                <p class="text-theme-muted small mb-3">
                  Truy cập hồ sơ y tế mọi lúc mọi nơi, quản lý lịch hẹn khám cá nhân và theo dõi nhắc lịch chăm sóc sức khỏe.
                </p>
                <router-link to="/appointments" class="btn btn-sm btn-outline-info rounded-pill">
                  Quản lý lịch hẹn <i class="bi bi-arrow-right ms-1"></i>
                </router-link>
              </div>
            </div>
          </div>

          
          <div class="col-xl-3 col-md-6">
            <div class="feature-card h-100 bg-theme-card rounded-4 shadow-sm overflow-hidden border border-theme hover-lift-lg">
              <div class="feature-image position-relative">
                <img src="/images/cn4.jpg" alt="Bảo mật dữ liệu" class="w-100">
                <div class="feature-overlay">
                  <span class="badge bg-warning px-3 py-2 rounded-pill text-dark">
                    <i class="bi bi-shield-lock me-1"></i> An toàn
                  </span>
                </div>
              </div>
              <div class="p-4">
                <h5 class="fw-bold text-theme-main mb-2">
                  <i class="bi bi-lock text-warning me-2"></i>
                  Bảo mật dữ liệu y tế
                </h5>
                <p class="text-theme-muted small mb-3">
                  Mã hóa SSL bảo vệ thông tin y tế. Chỉ bạn và người được ủy quyền mới có thể truy cập hồ sơ.
                </p>
                <router-link to="/documents" class="btn btn-sm btn-outline-warning rounded-pill">
                  Xem kho tài liệu <i class="bi bi-arrow-right ms-1"></i>
                </router-link>
              </div>
            </div>
          </div>

          
          <div class="col-xl-3 col-md-6">
            <div class="feature-card h-100 bg-theme-card rounded-4 shadow-sm overflow-hidden border border-theme hover-lift-lg">
              <div class="feature-image position-relative">
                <img src="/images/cn5.jpg" alt="Hỗ trợ cộng đồng" class="w-100">
                <div class="feature-overlay">
                  <span class="badge bg-danger px-3 py-2 rounded-pill">
                    <i class="bi bi-heart me-1"></i> Cộng đồng
                  </span>
                </div>
              </div>
              <div class="p-4">
                <h5 class="fw-bold text-theme-main mb-2">
                  <i class="bi bi-people text-danger me-2"></i>
                  Diễn đàn sức khỏe
                </h5>
                <p class="text-theme-muted small mb-3">
                  Đặt câu hỏi, chia sẻ kinh nghiệm và kết nối với cộng đồng người dùng cùng quan tâm sức khỏe.
                </p>
                <router-link to="/forum" class="btn btn-sm btn-outline-danger rounded-pill">
                  Tham gia diễn đàn <i class="bi bi-arrow-right ms-1"></i>
                </router-link>
              </div>
            </div>
          </div>

          
          <div class="col-xl-3 col-md-6">
            <div class="feature-card h-100 bg-theme-card rounded-4 shadow-sm overflow-hidden border border-theme hover-lift-lg">
              <div class="feature-image position-relative">
                <img src="/images/cn6.jpg" alt="Quản lý đơn thuốc" class="w-100">
                <div class="feature-overlay">
                  <span class="badge bg-purple px-3 py-2 rounded-pill">
                    <i class="bi bi-capsule me-1"></i> Nhắc thuốc
                  </span>
                </div>
              </div>
              <div class="p-4">
                <h5 class="fw-bold text-theme-main mb-2">
                  <i class="bi bi-prescription2 text-purple me-2"></i>
                  Quản lý đơn thuốc
                </h5>
                <p class="text-theme-muted small mb-3">
                  Lưu trữ đơn thuốc, đặt lịch nhắc uống thuốc và theo dõi tiến trình điều trị.
                </p>
                <router-link to="/medications" class="btn btn-sm btn-outline-purple rounded-pill">
                  Quản lý thuốc <i class="bi bi-arrow-right ms-1"></i>
                </router-link>
              </div>
            </div>
          </div>

          
          <div class="col-xl-3 col-md-6">
            <div class="feature-card h-100 bg-theme-card rounded-4 shadow-sm overflow-hidden border border-theme hover-lift-lg">
              <div class="feature-image position-relative">
                <img src="/images/chatbot.jpg" alt="Chatbot Sức khỏe AI" class="w-100">
                <div class="feature-overlay">
                  <span class="badge bg-light text-primary px-3 py-2 rounded-pill">
                    <i class="bi bi-chat-heart me-1"></i> Trợ lý AI
                  </span>
                </div>
              </div>
              <div class="p-4">
                <h5 class="fw-bold text-theme-main mb-2">
                  <i class="bi bi-robot text-primary me-2"></i>
                  Chatbot Sức khỏe
                </h5>
                <p class="text-theme-muted small mb-3">
                  Trò chuyện với trợ lý AI để hỏi đáp sức khỏe, nhận gợi ý tham khảo và tra cứu nhanh.
                </p>
                <router-link to="/ai-consulting" class="btn btn-sm btn-outline-primary rounded-pill">
                  Trò chuyện AI <i class="bi bi-arrow-right ms-1"></i>
                </router-link>
              </div>
            </div>
          </div>

          
          <div class="col-xl-3 col-md-6">
            <div class="feature-card h-100 bg-theme-card rounded-4 shadow-sm overflow-hidden border border-theme hover-lift-lg">
              <div class="feature-image position-relative">
                <img src="/images/cosoytegannhat.png" alt="Cơ sở y tế gần đây" class="w-100">
                <div class="feature-overlay">
                  <span class="badge bg-light text-success px-3 py-2 rounded-pill">
                    <i class="bi bi-map me-1"></i> Bản đồ
                  </span>
                </div>
              </div>
              <div class="p-4">
                <h5 class="fw-bold text-theme-main mb-2">
                  <i class="bi bi-hospital text-success me-2"></i>
                  Cơ sở y tế gần đây
                </h5>
                <p class="text-theme-muted small mb-3">
                  Tìm bệnh viện, phòng khám, nhà thuốc gần vị trí của bạn trên bản đồ tương tác.
                </p>
                <router-link to="/facilities-map" class="btn btn-sm btn-outline-success rounded-pill">
                  Xem bản đồ <i class="bi bi-arrow-right ms-1"></i>
                </router-link>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    



    <section id="ai" class="ai-section py-5 position-relative overflow-hidden">
      <div class="ai-bg-pattern"></div>
      <div class="container position-relative z-1">
        <div class="row align-items-center g-5">
          <div class="col-lg-6">
            <span class="badge bg-white bg-opacity-20 text-white px-3 py-2 rounded-pill mb-3">
              <i class="bi bi-cpu me-1"></i> Công cụ phân tích AI
            </span>
            <h2 class="display-5 fw-bold text-white mb-4">
              Dự đoán nguy cơ bệnh tim mạch bằng AI
            </h2>
            <p class="text-white-50 mb-4 lead">
              Sử dụng trí tuệ nhân tạo phân tích dữ liệu sức khỏe của bạn để đánh giá nguy cơ tim mạch.
            </p>
            <ul class="list-unstyled mb-4">
              <li class="d-flex align-items-center mb-3 text-white">
                <div class="ai-check-icon me-3">
                  <i class="bi bi-check-lg"></i>
                </div>
                <span>Dự đoán nguy cơ tim mạch dựa trên dữ liệu sức khỏe cá nhân</span>
              </li>
              <li class="d-flex align-items-center mb-3 text-white">
                <div class="ai-check-icon me-3">
                  <i class="bi bi-check-lg"></i>
                </div>
                <span>Phân tích các chỉ số lâm sàng và lối sống</span>
              </li>
              <li class="d-flex align-items-center mb-3 text-white">
                <div class="ai-check-icon me-3">
                  <i class="bi bi-check-lg"></i>
                </div>
                <span>Trả về mức nguy cơ, cảnh báo và khuyến nghị tham khảo</span>
              </li>
            </ul>
            <router-link to="/ai-prediction" class="btn btn-light btn-lg rounded-pill px-5 shadow hover-lift">
              <i class="bi bi-heart-pulse me-2"></i> Thử ngay
            </router-link>
          </div>
          <div class="col-lg-6">
            <div class="ai-visual position-relative">
              <div class="ai-card bg-white bg-opacity-10 backdrop-blur rounded-4 p-4 shadow-lg border border-white border-opacity-20">
                <div class="d-flex align-items-center mb-4">
                  <div class="ai-avatar me-3">
                    <i class="bi bi-robot"></i>
                  </div>
                  <div>
                  <h6 class="text-white fw-bold mb-0">AI Risk Analyzer</h6>
                    <small class="text-white-50">Đang phân tích...</small>
                  </div>
                </div>
                <div class="ai-result-preview">
                  <div class="mb-3">
                    <div class="d-flex justify-content-between text-white-50 small mb-1">
                      <span>Nguy cơ tim mạch</span>
                      <span class="fw-bold text-warning">32%</span>
                    </div>
                    <div class="progress bg-white bg-opacity-20" style="height: 8px;">
                      <div class="progress-bar bg-warning" style="width: 32%"></div>
                    </div>
                  </div>
                  <div class="mb-3">
                    <div class="d-flex justify-content-between text-white-50 small mb-1">
                      <span>Độ tin cậy</span>
                      <span class="fw-bold text-success">87%</span>
                    </div>
                    <div class="progress bg-white bg-opacity-20" style="height: 8px;">
                      <div class="progress-bar bg-success" style="width: 87%"></div>
                    </div>
                  </div>
                  <div class="ai-tags d-flex flex-wrap gap-2 mt-3">
                    <span class="badge bg-white bg-opacity-20 text-white">Huyết áp bình thường</span>
                    <span class="badge bg-white bg-opacity-20 text-white">Cholesterol OK</span>
                    <span class="badge bg-white bg-opacity-20 text-white">BMI tốt</span>
                  </div>
                </div>
              </div>
              
              <div class="floating-badge badge-1">
                <i class="bi bi-heart-pulse"></i>
              </div>
              <div class="floating-badge badge-2">
                <i class="bi bi-activity"></i>
              </div>
              <div class="floating-badge badge-3">
                <i class="bi bi-graph-up"></i>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    



    <section class="community-section py-5 bg-theme-light">
      <div class="container">
        <div class="row align-items-center g-5">
          
          <div class="col-lg-6">
            <span class="badge bg-danger bg-opacity-10 text-danger px-3 py-2 rounded-pill mb-3">
              <i class="bi bi-people-fill me-1"></i> Kết nối cộng đồng
            </span>
            <h2 class="display-6 fw-bold text-theme-main mb-4">
              Diễn đàn & Nhắn tin
            </h2>
            <p class="text-theme-muted mb-4">
              Chia sẻ kinh nghiệm sức khỏe qua diễn đàn, tìm kiếm và nhắn tin trực tiếp với người dùng khác.
            </p>
            
            <div class="row g-3 mb-4">
              <div class="col-6">
                <div class="d-flex align-items-start gap-3">
                  <div class="community-icon bg-primary bg-opacity-10 text-primary rounded-3 p-2">
                    <i class="bi bi-pencil-square fs-4"></i>
                  </div>
                  <div>
                    <h6 class="fw-bold text-theme-main mb-1">Đăng bài</h6>
                    <p class="text-theme-muted small mb-0">Viết bài chia sẻ kinh nghiệm sức khỏe</p>
                  </div>
                </div>
              </div>
              <div class="col-6">
                <div class="d-flex align-items-start gap-3">
                  <div class="community-icon bg-success bg-opacity-10 text-success rounded-3 p-2">
                    <i class="bi bi-search fs-4"></i>
                  </div>
                  <div>
                    <h6 class="fw-bold text-theme-main mb-1">Tìm người</h6>
                    <p class="text-theme-muted small mb-0">Tìm kiếm và nhắn tin với người dùng</p>
                  </div>
                </div>
              </div>
              <div class="col-6">
                <div class="d-flex align-items-start gap-3">
                  <div class="community-icon bg-info bg-opacity-10 text-info rounded-3 p-2">
                    <i class="bi bi-bell fs-4"></i>
                  </div>
                  <div>
                    <h6 class="fw-bold text-theme-main mb-1">Thông báo</h6>
                    <p class="text-theme-muted small mb-0">Nhận thông báo khi có phản hồi</p>
                  </div>
                </div>
              </div>
              <div class="col-6">
                <div class="d-flex align-items-start gap-3">
                  <div class="community-icon bg-warning bg-opacity-10 text-warning rounded-3 p-2">
                    <i class="bi bi-hand-thumbs-up fs-4"></i>
                  </div>
                  <div>
                    <h6 class="fw-bold text-theme-main mb-1">Tương tác</h6>
                    <p class="text-theme-muted small mb-0">Like, bình luận bài viết</p>
                  </div>
                </div>
              </div>
            </div>

            <div class="d-flex flex-wrap gap-3">
              <router-link to="/forum" class="btn btn-danger rounded-pill px-4 shadow-sm hover-lift">
                <i class="bi bi-people me-2"></i> Vào Diễn đàn
              </router-link>
              <router-link to="/chat" class="btn btn-outline-primary rounded-pill px-4">
                <i class="bi bi-chat-dots me-2"></i> Nhắn tin
              </router-link>
            </div>
          </div>

          
          <div class="col-lg-6">
            <div class="community-preview position-relative">
              
              <div class="preview-card chat-preview bg-theme-card rounded-4 shadow-lg p-4 border border-theme">
                <div class="d-flex align-items-center mb-3 pb-3 border-bottom border-theme">
                  <div class="chat-avatar bg-primary text-white rounded-circle d-flex align-items-center justify-content-center me-3" style="width: 40px; height: 40px;">
                    <i class="bi bi-chat-dots"></i>
                  </div>
                  <div>
                    <h6 class="fw-bold text-theme-main mb-0">Tin nhắn</h6>
                    <small class="text-theme-muted">Tìm người dùng để trò chuyện</small>
                  </div>
                </div>
                <div class="chat-messages">
                  <div class="message-item d-flex gap-2 mb-2">
                    <div class="msg-avatar bg-info text-white rounded-circle d-flex align-items-center justify-content-center flex-shrink-0" style="width: 32px; height: 32px; font-size: 12px;">T</div>
                    <div class="msg-bubble bg-theme-light rounded-3 px-3 py-2">
                      <small class="text-theme-muted">Chào bạn, mình có câu hỏi về huyết áp...</small>
                    </div>
                  </div>
                  <div class="message-item d-flex gap-2 mb-2 justify-content-end">
                    <div class="msg-bubble bg-primary text-white rounded-3 px-3 py-2">
                      <small>Mình có thể giúp bạn, để mình chia sẻ!</small>
                    </div>
                  </div>
                </div>
              </div>

              
              <div class="preview-card forum-preview bg-theme-card rounded-4 shadow p-3 border border-theme position-absolute" style="bottom: -20px; left: -20px; max-width: 280px;">
                <div class="d-flex align-items-center gap-2 mb-2">
                  <i class="bi bi-file-earmark-text text-primary"></i>
                  <span class="fw-bold text-theme-main small">Bài viết mới trên diễn đàn</span>
                </div>
                <p class="text-theme-muted small mb-2">Kinh nghiệm theo dõi huyết áp hàng ngày...</p>
                <div class="d-flex gap-3 text-theme-muted small">
                  <span><i class="bi bi-heart text-danger me-1"></i> 5</span>
                  <span><i class="bi bi-chat me-1"></i> 2</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    



    <section class="how-it-works-section py-5 bg-theme-light">
      <div class="container">
        <div class="text-center mb-5">
          <span class="badge bg-success bg-opacity-10 text-success px-3 py-2 rounded-pill mb-3">
            <i class="bi bi-signpost-split me-1"></i> Hướng dẫn
          </span>
          <h2 class="display-6 fw-bold text-theme-main mb-3">
            Bắt đầu dễ dàng
          </h2>
          <p class="text-theme-muted mx-auto" style="max-width: 600px;">
            Chỉ với vài bước đơn giản, bạn có thể quản lý toàn bộ hồ sơ sức khỏe của mình
          </p>
        </div>

        <div class="row g-4">
          <div class="col-md-4" v-for="(step, index) in howItWorks" :key="index">
            <div class="step-card text-center p-4 bg-theme-card rounded-4 shadow-sm border border-theme h-100 hover-lift">
              <div class="step-number mb-3">{{ index + 1 }}</div>
              <div class="step-icon mb-3">
                <i :class="step.icon"></i>
              </div>
              <h5 class="fw-bold text-theme-main mb-2">{{ step.title }}</h5>
              <p class="text-theme-muted small mb-0">{{ step.description }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    



    <section id="about" class="trust-section py-5 bg-theme-card">
      <div class="container">
        <div class="row align-items-center g-5">
          <div class="col-lg-5">
            <span class="badge bg-primary bg-opacity-10 text-primary px-3 py-2 rounded-pill mb-3">
              <i class="bi bi-info-circle me-1"></i> Về hệ thống
            </span>
              <h2 class="display-6 fw-bold text-theme-main mb-4">
                Hệ thống quản lý hồ sơ sức khỏe cá nhân
              </h2>
              <p class="text-theme-muted mb-4">
                Health Record System tập trung vào lưu trữ hồ sơ, theo dõi chỉ số, quản lý lịch hẹn cá nhân và các công cụ hỗ trợ AI.
              </p>
            <div class="d-flex flex-wrap gap-4">
                <div class="trust-stat">
                  <div class="h4 fw-bold text-primary mb-0"><i class="bi bi-shield-check"></i></div>
                  <small class="text-theme-muted">Xác thực JWT</small>
                </div>
                <div class="trust-stat">
                  <div class="h4 fw-bold text-success mb-0"><i class="bi bi-file-earmark-medical"></i></div>
                  <small class="text-theme-muted">Hồ sơ tập trung</small>
                </div>
                <div class="trust-stat">
                  <div class="h4 fw-bold text-info mb-0"><i class="bi bi-graph-up"></i></div>
                  <small class="text-theme-muted">Theo dõi chỉ số</small>
                </div>
            </div>
          </div>
          <div class="col-lg-7">
            <div class="row g-3">
              <div class="col-6">
                <div class="trust-card p-4 bg-theme-light rounded-4 border border-theme h-100">
                  <i class="bi bi-shield-check text-primary fs-2 mb-3 d-block"></i>
                  <h6 class="fw-bold text-theme-main">Bảo mật đăng nhập</h6>
                  <p class="text-theme-muted small mb-0">Đăng nhập và xác thực bằng cơ chế JWT</p>
                </div>
              </div>
              <div class="col-6">
                <div class="trust-card p-4 bg-theme-light rounded-4 border border-theme h-100">
                  <i class="bi bi-journal-medical text-success fs-2 mb-3 d-block"></i>
                  <h6 class="fw-bold text-theme-main">Quản lý hồ sơ</h6>
                  <p class="text-theme-muted small mb-0">Lưu và tra cứu tài liệu y tế cá nhân</p>
                </div>
              </div>
              <div class="col-6">
                <div class="trust-card p-4 bg-theme-light rounded-4 border border-theme h-100">
                  <i class="bi bi-person-lock text-warning fs-2 mb-3 d-block"></i>
                  <h6 class="fw-bold text-theme-main">Chia sẻ có phân quyền</h6>
                  <p class="text-theme-muted small mb-0">Chủ hồ sơ kiểm soát quyền xem/chỉnh sửa</p>
                </div>
              </div>
              <div class="col-6">
                <div class="trust-card p-4 bg-theme-light rounded-4 border border-theme h-100">
                  <i class="bi bi-chat-dots text-info fs-2 mb-3 d-block"></i>
                  <h6 class="fw-bold text-theme-main">Cộng đồng & trò chuyện</h6>
                  <p class="text-theme-muted small mb-0">Trao đổi kinh nghiệm qua diễn đàn và chat</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    



    <section class="cta-section py-5 bg-primary position-relative overflow-hidden">
      <div class="cta-pattern"></div>
      <div class="container position-relative z-1 text-center">
        
        <template v-if="!isAuthenticated">
          <h2 class="display-5 fw-bold text-white mb-4">
            Bắt đầu quản lý sức khỏe ngay hôm nay
          </h2>
          <p class="lead text-white-50 mb-4 mx-auto" style="max-width: 600px;">
            Đăng ký miễn phí và trải nghiệm hệ thống quản lý hồ sơ sức khỏe thông minh
          </p>
          <div class="d-flex flex-wrap justify-content-center gap-3">
            <router-link to="/register" class="btn btn-light btn-lg rounded-pill px-5 shadow hover-lift">
              <i class="bi bi-person-plus me-2"></i> Đăng ký miễn phí
            </router-link>
            <router-link to="/login" class="btn btn-outline-light btn-lg rounded-pill px-5">
              <i class="bi bi-box-arrow-in-right me-2"></i> Đăng nhập
            </router-link>
          </div>
        </template>
        
        
        <template v-else>
          <h2 class="display-5 fw-bold text-white mb-4">
            Khám phá thêm các tính năng
          </h2>
          <p class="lead text-white-50 mb-4 mx-auto" style="max-width: 600px;">
            Truy cập Dashboard để quản lý hồ sơ sức khỏe hoặc thử ngay AI dự đoán tim mạch
          </p>
          <div class="d-flex flex-wrap justify-content-center gap-3">
            <router-link to="/dashboard" class="btn btn-light btn-lg rounded-pill px-5 shadow hover-lift">
              <i class="bi bi-speedometer2 me-2"></i> Vào Dashboard
            </router-link>
            <router-link to="/ai-prediction" class="btn btn-outline-light btn-lg rounded-pill px-5">
              <i class="bi bi-heart-pulse me-2"></i> AI Dự đoán
            </router-link>
          </div>
        </template>
      </div>
    </section>

    



    <footer class="home-footer py-5">
      <div class="container">
        <div class="row g-4 footer-top pb-4">
          <div class="col-lg-5">
            <div class="d-flex align-items-center mb-3">
              <div class="brand-icon me-2 bg-primary text-white">
                <i class="bi bi-heart-pulse-fill"></i>
              </div>
              <span class="brand-text fw-bold fs-5">Health<span class="text-primary">Record</span></span>
            </div>
            <p class="text-theme-muted small mb-3">
              Hệ thống quản lý hồ sơ sức khỏe thông minh, giúp bạn theo dõi và chăm sóc sức khỏe một cách hiệu quả.
            </p>
            <div class="d-flex gap-2">
              <a href="#" class="social-link"><i class="bi bi-facebook"></i></a>
              <a href="#" class="social-link"><i class="bi bi-twitter-x"></i></a>
              <a href="#" class="social-link"><i class="bi bi-linkedin"></i></a>
              <a href="#" class="social-link"><i class="bi bi-youtube"></i></a>
            </div>
          </div>

          <div class="col-lg-3 col-6">
            <h6 class="footer-title fw-bold mb-3">Điều hướng</h6>
            <ul class="list-unstyled footer-links mb-0">
              <li><router-link to="/dashboard">Dashboard</router-link></li>
              <li><router-link to="/documents">Hồ sơ y tế</router-link></li>
              <li><router-link to="/appointments">Lịch hẹn</router-link></li>
              <li><router-link to="/forum">Cộng đồng</router-link></li>
            </ul>
          </div>

          <div class="col-lg-4 col-6">
            <h6 class="footer-title fw-bold mb-3">Liên hệ</h6>
            <ul class="list-unstyled footer-contact mb-0">
              <li><i class="bi bi-envelope me-2"></i><a href="mailto:systemhealthrecord@gmail.com">systemhealthrecord@gmail.com</a></li>
              <li><i class="bi bi-telephone me-2"></i><a href="tel:0865012797">0865012797</a></li>
              <li><i class="bi bi-geo-alt me-2"></i>TP. Cần Thơ, Việt Nam</li>
            </ul>
          </div>
        </div>

        <div class="footer-bottom d-flex flex-wrap justify-content-between align-items-center pt-3">
          <p class="text-theme-muted small mb-0">
            &copy; 2026 HealthRecord System. All rights reserved.
          </p>
          <p class="text-theme-muted small mb-0">
            Made with <i class="bi bi-heart-fill text-danger"></i> in Vietnam
          </p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useAuthStore } from '../stores/auth';




const authStore = useAuthStore();
const isAuthenticated = computed(() => authStore.isAuthenticated);





const isDarkMode = ref(false);
const isScrolled = ref(false);
const showMobileNav = ref(false);


const initTheme = () => {
  const savedTheme = localStorage.getItem('theme');
  if (savedTheme) {
    isDarkMode.value = savedTheme === 'dark';
  } else {
    isDarkMode.value = window.matchMedia('(prefers-color-scheme: dark)').matches;
  }
  applyTheme();
};

const toggleTheme = () => {
  isDarkMode.value = !isDarkMode.value;
  applyTheme();
  localStorage.setItem('theme', isDarkMode.value ? 'dark' : 'light');
};

const applyTheme = () => {
  
  if (isDarkMode.value) {
    document.documentElement.setAttribute('data-bs-theme', 'dark');
  } else {
    document.documentElement.removeAttribute('data-bs-theme');
  }
};


const handleScroll = () => {
  isScrolled.value = window.scrollY > 50;
};





const currentSlide = ref(0);
let autoSlideInterval = null;

const bannerSlides = [
  {
    image: '/images/bannerdb.jpg',
    badge: 'Hồ sơ điện tử',
    icon: 'bi bi-file-earmark-medical',
    title: 'Quản lý hồ sơ sức khỏe thông minh',
    description: 'Lưu trữ và truy cập hồ sơ y tế mọi lúc mọi nơi. Bảo mật tuyệt đối với công nghệ mã hóa tiên tiến.',
    primaryLink: '/dashboard',
    primaryIcon: 'bi bi-speedometer2',
    primaryText: 'Xem Dashboard',
    secondaryLink: '/documents',
    secondaryText: 'Kho tài liệu'
  },
  {
    image: '/images/banner1.jpg',
    badge: 'Dinh dưỡng',
    icon: 'bi bi-heart-pulse',
    title: 'Chế độ dinh dưỡng khoa học',
    description: 'Theo dõi chỉ số cơ thể và kết quả phân tích để điều chỉnh chế độ ăn phù hợp hơn.',
    primaryLink: '/body-metrics',
    primaryIcon: 'bi bi-bar-chart',
    primaryText: 'Theo dõi chỉ số',
    secondaryLink: '/ai-prediction',
    secondaryText: 'Phân tích AI'
  },
  {
    image: '/images/banner2.jpg',
    badge: 'Vận động',
    icon: 'bi bi-bicycle',
    title: 'Lối sống năng động, khỏe mạnh',
    description: 'Ghi nhận hoạt động thể chất, theo dõi tiến trình tập luyện và cải thiện sức khỏe mỗi ngày.',
    primaryLink: '/body-metrics',
    primaryIcon: 'bi bi-activity',
    primaryText: 'Chỉ số cơ thể',
    secondaryLink: '/forum',
    secondaryText: 'Cộng đồng'
  },
  {
    image: '/images/banner3.jpg',
    badge: 'Y tế hiện đại',
    icon: 'bi bi-hospital',
    title: 'Hệ thống chăm sóc sức khỏe toàn diện',
    description: 'Theo dõi và quản lý lịch hẹn khám cá nhân để chủ động nhắc nhớ chăm sóc sức khỏe.',
    primaryLink: '/appointments',
    primaryIcon: 'bi bi-calendar-check',
    primaryText: 'Quản lý lịch hẹn',
    secondaryLink: '/facilities-map',
    secondaryText: 'Cơ sở y tế'
  }
];

const nextSlide = () => {
  currentSlide.value = (currentSlide.value + 1) % bannerSlides.length;
};

const prevSlide = () => {
  currentSlide.value = (currentSlide.value - 1 + bannerSlides.length) % bannerSlides.length;
};

const goToSlide = (index) => {
  currentSlide.value = index;
};

const startAutoSlide = () => {
  autoSlideInterval = setInterval(nextSlide, 5000);
};

const stopAutoSlide = () => {
  if (autoSlideInterval) {
    clearInterval(autoSlideInterval);
  }
};





const quickStats = [
  { icon: 'bi bi-shield-lock-fill', value: 'JWT', label: 'Xác thực phiên', color: '#0d6efd' },
  { icon: 'bi bi-file-earmark-medical-fill', value: 'Hồ sơ', label: 'Tài liệu sức khỏe', color: '#198754' },
  { icon: 'bi bi-people-fill', value: 'Cộng đồng', label: 'Diễn đàn & Chat', color: '#0dcaf0' },
  { icon: 'bi bi-robot', value: 'AI', label: 'Dự đoán tim mạch', color: '#ffc107' }
];

const howItWorks = [
  {
    icon: 'bi bi-person-plus-fill text-primary',
    title: 'Đăng ký tài khoản',
    description: 'Tạo tài khoản miễn phí chỉ trong 30 giây với email của bạn.'
  },
  {
    icon: 'bi bi-upload text-success',
    title: 'Tải lên hồ sơ',
    description: 'Upload các tài liệu y tế, kết quả xét nghiệm hoặc nhập thông tin sức khỏe.'
  },
  {
    icon: 'bi bi-graph-up-arrow text-info',
    title: 'Theo dõi & Phân tích',
    description: 'Xem biểu đồ, nhận phân tích AI và theo dõi sức khỏe của bạn theo thời gian.'
  }
];





onMounted(() => {
  initTheme();
  startAutoSlide();
  window.addEventListener('scroll', handleScroll);
});

onUnmounted(() => {
  stopAutoSlide();
  window.removeEventListener('scroll', handleScroll);
});
</script>

<style scoped>




.home-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: transparent;
  transition: all 0.3s ease;
}

.home-header.scrolled {
  background: var(--header-bg, rgba(255, 255, 255, 0.95));
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.1);
}

.brand-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: linear-gradient(135deg, #0d6efd, #0dcaf0);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.2rem;
}

.brand-text {
  font-size: 1.4rem;
  color: white;
  transition: color 0.3s ease;
}

.home-header.scrolled .brand-text {
  color: var(--text-main, #212529);
}

.navbar-nav .nav-link {
  color: rgba(255, 255, 255, 0.85);
  font-weight: 500;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.home-header.scrolled .navbar-nav .nav-link {
  color: var(--text-muted, #6c757d);
}

.navbar-nav .nav-link:hover {
  background: rgba(255, 255, 255, 0.15);
  color: white;
}

.home-header.scrolled .navbar-nav .nav-link:hover {
  background: var(--bg-light, #f8f9fa);
  color: var(--text-main, #212529);
}

.navbar-toggler {
  color: white;
}

.home-header.scrolled .navbar-toggler {
  color: var(--text-main, #212529);
}

.btn-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.15);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.home-header.scrolled .btn-icon {
  background: var(--bg-light, #f8f9fa);
  color: var(--text-main, #212529);
}

.btn-icon:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: scale(1.05);
}


@media (max-width: 991.98px) {
  .navbar-collapse {
    position: fixed;
    top: 0;
    left: -100%;
    width: 80%;
    max-width: 300px;
    height: 100vh;
    background: var(--bg-card, #ffffff);
    padding: 2rem;
    transition: left 0.3s ease;
    z-index: 1100;
    box-shadow: 5px 0 30px rgba(0, 0, 0, 0.2);
  }

  .navbar-collapse.show-mobile {
    left: 0;
  }

  .navbar-collapse .nav-link {
    color: var(--text-main, #212529) !important;
    padding: 0.75rem 1rem;
    border-radius: 8px;
  }

  .navbar-collapse .nav-link:hover {
    background: var(--bg-light, #f8f9fa) !important;
  }

  .navbar-actions {
    flex-direction: column;
    margin-top: 1rem;
    padding-top: 1rem;
    border-top: 1px solid var(--border-color, #dee2e6);
  }

  .navbar-actions .btn {
    width: 100%;
    justify-content: center;
  }
}





.hero-section {
  position: relative;
  min-height: 85vh;
}

.carousel-container {
  position: relative;
  width: 100%;
  height: 85vh;
  overflow: hidden;
}

.carousel-slides {
  display: flex;
  height: 100%;
  transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.carousel-slide {
  min-width: 100%;
  height: 100%;
  position: relative;
}

.slide-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.slide-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(13, 110, 253, 0.85) 0%, rgba(0, 0, 0, 0.6) 100%);
  z-index: 1;
}

.slide-content {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 2;
  display: flex;
  align-items: center;
}

.min-vh-60 {
  min-height: 60vh;
}


.carousel-nav {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 50px;
  height: 50px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  font-size: 1.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
  z-index: 3;
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
}

.carousel-nav:hover {
  background: rgba(255, 255, 255, 0.4);
  transform: translateY(-50%) scale(1.1);
}

.carousel-prev {
  left: 20px;
}

.carousel-next {
  right: 20px;
}


.carousel-dots {
  position: absolute;
  bottom: 30px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 12px;
  z-index: 3;
}

.carousel-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.5);
  background: transparent;
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 0;
}

.carousel-dot:hover {
  background: rgba(255, 255, 255, 0.5);
}

.carousel-dot.active {
  background: white;
  border-color: white;
  transform: scale(1.2);
}


.scroll-indicator {
  position: absolute;
  bottom: 30px;
  left: 30px;
  display: flex;
  align-items: center;
  gap: 10px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.85rem;
  z-index: 3;
}

.mouse {
  width: 24px;
  height: 38px;
  border: 2px solid rgba(255, 255, 255, 0.5);
  border-radius: 12px;
  position: relative;
}

.wheel {
  width: 4px;
  height: 8px;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 2px;
  position: absolute;
  top: 6px;
  left: 50%;
  transform: translateX(-50%);
  animation: scroll-wheel 1.5s infinite;
}

@keyframes scroll-wheel {
  0% { opacity: 1; transform: translateX(-50%) translateY(0); }
  100% { opacity: 0; transform: translateX(-50%) translateY(12px); }
}


.animate-fade-down {
  animation: fadeDown 0.6s ease-out forwards;
}

.animate-fade-up {
  animation: fadeUp 0.6s ease-out 0.2s forwards;
  opacity: 0;
}

.animate-fade-up-delay {
  animation: fadeUp 0.6s ease-out 0.4s forwards;
  opacity: 0;
}

.animate-fade-up-delay-2 {
  animation: fadeUp 0.6s ease-out 0.6s forwards;
  opacity: 0;
}

@keyframes fadeDown {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes fadeUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}





.stat-icon {
  font-size: 2rem;
}

.stat-value {
  font-size: 1.5rem;
}





.feature-card {
  transition: all 0.3s ease;
}

.feature-image {
  height: 200px;
  overflow: hidden;
}

.feature-image img {
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.feature-card:hover .feature-image img {
  transform: scale(1.1);
}

.feature-card .p-4 {
  display: flex;
  flex-direction: column;
  min-height: 220px;
}

.feature-card .p-4 .btn {
  margin-top: auto;
  align-self: flex-start;
}

.feature-overlay {
  position: absolute;
  top: 12px;
  left: 12px;
  z-index: 2;
}

.hover-lift-lg {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.hover-lift-lg:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15) !important;
}


.bg-purple {
  background-color: #6f42c1 !important;
}

.text-purple {
  color: #6f42c1 !important;
}

.btn-outline-purple {
  color: #6f42c1;
  border-color: #6f42c1;
}

.btn-outline-purple:hover {
  background-color: #6f42c1;
  color: white;
}





.ai-section {
  background: linear-gradient(135deg, var(--primary-color, #0d6efd) 0%, var(--info-color, #06b6d4) 100%);
}

.ai-bg-pattern {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    radial-gradient(circle at 20% 80%, rgba(255,255,255,0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255,255,255,0.1) 0%, transparent 50%);
  pointer-events: none;
}

.ai-check-icon {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.9rem;
}

.ai-visual {
  padding: 20px;
}

.ai-card {
  backdrop-filter: blur(10px);
}

.ai-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary-color, #0d6efd), var(--info-color, #06b6d4));
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.5rem;
}

.floating-badge {
  position: absolute;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  color: var(--primary-color, #0d6efd);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  animation: float 3s ease-in-out infinite;
}

.badge-1 {
  top: 0;
  right: 20%;
  animation-delay: 0s;
}

.badge-2 {
  bottom: 20%;
  left: 0;
  animation-delay: 1s;
}

.badge-3 {
  bottom: 0;
  right: 10%;
  animation-delay: 2s;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-15px); }
}





.step-number {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #0d6efd, #0a58ca);
  color: white;
  font-weight: bold;
  font-size: 1.2rem;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.step-icon {
  font-size: 2.5rem;
}





.community-icon {
  min-width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.community-preview {
  position: relative;
  padding: 20px;
}

.preview-card.chat-preview {
  position: relative;
  z-index: 2;
}

.preview-card.forum-preview {
  z-index: 3;
}

@media (max-width: 991.98px) {
  .preview-card.forum-preview {
    position: relative !important;
    bottom: auto !important;
    left: auto !important;
    max-width: 100% !important;
    margin-top: 1rem;
  }
  
  .community-preview {
    padding: 0;
  }
}





.cta-pattern {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    radial-gradient(circle at 10% 20%, rgba(255,255,255,0.1) 0%, transparent 40%),
    radial-gradient(circle at 90% 80%, rgba(255,255,255,0.1) 0%, transparent 40%);
  pointer-events: none;
}





.hover-lift {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.hover-lift:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
}

.backdrop-blur {
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.z-1 { z-index: 1; }
.z-2 { z-index: 2; }
.z-3 { z-index: 3; }





.bg-theme-card {
  background-color: var(--bg-card, #ffffff);
}

.bg-theme-light {
  background-color: var(--bg-light, #f8f9fa);
}

.text-theme-main {
  color: var(--text-main, #212529);
}

.text-theme-muted {
  color: var(--text-muted, #6c757d);
}

.border-theme {
  border-color: var(--border-color, #dee2e6) !important;
}





@media (max-width: 991.98px) {
  .hero-section,
  .carousel-container {
    min-height: 70vh;
    height: 70vh;
  }

  .slide-content h1 {
    font-size: 2rem;
  }

  .carousel-nav {
    width: 40px;
    height: 40px;
    font-size: 1.2rem;
  }

  .carousel-prev {
    left: 10px;
  }

  .carousel-next {
    right: 10px;
  }

  .scroll-indicator {
    display: none;
  }

  .ai-visual {
    margin-top: 2rem;
  }

  .floating-badge {
    display: none;
  }
}

@media (max-width: 767.98px) {
  .hero-section,
  .carousel-container {
    min-height: 52vh;
    height: 52vh;
  }

  .slide-content h1 {
    font-size: 1.5rem;
  }

  .slide-content .lead {
    font-size: 0.9rem;
  }

  .slide-content .btn {
    padding: 0.4rem 0.8rem;
    font-size: 0.85rem;
  }

  .home-page .py-5 {
    padding-top: 2rem !important;
    padding-bottom: 2rem !important;
  }

  .home-page .py-4 {
    padding-top: 1rem !important;
    padding-bottom: 1rem !important;
  }

  .feature-image {
    height: 120px;
  }

  .feature-card .p-4 {
    padding: 0.85rem !important;
    min-height: auto;
  }

  .features-section .feature-card p {
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .stat-icon {
    font-size: 1.3rem;
  }

  .stat-value {
    font-size: 1rem;
  }

  .stat-label {
    font-size: 0.7rem;
  }

  
  .community-icon {
    min-width: 40px;
    height: 40px;
  }

  .community-icon i {
    font-size: 1rem !important;
  }

  
  .display-6 {
    font-size: 1.5rem;
  }

  
  .d-flex.flex-wrap.gap-3 .btn {
    width: 100%;
    justify-content: center;
  }

  
  .ai-card {
    padding: 1rem !important;
  }

  .ai-visual {
    display: none;
  }

  
  .trust-stat {
    text-align: center;
  }

  .community-preview {
    display: none;
  }
}






[data-bs-theme="dark"] .home-header.scrolled {
  background: rgba(30, 41, 59, 0.95);
}

[data-bs-theme="dark"] .home-header.scrolled .brand-text {
  color: var(--text-main, #e2e8f0);
}

[data-bs-theme="dark"] .home-header.scrolled .navbar-nav .nav-link {
  color: var(--text-muted, #94a3b8);
}

[data-bs-theme="dark"] .home-header.scrolled .navbar-nav .nav-link:hover {
  background: var(--hover-bg, #334155);
  color: var(--text-main, #e2e8f0);
}

[data-bs-theme="dark"] .home-header.scrolled .btn-icon {
  background: var(--bg-light, #1e293b);
  color: var(--text-main, #e2e8f0);
}

[data-bs-theme="dark"] .slide-overlay {
  background: linear-gradient(135deg, rgba(13, 110, 253, 0.9) 0%, rgba(0, 0, 0, 0.85) 100%);
}

[data-bs-theme="dark"] .ai-section {
  background: linear-gradient(135deg, var(--primary-color, #3b82f6) 0%, var(--info-color, #06b6d4) 100%);
}

[data-bs-theme="dark"] .floating-badge {
  background: var(--bg-card, #1e293b);
  color: var(--primary-color, #3b82f6);
}

[data-bs-theme="dark"] .trust-card {
  background-color: var(--bg-card, #1e293b);
  border-color: var(--border-color, #334155);
}

[data-bs-theme="dark"] .step-card {
  background-color: var(--bg-card, #1e293b);
  border-color: var(--border-color, #334155);
}

[data-bs-theme="dark"] .feature-card {
  background-color: var(--bg-card, #1e293b);
  border-color: var(--border-color, #334155);
}

[data-bs-theme="dark"] .cta-section {
  background: linear-gradient(135deg, var(--primary-color, #3b82f6) 0%, var(--info-color, #06b6d4) 100%);
}

[data-bs-theme="dark"] .features-section,
[data-bs-theme="dark"] .how-it-works-section,
[data-bs-theme="dark"] .trust-section {
  background-color: var(--bg-main, #0f172a);
}





.home-footer {
  background: linear-gradient(180deg, var(--bg-card, #ffffff) 0%, var(--bg-main, #f1f5f9) 100%);
  color: var(--text-main, #1e293b);
  border-top: 1px solid var(--border-color, #e2e8f0);
}

.footer-top {
  border-bottom: 1px solid var(--border-color, #e2e8f0);
}

.footer-title {
  color: var(--text-main, #1e293b);
  font-size: 0.95rem;
}

.social-link {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: var(--bg-light, #f8fafc);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--text-main, #1e293b);
  transition: all 0.3s ease;
  text-decoration: none;
}

.social-link:hover {
  background: var(--primary-color, #0d6efd);
  color: #fff;
  transform: translateY(-3px);
}

.footer-links li {
  margin-bottom: 0.5rem;
}

.footer-links a {
  color: var(--text-muted, #64748b);
  text-decoration: none;
  font-size: 0.9rem;
  transition: color 0.3s ease;
}

.footer-links a:hover {
  color: var(--primary-color, #0d6efd);
}

.footer-contact li {
  color: var(--text-muted, #64748b);
  font-size: 0.85rem;
  margin-bottom: 0.65rem;
  display: flex;
  align-items: center;
}

.footer-contact li:last-child {
  margin-bottom: 0;
}

.footer-contact a {
  color: var(--text-muted, #64748b);
  text-decoration: none;
  transition: color 0.3s ease;
}

.footer-contact a:hover {
  color: var(--primary-color, #0d6efd);
}

@media (max-width: 767.98px) {
  .footer-bottom {
    gap: 0.5rem;
  }
}
</style>
