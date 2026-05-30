package marius.server.data.dto;

import jakarta.validation.constraints.*;

public record UserAuthDto(

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
        String password
) {
}
