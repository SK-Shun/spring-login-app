package com.example.demo.controller.api.v1;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.request.RegisterRequest;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserApiController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(
            @AuthenticationPrincipal UserDetails userDetails) {

        log.debug("GET /api/v1/user/me: email={}", userDetails.getUsername());

        return ResponseEntity.ok(
                UserResponse.from(userService.findByEmail(userDetails.getUsername()))
        );
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @RequestBody @Valid RegisterRequest request) {

        log.debug("POST /api/v1/user/register: email={}", request.getEmail());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(UserResponse.from(userService.register(request)));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe(
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("DELETE /api/v1/user/me: email={}", userDetails.getUsername());

        userService.disable(userDetails.getUsername());

        return ResponseEntity.noContent().build();
    }
}
