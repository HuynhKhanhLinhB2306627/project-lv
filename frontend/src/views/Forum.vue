<template>
  <div class="forum-modern-wrapper bg-theme-light min-vh-100">
    
    <div class="d-lg-none bg-theme-card border-bottom border-theme sticky-top z-3 p-2 px-3">
      <div class="d-flex justify-content-between align-items-center">
        <h5 class="fw-bold text-primary mb-0">Cộng đồng</h5>
        <button class="btn btn-light bg-theme-light text-theme-main border-theme rounded-circle" @click="showMobileMenu = !showMobileMenu">
          <i class="bi bi-list fs-4"></i>
        </button>
      </div>
    </div>

    <div class="container-fluid py-4 px-2 px-md-4 max-w-1400">
      <div class="row g-4 justify-content-center">
        
        
        <div class="col-xl-3 col-lg-3 d-none d-lg-block">
          <div class="sticky-top" style="top: 0px;">
            <div class="nav-card bg-theme-card rounded-4 shadow-sm p-2 mb-4 border border-theme">
              <div class="p-3">
                <h6 class="text-uppercase small fw-bold text-theme-muted tracking-wider mb-0">Menu chính</h6>
              </div>
              <div class="d-flex flex-column gap-1">
                <button class="menu-item text-theme-main" :class="{ active: activeCategory === 0 && !viewingSaved }" @click="viewAllPosts">
                  <i class="bi bi-house-door-fill text-primary"></i> <span>Bảng tin sức khỏe</span>
                </button>
                <button class="menu-item text-theme-main" :class="{ active: viewingSaved }" @click="fetchSavedPosts">
                  <i class="bi bi-bookmark-fill text-warning"></i> <span>Mục đã lưu</span>
                </button>
                <button class="menu-item text-theme-main" :class="{ active: activeCategory === 999 && !viewingSaved }" @click="filterByCategory(999)">
                  <i class="bi bi-person-fill-lock text-info"></i> <span>Chỉ mình tôi</span>
                </button>
              </div>
              
              <div class="p-3 mt-2">
                <h6 class="text-uppercase small fw-bold text-theme-muted tracking-wider mb-0">Chủ đề phổ biến</h6>
              </div>
              <div class="d-flex flex-column gap-1 overflow-auto custom-scrollbar" style="max-height: 300px;">
                <button v-for="cat in categories" :key="cat.id" 
                        class="menu-item text-theme-main" :class="{ active: activeCategory === cat.id && !viewingSaved }" 
                        @click="filterByCategory(cat.id)">
                  <div class="cat-icon-circle bg-theme-light text-theme-muted">{{ cat.name.charAt(0) }}</div>
                  <span>{{ cat.name }}</span>
                </button>
              </div>
            </div>
          </div>
        </div>

        
        <div class="col-xl-6 col-lg-7 col-md-10">
          
          <div class="card bg-theme-card border border-theme shadow-sm rounded-4 mb-4 p-3 create-post-card" v-if="!viewingSaved">
            <div class="d-flex align-items-center gap-3 px-2">
              <img :src="`https://ui-avatars.com/api/?name=${userName}&background=0d6efd&color=fff&bold=true`" class="rounded-circle border border-2 border-theme shadow-sm" width="42" height="42">
              <div class="fake-input-modern flex-grow-1 bg-theme-light text-theme-muted border border-theme" @click="openCreateModal()">
                {{ userName }} ơi, bạn đang nghĩ gì?
              </div>
            </div>
            <hr class="my-3 border-theme opacity-50">
            <div class="d-flex justify-content-between px-2">
              <button class="post-tool-btn text-theme-muted" @click="openCreateModal()">
                <i class="bi bi-images text-success"></i> <span>Ảnh & Video</span>
              </button>
              <button class="post-tool-btn text-theme-muted" @click="openCreateModal()">
                <i class="bi bi-question-circle-fill text-primary"></i> <span>Hỏi đáp y khoa</span>
              </button>
              <button class="post-tool-btn text-theme-muted" @click="openCreateModal()">
                <i class="bi bi-heart-pulse-fill text-danger"></i> <span>Chia sẻ kinh nghiệm</span>
              </button>
            </div>
          </div>

          
          <div v-if="loading" class="text-center py-5">
            <div class="spinner-grow text-primary" role="status"></div>
            <p class="text-theme-muted mt-3 fw-medium">Đang cập nhật bảng tin...</p>
          </div>

          
          <div v-else-if="posts.length === 0" class="text-center py-5 bg-theme-card rounded-4 shadow-sm border border-theme">
            <i class="bi bi-journal-x display-1 text-theme-muted opacity-25"></i>
            <h5 class="mt-3 text-theme-muted">Chưa có bài viết nào ở đây</h5>
            <button class="btn btn-primary rounded-pill mt-2 fw-bold" @click="viewAllPosts">Quay lại trang chủ</button>
          </div>

          
          <TransitionGroup v-else name="list" tag="div">
            <div v-for="post in posts" :key="post.id" class="card bg-theme-card border border-theme shadow-sm rounded-4 mb-4 modern-post-card overflow-hidden">
              
              <div class="p-3 pb-2 d-flex justify-content-between align-items-center">
                <div class="d-flex align-items-center gap-2">
                  <div class="position-relative flex-shrink-0">
                    <img :src="`https://ui-avatars.com/api/?name=${post.author}&background=random&color=fff&bold=true`" class="rounded-circle border border-theme" width="44" height="42">
                    <div class="active-dot border border-theme"></div>
                  </div>
                    <div class="min-width-0 flex-grow-1">
                    <h6 class="mb-0 fw-bold d-flex align-items-center gap-2 text-theme-main">
                      <span class="text-truncate" style="max-width: 150px;">{{ post.author }}</span>
                      <i class="bi bi-patch-check-fill text-primary flex-shrink-0" style="font-size: 0.8rem;"></i>
                      <button v-if="post.authorId !== currentUserId" 
                              class="btn btn-sm btn-light bg-theme-light text-primary border-theme rounded-pill px-2 py-0 flex-shrink-0" 
                              style="font-size: 0.7rem; font-weight: 700;"
                              @click.stop="startChatWithAuthor(post)" 
                              title="Nhắn tin cho người đăng">
                        <i class="bi bi-chat-dots-fill me-1"></i>Chat
                      </button>
                    </h6>
                    <div class="d-flex align-items-center gap-1 text-theme-muted" style="font-size: 0.75rem;">
                      <span>{{ post.date }}</span>
                      <i class="bi bi-dot"></i>
                      
                      <span v-if="post.status === 'PENDING'" class="text-danger fw-bold"><i class="bi bi-hourglass-split"></i> Chờ duyệt</span>
                      <span v-else-if="post.status === 'REJECTED'" class="text-danger fw-bold"><i class="bi bi-x-circle"></i> Bị từ chối</span>
                      <template v-else>
                        <i v-if="post.isPublic" class="bi bi-globe-americas" title="Công khai"></i>
                        <i v-else class="bi bi-lock-fill text-warning" title="Chỉ mình tôi"></i>
                      </template>
                      <i class="bi bi-dot"></i>
                      <span class="ms-1 fw-medium text-primary bg-primary bg-opacity-10 px-2 rounded-pill border border-primary border-opacity-10">{{ post.category }}</span>
                    </div>
                  </div>
                </div>
                <div class="dropdown">
                  <button class="btn btn-light bg-theme-light text-theme-main border-theme rounded-circle shadow-none p-0 d-flex align-items-center justify-content-center" style="width: 32px; height: 32px;" data-bs-toggle="dropdown">
                    <i class="bi bi-three-dots fs-5"></i>
                  </button>
                  <ul class="dropdown-menu dropdown-menu-end shadow-lg border border-theme p-2 rounded-3 bg-theme-card">
                    <li><button class="dropdown-item rounded-2 text-theme-main" @click="toggleSave(post)"><i :class="post.isSaved ? 'bi-bookmark-fill text-warning' : 'bi-bookmark'"></i> {{ post.isSaved ? 'Bỏ lưu' : 'Lưu bài viết' }}</button></li>
                    <template v-if="post.authorId === currentUserId">
                      <li><button class="dropdown-item rounded-2 text-theme-main" @click="openEditModal(post)"><i class="bi bi-pencil-square me-2 text-primary"></i> Sửa bài</button></li>
                      <li><button class="dropdown-item rounded-2 text-danger" @click="handleDeletePost(post.id)"><i class="bi bi-trash3-fill me-2"></i> Xóa bài</button></li>
                    </template>
                    <li><button class="dropdown-item rounded-2 text-theme-main" @click="openReportModal(post)"><i class="bi bi-flag-fill me-2"></i> Báo cáo</button></li>
                  </ul>
                </div>
              </div>

              
              <div class="px-3 pb-3">
                <h5 class="fw-bold mb-2 text-theme-main mt-1 text-break-word" style="font-size: 1.1rem; line-height: 1.4;">{{ post.title }}</h5>
                <p class="text-theme-main mb-0 post-content-text text-break-word" style="line-height: 1.5; white-space: pre-line;">{{ post.content }}</p>
              </div>

              
              <div v-if="post.media" class="post-media-area bg-dark overflow-hidden position-relative" @click="openPostDetail(post)">
                <template v-if="isVideoMedia(post.media)">
                  <div class="video-wrapper">
                    <video :src="mediaUrl(post.media)" controls class="w-100" style="max-height: 600px; display: block;"></video>
                  </div>
                </template>
                <img v-else :src="mediaUrl(post.media)" class="w-100 d-block" style="max-height: 600px; object-fit: contain;">
              </div>

              
              <div class="px-3 py-2 d-flex justify-content-between align-items-center border-bottom border-theme mx-2 mt-1">
                <div class="d-flex align-items-center gap-1 position-relative like-container">
                  <div class="like-icons d-flex align-items-center me-1">
                    <div class="icon-circle bg-primary"><i class="bi bi-hand-thumbs-up-fill text-white"></i></div>
                    <div class="icon-circle bg-danger ms-n1"><i class="bi bi-heart-fill text-white"></i></div>
                  </div>
                  <span class="small text-theme-muted fw-medium cursor-pointer like-stat-text">{{ post.likes }} lượt thích</span>
                  
                  
                  <div v-if="post.likedByNames && post.likedByNames.length > 0" class="like-tooltip shadow-lg rounded-3 p-2 border border-theme">
                    <div class="fw-bold border-bottom border-theme border-opacity-25 pb-1 mb-1" style="font-size: 0.7rem;">Đã thích bởi:</div>
                    <div v-for="name in post.likedByNames.slice(0, 10)" :key="name" class="extra-small py-1">{{ name }}</div>
                    <div v-if="post.likedByNames.length > 10" class="extra-small text-theme-muted mt-1">... và {{ post.likedByNames.length - 10 }} người khác</div>
                  </div>
                </div>
                <div class="small text-theme-muted fw-medium d-flex gap-2">
                  <span>{{ post.commentsCount }} bình luận</span>
                  <span>{{ post.views }} lượt xem</span>
                </div>
              </div>

              
              <div class="p-1 d-flex gap-1 mx-2">
                <button class="flex-fill btn-interaction text-theme-muted" :class="{ 'active': post.isLiked }" @click="toggleLike(post)">
                  <i :class="post.isLiked ? 'bi-hand-thumbs-up-fill' : 'bi-hand-thumbs-up'"></i> Thích
                </button>
                <button class="flex-fill btn-interaction text-theme-muted" @click="openPostDetail(post)">
                  <i class="bi bi-chat-square-text"></i> Bình luận
                </button>
                <button class="flex-fill btn-interaction text-theme-muted">
                  <i class="bi bi-share"></i> Chia sẻ
                </button>
              </div>
            </div>
          </TransitionGroup>
        </div>

        
        <div class="col-xl-3 col-lg-2 d-none d-xl-block">
          <div class="sticky-top" style="top: 0px;">
            <div class="card bg-theme-card border border-theme shadow-sm rounded-4 p-3 mb-4">
              <h6 class="fw-bold mb-3 d-flex align-items-center gap-2 text-theme-main">
                <i class="bi bi-graph-up-arrow text-danger"></i> Xu hướng sức khỏe
              </h6>
              <div class="d-flex flex-wrap gap-2">
                <span class="trending-tag bg-theme-light text-theme-main border border-theme">#DinhDuongThanhDam</span>
                <span class="trending-tag bg-theme-light text-theme-main border border-theme">#BaiTapTimMach</span>
                <span class="trending-tag bg-theme-light text-theme-main border border-theme">#YogaGiamCangThang</span>
                <span class="trending-tag bg-theme-light text-theme-main border border-theme">#MeoNguNgon</span>
              </div>
            </div>

            <div class="card bg-theme-card border border-theme shadow-sm rounded-4 overflow-hidden">
              <div class="p-3 border-bottom border-theme d-flex justify-content-between align-items-center">
                <h6 class="fw-bold mb-0 text-theme-main">Chuyên gia tư vấn</h6>
                <a href="#" class="small text-decoration-none fw-bold text-primary">Xem tất cả</a>
              </div>
              <div class="p-2">
                <div v-for="dr in topDoctors" :key="dr.name" class="doctor-item-modern d-flex align-items-center gap-3 p-2 rounded-3 mb-1 cursor-pointer hover-bg" @click="startChat(dr)">
                  <img :src="`https://ui-avatars.com/api/?name=${dr.name}&background=00a3bf&color=fff&bold=true`" class="rounded-circle shadow-sm border border-theme" width="44" height="44">
                  <div class="flex-grow-1 overflow-hidden">
                    <div class="fw-bold text-theme-main small text-truncate">{{ dr.name }}</div>
                    <div class="text-theme-muted extra-small text-truncate">{{ dr.specialty }}</div>
                  </div>
                  <div class="btn-chat-mini"><i class="bi bi-chat-text-fill"></i></div>
                </div>
              </div>
            </div>
            
            <div class="mt-4 px-3">
              <div class="d-flex flex-wrap gap-2 small text-theme-muted">
                <span>Quyền riêng tư</span><span>•</span>
                <span>Điều khoản</span><span>•</span>
                <span>Quảng cáo</span><span>•</span>
                <span>Gemini Health © 2026</span>
              </div>
            </div>
          </div>
        </div>

      </div>
    </div>

    
    <div v-if="showCreateModal" class="modal-overlay d-flex align-items-center justify-content-center p-2" @click.self="showCreateModal = false">
      <div class="card bg-theme-card border border-theme shadow-lg rounded-4 w-100 overflow-hidden" style="max-width: 550px; max-height: 95vh;">
        <div class="p-3 border-bottom border-theme d-flex align-items-center position-relative">
          <h5 class="fw-bold mb-0 text-center flex-grow-1 text-theme-main">{{ isEditMode ? 'Chỉnh sửa bài viết' : 'Tạo bài viết mới' }}</h5>
          <button class="btn btn-light bg-theme-light text-theme-main border-theme rounded-circle position-absolute end-0 me-3 shadow-sm" style="width: 36px; height: 36px;" @click="showCreateModal = false">
            <i class="bi bi-x-lg"></i>
          </button>
        </div>
        
        <div class="p-3 overflow-auto custom-scrollbar bg-theme-card">
          <div class="d-flex align-items-center justify-content-between mb-3">
            <div class="d-flex align-items-center gap-2">
              <img :src="`https://ui-avatars.com/api/?name=${userName}&background=0d6efd&color=fff&bold=true`" class="rounded-circle border border-theme" width="45" height="45">
              <div>
                <h6 class="fw-bold mb-0 text-theme-main">{{ userName }}</h6>
                <div class="dropdown">
                  <button class="btn btn-light bg-theme-light text-theme-main border-theme btn-sm rounded-pill py-0 px-2 small mt-1 d-flex align-items-center gap-1 border" data-bs-toggle="dropdown">
                    <i v-if="newPost.isPublic" class="bi bi-globe-americas text-primary"></i>
                    <i v-else class="bi bi-lock-fill text-warning"></i>
                    {{ newPost.isPublic ? 'Công khai' : 'Chỉ mình tôi' }}
                    <i class="bi bi-caret-down-fill" style="font-size: 0.6rem;"></i>
                  </button>
                  <ul class="dropdown-menu shadow-lg border border-theme bg-theme-card">
                    <li><a class="dropdown-item py-2 small text-theme-main" href="#" @click.prevent="newPost.isPublic = true"><i class="bi bi-globe-americas me-2 text-primary"></i> Công khai</a></li>
                    <li><a class="dropdown-item py-2 small text-theme-main" href="#" @click.prevent="newPost.isPublic = false"><i class="bi bi-lock-fill me-2 text-warning"></i> Chỉ mình tôi</a></li>
                  </ul>
                </div>
              </div>
            </div>


          </div>

          <select v-model="newPost.categoryId" class="form-select border border-theme bg-theme-light rounded-3 mb-3 fw-bold text-primary shadow-sm">
            <option :value="null" disabled>-- Chọn chủ đề bài viết --</option>
            <option v-for="cat in categories" :key="cat.id" :value="cat.id" class="bg-theme-card">{{ cat.name }}</option>
          </select>
          
          <input type="text" v-model="newPost.title" class="form-control bg-transparent text-theme-main border-0 fw-bold fs-5 mb-2 shadow-none px-0" placeholder="Tiêu đề bài viết...">
          <textarea v-model="newPost.content" class="form-control bg-transparent text-theme-main border-0 shadow-none px-0" rows="5" style="resize:none; font-size: 1.1rem;" :placeholder="`${userName} ơi, bạn đang nghĩ gì?`"></textarea>
          
          <div v-if="newPost.imagePreview" class="position-relative mt-3 rounded-4 overflow-hidden border border-theme shadow-sm bg-black">
            <template v-if="isVideoMedia(newPost.imagePreview) || (newPost.image && newPost.image.type.startsWith('video'))">
              <video :src="mediaUrl(newPost.imagePreview)" class="w-100 d-block" style="max-height: 350px;"></video>
            </template>
            <img v-else :src="mediaUrl(newPost.imagePreview)" class="w-100 d-block" style="max-height: 350px; object-fit: contain;">
            <button class="btn btn-dark rounded-circle position-absolute top-0 end-0 m-2 opacity-75 shadow" style="width: 30px; height: 30px; padding: 0; border: none;" @click="removeImage">
              <i class="bi bi-x"></i>
            </button>
          </div>
        </div>
        
        <div class="p-3 border-top border-theme bg-theme-card">
          <div class="d-flex justify-content-between align-items-center border border-theme bg-theme-light rounded-4 p-2 px-3 mb-3 shadow-sm">
            <span class="fw-bold small text-theme-main">Thêm vào bài viết</span>
            <div class="d-flex gap-3">
              <label class="mb-0 cursor-pointer text-success hover-scale"><i class="bi bi-images fs-4"></i><input type="file" class="d-none" @change="handleImageUpload" accept="image/*,video/*"></label>
              <div class="text-primary cursor-pointer hover-scale"><i class="bi bi-person-plus-fill fs-4"></i></div>
              <div class="text-warning cursor-pointer hover-scale"><i class="bi bi-emoji-smile-fill fs-4"></i></div>
            </div>
          </div>
          <button class="btn btn-primary w-100 fw-bold py-2 rounded-3 shadow-sm btn-lg" 
                  :disabled="!newPost.title.trim() || !newPost.content.trim() || !newPost.categoryId || createLoading"
                  @click="submitPost">
            <span v-if="createLoading" class="spinner-border spinner-border-sm me-2"></span> {{ isEditMode ? 'Cập nhật bài viết' : 'Đăng bài' }}
          </button>
        </div>
      </div>
    </div>

    
    <div v-if="selectedPost" class="modal-overlay d-flex align-items-center justify-content-center" @click.self="closePostDetail">
      <div class="card border border-theme shadow-lg w-100 h-100 overflow-hidden bg-theme-card modern-detail-container" style="max-width: 1200px; max-height: 90vh;">
        <button class="btn btn-dark rounded-circle position-absolute z-3 shadow m-3 end-0 border border-white border-opacity-25" style="width: 36px; height: 36px; border: none; background: rgba(0,0,0,0.5);" @click="closePostDetail">
          <i class="bi bi-x-lg"></i>
        </button>

        <div class="row g-0 h-100">
          
          <div class="col-lg-8 bg-black d-flex align-items-center justify-content-center media-section-full" :class="{ 'd-none': !selectedPost.media }">
            <template v-if="selectedPost.media">
              <video v-if="isVideoMedia(selectedPost.media)" :src="mediaUrl(selectedPost.media)" controls class="w-100 h-100" style="max-height: 90vh;"></video>
              <img v-else :src="mediaUrl(selectedPost.media)" class="w-100 h-100" style="object-fit: contain;">
            </template>
          </div>
          
          
          <div class="col-lg d-flex flex-column bg-theme-card h-100 overflow-hidden">
            <div class="p-3 border-bottom border-theme d-flex align-items-center gap-3">
              <img :src="`https://ui-avatars.com/api/?name=${selectedPost.author}&background=random&color=fff&bold=true`" class="rounded-circle border border-theme flex-shrink-0" width="40" height="40">
              <div class="min-width-0 flex-grow-1">
                <h6 class="mb-0 fw-bold text-theme-main d-flex align-items-center gap-1">
                  <span class="text-truncate" style="max-width: 200px;">{{ selectedPost.author }}</span>
                  <i class="bi bi-patch-check-fill text-primary small flex-shrink-0"></i>
                </h6>
                <div class="text-theme-muted extra-small text-truncate">
                  {{ selectedPost.date }} • {{ selectedPost.category }} •
                  <i v-if="selectedPost.isPublic" class="bi bi-globe-americas ms-1"></i>
                  <i v-else class="bi bi-lock-fill text-warning ms-1"></i>
                </div>
              </div>
            </div>
            
            <div class="flex-grow-1 overflow-auto custom-scrollbar bg-theme-card">
              <div class="p-3 border-bottom border-theme">
                <h5 class="fw-bold mb-2 text-theme-main text-break-word">{{ selectedPost.title }}</h5>
                <p class="mb-0 text-theme-main text-break-word" style="white-space: pre-line; line-height: 1.6;">{{ selectedPost.content }}</p>
                
                <div class="d-flex align-items-center gap-4 mt-4 pt-2 border-top border-theme">
                  <div class="interaction-stat text-theme-muted" :class="{'text-primary fw-bold': selectedPost.isLiked}" @click="toggleLike(selectedPost)">
                    <i :class="selectedPost.isLiked ? 'bi-hand-thumbs-up-fill text-primary' : 'bi-hand-thumbs-up'"></i>
                    <span>{{ selectedPost.likes }} Thích</span>
                  </div>
                  <div class="interaction-stat text-theme-muted">
                    <i class="bi bi-chat-square-text"></i>
                    <span>{{ selectedPost.commentsCount }} Bình luận</span>
                  </div>
                </div>
              </div>

              
              <div class="p-3 bg-theme-light min-vh-50">
                <div v-if="commentsLoading" class="text-center py-4"><div class="spinner-border text-primary border-2"></div></div>
                <div v-else-if="postComments.length === 0" class="text-center py-5 opacity-50">
                  <i class="bi bi-chat-quote display-4 text-theme-muted"></i>
                  <p class="small mt-2 text-theme-muted">Chưa có bình luận nào.</p>
                </div>
                
                <div v-for="cmt in postComments" :key="cmt.id" class="comment-item mb-3 d-flex gap-2">
                  <img :src="`https://ui-avatars.com/api/?name=${cmt.authorName}&background=random&color=fff&bold=true`" class="rounded-circle border border-theme" width="32" height="32">
                  <div class="flex-grow-1">
                    <div class="comment-bubble bg-theme-card border border-theme shadow-sm">
                      <div class="d-flex justify-content-between align-items-start gap-2">
                        <div class="fw-bold small text-theme-main text-truncate" style="max-width: 150px;">{{ cmt.authorName }}</div>
                        <button v-if="cmt.authorId === currentUserId" 
                                class="btn btn-sm btn-link text-danger p-0 flex-shrink-0" 
                                @click="handleDeleteComment(cmt.id)" 
                                title="Xóa bình luận">
                          <i class="bi bi-trash3-fill"></i>
                        </button>
                      </div>
                      <div class="comment-text text-theme-main">{{ cmt.content }}</div>
                    </div>
                    <div class="d-flex gap-3 mt-1 ms-2 extra-small text-theme-muted fw-bold">
                      <span>{{ cmt.createdAt }}</span>
                      <span class="cursor-pointer hover-text-primary" @click="replyTo(cmt)">Phản hồi</span>
                    </div>
                    
                    
                    <div v-if="cmt.replies && cmt.replies.length > 0" class="ms-3 mt-2 ps-2 border-start border-theme border-2">
                      <div v-for="reply in cmt.replies" :key="reply.id" class="d-flex gap-2 mb-2">
                        <img :src="`https://ui-avatars.com/api/?name=${reply.authorName}&background=random&color=fff&bold=true`" class="rounded-circle border border-theme flex-shrink-0" width="24" height="24">
                        <div class="comment-bubble bg-theme-card border border-theme shadow-sm py-1 px-2 flex-grow-1 min-width-0">
                          <div class="d-flex justify-content-between align-items-start gap-2">
                            <span class="fw-bold extra-small text-theme-main text-truncate" style="max-width: 120px;">{{ reply.authorName }}</span>
                            <button v-if="reply.authorId === currentUserId" 
                                    class="btn btn-sm btn-link text-danger p-0 flex-shrink-0" 
                                    @click="handleDeleteComment(reply.id)" 
                                    title="Xóa phản hồi"
                                    style="font-size: 0.7rem;">
                              <i class="bi bi-trash3-fill"></i>
                            </button>
                          </div>
                          <div class="extra-small text-theme-main comment-text">{{ reply.content }}</div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            
            <div class="p-3 border-top border-theme shadow-sm bg-theme-card">
              <div v-if="replyingTo" class="reply-hint d-flex justify-content-between align-items-center mb-2 px-2 text-theme-muted">
                <span class="extra-small">Đang phản hồi <strong class="text-theme-main">{{ replyingTo.authorName }}</strong></span>
                <button class="btn-close" :class="{'btn-close-white': isDarkMode}" style="font-size: 0.5rem;" @click="replyingTo = null"></button>
              </div>
              <div class="input-group">
                <input type="text" class="form-control rounded-pill-start border border-theme bg-theme-light text-theme-main shadow-none" 
                       v-model="commentText" placeholder="Viết bình luận..." @keyup.enter="handleComment" ref="commentInputRef" />
                <button class="btn btn-primary rounded-pill-end border-0 px-3 shadow-sm" @click="handleComment" :disabled="!commentText.trim() || commentLoading">
                  <i class="bi bi-send-fill"></i>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    
    <div v-if="reportModalPost" class="modal-overlay d-flex align-items-center justify-content-center p-3" @click.self="reportModalPost = null">
      <div class="card bg-theme-card border border-theme shadow-lg rounded-4 p-4 w-100 shadow-xl" style="max-width: 400px;">
        <h5 class="fw-bold mb-1 text-theme-main">Báo cáo bài viết</h5>
        <p class="small text-theme-muted mb-4">Hãy giúp chúng tôi hiểu chuyện gì đang xảy ra.</p>
        <div class="d-flex flex-column gap-2 mb-4">
          <button v-for="r in reportReasons" :key="r" 
                  class="btn btn-outline-theme text-theme-main text-start border border-theme rounded-3 p-3 small shadow-sm" 
                  :class="{'active-report': selectedReportReason === r}"
                  @click="selectedReportReason = r">
            {{ r }}
          </button>
        </div>
        <div class="d-flex gap-2">
          <button class="btn btn-light bg-theme-light text-theme-main border-theme flex-fill rounded-pill fw-bold" @click="reportModalPost = null">Hủy</button>
          <button class="btn btn-danger flex-fill rounded-pill fw-bold shadow-sm" :disabled="!selectedReportReason" @click="submitReport">Gửi</button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted, reactive, nextTick, computed } from 'vue';
