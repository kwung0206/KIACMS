package com.kiacms.project.repository;

import com.kiacms.project.entity.MentorApplication;
import com.kiacms.project.entity.ProjectPost;
import com.kiacms.project.enums.ApplicationStatus;
import com.kiacms.user.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentorApplicationRepository extends JpaRepository<MentorApplication, UUID> {

    List<MentorApplication> findAllByProjectPostOrderByCreatedAtAsc(ProjectPost projectPost);

    List<MentorApplication> findAllByApplicantOrderByCreatedAtDesc(User applicant);

    Optional<MentorApplication> findByProjectPostAndApplicant(ProjectPost projectPost, User applicant);

    Optional<MentorApplication> findByIdAndApplicant(UUID id, User applicant);

    long countByProjectPostAndStatus(ProjectPost projectPost, ApplicationStatus status);
}
