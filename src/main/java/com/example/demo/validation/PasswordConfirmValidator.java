package com.example.demo.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import com.example.demo.dto.request.RegisterRequest;

public class PasswordConfirmValidator
        implements ConstraintValidator<PasswordConfirm, RegisterRequest> {

    @Override
    public boolean isValid(RegisterRequest request, ConstraintValidatorContext context) {

        if (request.getPassword() == null || request.getConfirmPassword() == null) {
            return true;
        }

        boolean isValid = request.getPassword().equals(request.getConfirmPassword());

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("パスワードと確認用パスワードが一致しません")
                    .addPropertyNode("confirmPassword")
                    .addConstraintViolation();
        }

        return isValid;
    }
}