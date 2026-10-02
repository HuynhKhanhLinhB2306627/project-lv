package com.healthrecord.healthrecord.repository;

import com.healthrecord.healthrecord.entity.Category;
import com.healthrecord.healthrecord.entity.Post;
import com.healthrecord.healthrecord.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    
    
    
    @Query("SELECT DISTINCT p FROM Post p " +
           "LEFT JOIN FETCH p.author " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.likedUsers " +
           "WHERE (p.status = 'APPROVED' AND (p.isPublic = true OR p.author = :currentUser)) " +
           "OR (p.author = :currentUser AND p.status = 'PENDING') " +
           "ORDER BY p.createdAt DESC")
    List<Post> findAllVisiblePosts(@Param("currentUser") User currentUser);

    
    @Query("SELECT DISTINCT p FROM Post p " +
           "LEFT JOIN FETCH p.author " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.likedUsers " +
           "WHERE p.category = :category " +
           "AND ((p.status = 'APPROVED' AND (p.isPublic = true OR p.author = :currentUser)) " +
           "OR (p.author = :currentUser AND p.status = 'PENDING')) " +
           "ORDER BY p.createdAt DESC")
    List<Post> findVisiblePostsByCategory(@Param("category") Category category, @Param("currentUser") User currentUser);

    
    @Query("SELECT DISTINCT p FROM Post p " +
           "LEFT JOIN FETCH p.author " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.likedUsers " +
           "WHERE p.status = :status " +
           "ORDER BY p.createdAt DESC")
    List<Post> findByStatusOrderByCreatedAtDesc(@Param("status") Post.PostStatus status);

    
    @Query("SELECT DISTINCT p FROM Post p " +
           "LEFT JOIN FETCH p.author " +
           "LEFT JOIN FETCH p.category " +
           "WHERE p.category = :category AND p.status = :status " +
           "ORDER BY p.createdAt DESC")
    List<Post> findByCategoryAndStatusOrderByCreatedAtDesc(@Param("category") Category category, @Param("status") Post.PostStatus status);

    
    @Query("SELECT DISTINCT p FROM Post p " +
           "LEFT JOIN FETCH p.author " +
           "LEFT JOIN FETCH p.category " +
           "WHERE p.author = :author AND p.isPublic = false " +
           "ORDER BY p.createdAt DESC")
    List<Post> findByAuthorAndIsPublicFalseOrderByCreatedAtDesc(@Param("author") User author);
    
    
    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
    void incrementViewCount(@Param("postId") Long postId);
}
