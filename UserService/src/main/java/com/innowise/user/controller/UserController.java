package com.innowise.user.controller;

import com.innowise.common.dto.UserDto;
import com.innowise.user.dto.CreateUserRequest;
import lombok.RequiredArgsConstructor;
import com.innowise.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDto> createUserProfile(@Valid @RequestBody CreateUserRequest request) {
        UserDto userDto = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }


    @GetMapping
    public ResponseEntity<?> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        Jwt jwt = (Jwt) authentication.getPrincipal();
        String role = jwt.getClaim("role");
        Long currentUserId = jwt.getClaim("userId");

        if ("ADMIN".equals(role)) {
            Page<UserDto> users = userService.getAllUsers(page, size);
            return ResponseEntity.ok(users);
        } else {
            UserDto user = userService.getUserById(currentUserId);
            Page<UserDto> userPage = new PageImpl<>(List.of(user), PageRequest.of(0, 1), 1);
            return ResponseEntity.ok(userPage);
        }
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