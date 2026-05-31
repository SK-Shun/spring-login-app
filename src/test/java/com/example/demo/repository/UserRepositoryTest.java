package com.example.demo.repository;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import com.example.demo.entity.User;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        User testUser = User.builder()
                .email("test@example.com")
                .username("テストユーザー")
                .password("encodedPassword")
                .enabled(true)
                .build();
        userRepository.save(testUser);
    }

    @Test
    @DisplayName("メールアドレスでユーザーを取得できる")
    void findByEmail_success() {
        var result = userRepository.findByEmail("test@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
        assertThat(result.get().getUsername()).isEqualTo("テストユーザー");
    }

    @Test
    @DisplayName("存在しないメールアドレスはemptyを返す")
    void findByEmail_notFound() {
        var result = userRepository.findByEmail("notfound@example.com");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("登録済みメールアドレスはtrueを返す")
    void existsByEmail_true() {
        assertThat(userRepository.existsByEmail("test@example.com")).isTrue();
    }

    @Test
    @DisplayName("未登録メールアドレスはfalseを返す")
    void existsByEmail_false() {
        assertThat(userRepository.existsByEmail("notfound@example.com")).isFalse();
    }

    @Test
    @DisplayName("ユーザーを保存するとcreatedAtとupdatedAtが自動設定される")
    void save_timestampsAreSet() {
        var result = userRepository.findByEmail("test@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getCreatedAt()).isNotNull();
        assertThat(result.get().getUpdatedAt()).isNotNull();
    }
}