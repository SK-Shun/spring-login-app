package com.example.demo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.example.demo.validation.PasswordConfirm;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@PasswordConfirm 
public class RegisterRequest {

    @NotBlank(message = "メールアドレスを入力してください")
    @Email(message = "正しいメールアドレスの形式で入力してください")
    @Size(max = 254, message = "メールアドレスは254文字以内で入力してください")
    private String email;

    @NotBlank(message = "ユーザー名を入力してください")
    @Size(min = 1, max = 50, message = "ユーザー名は1文字以上50文字以内で入力してください")
    private String username;

    @NotBlank(message = "パスワードを入力してください")
    @Size(min = 8, max = 100, message = "パスワードは8文字以上100文字以内で入力してください")
    private String password;

    @NotBlank(message = "確認用パスワードを入力してください")
    private String confirmPassword;
}