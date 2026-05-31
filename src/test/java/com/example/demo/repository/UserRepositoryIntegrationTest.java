package com.example.demo.repository;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.example.demo.TestcontainersConfiguration;
import com.example.demo.entity.User;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("integration")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("PostgreSQLで正常にユーザーを保存・取得できる")
    void save_and_findByEmail() {
        User user = User.builder()
                .email("integration@example.com")
                .username("結合テストユーザー")
                .password("encodedPassword")
                .enabled(true)
                .build();

        userRepository.save(user);

        var result = userRepository.findByEmail("integration@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("integration@example.com");
        assertThat(result.get().getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("PostgreSQLでメールアドレスのユニーク制約が効いている")
    void uniqueConstraint_email() {
        User user1 = User.builder()
                .email("dup@example.com")
                .username("ユーザー1")
                .password("encodedPassword")
                .enabled(true)
                .build();

        User user2 = User.builder()
                .email("dup@example.com")
                .username("ユーザー2")
                .password("encodedPassword")
                .enabled(true)
                .build();

        userRepository.save(user1);

        org.junit.jupiter.api.Assertions.assertThrows(
                Exception.class,
                () -> userRepository.saveAndFlush(user2)
        );
    }
}