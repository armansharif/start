package com.project.user.entity;

import com.project.common.persistence.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Application user.
 *
 * <p>Roles are linked through {@code USER_ROLE} and resolved by the DAO; the entity holds
 * no object relations on purpose (see the persistence architecture rules).</p>
 *
 * <p>{@code birthDate} is stored as a calendar date in the Gregorian calendar. The Persian
 * representation exists only in DTOs and is produced by {@code common-datetime}.</p>
 */
@Entity
@Table(name = "USERS")
public class User extends BaseEntity {

    @Column(name = "USERNAME", nullable = false, length = 100, unique = true)
    private String username;

    @Column(name = "PASSWORD_HASH", nullable = false, length = 200)
    private String passwordHash;

    @Column(name = "FULL_NAME", nullable = false, length = 200)
    private String fullName;

    @Column(name = "MOBILE", length = 20)
    private String mobile;

    @Column(name = "EMAIL", length = 200)
    private String email;

    @Column(name = "BIRTH_DATE")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "LAST_LOGIN_AT")
    private Instant lastLoginAt;

    protected User() {
        // for JPA
    }

    public User(String username, String passwordHash, String fullName) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.status = UserStatus.ACTIVE;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void changePasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public void markLogin(Instant when) {
        this.lastLoginAt = when;
    }
}
