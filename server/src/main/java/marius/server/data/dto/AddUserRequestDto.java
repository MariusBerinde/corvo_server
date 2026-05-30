package marius.server.data.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record AddUserRequestDto(
        @NotNull(message = "user object is required")
        @Valid
        UserRegistrationDto user
) {}
