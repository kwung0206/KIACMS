package com.kiacms.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateMyProfileRequest(
        @NotBlank(message = "이름은 필수입니다.")
        @Size(max = 50, message = "이름은 50자 이하여야 합니다.")
        String name,

        @Size(max = 20, message = "전화번호는 20자 이하여야 합니다.")
        @Pattern(
                regexp = "^[0-9+\\-()\\s]*$",
                message = "전화번호에는 숫자와 + - ( ) 공백만 사용할 수 있습니다."
        )
        String phoneNumber,

        @Size(max = 1000, message = "자기소개는 1000자 이하여야 합니다.")
        String bio
) {
}