import { useRoute } from 'vue-router';
import axios from 'axios';
import { toast } from '../utils/toast';

const route = useRoute();
const userName = ref('Người dùng');


const mediaUrl = (media) => {
  if (!media) return '';
  if (media.startsWith('http') || media.startsWith('data:')) return media;
  const baseUrl = axios.defaults.baseURL || '';
  if (media.startsWith('/uploads/')) return baseUrl + media;
  return `${baseUrl}/uploads/${media}`;
};

const isVideoMedia = (media) => {
  if (!media) return false;
  const normalized = media.toLowerCase();
  return normalized.includes('.mp4') || normalized.includes('video/upload');
};

const categories = ref([]);
const activeCategory = ref(0);
const viewingSaved = ref(false);
const posts = ref([]);
const loading = ref(true);
const createLoading = ref(false);

const showCreateModal = ref(false);
const selectedPost = ref(null);
const postComments = ref([]);
const commentsLoading = ref(false);
const commentText = ref('');
const commentLoading = ref(false);
const replyingTo = ref(null);
const commentInputRef = ref(null);
const showMobileMenu = ref(false);

const isEditMode = ref(false);
const editingPostId = ref(null);
const removeImageOnEdit = ref(false);

const newPost = reactive({ 
  title: '', 
  content: '', 
  categoryId: null, 
  image: null, 
  imagePreview: null,
  isPublic: true 
});

