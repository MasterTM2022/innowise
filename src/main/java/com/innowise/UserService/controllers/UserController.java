package com.innowise.UserService.controllers;

import lombok.RequiredArgsConstructor;
import com.innowise.UserService.dto.UserDto;
import com.innowise.UserService.entity.User;
import com.innowise.UserService.mapper.UserMapper;
import com.innowise.UserService.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDto userDto) {
        User user = userMapper.toEntity(userDto);
        UserDto createdUserDto = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUserDto);
    }

    @GetMapping
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.getAllUsers(page, size));
    }

    @GetMapping("/search")
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

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        UserDto updatedUser = userService.updateUser(id, userDto);
        return ResponseEntity.status(HttpStatus.OK).body(updatedUser);
    }

    @DeleteMapping("/deleteUser/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
