package ru.almasgali.filesync_server;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class FilesyncServerApplicationTests {

    @Value("${server.check-state}")
    private String isTest;
    private final Path testFile = Path.of("/src/test/resources/a.txt");
    @Autowired
    private MockMvc mockMvc;
    private String testUserJWT;

    @Test
    void contextLoads() {
        Assertions.assertNotNull(mockMvc);
        Assertions.assertEquals("test", isTest);
    }

    @Test
    void shortUsername() throws Exception {
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(constructAuthRequest("tst", "testtest")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shortPassword() throws Exception {
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(constructAuthRequest("test", "1")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerUser() throws Exception {
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(constructAuthRequest("test", "testtest")))
                .andExpect(status().isCreated());
    }

    @Test
    void registerExistingUser() throws Exception {
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(constructAuthRequest("test", "testtest")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void authUser() throws Exception {
        MvcResult res = mockMvc.perform(post("/user/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(constructAuthRequest("test", "testtest")))
                .andExpect(status().isOk())
                .andReturn();

        String content = res.getResponse().getContentAsString();
        String token = content.substring(10, content.length() - 2);
        testUserJWT = token;
    }

    @Test
    void authWrongUsername() throws Exception {
        mockMvc.perform(post("/user/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(constructAuthRequest("wrong", "testtest")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authWrongPassword() throws Exception {
        mockMvc.perform(post("/user/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(constructAuthRequest("test", "wrong")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void uploadFileNegativeTimestamp() throws Exception {

//        Resource fileResource = new UrlResource(testFile.toUri());
//
//        mockMvc.perform(post("/files")
//                        .header("Authorization", "Bearer " + testUserJWT)
//                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
//                        .content(constructAuthRequest("test", "wrong")))
//                .andExpect(status().isUnauthorized());
    }

    private String constructAuthRequest(String username, String password) {
        return "{\"username\" : \"" + username + "\", \"password\" : \"" + password + "\" }";
    }
}
