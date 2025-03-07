package be.uliege.speam.team03.MDTools.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.models.Picture;
import be.uliege.speam.team03.MDTools.models.PictureType;
import be.uliege.speam.team03.MDTools.repositories.PictureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class PictureStorageServiceTest {

    @Mock
    private PictureRepository pictureRepository;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private PictureStorageService pictureStorageService;

    @TempDir
    Path tempUploadDir;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(pictureStorageService, "uploadDir", tempUploadDir.toString());
    }

    @Test
    void storePicture_ValidFile_ShouldSaveMetadataAndFile() throws IOException {
        // Arrange
        PictureType pictureType = PictureType.INSTRUMENT;
        Long referenceId = 1L;
        String originalFileName = "test.jpg";
        byte[] content = "test content".getBytes();

        when(multipartFile.getOriginalFilename()).thenReturn(originalFileName);
        when(multipartFile.getInputStream()).thenReturn(new java.io.ByteArrayInputStream(content));
        when(pictureRepository.save(any(Picture.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Picture result = pictureStorageService.storePicture(multipartFile, pictureType, referenceId);

        // Assert
        assertNotNull(result);
        assertTrue(result.getFileName().endsWith(".jpg"));
        assertEquals(pictureType, result.getPictureType());
        assertEquals(referenceId, result.getReferenceId());
        assertNotNull(result.getUploadDate());

        Path storedFile = tempUploadDir.resolve(result.getFileName());
        assertTrue(Files.exists(storedFile));
        assertEquals(content.length, Files.size(storedFile));
    }

    @Test
    void getPicturesIdByReferenceIdAndPictureType_ShouldReturnFileNames() {
        // Arrange
        Long referenceId = 1L;
        PictureType pictureType = PictureType.CATEGORY;
        List<Picture> mockPictures = List.of(
            createTestPicture("file1.jpg", pictureType, referenceId),
            createTestPicture("file2.jpg", pictureType, referenceId)
        );

        when(pictureRepository.findByReferenceIdAndPictureType(referenceId, pictureType))
            .thenReturn(mockPictures);

        // Act
        List<Long> result = pictureStorageService.getPicturesIdByReferenceIdAndPictureType(referenceId, pictureType);

        // Assert
        assertEquals(2, result.size());
    }

    @Test
    void loadPicture_ExistingFile_ShouldReturnResource() throws IOException {
        // Arrange
        String fileName = "existing.jpg";
        Files.createFile(tempUploadDir.resolve(fileName));
        
        Picture mockPicture = new Picture();
        mockPicture.setFileName(fileName);
        when(pictureRepository.findById(1L)).thenReturn(Optional.of(mockPicture));

        // Act
        var resource = pictureStorageService.loadPicture(1L);

        // Assert
        assertTrue(resource.exists());
        assertTrue(resource.isReadable());
    }

    @Test
    void loadPicture_NonExistingMetadata_ShouldThrowException() {
        when(pictureRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> 
            pictureStorageService.loadPicture(999L));
    }

    @Test
    void deletePicture_ExistingFile_ShouldDeleteFileAndMetadata() throws IOException {
        // Arrange
        String fileName = "to-delete.jpg";
        Path filePath = tempUploadDir.resolve(fileName);
        Files.createFile(filePath);

        Picture mockPicture = new Picture();
        mockPicture.setFileName(fileName);
        when(pictureRepository.findById(1L)).thenReturn(Optional.of(mockPicture));

        // Act
        pictureStorageService.deletePicture(1L);

        // Assert
        assertFalse(Files.exists(filePath));
        verify(pictureRepository).delete(mockPicture);
    }

    @Test
    void deletePicture_MissingFile_ShouldThrowException() {
        Picture mockPicture = new Picture();
        mockPicture.setFileName("missing.jpg");
        when(pictureRepository.findById(1L)).thenReturn(Optional.of(mockPicture));

        assertThrows(RuntimeException.class, () ->
            pictureStorageService.deletePicture(1L));
        verify(pictureRepository, never()).delete(any());
    }

    private Picture createTestPicture(String fileName, PictureType type, Long referenceId) {
        Picture picture = new Picture();
        picture.setFileName(fileName);
        picture.setPictureType(type);
        picture.setReferenceId(referenceId);
        picture.setUploadDate(LocalDateTime.now());
        return picture;
    }
}