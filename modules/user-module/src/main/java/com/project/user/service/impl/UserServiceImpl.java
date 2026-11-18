package com.project.user.service.impl;

import com.project.common.core.error.BusinessException;
import com.project.common.core.error.DuplicateResourceException;
import com.project.common.core.error.ValidationException;
import com.project.common.core.paging.PageResult;
import com.project.common.datetime.converter.PersianDateConverter;
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
import com.project.user.entity.UserRole;
import com.project.user.entity.UserStatus;
import com.project.user.error.UserErrorCodes;
import com.project.user.interface_.dao.UserDao;
import com.project.user.interface_.dao.UserReportDao;
import com.project.user.interface_.service.UserService;
import com.project.user.mapper.UserMapper;
import com.project.user.search.UserSearchModel;
import com.project.user.view.UserListItemView;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Reference implementation of a business service.
 *
 * <p>Shows the intended shape: the controller is thin, the service owns rules and
 * transactions, persistence goes through DAOs, dates are converted once at the boundary,
 * duplicates are reported with a stable business code and important changes are audited.</p>
 */
public class UserServiceImpl implements UserService {

    private static final String ENTITY_NAME = "USER";

    private final UserDao userDao;
    private final UserReportDao userReportDao;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final PersianDateParser persianDateParser;
    private final PersianDateConverter persianDateConverter;
    private final PersianDateFormatter persianDateFormatter;
    private final AuditRecorder auditRecorder;

