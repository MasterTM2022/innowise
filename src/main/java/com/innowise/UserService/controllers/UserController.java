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

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final AppUserRepository appUserRepository;
    private final SecurityUtils securityUtils;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserDto createdUser = userService.createProfileForCurrentUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

//    @PutMapping("/link-profile")
//    @PreAuthorize("#request.userId == authentication.principal.id or hasRole('ADMIN')")
//    public ResponseEntity<Void> linkUserProfile(@RequestBody LinkProfileRequest request) {
//        Long currentAppUserId = securityUtils.getCurrentAppUser(appUserRepository).getId();
//        userService.linkAppUserToExistingUser(currentAppUserId, request.userId());
//        return ResponseEntity.ok().build();
//    }


    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.getAllUsers(page, size));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> searchUser(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String email) {

        if (id != null) {
            return ResponseEntity.status(HttpStatus.OK).body(userService.getUserById(id));
        }

        if (email != null) {
            return ResponseEntity.status(HttpStatus.OK).body(userService.getUserByEmail(email));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Required parameter: 'id' or 'email'");
    }

    @PutMapping("/{id}")
    @PreAuthorize("#id == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        UserDto updatedUser = userService.updateUser(id, userDto);
        return ResponseEntity.status(HttpStatus.OK).body(updatedUser);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("#id == authentication.principal.appUser.id or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}