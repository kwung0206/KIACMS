package com.kiacms.course.repository;

import com.kiacms.course.entity.Course;
import com.kiacms.course.entity.CourseSession;
import com.kiacms.user.entity.User;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseSessionRepository extends JpaRepository<CourseSession, UUID> {

    List<CourseSession> findAllByCourseOrderBySessionOrderAsc(Course course);

    List<CourseSession> findAllByInstructorAndSessionDateBetweenOrderBySessionDateAsc(User instructor, LocalDate from, LocalDate to);

    List<CourseSession> findAllByInstructorOrderBySessionDateAscStartTimeAsc(User instructor);

    List<CourseSession> findAllByCourseInOrderBySessionDateAscStartTimeAsc(List<Course> courses);

    List<CourseSession> findAllBySessionDateBetweenOrderBySessionDateAscStartTimeAsc(LocalDate from, LocalDate to);

    Optional<CourseSession> findByIdAndInstructor(UUID id, User instructor);
}
