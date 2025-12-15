package com.innowise.UserService.service.security;

import com.innowise.UserService.entity.AppUser;
import com.innowise.UserService.entity.User;
import com.innowise.UserService.security.service.AppUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service("userSecurityService")
@RequiredArgsConstructor
public class UserSecurityService {

    public boolean canManageUser(Authentication auth, Long userId) {
        if (isAdmin(auth)) return true;

        User currentUser = getCurrentUser(auth);
        return currentUser != null && currentUser.getId().equals(userId);
    }

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(AppUser.Role.ADMIN.name()));
    }

    private User getCurrentUser(Authentication auth) {
        if (!(auth.getPrincipal() instanceof AppUserDetails details)) {
            return null;
        }
        return details.getAppUser().getUser();
    }
}
