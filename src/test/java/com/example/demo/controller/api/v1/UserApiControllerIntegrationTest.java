package com.example.demo.controller.api.v1;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.example.demo.TestcontainersConfiguration;
import com.example.demo.repository.UserRepository;

@SpringBootTest
@ActiveProfiles("integration")
@Import(TestcontainersConfiguration.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserApiControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    private MockMvc mockMvc;

    private static MockHttpSession session;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @Order(1)
    @DisplayName("① ユーザー登録できる")
    void register() throws Exception {
        mockMvc.perform(post("/api/v1/user/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "integration@example.com",
                                  "username": "結合テストユーザー",
                                  "password": "password123",
                                  "confirmPassword": "password123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("integration@example.com"))
                .andExpect(jsonPath("$.username").value("結合テストユーザー"))
                .andExpect(jsonPath("$.password").doesNotExist());

        assertThat(userRepository.existsByEmail("integration@example.com")).isTrue();
    }

    @Test
    @Order(2)
    @DisplayName("② 未認証で /api/v1/user/me にアクセスすると401が返る")
    void getMe_unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/user/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(3)
    @DisplayName("③ 登録したユーザーでログインするとセッションが発行される")
    void login() throws Exception {
        MvcResult result = mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "integration@example.com")
                        .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user"))
                .andReturn();

        session = (MockHttpSession) result.getRequest().getSession();
        assertThat(session).isNotNull();
    }

    @Test
    @Order(4)
    @DisplayName("④ ログイン済みセッションで /api/v1/user/me にアクセスするとユーザー情報が返る")
    void getMe_authenticated() throws Exception {
        mockMvc.perform(get("/api/v1/user/me")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("integration@example.com"))
                .andExpect(jsonPath("$.username").value("結合テストユーザー"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @Order(5)
    @DisplayName("⑤ ログイン済みセッションで DELETE /api/v1/user/me するとアカウントが無効化される")
    void deleteMe() throws Exception {
        mockMvc.perform(delete("/api/v1/user/me")
                        .with(csrf())
                        .session(session))
                .andExpect(status().isNoContent());

        var user = userRepository.findByEmail("integration@example.com");
        assertThat(user).isPresent();
        assertThat(user.get().isEnabled()).isFalse();
    }

    @Test
    @Order(6)
    @DisplayName("⑥ 無効化されたユーザーでログインすると失敗する")
    void login_disabledUser() throws Exception {
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "integration@example.com")
                        .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }
}
