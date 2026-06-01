package com.example.demo.service;

import com.example.demo.dto.request.RegisterRequest;
import com.example.demo.entity.User;
import com.example.demo.exception.DuplicateEmailException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private RegisterRequest request;

    @BeforeEach
    void setUp() {
        request = new RegisterRequest();
        request.setEmail("new@example.com");
        request.setUsername("新規ユーザー");
        request.setPassword("password123");
        request.setConfirmPassword("password123");
    }

    @Test
    @DisplayName("正常にユーザー登録できる")
    void register_success() {
        given(userRepository.existsByEmail(anyString())).willReturn(false);
        given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willAnswer(inv -> inv.getArgument(0));

        User result = userService.register(request);

        assertThat(result.getEmail()).isEqualTo("new@example.com");
        assertThat(result.getUsername()).isEqualTo("新規ユーザー");
        assertThat(result.getPassword()).isEqualTo("encodedPassword");
        assertThat(result.isEnabled()).isTrue();

        verify(userRepository).existsByEmail("new@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("重複メールアドレスで登録するとDuplicateEmailExceptionが発生する")
    void register_duplicateEmail_throwsException() {
        given(userRepository.existsByEmail(anyString())).willReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(DuplicateEmailException.class);

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("メールアドレスでユーザーを取得できる")
    void findByEmail_success() {
        User mockUser = User.builder()
                .email("test@example.com")
                .username("テストユーザー")
                .password("encodedPassword")
                .enabled(true)
                .build();

        given(userRepository.findByEmail("test@example.com"))
                .willReturn(Optional.of(mockUser));

        User result = userService.findByEmail("test@example.com");

        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("存在しないメールアドレスでUserNotFoundExceptionが発生する")
    void findByEmail_notFound() {
        given(userRepository.findByEmail(anyString()))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByEmail("notfound@example.com"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("アカウントを無効化できる")
    void disable_success() {
        User mockUser = User.builder()
                .email("test@example.com")
                .username("テストユーザー")
                .password("encodedPassword")
                .enabled(true)
                .build();

        given(userRepository.findByEmail("test@example.com"))
                .willReturn(Optional.of(mockUser));
        given(userRepository.save(any(User.class)))
                .willReturn(mockUser);

        userService.disable("test@example.com");

        assertThat(mockUser.isEnabled()).isFalse();
        verify(userRepository).save(mockUser);
    }

    @Test
    @DisplayName("存在しないメールアドレスで無効化するとUserNotFoundExceptionが発生する")
    void disable_userNotFound() {
        given(userRepository.findByEmail(anyString()))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.disable("notfound@example.com"))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).save(any());
    }
}
