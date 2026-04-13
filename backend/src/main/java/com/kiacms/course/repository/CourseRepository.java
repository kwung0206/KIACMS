package com.kiacms.course.repository;

import com.kiacms.course.entity.Course;
import com.kiacms.course.enums.CourseStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, UUID> {

    Optional<Course> findByCourseCode(String courseCode);

    boolean existsByCourseCode(String courseCode);

    List<Course> findAllByStatusOrderByStartDateAsc(CourseStatus status);
}