    public UserServiceImpl(UserDao userDao,
                           UserReportDao userReportDao,
                           UserRoleRepository userRoleRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           UserMapper userMapper,
                           PersianDateParser persianDateParser,
                           PersianDateConverter persianDateConverter,
                           PersianDateFormatter persianDateFormatter,
                           AuditRecorder auditRecorder) {
        this.userDao = Objects.requireNonNull(userDao, "userDao must not be null");
        this.userReportDao = Objects.requireNonNull(userReportDao, "userReportDao must not be null");
        this.userRoleRepository = Objects.requireNonNull(userRoleRepository, "userRoleRepository must not be null");
        this.roleRepository = Objects.requireNonNull(roleRepository, "roleRepository must not be null");
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder, "passwordEncoder must not be null");
        this.userMapper = Objects.requireNonNull(userMapper, "userMapper must not be null");
        this.persianDateParser = Objects.requireNonNull(persianDateParser, "persianDateParser must not be null");
        this.persianDateConverter = Objects.requireNonNull(persianDateConverter, "persianDateConverter must not be null");
        this.persianDateFormatter = Objects.requireNonNull(persianDateFormatter, "persianDateFormatter must not be null");
        this.auditRecorder = Objects.requireNonNull(auditRecorder, "auditRecorder must not be null");
    }

    @Override
    @Transactional
    public UserResponse create(CreateUserRequest request) {
        if (userDao.existsByUsername(request.username())) {
            throw new DuplicateResourceException(UserErrorCodes.DUPLICATE_USERNAME, request.username());
        }
        if (userDao.existsByMobile(request.mobile())) {
            throw new DuplicateResourceException(UserErrorCodes.DUPLICATE_MOBILE, request.mobile());
        }

        User user = new User(request.username().trim(), passwordEncoder.encode(request.password()),
                request.fullName().trim());
        user.setMobile(blankToNull(request.mobile()));
        user.setEmail(blankToNull(request.email()));
        user.setBirthDate(parseBirthDate(request.birthDatePersian()));

        User saved = userDao.save(user);
        if (request.roles() != null && !request.roles().isEmpty()) {
            assignRolesInternal(saved.getId(), Set.copyOf(request.roles()));
        }

        auditRecorder.record("USER_CREATE", ENTITY_NAME, String.valueOf(saved.getId()),
                null, toAuditValue(saved), null);

        return toResponse(saved);
    }

    @Override
    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request) {
        User user = userDao.requireById(id);
        String before = toAuditValue(user);

        user.setFullName(request.fullName().trim());
        user.setMobile(blankToNull(request.mobile()));
        user.setEmail(blankToNull(request.email()));
        if (request.birthDatePersian() != null && !request.birthDatePersian().isBlank()) {
            user.setBirthDate(parseBirthDate(request.birthDatePersian()));
        }
        if (request.status() != null) {
            user.setStatus(request.status());
        }

        User saved = userDao.save(user);
        auditRecorder.record("USER_UPDATE", ENTITY_NAME, String.valueOf(id), before, toAuditValue(saved), null);

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return toResponse(userDao.requireById(id));
    }

    @Override
    @Transactional
    public void changeStatus(Long id, UserStatus status) {
        User user = userDao.requireById(id);
        String before = user.getStatus().name();
        user.setStatus(status);
        userDao.save(user);
        auditRecorder.record("USER_STATUS_CHANGE", ENTITY_NAME, String.valueOf(id), before, status.name(), null);
    }

    @Override
    @Transactional
    public void assignRoles(Long id, Set<String> roleCodes) {
        userDao.requireById(id);
        String before = currentRoleCodes(id).toString();
        assignRolesInternal(id, roleCodes);
        auditRecorder.record("USER_ROLE_ASSIGN", ENTITY_NAME, String.valueOf(id), before,
                roleCodes == null ? "[]" : roleCodes.toString(), null);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        userDao.requireById(id);
        userRoleRepository.deleteByUserId(id);
        userDao.deleteById(id);
        auditRecorder.record("USER_DELETE", ENTITY_NAME, String.valueOf(id), null, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<UserResponse> search(UserSearchModel searchModel) {
        return userDao.search(searchModel).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<UserListItemView> report(UserSearchModel searchModel) {
        return userReportDao.search(searchModel);
    }

    // ------------------------------------------------------------------ helpers

    private void assignRolesInternal(Long userId, Set<String> roleCodes) {
        userRoleRepository.deleteByUserId(userId);
        if (roleCodes == null || roleCodes.isEmpty()) {
            return;
        }
        List<Role> roles = roleRepository.findByCodeIn(roleCodes);
        if (roles.size() != roleCodes.size()) {
            throw new BusinessException(UserErrorCodes.UNKNOWN_ROLE);
        }
        roles.forEach(role -> userRoleRepository.save(new UserRole(userId, role.getId())));
    }

    private List<String> currentRoleCodes(Long userId) {
        List<Long> roleIds = userRoleRepository.findByUserId(userId).stream()
                .map(UserRole::getRoleId)
                .toList();
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return roleRepository.findByIdIn(roleIds).stream().map(Role::getCode).toList();
    }

    private UserResponse toResponse(User user) {
        List<String> roles = currentRoleCodes(user.getId());
        UserResponse mapped = userMapper.toResponse(user);
        return mapped.withDisplay(
                user.getBirthDate() == null ? null : persianDateFormatter.format(user.getBirthDate()),
                user.getCreatedDate() == null ? null : persianDateFormatter.format(user.getCreatedDate()),
                roles);
    }

    private LocalDate parseBirthDate(String persianDate) {
        if (persianDate == null || persianDate.isBlank()) {
            return null;
        }
        try {
            return persianDateConverter.toGregorian(
                    persianDateParser.parse(persianDate, "birthDatePersian"));
        } catch (RuntimeException ex) {
            throw new ValidationException("birthDatePersian", UserErrorCodes.INVALID_BIRTH_DATE);
        }
    }

    /**
     * Audit snapshot. Only non sensitive fields; the password hash never leaves the entity.
     */
    private static String toAuditValue(User user) {
        return "username=" + user.getUsername()
                + ", fullName=" + user.getFullName()
                + ", mobile=" + user.getMobile()
                + ", status=" + user.getStatus();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
