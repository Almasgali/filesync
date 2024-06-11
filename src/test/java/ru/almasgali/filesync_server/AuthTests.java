package ru.almasgali.filesync_server;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.almasgali.util.TestsUtil;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthTests {

    @Value("${server.check-state}")
    private String isTest;
    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
        Assertions.assertNotNull(mockMvc);
        Assertions.assertEquals("test", isTest);
    }

    @Test
    void shortUsername() throws Exception {
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestsUtil.constructAuthRequest("tst", "testtest")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void noUsername() throws Exception {
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestsUtil.constructAuthRequest("", "testtest")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void noContent() throws Exception {
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shortPassword() throws Exception {
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestsUtil.constructAuthRequest("test", "1")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void noPassword() throws Exception {
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestsUtil.constructAuthRequest("test", "")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void nullUsernameAndPassword() throws Exception {
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestsUtil.constructAuthRequest(null, null)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(1)
    void registerUser() throws Exception {
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestsUtil.constructAuthRequest("test", "testtest")))
                .andExpect(status().isCreated());
    }

    @Test
    @Order(2)
    void registerExistingUser() throws Exception {
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestsUtil.constructAuthRequest("test", "testtest")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(3)
    void authUser() throws Exception {
        MvcResult res = mockMvc.perform(post("/user/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestsUtil.constructAuthRequest("test", "testtest")))
                .andExpect(status().isOk())
                .andReturn();

        String content = res.getResponse().getContentAsString();
        Assertions.assertTrue(content.contains("token"));
    }

    @Test
    @Order(4)
    void authWrongUsername() throws Exception {
        mockMvc.perform(post("/user/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestsUtil.constructAuthRequest("wrong", "testtest")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(5)
    void authWrongPassword() throws Exception {
        mockMvc.perform(post("/user/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestsUtil.constructAuthRequest("test", "wrongwrong")))
                .andExpect(status().isUnauthorized());
    }
}
