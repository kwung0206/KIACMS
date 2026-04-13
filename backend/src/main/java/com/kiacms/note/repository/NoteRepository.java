package com.kiacms.note.repository;

import com.kiacms.course.entity.Course;
import com.kiacms.course.entity.CourseSession;
import com.kiacms.note.entity.Note;
import com.kiacms.user.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository<Note, UUID> {

    List<Note> findAllByAuthorOrderByCreatedAtDesc(User author);

    List<Note> findAllByAuthorAndCourseOrderByCreatedAtDesc(User author, Course course);

    List<Note> findAllByAuthorAndCourseSessionOrderByCreatedAtDesc(User author, CourseSession courseSession);

    List<Note> findAllByCourseOrderByCreatedAtDesc(Course course);

    List<Note> findAllByCourseSessionOrderByCreatedAtDesc(CourseSession courseSession);

    Optional<Note> findByIdAndAuthor(UUID id, User author);
}