const isDarkMode = computed(() => document.documentElement.getAttribute('data-bs-theme') === 'dark');

const reportModalPost = ref(null);
const selectedReportReason = ref('');
const reportReasons = [
  'Nội dung không chính xác về y tế',
  'Thông tin sai lệch hoặc gây hiểu lầm',
  'Ngôn từ gây thù ghét hoặc xúc phạm',
  'Quảng cáo, spam không phù hợp',
  'Lý do khác'
];

const topDoctors = [ 
  { name: 'Dr. Lê Trần', specialty: 'Chuyên khoa Tim mạch' }, 
  { name: 'Dr. Phạm Hùng', specialty: 'Chuyên gia Dinh dưỡng' },
  { name: 'Dr. Nguyễn Anh', specialty: 'Nội tổng quát' }
];

const currentUserId = ref(null);

const fetchUser = async () => {
  try { 
    const res = await axios.get('/api/community/me'); 
    userName.value = res.data.fullName; 
    currentUserId.value = res.data.id;
  } catch (err) { console.error("Lỗi lấy user:", err); }
};

const fetchCategories = async () => {
  try {
    const res = await axios.get('/api/community/categories');
    categories.value = res.data;
  } catch (err) { console.error("Lỗi tải danh mục:", err); }
};

const fetchPosts = async (catId = 0) => {
  loading.value = true;
  viewingSaved.value = false;
  activeCategory.value = catId;
  try {
    let url = '/api/community/posts';
    const params = new URLSearchParams();
    
    if (catId === 999) {
      params.append('privateOnly', 'true');
    } else if (catId > 0) {
      params.append('categoryId', catId);
    }
    
    const queryString = params.toString();
    if (queryString) url += '?' + queryString;

    const res = await axios.get(url);
    posts.value = (res.data || []).map(p => ({
      ...p,
      isLiked: !!p.isLiked,
      isSaved: (viewingSaved.value ? true : !!p.isSaved),
      isPublic: !!p.isPublic,
      status: p.status || 'APPROVED'
    }));
  } catch (err) {
    console.error("Lỗi tải bài viết:", err);
  } finally {
    loading.value = false;
  }
};

