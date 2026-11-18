package com.project.user.dto.request;

import com.project.common.datetime.validator.ValidPersianDate;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Payload for creating a user.
 *
 * <p>Dates arrive in the Persian calendar and are converted at the boundary; the password
 * is only ever carried in memory and stored hashed.</p>
 */
public record CreateUserRequest(

        @NotBlank(message = "{validation.username.required}")
        @Size(max = 100, message = "{validation.username.size}")
        @Pattern(regexp = "^[a-zA-Z0-9._-]{3,100}$", message = "{validation.username.pattern}")
        String username,

        @NotBlank(message = "{validation.password.required}")
        @Size(min = 8, max = 100, message = "{validation.password.length}")
        String password,

        @NotBlank(message = "{validation.fullName.required}")
        @Size(max = 200, message = "{validation.fullName.size}")
        String fullName,

        @Size(max = 20, message = "{validation.mobile.size}")
        @Pattern(regexp = "^$|^09\\d{9}$", message = "{validation.mobile.pattern}")
        String mobile,

        @Email(message = "{validation.email.pattern}")
        @Size(max = 200, message = "{validation.email.size}")
        String email,

        @ValidPersianDate
        String birthDatePersian,

        List<String> roles) {
}
