package com.kiacms.global.security;

import com.kiacms.global.exception.BusinessException;
import com.kiacms.global.exception.ErrorCode;
import com.kiacms.user.entity.User;
import com.kiacms.user.enums.UserStatus;
import com.kiacms.user.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmailAndDeletedAtIsNull(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));
        return CustomUserPrincipal.from(user);
    }

    public CustomUserPrincipal loadApprovedUserPrincipalById(UUID userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        if (user.getStatus() != UserStatus.APPROVED) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        return CustomUserPrincipal.from(user);
    }
}
