package com.healthrecord.healthrecord.service;

import com.healthrecord.healthrecord.entity.*;
import com.healthrecord.healthrecord.repository.*;
import com.healthrecord.healthrecord.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ForumService {

    @Autowired private PostRepository postRepository;
    @Autowired private CommentRepository commentRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private SavedPostRepository savedPostRepository;
    @Autowired private PostReportRepository postReportRepository;
    @Autowired private CloudinaryService cloudinaryService;

    @Autowired private NotificationService notificationService;

    
    @Transactional(readOnly = true)
    public List<Post> getAllApprovedPosts(User currentUser) {
        if (currentUser == null) {
            return postRepository.findByStatusOrderByCreatedAtDesc(Post.PostStatus.APPROVED);
        }
        return postRepository.findAllVisiblePosts(currentUser);
    }

    
    @Transactional(readOnly = true)
    public List<Post> getAllApprovedPostsForAdmin() {
        return postRepository.findByStatusOrderByCreatedAtDesc(Post.PostStatus.APPROVED);
    }

    @Transactional(readOnly = true)
    public List<Post> getAllPendingPosts() {
        return postRepository.findByStatusOrderByCreatedAtDesc(Post.PostStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public Post getPostById(Long id) {
        return postRepository.findById(id).orElse(null);
    }

    @Transactional
    public void toggleSavePost(Long postId) {
        User user = SecurityUtils.getCurrentUser();
        Post post = postRepository.findById(postId).orElseThrow();
        Optional<SavedPost> existing = savedPostRepository.findByUserAndPost(user, post);
        if (existing.isPresent()) {
            savedPostRepository.delete(existing.get());
        } else {
            savedPostRepository.save(new SavedPost(user, post));
        }
    }

    @Transactional(readOnly = true)
    public boolean isPostSaved(Long postId) {
        User user = SecurityUtils.getCurrentUser();
        if (user == null) return false;
        Post post = postRepository.findById(postId).orElse(null);
        return post != null && savedPostRepository.existsByUserAndPost(user, post);
    }

    @Transactional(readOnly = true)
    public List<SavedPost> getSavedPostsByUser() {
        User user = SecurityUtils.getCurrentUser();
        return savedPostRepository.findByUserOrderBySavedAtDesc(user);
    }

    @Transactional
    public void reportPost(Long postId, String reason) {
        User user = SecurityUtils.getCurrentUser();
        Post post = postRepository.findById(postId).orElseThrow();
        postReportRepository.save(new PostReport(user, post, reason));
    }

    @Transactional(readOnly = true)
    public List<PostReport> getAllActiveReports() {
        return postReportRepository.findByResolvedFalseOrderByReportedAtDesc();
    }

    @Transactional
    public void resolveReport(Long reportId) {
        postReportRepository.findById(reportId).ifPresent(r -> {
            r.setResolved(true);
            postReportRepository.save(r);
        });
    }

    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Post> getPostsByCategory(Long categoryId, User currentUser) {
        Category category = categoryRepository.findById(categoryId).orElse(null);
        if (category == null) return getAllApprovedPosts(currentUser);
        if (currentUser == null) {
            return postRepository.findByCategoryAndStatusOrderByCreatedAtDesc(category, Post.PostStatus.APPROVED);
        }
        return postRepository.findVisiblePostsByCategory(category, currentUser);
    }

    @Transactional
    public void createPost(String title, String content, Long categoryId, MultipartFile file, boolean isPublic) {
        User user = SecurityUtils.getCurrentUser();
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Category ID"));

        Post post = new Post();
        post.setTitle(title);
        post.setContent(content);
        post.setAuthor(user);
        post.setCategory(category);
        post.setPublic(isPublic);
        post.setStatus(Post.PostStatus.PENDING);

        
        
        String cloudinaryPublicId = null;
        String mediaUrl = null;
        
        if (file != null && !file.isEmpty()) {
            try {
                Map uploadResult = cloudinaryService.upload(file);
                mediaUrl = (String) uploadResult.get("secure_url");
                cloudinaryPublicId = (String) uploadResult.get("public_id");
                post.setMediaPath(mediaUrl);
                post.setCloudinaryPublicId(cloudinaryPublicId);
            } catch (IOException ex) {
                throw new RuntimeException("Lỗi upload media lên Cloudinary.", ex);
            }
        }
        
        
        try {
            postRepository.save(post);
        } catch (Exception ex) {
            
            if (cloudinaryPublicId != null) {
                try {
                    cloudinaryService.delete(cloudinaryPublicId);
                    System.out.println("Compensating transaction: Deleted Cloudinary image " + cloudinaryPublicId);
                } catch (IOException deleteEx) {
                    System.err.println("CRITICAL: Failed to delete orphaned Cloudinary image: " + cloudinaryPublicId);
                }
            }
            throw new RuntimeException("Lỗi lưu bài viết vào database.", ex);
        }
    }

    @Transactional
    public void updatePost(Long postId, String title, String content, Long categoryId, MultipartFile file, boolean removeImage, boolean isPublic) {
        User user = SecurityUtils.getCurrentUser();
        Post post = postRepository.findById(postId).orElseThrow();
        
        if (!post.getAuthor().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền sửa bài viết này.");
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Category ID"));

        post.setTitle(title);
        post.setContent(content);
        post.setCategory(category);
        post.setPublic(isPublic);
        post.setStatus(Post.PostStatus.PENDING);

        
        String oldCloudinaryId = post.getCloudinaryPublicId();
        String newCloudinaryId = null;
        String newMediaUrl = null;
        boolean shouldDeleteOldImage = false;

        
        if (removeImage || (file != null && !file.isEmpty())) {
            shouldDeleteOldImage = (oldCloudinaryId != null);
            post.setMediaPath(null);
            post.setCloudinaryPublicId(null);
        }

        
        if (file != null && !file.isEmpty()) {
            try {
                Map uploadResult = cloudinaryService.upload(file);
                newMediaUrl = (String) uploadResult.get("secure_url");
                newCloudinaryId = (String) uploadResult.get("public_id");
                post.setMediaPath(newMediaUrl);
                post.setCloudinaryPublicId(newCloudinaryId);
            } catch (IOException ex) {
                throw new RuntimeException("Lỗi upload media mới lên Cloudinary.", ex);
            }
        }

        
        try {
            postRepository.save(post);
            
            
            if (shouldDeleteOldImage && oldCloudinaryId != null) {
                try {
                    cloudinaryService.delete(oldCloudinaryId);
                } catch (IOException e) {
                    System.err.println("Warning: Failed to delete old Cloudinary image: " + oldCloudinaryId);
                }
            }
        } catch (Exception ex) {
            
            if (newCloudinaryId != null) {
                try {
                    cloudinaryService.delete(newCloudinaryId);
                    System.out.println("Compensating transaction: Deleted new Cloudinary image " + newCloudinaryId);
                } catch (IOException deleteEx) {
                    System.err.println("CRITICAL: Failed to delete orphaned Cloudinary image: " + newCloudinaryId);
                }
            }
            throw new RuntimeException("Lỗi cập nhật bài viết.", ex);
        }
    }

    @Transactional
    public Comment addComment(Long postId, String content, Long parentId) {
        User user = SecurityUtils.getCurrentUser();
        Post post = postRepository.findById(postId).orElseThrow();

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setUser(user);
        comment.setPost(post);

        if (parentId != null) {
            Comment parent = commentRepository.findById(parentId).orElse(null);
            comment.setParentComment(parent);
            
            User author = post.getAuthor();
            if (author != null && !user.getId().equals(author.getId())) {
                notificationService.createAndSendNotification(author, user.getFullName() + " đã bình luận về bài viết của bạn: \"" + post.getTitle() + "\"", "/forum?postId=" + post.getId());
            }
            
            if (parent != null && parent.getUser() != null && !user.getId().equals(parent.getUser().getId()) && !parent.getUser().getId().equals(author.getId())) {
                notificationService.createAndSendNotification(parent.getUser(), user.getFullName() + " đã trả lời bình luận của bạn trong bài viết: \"" + post.getTitle() + "\"", "/forum?postId=" + post.getId());
            }
        } else {
            User author = post.getAuthor();
            if (author != null && !user.getId().equals(author.getId())) {
                notificationService.createAndSendNotification(author, user.getFullName() + " đã bình luận về bài viết của bạn: \"" + post.getTitle() + "\"", "/forum?postId=" + post.getId());
            }
        }

        return commentRepository.save(comment);
    }

    @Transactional
    public void increaseViewCount(Long postId) {
        
        
        postRepository.incrementViewCount(postId);
    }

    @Transactional
    public void toggleLike(Long postId) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) return;
        
        User dbUser = userRepository.findById(currentUser.getId()).orElse(null);
        if (dbUser == null) return;

        Post post = postRepository.findById(postId).orElse(null);
        if (post != null) {
            boolean isLiking = !post.getLikedUsers().contains(dbUser);
            if (!isLiking) {
                post.getLikedUsers().remove(dbUser);
            } else {
                post.getLikedUsers().add(dbUser);

                User author = post.getAuthor();
                if (author != null && !dbUser.getId().equals(author.getId())) {
                    notificationService.createAndSendNotification(author, dbUser.getFullName() + " đã thích bài viết của bạn: \"" + post.getTitle() + "\"", "/forum?postId=" + post.getId());
                }
            }
            postRepository.save(post);
        }
    }

    @Transactional
    public void approvePost(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow();
        post.setStatus(Post.PostStatus.APPROVED);
        postRepository.save(post);
    }

    @Transactional
    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId).orElse(null);
        if (post != null) {
            
            List<PostReport> reports = postReportRepository.findByPost(post);
            if (!reports.isEmpty()) {
                postReportRepository.deleteAll(reports);
            }
            
            
            List<SavedPost> savedPosts = savedPostRepository.findByPost(post);
            if (!savedPosts.isEmpty()) {
                savedPostRepository.deleteAll(savedPosts);
            }
            
            
            if (post.getCloudinaryPublicId() != null) {
                try {
                    cloudinaryService.delete(post.getCloudinaryPublicId());
                } catch (IOException e) {
                    System.err.println("Lỗi xóa media trên Cloudinary khi xóa bài viết: " + e.getMessage());
                }
            }
            
            
            postRepository.delete(post);
        }
    }

    public Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId).orElse(null);
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElse(null);
        if (comment != null) {
            
            
            commentRepository.delete(comment);
            
            
            commentRepository.flush();
        }
    }
}
