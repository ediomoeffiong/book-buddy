package com.bookbuddy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class IntegrationTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private com.bookbuddy.repository.UserRepository userRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Test
    void registerAndLogin_variations() throws Exception {
        // register
        String regJson = "{\"fullName\":\"IT User\",\"email\":\"ituser@example.com\",\"username\":\"ituser\",\"password\":\"Password123!\",\"confirmPassword\":\"Password123!\"}";
        String regResp = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(regJson))
                .andExpect(status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString();

        JsonNode rnode = objectMapper.readTree(regResp);
        assertThat(rnode.has("token")).isTrue();

        // login using email
        String loginEmail = "{\"email\":\"ituser@example.com\",\"password\":\"Password123!\"}";
        String le = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginEmail))
                .andExpect(status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString();
        JsonNode lnode = objectMapper.readTree(le);
        assertThat(lnode.has("token")).isTrue();

        // login using usernameOrEmail
        String loginU = "{\"usernameOrEmail\":\"ituser\",\"password\":\"Password123!\"}";
        String lu = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginU))
                .andExpect(status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString();
        JsonNode lun = objectMapper.readTree(lu);
        assertThat(lun.has("token")).isTrue();
    }

    @Test
    void publicEndpoints_listAndExternalSearch() throws Exception {
        // list books (should succeed even if empty)
        mvc.perform(get("/api/books?page=0&size=10"))
                .andExpect(status().is2xxSuccessful());

        // external search should return JSON with a value array
        String resp = mvc.perform(get("/api/books/search/external?query=harry&maxResults=2"))
                .andExpect(status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString();
                JsonNode node = objectMapper.readTree(resp);
                // external API may return either an object with a `value` array or a JSON array directly
                JsonNode itemsNode;
                if (node.isArray()) {
                        itemsNode = node;
                } else if (node.has("value")) {
                        itemsNode = node.get("value");
                } else {
                        itemsNode = node;
                }

                assertThat(itemsNode.isArray()).isTrue();

                // After adding a fallback cover image, every external result should include a non-empty coverImageUrl
                for (JsonNode item : itemsNode) {
                        if (item.isObject()) {
                                JsonNode cover = item.get("coverImageUrl");
                                assertThat(cover).isNotNull();
                                assertThat(cover.asText()).isNotBlank();
                        }
                }
    }

    @Test
    void protectedEndpoint_requiresAuthOrReturns4xx() throws Exception {
        // without auth, expect 4xx (404 if missing or 403 if protected)
        mvc.perform(get("/api/books/1")).andExpect(status().is4xxClientError());
    }

    @Test
    void importTopEndpoint_requiresAdminAndSaves() throws Exception {
        // create an admin user directly
        userRepository.deleteAll();
        com.bookbuddy.model.User admin = com.bookbuddy.model.User.builder()
                .username("admintest")
                .email("admin@example.com")
                .password(passwordEncoder.encode("AdminPass123!"))
                .role(com.bookbuddy.model.User.Role.ADMIN)
                .build();
        userRepository.save(admin);

        // login to get token
        String loginJson = "{\"usernameOrEmail\":\"admintest\",\"password\":\"AdminPass123!\"}";
        String loginResp = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginJson))
                .andExpect(status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString();
        JsonNode lnode = objectMapper.readTree(loginResp);
        String token = lnode.get("token").asText();

        // call import/top
        String importResp = mvc.perform(post("/api/books/import/top?query=harry&maxResults=2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString();

        JsonNode arr = objectMapper.readTree(importResp);
        assertThat(arr.isArray()).isTrue();
        assertThat(arr.size()).isGreaterThan(0);
        for (JsonNode item : arr) {
            if (item.isObject()) {
                JsonNode idNode = item.get("id");
                assertThat(idNode).isNotNull();
                assertThat(idNode.asLong()).isGreaterThan(0);
            }
        }
    }
}

