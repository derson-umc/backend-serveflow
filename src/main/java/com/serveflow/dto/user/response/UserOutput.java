package com.serveflow.dto.user.response;

import com.serveflow.model.user.UserRole;
import com.serveflow.model.user.User;
import com.serveflow.util.EmailMaskUtils;

public record UserOutput(Long id, String username, String email, UserRole role, String jobposition) {

    public static UserOutput from(User user) {
        return new UserOutput(
                user.getId(),
                user.getUsername(),
                EmailMaskUtils.mask(user.getEmail()),
                user.getRole(),
                user.getJobposition()
        );
    }
}