const fetchSavedPosts = async () => {
  loading.value = true;
  viewingSaved.value = true;
  activeCategory.value = -1;
  try {
    const res = await axios.get('/api/community/saved-posts');
    posts.value = (res.data || []).map(p => ({
      ...p,
      isLiked: !!p.isLiked,
      isSaved: true,
      isPublic: !!p.isPublic,
      status: p.status || 'APPROVED'
    }));
  } catch (err) {
    console.error("Lỗi tải bài viết đã lưu:", err);
  } finally {
    loading.value = false;
  }
};

const viewAllPosts = () => fetchPosts(0);

const filterByCategory = (id) => { activeCategory.value = id; fetchPosts(id); };

const openCreateModal = () => {
  isEditMode.value = false;
  editingPostId.value = null;
  removeImageOnEdit.value = false;
  newPost.title = '';
  newPost.content = '';
  newPost.image = null;
  newPost.imagePreview = null;
  newPost.isPublic = true; 
  if (categories.value.length > 0) newPost.categoryId = categories.value[0].id;
  showCreateModal.value = true;
};

const openEditModal = (post) => {
  isEditMode.value = true;
  editingPostId.value = post.id;
  removeImageOnEdit.value = false;
  newPost.title = post.title;
  newPost.content = post.content;
  newPost.categoryId = categories.value.find(c => c.name === post.category)?.id || null;
  newPost.image = null;
  newPost.imagePreview = post.media;
  newPost.isPublic = post.isPublic; 
  showCreateModal.value = true;
};


