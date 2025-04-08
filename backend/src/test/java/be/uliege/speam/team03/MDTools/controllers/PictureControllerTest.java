package be.uliege.speam.team03.MDTools.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import be.uliege.speam.team03.MDTools.config.TestSecurityConfig;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.models.Picture;
import be.uliege.speam.team03.MDTools.models.PictureType;
import be.uliege.speam.team03.MDTools.services.PictureStorageService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.core.io.Resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) // mandatory to test multipart requests
@ContextConfiguration(classes = TestSecurityConfig.class)
@ExtendWith(MockitoExtension.class)
public class PictureControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private PictureStorageService storageService;

    @InjectMocks
    private PictureController pictureController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(pictureController).build();
        objectMapper = new ObjectMapper();
    }

    // Happy path test
    @Test
    void uploadPicture_ValidRequest_ReturnsCreated() throws Exception {
        Picture expectedPicture = new Picture(1L, "test.png", PictureType.INSTRUMENT, 1L, null);
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", "content".getBytes());

        when(storageService.storePicture(any(MultipartFile.class), eq(PictureType.INSTRUMENT), eq(1L)))
                .thenReturn(expectedPicture);

        mockMvc.perform(multipart("/api/pictures")
                .file(file)
                .param("type", "INSTRUMENT")
                .param("referenceId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("test.png"))
                .andExpect(jsonPath("$.pictureType").value("INSTRUMENT"))
                .andExpect(jsonPath("$.referenceId").value(1));
    }

    // Edge case: Missing file parameter
    @Test
    void uploadPicture_MissingFile_ReturnsBadRequest() throws Exception {
        mockMvc.perform(multipart("/api/pictures")
                .param("type", "INSTRUMENT")
                .param("referenceId", "1"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(storageService);
    }

    // Edge case: Empty file
    @Test
    void uploadPicture_EmptyFile_ThrowsIllegalArgumentException() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.png", "image/png", new byte[0]);

        when(storageService.storePicture(eq(emptyFile), any(), any()))
                .thenThrow(new IllegalArgumentException("File cannot be empty"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pictureController.uploadPicture(emptyFile, "INSTRUMENT", 1L);
        });

        assertEquals("File cannot be empty", exception.getMessage());
    }

    // Edge case: Invalid picture type
    @Test
    void uploadPicture_InvalidType_ThrowsIllegalArgumentException() {
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", "content".getBytes());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pictureController.uploadPicture(file, "INVALID_TYPE", 1L);
        });

        assertTrue(exception.getMessage().contains("No enum constant"));
    }

    // Edge case: Invalid reference ID
    @Test
    void uploadPicture_NegativeReferenceId_ThrowsIllegalArgumentException() {
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", "content".getBytes());

        when(storageService.storePicture(any(MultipartFile.class), any(PictureType.class), eq(-1L)))
                .thenThrow(new IllegalArgumentException("Invalid reference ID"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pictureController.uploadPicture(file, "INSTRUMENT", -1L);
        });

        assertEquals("Invalid reference ID", exception.getMessage());
    }

    // Edge case: Unsupported media type
    @Test
    void uploadPicture_UnsupportedMediaType_ThrowsUnsupportedOperationException() {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());

        when(storageService.storePicture(eq(file), any(), any()))
                .thenThrow(new UnsupportedOperationException("Unsupported file type"));

        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> {
            pictureController.uploadPicture(file, "INSTRUMENT", 1L);
        });

        assertEquals("Unsupported file type", exception.getMessage());
    }

    // ! This test is not working because the large file size is not restricted in
    // the test environment, no solution found
    // // Edge case: Large file size
    // @Test
    // void uploadPicture_FileTooLarge_ReturnsPayloadTooLarge() throws Exception {
    // byte[] largeFile = new byte[1024 * 1024 * 11]; // 11MB
    // MockMultipartFile file = new MockMultipartFile("file", "large.png",
    // "image/png", largeFile);

    // mockMvc.perform(multipart("/api/pictures")
    // .file(file)
    // .param("type", "INSTRUMENT")
    // .param("referenceId", "1"))
    // .andExpect(status().isPayloadTooLarge());
    // }

    // Edge case: Missing type parameter
    @Test
    void uploadPicture_MissingType_ReturnsBadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", "content".getBytes());

        mockMvc.perform(multipart("/api/pictures")
                .file(file)
                .param("referenceId", "1"))
                .andExpect(status().isBadRequest());
    }

    // Edge case: Missing referenceId parameter
    @Test
    void uploadPicture_MissingReferenceId_ReturnsBadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", "content".getBytes());

        mockMvc.perform(multipart("/api/pictures")
                .file(file)
                .param("type", "INSTRUMENT"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUploadPictures() throws Exception {
        // Prepare test data
        MockMultipartFile file1 = new MockMultipartFile("files", "test1.png", "image/png", "file content 1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("files", "test2.png", "image/png", "file content 2".getBytes());
        List<MultipartFile> files = new ArrayList<>();
        files.add(file1);
        files.add(file2);

        // Mock service response
        Picture picture1 = new Picture(1L, "test1.png", PictureType.INSTRUMENT, 1L, null);
        Picture picture2 = new Picture(2L, "test2.png", PictureType.INSTRUMENT, 1L, null);
        when(storageService.storePicture(file1, PictureType.INSTRUMENT, 1L)).thenReturn(picture1);
        when(storageService.storePicture(file2, PictureType.INSTRUMENT, 1L)).thenReturn(picture2);

        // Create expected JSON
        List<Picture> expectedPictures = Arrays.asList(picture1, picture2);
        ObjectMapper objectMapper = new ObjectMapper();
        String expectedJson = objectMapper.writeValueAsString(expectedPictures);

        // Perform the POST request and assert the response
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/pictures/multiple")
                .file(file1)
                .file(file2)
                .param("type", "instrument")
                .param("referenceId", "1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().json(expectedJson, true)); // Strict JSON comparison

        // Verify service interactions
        verify(storageService, times(1)).storePicture(file1, PictureType.INSTRUMENT, 1L);
        verify(storageService, times(1)).storePicture(file2, PictureType.INSTRUMENT, 1L);
    }

    @Test
    void getPicture_ValidId_ReturnsFileWithContentDisposition() throws Exception {
        Long validId = 1L;
        String filename = "test.jpg";
        Resource mockResource = mock(Resource.class);
        when(mockResource.getFilename()).thenReturn(filename);
        when(storageService.loadPicture(validId)).thenReturn(mockResource);

        mockMvc.perform(get("/api/pictures/{id}", validId))
                .andExpect(status().isOk())
                .andExpect(
                        header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\""));
    }

    @Test
    void getPicture_NonExistentId_ReturnsNotFound() throws Exception {
        Long nonExistentId = 999L;
        when(storageService.loadPicture(nonExistentId))
                .thenThrow(new ResourceNotFoundException("Picture not found"));

        mockMvc.perform(get("/api/pictures/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPicture_InvalidIdFormat_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/pictures/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPicture_NegativeId_ReturnsNotFound() throws Exception {
        Long negativeId = -1L;
        when(storageService.loadPicture(negativeId))
                .thenThrow(new ResourceNotFoundException("Invalid ID"));

        mockMvc.perform(get("/api/pictures/{id}", negativeId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPicture_ZeroId_ReturnsNotFound() throws Exception {
        Long zeroId = 0L;
        when(storageService.loadPicture(zeroId))
                .thenThrow(new ResourceNotFoundException("Invalid ID"));

        mockMvc.perform(get("/api/pictures/{id}", zeroId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPicture_VeryLargeId_ReturnsNotFound() throws Exception {
        Long largeId = Long.MAX_VALUE;
        when(storageService.loadPicture(largeId))
                .thenThrow(new ResourceNotFoundException("Picture not found"));

        mockMvc.perform(get("/api/pictures/{id}", largeId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPicture_FilenameNull_HandlesNullFilename() throws Exception {
        Long id = 2L;
        Resource mockResource = mock(Resource.class);
        when(mockResource.getFilename()).thenReturn(null);
        when(storageService.loadPicture(id)).thenReturn(mockResource);

        mockMvc.perform(get("/api/pictures/{id}", id))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"null\""));
    }

    @Test
    void getPicture_FilenameWithSpecialCharacters_ReturnsEscapedHeader() throws Exception {
        Long id = 3L;
        String filename = "file\"with' special &chars.txt";
        Resource mockResource = mock(Resource.class);
        when(mockResource.getFilename()).thenReturn(filename);
        when(storageService.loadPicture(id)).thenReturn(mockResource);

        mockMvc.perform(get("/api/pictures/{id}", id))
                .andExpect(status().isOk())
                .andExpect(
                        header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\""));
    }

    @Test
    void deletePicture_ValidId_ReturnsNoContent() throws Exception {
        Long validId = 1L;
        doNothing().when(storageService).deletePicture(validId);

        mockMvc.perform(delete("/api/pictures/{id}", validId))
                .andExpect(status().isNoContent());
    }

    @Test
    void deletePicture_NonExistentId_ReturnsNotFound() throws Exception {
        Long nonExistentId = 999L;
        doThrow(new ResourceNotFoundException("Picture not found"))
                .when(storageService).deletePicture(nonExistentId);

        mockMvc.perform(delete("/api/pictures/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletePicture_InvalidIdFormat_ReturnsBadRequest() throws Exception {
        mockMvc.perform(delete("/api/pictures/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deletePicture_NegativeId_ReturnsNotFound() throws Exception {
        Long negativeId = -1L;
        doThrow(new ResourceNotFoundException("Invalid ID"))
                .when(storageService).deletePicture(negativeId);

        mockMvc.perform(delete("/api/pictures/{id}", negativeId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletePicture_ZeroId_ReturnsNotFound() throws Exception {
        Long zeroId = 0L;
        doThrow(new ResourceNotFoundException("Invalid ID"))
                .when(storageService).deletePicture(zeroId);

        mockMvc.perform(delete("/api/pictures/{id}", zeroId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletePicture_VeryLargeId_ReturnsNotFound() throws Exception {
        Long largeId = Long.MAX_VALUE;
        doThrow(new ResourceNotFoundException("Picture not found"))
                .when(storageService).deletePicture(largeId);

        mockMvc.perform(delete("/api/pictures/{id}", largeId))
                .andExpect(status().isNotFound());
    }
}
