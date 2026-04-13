package com.kiacms.project.repository;

import com.kiacms.project.entity.ProjectDeletionHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectDeletionHistoryRepository extends JpaRepository<ProjectDeletionHistory, java.util.UUID> {

    List<ProjectDeletionHistory> findAllByOrderByDeletedAtDesc();
}
