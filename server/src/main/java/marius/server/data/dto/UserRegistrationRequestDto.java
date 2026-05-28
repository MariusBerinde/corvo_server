package marius.server.data.dto;

import jakarta.validation.constraints.*;

public record UserRegistrationRequestDto(
        @NotNull
        @NotEmpty
        @NotBlank(message = "username is required")
        String username,

        @Email(message = "email not valid")
        @NotNull
        @NotEmpty
        @NotBlank(message = "email is required")
        String email
) {
}
