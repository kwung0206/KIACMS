package com.kiacms.user.repository;

import com.kiacms.user.entity.User;
import com.kiacms.user.enums.RoleType;
import com.kiacms.user.enums.UserStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumberAndIdNotAndDeletedAtIsNull(String phoneNumber, UUID id);

    Optional<User> findByIdAndDeletedAtIsNull(UUID id);

    List<User> findAllByStatusAndDeletedAtIsNullOrderByCreatedAtAsc(UserStatus status);

    List<User> findAllByRoleTypeAndStatusAndDeletedAtIsNullOrderByNameAsc(RoleType roleType, UserStatus status);

    @Query("""
            select u
            from User u
            where u.deletedAt is null
              and u.roleType = :roleType
              and u.status = :status
              and (
                    lower(u.name) like lower(concat('%', :keyword, '%'))
                    or lower(u.email) like lower(concat('%', :keyword, '%'))
                  )
            order by u.name asc, u.createdAt asc
            """)
    List<User> searchUsersByRoleAndStatus(
            @Param("roleType") RoleType roleType,
            @Param("status") UserStatus status,
            @Param("keyword") String keyword
    );
}
