package com.project.user.controller;

import com.project.common.core.paging.PageResult;
import com.project.common.web.api.ApiResponse;
import com.project.common.web.api.PageResponse;
import com.project.user.dto.request.AssignRolesRequest;
import com.project.user.dto.request.ChangeStatusRequest;
import com.project.user.dto.request.CreateUserRequest;
import com.project.user.dto.request.UpdateUserRequest;
import com.project.user.dto.response.UserResponse;
import com.project.user.interface_.service.UserService;
import com.project.user.search.UserSearchModel;
import com.project.user.view.UserListItemView;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * User endpoints.
 *
 * <p>The controller validates transport concerns (binding, HTTP status) and delegates
 * everything else. Authorisation is enforced here and, more importantly, in the service
 * layer decisions that depend on data; hiding a button is never a control.</p>
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('user:create')")
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(userService.create(request), "User created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('user:update')")
    public ApiResponse<UserResponse> update(@PathVariable Long id,
                                            @Valid @RequestBody UpdateUserRequest request) {
        return ApiResponse.of(userService.update(id, request), "User updated");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user:read')")
    public ApiResponse<UserResponse> getById(@PathVariable Long id) {
        return ApiResponse.of(userService.getById(id));
    }

    @PostMapping("/search")
    @PreAuthorize("hasAuthority('user:read')")
    public ApiResponse<PageResponse<UserResponse>> search(@RequestBody UserSearchModel searchModel) {
        PageResult<UserResponse> result = userService.search(searchModel);
        return ApiResponse.of(PageResponse.from(result));
    }

    /**
     * Same criteria, different read model: served by SQL and mapped to a flat view.
     */
    @PostMapping("/report")
    @PreAuthorize("hasAuthority('user:read')")
    public ApiResponse<PageResponse<UserListItemView>> report(@RequestBody UserSearchModel searchModel) {
        return ApiResponse.of(PageResponse.from(userService.report(searchModel)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('user:update')")
    public ApiResponse<Void> changeStatus(@PathVariable Long id,
                                          @Valid @RequestBody ChangeStatusRequest request) {
        userService.changeStatus(id, request.status());
        return ApiResponse.of(null, "Status changed");
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('user:assign-role')")
    public ApiResponse<Void> assignRoles(@PathVariable Long id,
                                         @Valid @RequestBody AssignRolesRequest request) {
        userService.assignRoles(id, request.roles());
        return ApiResponse.of(null, "Roles assigned");
    }

    @DeleteMapping("/{id}/roles/{roleCode}")
    @PreAuthorize("hasAuthority('user:assign-role')")
    public ApiResponse<Void> removeRole(@PathVariable Long id, @PathVariable String roleCode) {
        userService.removeRole(id, roleCode);
        return ApiResponse.of(null, "Role removed");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user:delete')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ApiResponse.of(null, "User deleted");
    }
}
