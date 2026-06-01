package com.example.demo.controller.api.v1;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;

@SpringBootTest
@ActiveProfiles("test")
class UserApiControllerTest {

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
    @DisplayName("GET /api/v1/user/me でログイン中のユーザー情報を取得できる")
    @WithMockUser(username = "test@example.com")
    void getMe_success() throws Exception {
        User mockUser = User.builder()
                .email("test@example.com")
                .username("テストユーザー")
                .password("encodedPassword")
                .enabled(true)
                .build();

        given(userService.findByEmail("test@example.com"))
                .willReturn(mockUser);

        mockMvc.perform(get("/api/v1/user/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.username").value("テストユーザー"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("未認証で GET /api/v1/user/me にアクセスすると401が返る")
    void getMe_unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/user/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/user/register で正常に登録できる")
    void register_success() throws Exception {
        User mockUser = User.builder()
                .email("new@example.com")
                .username("新規ユーザー")
                .password("encodedPassword")
                .enabled(true)
                .build();

        given(userService.register(any()))
                .willReturn(mockUser);

        mockMvc.perform(post("/api/v1/user/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "new@example.com",
                                  "username": "新規ユーザー",
                                  "password": "password123",
                                  "confirmPassword": "password123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("new@example.com"))
                .andExpect(jsonPath("$.username").value("新規ユーザー"));
    }

    @Test
    @DisplayName("DELETE /api/v1/user/me でアカウントを無効化できる")
    @WithMockUser(username = "test@example.com")
    void deleteMe_success() throws Exception {
        willDoNothing().given(userService).disable("test@example.com");

        mockMvc.perform(delete("/api/v1/user/me").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("存在しないユーザーで GET /api/v1/user/me にアクセスすると404が返る")
    @WithMockUser(username = "ghost@example.com")
    void getMe_userNotFound() throws Exception {
        given(userService.findByEmail("ghost@example.com"))
                .willThrow(new com.example.demo.exception.UserNotFoundException("ghost@example.com"));

        mockMvc.perform(get("/api/v1/user/me"))
                .andExpect(status().isNotFound());
    }
}
