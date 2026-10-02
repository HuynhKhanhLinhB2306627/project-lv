package com.healthrecord.healthrecord.repository;

import com.healthrecord.healthrecord.entity.Post;
import com.healthrecord.healthrecord.entity.PostReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PostReportRepository extends JpaRepository<PostReport, Long> {
    List<PostReport> findByResolvedFalseOrderByReportedAtDesc();
    List<PostReport> findByPost(Post post);
}
