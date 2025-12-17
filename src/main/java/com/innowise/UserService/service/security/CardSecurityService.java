package com.innowise.UserService.service.security;

import com.innowise.UserService.entity.AppUser;
import com.innowise.UserService.entity.User;
import com.innowise.UserService.repository.CardRepository;
import com.innowise.UserService.security.service.AppUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

@Service("cardSecurityService")
@RequiredArgsConstructor
public class CardSecurityService {

    private final CardRepository cardRepository;

    public boolean canViewCard(Authentication authentication, Long cardId) {
        if (isAdmin(authentication)) {
            return true;
        }
        User user = getCurrentUser(authentication);
        return user != null && isCardOwner(cardId, user.getId());
    }

    public boolean canDeleteCard(Authentication authentication, Long cardId) {
        return canViewCard(authentication, cardId);
    }

    private boolean isAdmin(Authentication authentication) {
        if (authentication == null) return false;
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals(AppUser.Role.ADMIN.name()));
    }

    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof AppUserDetails appUserDetails)) {
            return null;
        }

        AppUser appUser = appUserDetails.getAppUser();
        return appUser != null ? appUser.getUser() : null;
    }

    private boolean isCardOwner(Long cardId, Long userId) {
        return cardRepository.findById(cardId)
                .map(card -> card.getUser() != null && card.getUser().getId().equals(userId))
                .orElse(false);
    }
}