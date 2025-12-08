package com.innowise.UserService.security.service;

import com.innowise.UserService.entity.AppUser;
import com.innowise.UserService.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser appUser = appUserRepository.findByUsername(username).
                orElseThrow(() -> new UsernameNotFoundException("User «" + username + "» not found"));

        return new AppUserDetails(appUser);
    }
}
