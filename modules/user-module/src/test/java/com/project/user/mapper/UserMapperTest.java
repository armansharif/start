package com.project.user.mapper;

import com.project.user.dto.response.UserResponse;
import com.project.user.entity.User;
import com.project.user.entity.UserStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper mapper = new UserMapperImpl();

    @Test
    void mapsPersistentFieldsAndLeavesDisplayFieldsForTheService() {
        User user = new User("ali", "{bcrypt}hash", "Ali Rezaei");
        user.setMobile("09121234567");
        user.setEmail("ali@example.com");
        user.setStatus(UserStatus.LOCKED);

        UserResponse response = mapper.toResponse(user);

        assertThat(response.username()).isEqualTo("ali");
        assertThat(response.fullName()).isEqualTo("Ali Rezaei");
        assertThat(response.mobile()).isEqualTo("09121234567");
        assertThat(response.email()).isEqualTo("ali@example.com");
        assertThat(response.status()).isEqualTo("LOCKED");
        assertThat(response.birthDatePersian()).isNull();
        assertThat(response.roles()).isNull();
    }

    @Test
    void doesNotExposeThePasswordHash() {
        User user = new User("ali", "{bcrypt}secret-hash", "Ali");

        assertThat(mapper.toResponse(user).toString()).doesNotContain("secret-hash");
    }
}
