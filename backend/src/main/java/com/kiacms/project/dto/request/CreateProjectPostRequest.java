package com.kiacms.project.dto.request;

import com.kiacms.project.enums.ContactMethodType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record CreateProjectPostRequest(
        @NotBlank
        @Size(max = 200)
        String title,

        @NotBlank
        @Size(max = 10000)
        String description,

        @NotBlank
        @Size(max = 5000)
        String goal,

        @NotBlank
        @Size(max = 3000)
        String techStack,

        @NotBlank
        @Size(max = 100)
        String durationText,

        @NotNull
        ContactMethodType contactMethod,

        @NotBlank
        @Size(max = 255)
        String contactValue,

        @NotBlank
        @Size(max = 5000)
        String pmIntroduction,

        @NotBlank
        @Size(max = 5000)
        String pmBackground,

        @FutureOrPresent
        LocalDate recruitUntil,

        @NotEmpty
        @Size(max = 10)
        List<@Valid CreateProjectPositionRequest> positions
) {
}
