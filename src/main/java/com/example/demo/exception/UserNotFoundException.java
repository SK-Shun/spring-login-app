package com.example.demo.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long id) {
        super("ユーザーが見つかりません: id=" + id);
    }

    public UserNotFoundException(String email) {
        super("ユーザーが見つかりません: email=" + email);
    }
}