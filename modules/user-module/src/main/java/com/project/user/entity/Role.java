package com.project.user.entity;

import com.project.common.persistence.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Role assigned to users. Permissions are attached through {@code ROLE_PERMISSION}.
 */
@Entity
@Table(name = "ROLES")
public class Role extends BaseEntity {

    @Column(name = "CODE", nullable = false, length = 50, unique = true)
    private String code;

    @Column(name = "TITLE", nullable = false, length = 100)
    private String title;

    protected Role() {
        // for JPA
    }

    public Role(String code, String title) {
        this.code = code;
        this.title = title;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
