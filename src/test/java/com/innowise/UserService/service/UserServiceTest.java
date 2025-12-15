package com.innowise.UserService.service;

import com.innowise.UserService.dto.UserDto;
import com.innowise.UserService.entity.AppUser;
import com.innowise.UserService.entity.User;
import com.innowise.UserService.mapper.UserMapper;
import com.innowise.UserService.repository.AppUserRepository;
import com.innowise.UserService.repository.UserRepository;
import com.innowise.UserService.service.exception.AppUserNotFoundException;
import com.innowise.UserService.service.exception.EmailAlreadyExistsException;
import com.innowise.UserService.service.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.*;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;


    @Test
    void getUserById_ShouldReturnUserDto_WhenUserExists() {
        // Given
        Long id = 1L;
        String name = "Ivan";
        String surname = "Ivanov";
        LocalDate birthDate = LocalDate.of(1990, 5, 15);
        String email = "ivan@example.com";

        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setSurname(surname);
        user.setBirthDate(birthDate);
        user.setEmail(email);

        UserDto userDto = new UserDto(id, name, surname, birthDate, email);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userDto);

        // When
        UserDto result = userService.getUserById(id);

        // Then
        assertEquals(userDto, result);
        verify(userRepository).findById(id);
        verify(userMapper).toDto(user);
    }

    @Test
    void getUserById_ShouldThrowException_WhenUserDoesNotExist() {
        // Given
        Long id = 999L;
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(UserNotFoundException.class, () -> userService.getUserById(id));
        verify(userRepository).findById(id);
    }

    @Test
    void createUser_ShouldThrowException_WhenEmailAlreadyExists() {
        // Given
        User user = new User();
        user.setEmail("ivan@example.com");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        // When / Then
        assertThrows(EmailAlreadyExistsException.class, () -> userService.createUser(user));
        verify(userRepository).findByEmail(user.getEmail());
    }

    @Test
    void updateUser_ShouldThrowException_WhenEmailAlreadyExists() {
        // Given
        Long id = 1L;
        Long idExisting = 2L;
        String email = "ivan@example.com";

        String name = "Ivan";
        String surname = "Ivanov";
        LocalDate birthDate = LocalDate.of(1990, 5, 15);
        UserDto userDto = new UserDto(id,  name, surname, birthDate, email);

        User existingUser = new User();
        existingUser.setId(idExisting);
        existingUser.setEmail(email);

        User currentUser = new User();
        currentUser.setId(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(currentUser));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

        // When / Then
        assertThrows(EmailAlreadyExistsException.class, () -> userService.updateUser(id, userDto));
    }

    @Test
    void getAllUsers_ShouldReturnPageOfUserDtos_WhenCalled() {
        // Given
        int page = 0;
        int size = 10;

        User user1 = new User();
        user1.setId(1L);
        user1.setName("Ivan");
        user1.setSurname("Ivanov");
        user1.setEmail("ivan@example.com");
        user1.setBirthDate(LocalDate.of(1990, 5, 15));

        User user2 = new User();
        user2.setId(2L);
        user2.setName("Petr");
        user2.setSurname("Petrov");
        user2.setEmail("petr@example.com");
        user2.setBirthDate(LocalDate.of(1985, 7, 20));

        List<User> userList = Arrays.asList(user1, user2);
        Page<User> userPage = new PageImpl<>(userList);

        UserDto userDto1 = new UserDto(1L, "Ivan", "Ivanov", LocalDate.of(1990, 5, 15), "ivan@example.com");
        UserDto userDto2 = new UserDto(2L, "Petr", "Petrov", LocalDate.of(1985, 7, 20), "petr@example.com");

        List<UserDto> userDtoList = Arrays.asList(userDto1, userDto2);
        Page<UserDto> expectedPage = new PageImpl<>(userDtoList);

        Pageable pageable = PageRequest.of(page, size);
        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(userMapper.toDto(user1)).thenReturn(userDto1);
        when(userMapper.toDto(user2)).thenReturn(userDto2);

        // When
        Page<UserDto> result = userService.getAllUsers(page, size);

        // Then
        assertEquals(expectedPage.getContent(), result.getContent());
        assertEquals(expectedPage.getTotalElements(), result.getTotalElements());
        assertEquals(expectedPage.getTotalPages(), result.getTotalPages());
        assertEquals(expectedPage.getNumber(), result.getNumber());
        assertEquals(expectedPage.getSize(), result.getSize());

        verify(userRepository).findAll(pageable);
        verify(userMapper).toDto(user1);
        verify(userMapper).toDto(user2);
    }

    @Test
    void getUserByEmail_ShouldReturnUserDto_WhenUserExists() {
        // Given
        String email = "ivan@example.com";

        User user = new User();
        user.setId(1L);
        user.setName("Ivan");
        user.setSurname("Ivanov");
        user.setEmail(email);
        user.setBirthDate(LocalDate.of(1990, 5, 15));

        UserDto expectedDto = new UserDto(1L, "Ivan", "Ivanov", LocalDate.of(1990, 5, 15), email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(expectedDto);

        // When
        UserDto result = userService.getUserByEmail(email);

        // Then
        assertEquals(expectedDto, result);

        verify(userRepository).findByEmail(email);
        verify(userMapper).toDto(user);
    }

    @Test
    void getUserByEmail_ShouldThrowException_WhenUserDoesNotExist() {
        // Given
        String email = "nonexistent@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(UserNotFoundException.class, () -> userService.getUserByEmail(email));

        verify(userRepository).findByEmail(email);
    }

    @Test
    void deleteUser_ShouldDeleteUser_WhenUserExists() {
        // Given
        Long id = 1L;

        when(userRepository.existsById(id)).thenReturn(true);

        // When
        userService.deleteUser(id);

        // Then
        verify(userRepository).existsById(id);
        verify(userRepository).deleteById(id);
    }

    @Test
    void deleteUser_ShouldThrowException_WhenUserDoesNotExist() {
        // Given
        Long id = 999L;

        when(userRepository.existsById(id)).thenReturn(false);

        // When / Then
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(id));
        verify(userRepository).existsById(id);
        verify(userRepository, never()).deleteById(id);
    }

    @Test
    void linkAppUserToExistingUser_ShouldLinkUser_WhenBothExist() {
        // Given
        Long appUserId = 1L;
        Long userId = 2L;

        AppUser appUser = new AppUser();
        appUser.setId(appUserId);

        User user = new User();
        user.setId(userId);
        user.setName("Ivan");

        when(appUserRepository.findById(appUserId)).thenReturn(Optional.of(appUser));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        userService.linkAppUserToExistingUser(appUserId, userId);

        // Then
        assertThat(appUser.getUser()).isNotNull();
        assertThat(appUser.getUser().getId()).isEqualTo(userId);
        assertThat(appUser.getUser().getName()).isEqualTo("Ivan");

        verify(appUserRepository).findById(appUserId);
        verify(userRepository).findById(userId);
        verify(appUserRepository).save(appUser);
    }

    @Test
    void linkAppUserToExistingUser_ShouldThrowAppUserNotFound_WhenAppUserMissing() {
        // Given
        Long appUserId = 1L;
        Long userId = 2L;

        when(appUserRepository.findById(appUserId)).thenReturn(Optional.empty());

        // When / Then
        AppUserNotFoundException exception = assertThrows(
                AppUserNotFoundException.class,
                () -> userService.linkAppUserToExistingUser(appUserId, userId)
        );

        assertThat(exception.getMessage()).isEqualTo("AppUser not found");
        verify(appUserRepository).findById(appUserId);
        verify(userRepository, never()).findById(any());
        verify(appUserRepository, never()).save(any());
    }

    @Test
    void linkAppUserToExistingUser_ShouldThrowUserNotFound_WhenUserMissing() {
        // Given
        Long appUserId = 1L;
        Long userId = 2L;

        AppUser appUser = new AppUser();
        appUser.setId(appUserId);

        when(appUserRepository.findById(appUserId)).thenReturn(Optional.of(appUser));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When / Then
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.linkAppUserToExistingUser(appUserId, userId)
        );

        assertThat(exception.getMessage()).isEqualTo("User not found");
        verify(appUserRepository).findById(appUserId);
        verify(userRepository).findById(userId);
        verify(appUserRepository, never()).save(any());
    }


}
