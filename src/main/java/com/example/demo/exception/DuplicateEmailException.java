package com.example.demo.exception;

public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String email) {
        super("このメールアドレスはすでに登録されています: " + email);
    }
}