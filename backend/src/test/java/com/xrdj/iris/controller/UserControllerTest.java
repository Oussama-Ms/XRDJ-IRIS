package com.xrdj.iris.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xrdj.iris.dto.CreateUserRequest;
import com.xrdj.iris.model.Role;
import com.xrdj.iris.model.User;
import com.xrdj.iris.repository.UserRepository;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private UserRepository userRepository;

    @MockBean private PasswordEncoder passwordEncoder;

    @Autowired private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        User user1 =
                User.builder()
                        .id(1L)
                        .username("admin")
                        .password("hashed_pwd")
                        .role(Role.ROLE_ADMIN)
                        .build();
        User user2 =
                User.builder()
                        .id(2L)
                        .username("user")
                        .password("hashed_pwd")
                        .role(Role.ROLE_USER)
                        .build();
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));
    }

    @Test
    @WithMockUser(
            username = "admin",
            roles = {"ADMIN"})
    public void testGetAllUsers() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value("admin"))
                .andExpect(jsonPath("$[1].username").value("user"));
    }

    @Test
    @WithMockUser(
            username = "admin",
            roles = {"ADMIN"})
    public void testCreateUser() throws Exception {
        CreateUserRequest req = new CreateUserRequest();
        req.setUsername("newuser");
        req.setPassword("newpass");
        req.setRole("ROLE_USER");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("newpass")).thenReturn("encoded");

        User savedUser =
                User.builder()
                        .id(3L)
                        .username("newuser")
                        .password("encoded")
                        .role(Role.ROLE_USER)
                        .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        mockMvc.perform(
                        post("/api/admin/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User created successfully!"));
    }
}
