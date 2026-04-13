package com.kiacms.course.repository;

import com.kiacms.course.entity.Course;
import com.kiacms.course.entity.Enrollment;
import com.kiacms.course.enums.EnrollmentStatus;
import com.kiacms.user.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    List<Enrollment> findAllByStudentOrderByCreatedAtDesc(User student);

    List<Enrollment> findAllByCourseOrderByCreatedAtAsc(Course course);

    List<Enrollment> findAllByCourseInAndStatusOrderByCreatedAtAsc(List<Course> courses, EnrollmentStatus status);

    Optional<Enrollment> findByStudentAndCourse(User student, Course course);
}