const handleImageUpload = (e) => {
  const file = e.target.files[0];
  if (!file) return;
  newPost.image = file;
  removeImageOnEdit.value = false;
  const reader = new FileReader();
  reader.onload = (event) => { newPost.imagePreview = event.target.result; };
  reader.readAsDataURL(file);
};

const removeImage = () => { 
  newPost.image = null; 
  newPost.imagePreview = null; 
  if (isEditMode.value) removeImageOnEdit.value = true;
};

const submitPost = async () => {
  if (!newPost.categoryId) { toast.warning('Vui lòng chọn chủ đề bài viết!'); return; }
  if (!newPost.title.trim() || !newPost.content.trim()) { toast.warning('Vui lòng nhập tiêu đề và nội dung!'); return; }
  
  createLoading.value = true;
  try {
    const formData = new FormData();
    formData.append('title', newPost.title.trim());
    formData.append('content', newPost.content.trim());
    formData.append('categoryId', newPost.categoryId);
    formData.append('isPublic', newPost.isPublic); 
    if (newPost.image) formData.append('file', newPost.image);

    let res;
    if (isEditMode.value) {
      formData.append('removeImage', removeImageOnEdit.value);
      res = await axios.post(`/api/community/post/${editingPostId.value}/update`, formData);
    } else {
      res = await axios.post('/api/community/posts/create', formData);
    }

    if (res.data.success) {
      toast.success(isEditMode.value ? "Cập nhật thành công!" : "Đăng bài thành công! Bài viết đang chờ duyệt.");
      showCreateModal.value = false;
      fetchPosts(activeCategory.value);
    }
  } catch (err) { 
    toast.error("Lỗi: " + (err.response?.data?.message || err.message)); 
  } finally { 
    createLoading.value = false; 
  }
};

