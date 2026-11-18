package com.project.user.entity;

import com.project.common.persistence.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Link between a user and a role, kept as an id pair.
 *
 * <p>The database enforces the foreign keys and the unique pair; the entity only carries
 * the two identifiers, exactly like every other relationship in this platform.</p>
 */
@Entity
@Table(name = "USER_ROLE")
public class UserRole extends BaseEntity {

    @Column(name = "USER_ID", nullable = false, updatable = false)
    private Long userId;

    @Column(name = "ROLE_ID", nullable = false, updatable = false)
    private Long roleId;

    protected UserRole() {
        // for JPA
    }

    public UserRole(Long userId, Long roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getRoleId() {
        return roleId;
    }
}
