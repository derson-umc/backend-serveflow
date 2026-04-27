package com.serveflow.Service.User;

public interface EmailService {

    void sendPasswordResetEmail(String recipient, String username, String resetToken);
}
