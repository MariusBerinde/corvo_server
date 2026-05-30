package marius.server.data.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateRequestRoleDto(
        @NotBlank(message = "name is required")
        String username,

        @NotNull(message = "user object is required")
        @Valid
        UpdateRequestRoleUserDto user
) {
}
