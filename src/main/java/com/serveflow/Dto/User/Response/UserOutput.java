package com.serveflow.Dto.User.Response;

import com.serveflow.Model.User.UserRole;
import com.serveflow.Model.User.User;

public record UserOutput(Long id, String username, UserRole role) {

    public static UserOutput from(User user) {
        return new UserOutput(user.getId(), user.getUsername(), user.getRole());
    }
}
