package com.example.demo.controller.web;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.example.demo.TestcontainersConfiguration;
import com.example.demo.repository.UserRepository;

@SpringBootTest
@ActiveProfiles("integration")
@Import(TestcontainersConfiguration.class)
class RegisterControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        userRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /register で登録画面が表示される")
    void showRegisterForm() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("registerRequest"));
    }

    @Test
    @DisplayName("正常な入力で登録するとDBにレコードが作成されログイン画面にリダイレクトされる")
    void register_success() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("email", "new@example.com")
                        .param("username", "新規ユーザー")
                        .param("password", "password123")
                        .param("confirmPassword", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        assertThat(userRepository.existsByEmail("new@example.com")).isTrue();

        var user = userRepository.findByEmail("new@example.com");
        assertThat(user).isPresent();
        assertThat(user.get().getPassword()).startsWith("$2a$");
    }

    @Test
    @DisplayName("メールアドレス未入力でバリデーションエラーになりDBに登録されない")
    void register_emailBlank_validationError() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("email", "")
                        .param("username", "新規ユーザー")
                        .param("password", "password123")
                        .param("confirmPassword", "password123"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors("registerRequest", "email"));

        assertThat(userRepository.existsByEmail("")).isFalse();
    }

    @Test
    @DisplayName("パスワード不一致でバリデーションエラーになりDBに登録されない")
    void register_passwordMismatch_validationError() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("email", "new@example.com")
                        .param("username", "新規ユーザー")
                        .param("password", "password123")
                        .param("confirmPassword", "different"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors("registerRequest", "confirmPassword"));

        assertThat(userRepository.existsByEmail("new@example.com")).isFalse();
    }

    @Test
    @DisplayName("重複メールアドレスで登録するとフィールドエラーになりDBに重複登録されない")
    void register_duplicateEmail() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("email", "dup@example.com")
                        .param("username", "ユーザー1")
                        .param("password", "password123")
                        .param("confirmPassword", "password123"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("email", "dup@example.com")
                        .param("username", "ユーザー2")
                        .param("password", "password123")
                        .param("confirmPassword", "password123"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors("registerRequest", "email"));

        assertThat(userRepository.findAll())
                .hasSize(1)
                .extracting("email")
                .containsExactly("dup@example.com");
    }

    @Test
    @DisplayName("登録後にログインできる")
    void register_thenLogin() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("email", "login@example.com")
                        .param("username", "ログインユーザー")
                        .param("password", "password123")
                        .param("confirmPassword", "password123"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "login@example.com")
                        .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user"));
    }
}