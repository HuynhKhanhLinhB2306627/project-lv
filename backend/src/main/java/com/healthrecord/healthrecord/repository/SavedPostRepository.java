package com.healthrecord.healthrecord.repository;

import com.healthrecord.healthrecord.entity.SavedPost;
import com.healthrecord.healthrecord.entity.User;
import com.healthrecord.healthrecord.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SavedPostRepository extends JpaRepository<SavedPost, Long> {
    List<SavedPost> findByUserOrderBySavedAtDesc(User user);
    Optional<SavedPost> findByUserAndPost(User user, Post post);
    boolean existsByUserAndPost(User user, Post post);
    List<SavedPost> findByPost(Post post);
}
