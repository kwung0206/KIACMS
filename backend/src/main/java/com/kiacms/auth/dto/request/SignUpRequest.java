package com.kiacms.auth.dto.request;

import com.kiacms.user.enums.RoleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
        @NotBlank
        @Email
        @Size(max = 150)
        String email,

        @NotBlank
        @Size(min = 8, max = 100)
        String password,

        @NotBlank
        @Size(max = 50)
        String name,

        @Size(max = 20)
        @Pattern(regexp = "^[0-9+\\-()\\s]*$", message = "Phone number contains invalid characters.")
        String phoneNumber,

        @NotNull
        RoleType roleType
) {
}
