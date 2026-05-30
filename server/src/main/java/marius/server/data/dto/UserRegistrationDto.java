package marius.server.data.dto;

import jakarta.validation.constraints.*;


public record UserRegistrationDto(

        @NotBlank(message = "name is required")
        String name,

        @Email(message = "email not valid")
        @NotNull
        @NotEmpty
        @NotBlank(message = "email is required")
        String email,

        @NotBlank
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[.,/#!?$%\\^&*;:{}=\\-_()\\[\\]|'`~@.]).{6,}$",
                message = "password must contain at least 6 characters, one uppercase, one lowercase, one digit and one special character"
        )
        String password,

        @NotNull
        @NotNull(message = "role is required")
        @Min(value = 0, message = "role must be 0 or 1")
        @Max(value = 1, message = "role must be 0 or 1")
        Integer role
) {
}
