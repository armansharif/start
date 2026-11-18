package com.project.user.service.impl;

import com.project.common.core.error.BusinessException;
import com.project.common.core.error.DuplicateResourceException;
import com.project.common.core.error.ValidationException;
import com.project.common.core.paging.PageResult;
import com.project.common.datetime.converter.PersianDateConverter;
import com.project.common.datetime.converter.Time4JPersianDateConverter;
import com.project.common.datetime.formatter.PersianDateFormatter;
import com.project.common.datetime.parser.PersianDateParser;
import com.project.common.logging.audit.AuditRecorder;
import com.project.user.dao.RoleRepository;
import com.project.user.dao.UserRoleRepository;
import com.project.user.dto.request.CreateUserRequest;
import com.project.user.dto.request.UpdateUserRequest;
import com.project.user.dto.response.UserResponse;
import com.project.user.entity.Role;
import com.project.user.entity.User;
import com.project.user.entity.UserStatus;
import com.project.user.interface_.dao.UserDao;
import com.project.user.interface_.dao.UserReportDao;
import com.project.user.mapper.UserMapperImpl;
import com.project.user.search.UserSearchModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceImplTest {

    private UserDao userDao;
    private UserReportDao userReportDao;
    private UserRoleRepository userRoleRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;
    private UserServiceImpl service;

    private final PersianDateConverter converter = new Time4JPersianDateConverter();
    private final PersianDateParser parser = new PersianDateParser(converter);
    private final PersianDateFormatter formatter = new PersianDateFormatter(converter, true);

    @BeforeEach
    void setUp() {
        userDao = mock(UserDao.class);
        userReportDao = mock(UserReportDao.class);
        userRoleRepository = mock(UserRoleRepository.class);
        roleRepository = mock(RoleRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        when(passwordEncoder.encode(anyString())).thenReturn("{bcrypt}encoded");
        when(userDao.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service = new UserServiceImpl(userDao, userReportDao, userRoleRepository, roleRepository,
                passwordEncoder, new UserMapperImpl(), parser, converter, formatter, mock(AuditRecorder.class));
    }

    @Test
    void createHashesThePasswordAndParsesThePersianBirthDate() {
        UserResponse response = service.create(new CreateUserRequest(
                "ali", "Secret!123", "Ali Rezaei", "09121234567", "ali@example.com", "1370/01/01", null));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userDao).save(saved.capture());
        assertThat(saved.getValue().getPasswordHash()).isEqualTo("{bcrypt}encoded");
        assertThat(saved.getValue().getBirthDate()).isEqualTo(LocalDate.of(1991, 3, 21));
        assertThat(response.status()).isEqualTo(UserStatus.ACTIVE.name());
        assertThat(response.fullName()).isEqualTo("Ali Rezaei");
    }

    @Test
    void createRejectsADuplicateUsername() {
        when(userDao.existsByUsername("ali")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CreateUserRequest(
                "ali", "Secret!123", "Ali", null, null, null, null)))
                .isInstanceOf(DuplicateResourceException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode().code())
                        .isEqualTo("USER-1001"));

        verify(userDao, never()).save(any(User.class));
    }

    @Test
    void createRejectsADuplicateMobile() {
        when(userDao.existsByMobile("09121234567")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CreateUserRequest(
                "ali", "Secret!123", "Ali", "09121234567", null, null, null)))
                .isInstanceOf(DuplicateResourceException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode().code())
                        .isEqualTo("USER-1002"));
    }

    @Test
    void createRejectsAnUnknownRole() {
        when(roleRepository.findByCodeIn(Set.of("GHOST"))).thenReturn(List.of());

        assertThatThrownBy(() -> service.create(new CreateUserRequest(
                "ali", "Secret!123", "Ali", null, null, null, List.of("GHOST"))))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode().code())
                        .isEqualTo("USER-1003"));
    }

    @Test
    void createAssignsExistingRoles() {
        when(roleRepository.findByCodeIn(Set.of("ADMIN"))).thenReturn(List.of(new Role("ADMIN", "Administrator")));

        service.create(new CreateUserRequest("ali", "Secret!123", "Ali", null, null, null, List.of("ADMIN")));

        verify(userRoleRepository).save(any());
    }

    @Test
    void updateParsesThePersianBirthDate() {
        User user = new User("ali", "{bcrypt}hash", "Ali");
        when(userDao.requireById(7L)).thenReturn(user);

        service.update(7L, new UpdateUserRequest("Ali Rezaei", "09120000000", null, "1400/01/01", null));

        assertThat(user.getBirthDate()).isEqualTo(LocalDate.of(2021, 3, 21));
        assertThat(user.getFullName()).isEqualTo("Ali Rezaei");
        assertThat(user.getMobile()).isEqualTo("09120000000");
    }

    @Test
    void updateRejectsAnInvalidPersianDate() {
        when(userDao.requireById(7L)).thenReturn(new User("ali", "{bcrypt}hash", "Ali"));

        assertThatThrownBy(() -> service.update(7L,
                new UpdateUserRequest("Ali", null, null, "1400/13/01", null)))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void getByIdFillsThePersianDisplayValues() {
        User user = new User("ali", "{bcrypt}hash", "Ali");
        user.setBirthDate(LocalDate.of(2021, 3, 21));
        when(userDao.requireById(7L)).thenReturn(user);
        when(roleRepository.findByIdIn(any())).thenReturn(List.of(new Role("ADMIN", "Administrator")));
        when(userRoleRepository.findByUserId(any()))
                .thenReturn(List.of(new com.project.user.entity.UserRole(null, null)));

        UserResponse response = service.getById(7L);

        assertThat(response.birthDatePersian()).isEqualTo(formatter.format(LocalDate.of(2021, 3, 21)));
        assertThat(response.roles()).containsExactly("ADMIN");
    }

    @Test
    void searchMapsThePageToResponses() {
        User user = new User("ali", "{bcrypt}hash", "Ali");
        when(userDao.search(any())).thenReturn(PageResult.of(List.of(user), 0, 20, 1));

        PageResult<UserResponse> result = service.search(new UserSearchModel());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).username()).isEqualTo("ali");
    }

    @Test
    void deleteRemovesRoleLinksBeforeTheUser() {
        when(userDao.requireById(7L)).thenReturn(new User("ali", "{bcrypt}hash", "Ali"));

        service.delete(7L);

        verify(userRoleRepository).deleteByUserId(7L);
        verify(userDao).deleteById(7L);
    }

    @Test
    void formatterUsesTheConfiguredZoneForInstants() {
        assertThat(formatter.format(Instant.parse("2021-03-21T10:00:00Z"))).isNotBlank();
    }
}
