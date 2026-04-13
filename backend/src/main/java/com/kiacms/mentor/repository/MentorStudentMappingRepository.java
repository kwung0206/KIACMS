package com.kiacms.mentor.repository;

import com.kiacms.mentor.entity.MentorStudentMapping;
import com.kiacms.mentor.enums.MentorStudentMappingStatus;
import com.kiacms.user.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentorStudentMappingRepository extends JpaRepository<MentorStudentMapping, UUID> {

    List<MentorStudentMapping> findAllByMentorAndStatusOrderByStartDateDesc(User mentor, MentorStudentMappingStatus status);

    List<MentorStudentMapping> findAllByStudentAndStatusOrderByStartDateDesc(User student, MentorStudentMappingStatus status);

    Optional<MentorStudentMapping> findByMentorAndStudentAndStatus(User mentor, User student, MentorStudentMappingStatus status);

    Optional<MentorStudentMapping> findByIdAndMentor(UUID id, User mentor);
}
