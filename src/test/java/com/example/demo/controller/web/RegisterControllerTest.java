package com.example.demo.controller.web;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.example.demo.dto.request.RegisterRequest;
import com.example.demo.entity.User;
import com.example.demo.exception.DuplicateEmailException;
import com.example.demo.service.UserService;

@SpringBootTest
@ActiveProfiles("test")
class RegisterControllerTest {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private UserService userService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("GET /register で登録画面が表示される")
    @WithMockUser
    void showRegisterForm() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("registerRequest"));
    }

    @Test
    @DisplayName("正常な入力で登録するとログイン画面にリダイレクトされる")
    @WithMockUser
    void register_success() throws Exception {
        User mockUser = User.builder()
                .email("new@example.com")
                .username("新規ユーザー")
                .password("encodedPassword")
                .enabled(true)
                .build();

        given(userService.register(any(RegisterRequest.class))).willReturn(mockUser);

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("email", "new@example.com")
                        .param("username", "新規ユーザー")
                        .param("password", "password123")
                        .param("confirmPassword", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("メールアドレス未入力でバリデーションエラーになる")
    @WithMockUser
    void register_emailBlank_validationError() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("email", "")
                        .param("username", "新規ユーザー")
                        .param("password", "password123")
                        .param("confirmPassword", "password123"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("registerRequest", "email"));

        verify(userService, never()).register(any());
    }

    @Test
    @DisplayName("パスワード不一致でバリデーションエラーになる")
    @WithMockUser
    void register_passwordMismatch_validationError() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("email", "new@example.com")
                        .param("username", "新規ユーザー")
                        .param("password", "password123")
                        .param("confirmPassword", "different"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("registerRequest", "confirmPassword"));

        verify(userService, never()).register(any());
    }

    @Test
    @DisplayName("重複メールアドレスでフィールドエラーになる")
    @WithMockUser
    void register_duplicateEmail_fieldError() throws Exception {
        given(userService.register(any(RegisterRequest.class)))
                .willThrow(new DuplicateEmailException("new@example.com"));

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("email", "new@example.com")
                        .param("username", "新規ユーザー")
                        .param("password", "password123")
                        .param("confirmPassword", "password123"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors("registerRequest", "email"));
    }
}