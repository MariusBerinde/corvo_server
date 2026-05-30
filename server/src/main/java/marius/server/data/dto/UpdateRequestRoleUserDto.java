package marius.server.data.dto;

import jakarta.validation.constraints.*;

public record UpdateRequestRoleUserDto(

        @Email(message = "email not valid")
        @NotNull
        @NotEmpty
        @NotBlank(message = "email is required")
        String email,

        @NotNull
        @NotNull(message = "role is required")
        @Min(value = 0, message = "role must be 0 or 1")
        @Max(value = 1, message = "role must be 0 or 1")
        Integer role
) {
}