const handleDeletePost = async (id) => {
  if (!await toast.confirm("Xác nhận", "Bạn có chắc chắn muốn xóa bài viết này không?")) return;
  try {
    const res = await axios.delete(`/api/community/post/${id}`);
    if (res.data.success) {
      toast.success("Đã xóa bài viết.");
      if (viewingSaved.value) fetchSavedPosts();
      else fetchPosts(activeCategory.value);
    }
  } catch (err) { toast.error("Lỗi khi xóa bài viết"); }
};

const handleDeleteComment = async (commentId) => {
  if (!await toast.confirm("Xác nhận", "Bạn có chắc chắn muốn xóa bình luận này không?")) return;
  try {
    const res = await axios.delete(`/api/community/comment/${commentId}`);
    if (res.data.success) {
      toast.success(res.data.message || "Đã xóa bình luận.");
      
      if (selectedPost.value) {
        await fetchComments(selectedPost.value.id);
      }
    }
  } catch (err) { 
    console.error("Lỗi xóa bình luận:", err);
    const errorMsg = err.response?.data?.message || err.response?.data?.error || err.message || "Lỗi khi xóa bình luận";
    toast.error(errorMsg); 
  }
};

const toggleLike = async (post) => {
  const originalLiked = post.isLiked;
  const originalLikes = post.likes;
  post.isLiked = !post.isLiked;
  post.likes += post.isLiked ? 1 : -1;
  try {
    const res = await axios.post(`/api/community/post/${post.id}/like`);
    if (res.data.success) { 
      post.isLiked = res.data.isLiked; 
      post.likes = res.data.likeCount; 
    }
  } catch (err) { 
    post.isLiked = originalLiked; post.likes = originalLikes;
  }
};

