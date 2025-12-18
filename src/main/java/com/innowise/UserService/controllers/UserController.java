package com.innowise.UserService.controllers;

import com.innowise.UserService.dto.CreateUserRequest;
import com.innowise.UserService.dto.LinkProfileRequest;
import com.innowise.UserService.entity.AppUser;
import com.innowise.UserService.repository.AppUserRepository;
import com.innowise.UserService.repository.UserRepository;
import com.innowise.UserService.security.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import com.innowise.UserService.dto.UserDto;
import com.innowise.UserService.entity.User;
import com.innowise.UserService.mapper.UserMapper;
import com.innowise.UserService.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserDto createdUser = userService.createProfileForCurrentUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.getAllUsers(page, size));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> searchUser(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String email) {

        if (id != null) {
            return ResponseEntity.status(HttpStatus.OK).body(userService.getUserById(id));
        }
        if (email != null) {
            return ResponseEntity.status(HttpStatus.OK).body(userService.getUserByEmail(email));
        }
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Parameter 'id' and/or email are required");
    }

    @PutMapping("/{id}")
    @PreAuthorize("@userSecurityService.canManageUser(authentication, #id)")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        UserDto updatedUser = userService.updateUser(id, userDto);
        return ResponseEntity.status(HttpStatus.OK).body(updatedUser);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@userSecurityService.canManageUser(authentication, #id)")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}