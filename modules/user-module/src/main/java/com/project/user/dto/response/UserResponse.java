package com.project.user.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * User as returned to clients.
 *
 * <p>Dates appear twice: the canonical Gregorian value for software and the Persian value
 * for display. The two are never mixed; clients pick the one that fits their use.</p>
 */
public record UserResponse(
        Long id,
        String username,
        String fullName,
        String mobile,
        String email,
        LocalDate birthDate,
        String birthDatePersian,
        String status,
        List<String> roles,
        Instant createdDate,
        String createdDatePersian,
        Instant lastLoginAt) {

    /**
     * @return a copy with the display oriented fields filled in
     */
    public UserResponse withDisplay(String birthDatePersian, String createdDatePersian, List<String> roles) {
        return new UserResponse(id, username, fullName, mobile, email, birthDate, birthDatePersian,
                status, List.copyOf(roles == null ? List.of() : roles), createdDate, createdDatePersian,
                lastLoginAt);
    }
}
