package com.innowise.UserService.security.utils;

import com.innowise.UserService.entity.AppUser;
import com.innowise.UserService.entity.User;
import com.innowise.UserService.repository.AppUserRepository;
import com.innowise.UserService.security.service.AppUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    public AppUser getCurrentAppUser(AppUserRepository appUserRepository) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof AppUserDetails appUserDetails) {
            return appUserDetails.getAppUser();
        }

        if (principal instanceof org.springframework.security.core.userdetails.User springUser) {
            String username = springUser.getUsername();
            return appUserRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Current user not found"));
        }

        throw new IllegalStateException("Unknown principal type: " + principal.getClass());
    }

    public Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof AppUserDetails details)) {
            throw new IllegalStateException("User is not authenticated");
        }

        AppUser appUser = details.getAppUser();
        User user = appUser.getUser();

        if (user == null) {
            throw new IllegalStateException("No user profile linked to this account");
        }

        return user.getId();
    }

    public boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(AppUser.Role.ADMIN.name()));
    }
}