package com.project.user.dto.request;

import com.project.common.datetime.validator.ValidPersianDate;
import com.project.user.entity.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Payload for updating a user. The username and password are not editable here: renaming
 * an identity and changing a credential are separate, auditable operations.
 */
public record UpdateUserRequest(

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

        UserStatus status) {
}
