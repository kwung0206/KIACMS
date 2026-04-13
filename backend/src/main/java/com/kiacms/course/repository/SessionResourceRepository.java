package com.kiacms.course.repository;

import com.kiacms.course.entity.CourseSession;
import com.kiacms.course.entity.SessionResource;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionResourceRepository extends JpaRepository<SessionResource, UUID> {

    Optional<SessionResource> findByCourseSession(CourseSession courseSession);
}
