package com.project.user.entity;

import com.project.common.persistence.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Fine grained permission such as {@code user:create}.
 *
 * <p>Authorities are checked in the backend with {@code hasAuthority(...)}; hiding UI
 * elements is never the control.</p>
 */
@Entity
@Table(name = "PERMISSIONS")
public class Permission extends BaseEntity {

    @Column(name = "CODE", nullable = false, length = 100, unique = true)
    private String code;

    @Column(name = "TITLE", nullable = false, length = 150)
    private String title;

    protected Permission() {
        // for JPA
    }

    public Permission(String code, String title) {
        this.code = code;
        this.title = title;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }
}
