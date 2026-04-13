package com.kiacms.project.repository;

import com.kiacms.project.entity.ProjectPost;
import com.kiacms.project.enums.ProjectPostStatus;
import com.kiacms.user.entity.User;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectPostRepository extends JpaRepository<ProjectPost, UUID> {

    @Query("""
            select p
            from ProjectPost p
            where p.status = :status
              and p.status <> com.kiacms.project.enums.ProjectPostStatus.DELETED
            order by p.createdAt desc
            """)
    List<ProjectPost> findAllVisibleByStatusOrderByCreatedAtDesc(@Param("status") ProjectPostStatus status);

    @Query("""
            select p
            from ProjectPost p
            where p.status <> com.kiacms.project.enums.ProjectPostStatus.DELETED
            order by p.createdAt desc
            """)
    List<ProjectPost> findAllVisibleOrderByCreatedAtDesc();

    @Query("""
            select p
            from ProjectPost p
            where p.owner = :owner
              and p.status <> com.kiacms.project.enums.ProjectPostStatus.DELETED
            order by p.createdAt desc
            """)
    List<ProjectPost> findAllVisibleByOwnerOrderByCreatedAtDesc(@Param("owner") User owner);
}
