package com.kiacms.project.repository;

import com.kiacms.project.entity.ProjectPosition;
import com.kiacms.project.entity.ProjectPost;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectPositionRepository extends JpaRepository<ProjectPosition, UUID> {

    List<ProjectPosition> findAllByProjectPostOrderByCreatedAtAsc(ProjectPost projectPost);
}
