package com.innowise.user.service;

import com.innowise.common.dto.UserDto;
import com.innowise.user.dto.CreateUserRequest;
import com.innowise.user.entity.User;
import com.innowise.user.mapper.UserMapper;
import com.innowise.user.repository.UserRepository;
import com.innowise.user.exception.EmailAlreadyExistsException;
import com.innowise.user.exception.UserNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    // Create
    @Transactional
    public UserDto createUser(CreateUserRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new EmailAlreadyExistsException("Email " + request.email() + " already exists");
        }
        User user = new User();
        user.setName(request.name());
        user.setSurname(request.surname());
        user.setBirthDate(request.birthDate());
        user.setEmail(request.email());
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    // Get by ID
    @Cacheable(value = "users", key = "#id")
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));
        return userMapper.toDto(user);
    }

    // Get all with pagination
    public Page<UserDto> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> usersPage = userRepository.findAll(pageable);
        return usersPage.map(userMapper::toDto);
    }

    // Get by email
    @Cacheable(value = "users", key = "#email")
    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email " + email + " not found"));
        return userMapper.toDto(user);
    }

    // Update
    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public UserDto updateUser(Long id, UserDto userDetailsDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Optional<User> existingUserWithSameEmail = userRepository.findByEmail(userDetailsDto.getEmail());

        if (existingUserWithSameEmail.isPresent() &&
                !existingUserWithSameEmail.get().getId().equals(user.getId())) {
            throw new EmailAlreadyExistsException("Email " + userDetailsDto.getEmail() + " already exists");
        }

        user.setName(userDetailsDto.getName());
        user.setSurname(userDetailsDto.getSurname());
        user.setBirthDate(userDetailsDto.getBirthDate());
        user.setEmail(userDetailsDto.getEmail());

        User updatedUser = userRepository.save(user);

        return userMapper.toDto(updatedUser);
    }

    // Delete
    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found");
        }
        userRepository.deleteById(id);
    }
}