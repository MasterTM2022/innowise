package com.innowise.UserService.service;

import com.innowise.UserService.dto.UserDto;
import com.innowise.UserService.entity.User;
import com.innowise.UserService.mapper.UserMapper;
import com.innowise.UserService.repository.CardRepository;
import com.innowise.UserService.repository.UserRepository;
import com.innowise.UserService.service.exception.*;
import jakarta.transaction.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private UserMapper userMapper;

    // Create
    @Transactional
    public UserDto createUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Email " + user.getEmail() + " already exists");
        }
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