const toggleSave = async (post) => {
  try {
    const res = await axios.post(`/api/community/post/${post.id}/save`);
    if (res.data.success) {
      post.isSaved = res.data.isSaved;
      if (res.data.isSaved) toast.success("Đã lưu bài viết");
      if (viewingSaved.value && !post.isSaved) {
        posts.value = posts.value.filter(p => p.id !== post.id);
      }
    }
  } catch (err) { console.error(err); }
};

const openReportModal = (post) => {
  reportModalPost.value = post;
  selectedReportReason.value = '';
};

const submitReport = async () => {
  if (!reportModalPost.value || !selectedReportReason.value) return;
  try {
    const params = new URLSearchParams();
    params.append('reason', selectedReportReason.value);
    const res = await axios.post(`/api/community/post/${reportModalPost.value.id}/report`, params);
    if (res.data.success) {
      toast.success("Đã gửi báo cáo. Cảm ơn bạn!");
      reportModalPost.value = null;
    }
  } catch (err) { toast.error("Lỗi gửi báo cáo"); }
};

const openPostDetail = async (post) => {
  selectedPost.value = post;
  postComments.value = [];
  commentsLoading.value = true;
  try { 
    axios.post(`/api/community/post/${post.id}/view`);
    post.views++;
    
    const res = await axios.get(`/api/community/post/${post.id}/comments`); 
    postComments.value = res.data; 
  } catch (err) { console.error("Lỗi tải bình luận:", err); } finally { commentsLoading.value = false; }
};

const fetchComments = async (postId) => {
  commentsLoading.value = true;
  try {
    const res = await axios.get(`/api/community/post/${postId}/comments`); 
    postComments.value = res.data;
  } catch (err) { 
    console.error("Lỗi tải bình luận:", err); 
  } finally { 
    commentsLoading.value = false; 
  }
};

const closePostDetail = () => { selectedPost.value = null; replyingTo.value = null; commentText.value = ''; };

const handleComment = async () => {
  if (!commentText.value.trim() || !selectedPost.value) return;
  commentLoading.value = true;
  try {
    const formData = new FormData();
    formData.append('content', commentText.value.trim());
    if (replyingTo.value) formData.append('parentId', replyingTo.value.id);
    const res = await axios.post(`/api/community/post/${selectedPost.value.id}/comment`, formData);
    if (res.data.success) {
      if (replyingTo.value) {
        const parent = postComments.value.find(c => c.id === replyingTo.value.id);
        if (parent) { if (!parent.replies) parent.replies = []; parent.replies.push(res.data.comment); }
      } else {
        postComments.value.push(res.data.comment);
      }
      selectedPost.value.commentsCount = res.data.totalComments;
      commentText.value = ''; replyingTo.value = null;
      const p = posts.value.find(x => x.id === selectedPost.value.id);
      if(p) p.commentsCount = res.data.totalComments;
      toast.success("Đã gửi bình luận");
    }
  } catch (err) { toast.error("Lỗi khi bình luận"); } finally { commentLoading.value = false; }
};

const replyTo = (cmt) => { 
  replyingTo.value = cmt; 
  nextTick(() => { if (commentInputRef.value) commentInputRef.value.focus(); });
};

const startChatWithAuthor = async (post) => {
  try {
    const res = await axios.get(`/api/chat/room/with/${post.authorId}`);
    if (res.data && res.data.id) {
      window.location.href = `/chat?roomId=${res.data.id}`;
    }
  } catch (err) {
    console.error("Lỗi khi tạo phòng chat:", err);
    toast.error("Không thể kết nối với người dùng này lúc này.");
  }
};

const startChat = (dr) => { window.location.href = `/chat`; };

onMounted(async () => {
  await fetchUser();
  await fetchCategories();
  await fetchPosts();
  if (route.query.postId) {
    const res = await axios.get(`/api/community/post/${route.query.postId}`);
    if (res.data) openPostDetail(res.data);
  }
});
</script>

<style scoped>
.max-w-1400 { max-width: 1400px; margin: 0 auto; }
.tracking-wider { letter-spacing: 0.05em; }


