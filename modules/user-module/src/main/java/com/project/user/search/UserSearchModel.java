package com.project.user.search;

import com.project.common.core.search.BaseSearchModel;
import com.project.common.core.search.MatchOperator;
import com.project.common.search.annotation.Searchable;
import com.project.common.search.annotation.SearchableModel;

/**
 * Search criteria for users.
 *
 * <p>Only annotated fields reach the query surface. {@code passwordHash} is intentionally
 * absent, so no request can filter or sort by it.</p>
 */
@SearchableModel(
        dateColumn = "CREATED_DATE",
        dateProperty = "createdDate",
        dateColumnType = SearchableModel.DateColumnType.TIMESTAMP)
public class UserSearchModel extends BaseSearchModel {

    @Searchable(column = "USERNAME", sortable = true, keywordSearch = true, operator = MatchOperator.LIKE)
    private String username;

    @Searchable(column = "FULL_NAME", sortable = true, keywordSearch = true, operator = MatchOperator.LIKE)
    private String fullName;

    @Searchable(column = "MOBILE", operator = MatchOperator.STARTS_WITH)
    private String mobile;

    @Searchable(column = "EMAIL", operator = MatchOperator.LIKE)
    private String email;

    @Searchable(column = "STATUS", sortable = true)
    private String status;

    @Searchable(column = "BIRTH_DATE")
    private java.time.LocalDate birthDate;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public java.time.LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(java.time.LocalDate birthDate) {
        this.birthDate = birthDate;
    }
}
