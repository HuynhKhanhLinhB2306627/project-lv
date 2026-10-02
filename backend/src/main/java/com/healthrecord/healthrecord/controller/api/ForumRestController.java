package com.healthrecord.healthrecord.controller.api;

import com.healthrecord.healthrecord.dto.PostResponseDto;

import com.healthrecord.healthrecord.entity.Category;
import com.healthrecord.healthrecord.entity.Comment;
import com.healthrecord.healthrecord.entity.Post;
import com.healthrecord.healthrecord.entity.User;

import com.healthrecord.healthrecord.service.ForumService;
import com.healthrecord.healthrecord.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/community")
public class ForumRestController {

    @Autowired
    private ForumService forumService;

    @Autowired
    private com.healthrecord.healthrecord.repository.PostRepository postRepository;


    @GetMapping("/posts")
    @Transactional(readOnly = true)
    public ResponseEntity<List<PostResponseDto>> getPosts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false, defaultValue = "false") boolean privateOnly) {
        User currentUser = SecurityUtils.getCurrentUser();
        List<Post> posts;
        
        if (privateOnly) {
            posts = postRepository.findByAuthorAndIsPublicFalseOrderByCreatedAtDesc(currentUser);
        } else if (categoryId != null && categoryId > 0) {
            posts = forumService.getPostsByCategory(categoryId, currentUser);
        } else {
            posts = forumService.getAllApprovedPosts(currentUser);
        }
        
        return ResponseEntity.ok(posts.stream()
                .map(p -> convertToDto(p, currentUser))
                .collect(Collectors.toList()));
    }

    @GetMapping("/post/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<PostResponseDto> getPostDetails(@PathVariable Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        Post post = forumService.getPostById(id);
        if (post == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(convertToDto(post, currentUser));
    }

    @GetMapping("/saved-posts")
    @Transactional(readOnly = true)
    public ResponseEntity<List<PostResponseDto>> getSavedPosts() {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(401).build();
        
        return ResponseEntity.ok(forumService.getSavedPostsByUser().stream()
                .map(sp -> convertToDto(sp.getPost(), currentUser))
                .collect(Collectors.toList()));
    }

    @PostMapping("/post/{id}/save")
    public ResponseEntity<?> toggleSavePost(@PathVariable Long id) {
        try {
            forumService.toggleSavePost(id);
            return ResponseEntity.ok(Map.of("success", true, "isSaved", forumService.isPostSaved(id)));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/post/{id}/report")
    public ResponseEntity<?> reportPost(@PathVariable Long id, @RequestParam String reason) {
        try {
            forumService.reportPost(id, reason);
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã gửi báo cáo vi phạm."));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<List<Map<String, Object>>> getCategories() {
        List<Category> categories = forumService.getAllCategories();
        return ResponseEntity.ok(categories.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("name", c.getName());
            return map;
        }).collect(Collectors.toList()));
    }

    @PostMapping("/posts/create")
    public ResponseEntity<?> createPost(@RequestParam("title") String title,
                                        @RequestParam("content") String content,
                                        @RequestParam("categoryId") Long categoryId,
                                        @RequestParam(value = "isPublic", defaultValue = "true") boolean isPublic,
                                        @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            forumService.createPost(title, content, categoryId, file, isPublic);
            return ResponseEntity.ok(Map.of("success", true, "message", "Bài viết đã được gửi và đang chờ duyệt."));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/post/{id}/update")
    public ResponseEntity<?> updatePost(@PathVariable Long id,
                                        @RequestParam("title") String title,
                                        @RequestParam("content") String content,
                                        @RequestParam("categoryId") Long categoryId,
                                        @RequestParam(value = "isPublic", defaultValue = "true") boolean isPublic,
                                        @RequestParam(value = "file", required = false) MultipartFile file,
                                        @RequestParam(value = "removeImage", defaultValue = "false") boolean removeImage) {
        try {
            forumService.updatePost(id, title, content, categoryId, file, removeImage, isPublic);
            return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật bài viết thành công."));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/post/{id}/like")
    public ResponseEntity<?> toggleLike(@PathVariable("id") Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        try {
            forumService.toggleLike(id);
            Post post = forumService.getPostById(id);
            return ResponseEntity.ok(Map.of(
                "success", true, 
                "isLiked", post.isLikedBy(currentUser),
                "likeCount", post.getLikeCount()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/post/{id}/comments")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getComments(@PathVariable("id") Long id) {
        Post post = forumService.getPostById(id);
        if (post == null) return ResponseEntity.notFound().build();
        List<Map<String, Object>> comments = post.getComments().stream()
                .filter(c -> c.getParentComment() == null)
                .map(this::convertCommentToMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/post/{id}/comment")
    @Transactional
    public ResponseEntity<?> addComment(@PathVariable("id") Long id,
                                        @RequestParam("content") String content,
                                        @RequestParam(value = "parentId", required = false) Long parentId) {
        try {
            Comment comment = forumService.addComment(id, content, parentId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "comment", convertCommentToMap(comment),
                "totalComments", comment.getPost().getComments().size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/comment/{id}")
    @Transactional
    public ResponseEntity<?> deleteComment(@PathVariable Long id) {
        try {
            User currentUser = SecurityUtils.getCurrentUser();
            Comment comment = forumService.getCommentById(id);
            
            if (comment == null) {
                return ResponseEntity.status(404).body(Map.of("success", false, "message", "Không tìm thấy bình luận."));
            }
            
            if (!comment.getUser().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(403).body(Map.of("success", false, "message", "Bạn không có quyền xóa bình luận này."));
            }
            
            forumService.deleteComment(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã xóa bình luận thành công."));
            
        } catch (Exception e) {
            e.printStackTrace(); 
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Lỗi khi xóa bình luận: " + e.getMessage()));
        }
    }

    @PostMapping("/post/{id}/view")
    public ResponseEntity<?> incrementView(@PathVariable Long id) {
        forumService.increaseViewCount(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @DeleteMapping("/post/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        Post post = forumService.getPostById(id);
        if (post == null) return ResponseEntity.notFound().build();
        if (!post.getAuthor().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).body(Map.of("message", "Bạn không có quyền xóa bài viết này."));
        }
        try {
            forumService.deletePost(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã xóa bài viết thành công."));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe() {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(Map.of("id", currentUser.getId(), "fullName", currentUser.getFullName()));
    }


    private PostResponseDto convertToDto(Post post, User currentUser) {
        PostResponseDto dto = new PostResponseDto();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setAuthor(post.getAuthor().getFullName());
        dto.setAuthorId(post.getAuthor().getId());
        dto.setCategory(post.getCategory() != null ? post.getCategory().getName() : "Chung");
        dto.setMedia(post.getMediaPath());
        dto.setLikes(post.getLikeCount());
        dto.setViews(post.getViewCount());
        dto.setIsPublic(post.isPublic());
        dto.setStatus(post.getStatus() != null ? post.getStatus().name() : "PENDING");
        if (post.getCreatedAt() != null) {
            dto.setDate(post.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }
        dto.setIsLiked(post.isLikedBy(currentUser));
        dto.setCommentsCount(post.getComments().size());
        dto.setIsSaved(forumService.isPostSaved(post.getId()));
        dto.setLikedByNames(post.getLikedUsers().stream()
                .map(User::getFullName)
                .collect(Collectors.toList()));
        return dto;
    }

    private Map<String, Object> convertCommentToMap(Comment c) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", c.getId());
        map.put("authorId", c.getUser().getId());
        map.put("authorName", c.getUser().getFullName());
        map.put("content", c.getContent());
        if (c.getCreatedAt() != null) {
            map.put("createdAt", c.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")));
        }
        if (c.getReplies() != null && !c.getReplies().isEmpty()) {
            map.put("replies", c.getReplies().stream().map(this::convertCommentToMap).collect(Collectors.toList()));
        }
        return map;
    }
}
