package ru.almasgali.filesync_server;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.almasgali.util.TestsUtil.constructAuthRequest;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FileTests {
    @Value("${server.check-state}")
    private String isTest;
    private static String testUserJWT;
    private static final Path testFilev1Path = Path.of("./src/test/resources/v1/a.txt");
    private static final Path testFilev2Path = Path.of("./src/test/resources/v2/a.txt");
    private static MockMultipartFile testFilev1;
    private static MockMultipartFile testFilev2;
    @Autowired
    private MockMvc mockMvc;


    @BeforeAll
    static void prepare() throws IOException {
        Resource fileResourcev1 = new UrlResource(testFilev1Path.toUri());
        Assertions.assertNotNull(fileResourcev1);
        testFilev1 = new MockMultipartFile(
                "file", fileResourcev1.getFilename(),
                MediaType.MULTIPART_FORM_DATA_VALUE,
                fileResourcev1.getInputStream());
        Assertions.assertNotNull(testFilev1);

        Resource fileResourcev2 = new UrlResource(testFilev2Path.toUri());
        Assertions.assertNotNull(fileResourcev2);
        testFilev2 = new MockMultipartFile(
                "file", fileResourcev2.getFilename(),
                MediaType.MULTIPART_FORM_DATA_VALUE,
                fileResourcev2.getInputStream());
        Assertions.assertNotNull(testFilev2);
    }

    @AfterAll
    static void clear(@Value("${storage.root-location}") String root) {
        Assertions.assertNotNull(root);
        Path rootLocation = Path.of(root);
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }


    @Test
    @Order(1)
    void contextLoads() {
        Assertions.assertNotNull(mockMvc);
        Assertions.assertEquals("test", isTest);
    }

    @Test
    @Order(2)
    void prepareUser() throws Exception {
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(constructAuthRequest("test", "testtest")))
                .andExpect(status().isCreated());

        MvcResult res = mockMvc.perform(post("/user/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(constructAuthRequest("test", "testtest")))
                .andExpect(status().isOk())
                .andReturn();

        String content = res.getResponse().getContentAsString();
        testUserJWT = content.substring(10, content.length() - 2);
    }

    @Test
    @Order(3)
    void uploadNoToken() throws Exception {
        mockMvc.perform(multipart("/files")
                .file(testFilev1)
                .param("updated_at", "0")
        ).andExpect(status().isForbidden());
    }

    @Test
    @Order(4)
    void uploadBadToken() throws Exception {
        mockMvc.perform(multipart("/files")
                .file(testFilev1)
                .header("Authorization", "Bearer " + "Some.Bad.String")
                .param("updated_at", "0")
        ).andExpect(status().isBadRequest());
    }

    @Test
    @Order(5)
    void uploadNegativeTimestamp() throws Exception {
        mockMvc.perform(multipart("/files")
                .file(testFilev1)
                .header("Authorization", "Bearer " + testUserJWT)
                .param("updated_at", "-1")
        ).andExpect(status().isBadRequest());
    }

    @Test
    @Order(6)
    void uploadFile() throws Exception {
        mockMvc.perform(multipart("/files")
                .file(testFilev1)
                .header("Authorization", "Bearer " + testUserJWT)
                .param("updated_at", "0")
        ).andExpect(status().isCreated());
    }

    @Test
    @Order(7)
    void checkFileList() throws Exception {
        MvcResult res = mockMvc.perform(get("/files")
                .header("Authorization", "Bearer " + testUserJWT))
                .andExpect(status().isOk()).andReturn();

        String content = res.getResponse().getContentAsString();
        Assertions.assertTrue(content.contains("\"name\":\"a.txt\""));
    }

    @Test
    @Order(8)
    void checkFileContent() throws Exception {
        MvcResult res = mockMvc.perform(get("/files/a.txt")
                        .header("Authorization", "Bearer " + testUserJWT))
                .andExpect(status().isOk()).andReturn();

        String content = res.getResponse().getContentAsString();
        Assertions.assertEquals("Hello world v1.", content);
    }

    @Test
    @Order(9)
    void uploadFileSmallerTimestamp() throws Exception {
        mockMvc.perform(multipart("/files")
                .file(testFilev2)
                .header("Authorization", "Bearer " + testUserJWT)
                .param("updated_at", "0")
        ).andExpect(status().isBadRequest());
    }

    @Test
    @Order(10)
    void checkFileContentNotChanged() throws Exception {
        MvcResult res = mockMvc.perform(get("/files/a.txt")
                        .header("Authorization", "Bearer " + testUserJWT))
                .andExpect(status().isOk()).andReturn();

        String content = res.getResponse().getContentAsString();
        Assertions.assertEquals("Hello world v1.", content);
    }

    @Test
    @Order(11)
    void uploadFileBiggerTimestamp() throws Exception {
        mockMvc.perform(multipart("/files")
                .file(testFilev2)
                .header("Authorization", "Bearer " + testUserJWT)
                .param("updated_at", String.valueOf(System.currentTimeMillis()))
        ).andExpect(status().isCreated());
    }

    @Test
    @Order(12)
    void checkFileContentChanged() throws Exception {
        MvcResult res = mockMvc.perform(get("/files/a.txt")
                        .header("Authorization", "Bearer " + testUserJWT))
                .andExpect(status().isOk()).andReturn();

        String content = res.getResponse().getContentAsString();
        Assertions.assertEquals("Hello world v2.", content);
    }
}