.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  border: none;
  background: transparent;
  border-radius: 10px;
  font-weight: 600;
  font-size: 0.95rem;
  transition: all 0.2s;
  width: 100%;
  text-align: left;
}
.menu-item:hover { background-color: var(--hover-bg); }
.menu-item.active { background-color: rgba(13, 110, 253, 0.1); color: var(--primary-color) !important; }
.menu-item i { font-size: 1.4rem; width: 28px; text-align: center; }
.cat-icon-circle {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: 800;
}


.create-post-card { transition: all 0.3s; }
.fake-input-modern {
  border-radius: 20px;
  padding: 10px 16px;
  cursor: pointer;
  font-size: 1rem;
}
.fake-input-modern:hover { background-color: var(--hover-bg); }
.post-tool-btn {
  background: transparent;
  border: none;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border-radius: 8px;
  font-weight: 600;
  font-size: 0.85rem;
  transition: 0.2s;
  white-space: nowrap;
}
.post-tool-btn:hover { background-color: var(--hover-bg); }
.post-tool-btn i { font-size: 1.3rem; }


.modern-post-card { border: 1px solid var(--border-color); }
.active-dot {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 12px;
  height: 12px;
  background-color: #31a24c;
  border: 2px solid var(--bg-card);
  border-radius: 50%;
}
.post-content-text { font-size: 1rem; }
.video-wrapper { background: #000; display: flex; align-items: center; justify-content: center; width: 100%; }

.icon-circle {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.6rem;
  border: 1.5px solid white;
}

.btn-interaction {
  border: none;
  background: transparent;
  padding: 8px;
  font-weight: 600;
  font-size: 0.95rem;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: 0.2s;
}
.btn-interaction:hover { background-color: var(--hover-bg); }
.btn-interaction.active { color: var(--primary-color) !important; }
.btn-interaction i { font-size: 1.2rem; }


.trending-tag {
  padding: 6px 12px;
  border-radius: 15px;
  font-size: 0.8rem;
  font-weight: 600;
  cursor: pointer;
  transition: 0.2s;
}
.trending-tag:hover { background-color: var(--hover-bg); border-color: var(--primary-color) !important; }

.doctor-item-modern { transition: 0.2s; }
.doctor-item-modern:hover { background-color: var(--hover-bg); }
.btn-chat-mini {
  width: 32px;
  height: 32px;
  background-color: rgba(13, 110, 253, 0.1);
  color: var(--primary-color);
  border-radius: 50%;
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
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(10px);
  z-index: 2000;
}
.comment-bubble {
  padding: 8px 12px;
  border-radius: 18px;
  max-width: 100%;
  word-wrap: break-word;
  overflow-wrap: break-word;
  word-break: break-word;
}
.comment-text {
  white-space: pre-wrap;
  word-wrap: break-word;
  overflow-wrap: break-word;
}
.interaction-stat { cursor: pointer; display: flex; align-items: center; gap: 6px; transition: 0.2s; }
.interaction-stat:hover { opacity: 0.7; }


.like-container:hover .like-tooltip { 
  display: block; 
  opacity: 1; 
  visibility: visible;
  transform: translateY(-10px);
}
.like-tooltip {
  position: absolute;
  bottom: 100%;
  left: 0;
  background: var(--bg-card);
  color: var(--text-main);
  padding: 10px 15px;
  min-width: 150px;
  z-index: 100;
  display: none;
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s;
  pointer-events: none;
}


.sticky-top {
  max-height: calc(100vh - 100px);
  overflow-y: auto;
  padding-right: 5px;
}
.sticky-top::-webkit-scrollbar { width: 4px; }
.sticky-top::-webkit-scrollbar-thumb { background: var(--border-color); border-radius: 10px; }


.list-enter-active, .list-leave-active { transition: all 0.4s ease; }
.list-enter-from { opacity: 0; transform: translateY(30px); }
.list-leave-to { opacity: 0; transform: scale(0.9); }

.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: var(--border-color); border-radius: 10px; }

.hover-scale { transition: transform 0.2s; }
.hover-scale:hover { transform: scale(1.1); }

.btn-outline-theme:hover { background-color: var(--hover-bg); }
.border-theme { border-color: var(--border-color) !important; }


.btn-ai-suggest {
  background: linear-gradient(45deg, #0d6efd, #00d2ff);
  color: white;
  border: none;
  padding: 6px 15px;
  border-radius: 20px;
  font-weight: 700;
  font-size: 0.85rem;
  transition: all 0.3s;
}
.btn-ai-suggest:hover:not(:disabled) {
  transform: scale(1.05);
  box-shadow: 0 4px 15px rgba(13, 110, 253, 0.4);
}
.btn-ai-suggest:disabled {
  background: #ccc;
  cursor: not-allowed;
}
.animate-pulse {
  animation: pulse-border 2s infinite;
}
@keyframes pulse-border {
  0% { box-shadow: 0 0 0 0 rgba(13, 110, 253, 0.4); }
  70% { box-shadow: 0 0 0 10px rgba(13, 110, 253, 0); }
  100% { box-shadow: 0 0 0 0 rgba(13, 110, 253, 0); }
}

@media (max-width: 992px) {
  .modern-detail-container { flex-direction: column; overflow-y: auto !important; }
  .media-section-full { height: 300px !important; }
}

@media (max-width: 767.98px) {
  .forum-modern-wrapper .container-fluid {
    padding-left: 0.6rem !important;
    padding-right: 0.6rem !important;
    padding-top: 0.75rem !important;
  }

  .create-post-card {
    margin-bottom: 0.75rem !important;
  }

  .post-tool-btn {
    font-size: 0.75rem;
    padding: 0.4rem 0.35rem !important;
  }

  .modern-post-card .card-body,
  .modern-post-card .p-3 {
    padding: 0.75rem !important;
  }

  .modern-detail-container {
    max-height: 96dvh !important;
    border-radius: 12px !important;
  }

  .modern-detail-container .content-section-full,
  .modern-detail-container .comment-sidebar-modern {
    max-height: none !important;
  }

  .sticky-top {
    position: static !important;
    max-height: none !important;
    overflow: visible !important;
  }
}
</style>
