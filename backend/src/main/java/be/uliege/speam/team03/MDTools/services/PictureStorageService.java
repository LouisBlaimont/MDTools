package be.uliege.speam.team03.MDTools.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.models.Picture;
import be.uliege.speam.team03.MDTools.models.PictureType;
import be.uliege.speam.team03.MDTools.repositories.PictureRepository;
import lombok.extern.log4j.Log4j2;

/**
 * Service responsible for storing, loading, and deleting pictures.
 *
 * This service saves picture binaries to the filesystem and stores metadata in the database.
 * The filesystem path is controlled by {@code app.upload.dir}.
 */
@Log4j2
@Service
public class PictureStorageService {

    /**
     * Root directory where pictures are stored on the filesystem.
     *
     * Default value: "/app/pictures"
     */
    @Value("${app.upload.dir:/app/pictures}")
    private String uploadDir;

    @Autowired
    private PictureRepository pictureRepository;

    /**
     * Stores a picture received as a {@link MultipartFile} and saves its metadata in the database.
     *
     * @param file        Multipart file containing the picture bytes
     * @param pictureType Type of the picture (e.g., INSTRUMENT, CATEGORY)
     * @param referenceId Entity ID associated with this picture (e.g., instrumentId)
     * @return The persisted {@link Picture} metadata
     * @throws IllegalArgumentException if file is null/empty or arguments are invalid
     * @throws RuntimeException if filesystem or database operations fail
     */
    public Picture storePicture(MultipartFile file, PictureType pictureType, Long referenceId) {
        validateStoreArgs(pictureType, referenceId);

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        String originalFilename = file.getOriginalFilename();
        try (InputStream inputStream = file.getInputStream()) {
            return storePictureInternal(inputStream, originalFilename, pictureType, referenceId);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to store file", ex);
        }
    }

    /**
     * Stores a picture received as an {@link InputStream} and saves its metadata in the database.
     *
     * IMPORTANT: an {@link InputStream} does not carry a filename. Therefore this method
     * cannot reliably infer the file extension. This overload will store the file without extension.
     *
     * For bulk import from zip entries, prefer using {@link #storePicture(InputStream, String, PictureType, Long)}
     * to keep the original extension.
     *
     * @param inputStream Picture input stream
     * @param pictureType Type of the picture (e.g., INSTRUMENT, CATEGORY)
     * @param referenceId Entity ID associated with this picture (e.g., instrumentId)
     * @return The persisted {@link Picture} metadata
     * @throws IllegalArgumentException if arguments are invalid
     * @throws RuntimeException if filesystem or database operations fail
     */
    public Picture storePicture(InputStream inputStream, PictureType pictureType, Long referenceId) {
        return storePicture(inputStream, null, pictureType, referenceId);
    }

    /**
     * Stores a picture received as an {@link InputStream} with a known original filename and
     * saves its metadata in the database.
     *
     * This method is ideal for bulk imports (e.g., zip entries) because it preserves file extensions.
     *
     * @param inputStream       Picture input stream
     * @param originalFilename  Original filename (used only to extract the extension); can be null
     * @param pictureType       Type of the picture (e.g., INSTRUMENT, CATEGORY)
     * @param referenceId       Entity ID associated with this picture (e.g., instrumentId)
     * @return The persisted {@link Picture} metadata
     * @throws IllegalArgumentException if inputStream is null or arguments are invalid
     * @throws RuntimeException if filesystem or database operations fail
     */
    public Picture storePicture(InputStream inputStream, String originalFilename, PictureType pictureType, Long referenceId) {
        validateStoreArgs(pictureType, referenceId);

        if (inputStream == null) {
            throw new IllegalArgumentException("InputStream cannot be null");
        }

        try {
            return storePictureInternal(inputStream, originalFilename, pictureType, referenceId);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to store file", ex);
        }
    }

    /**
     * Stores a picture only if the given reference does not already have any picture
     * of the same {@link PictureType}.
     *
     * This is a fast and safe "anti-duplicate" strategy for bulk imports:
     * - If the instrument already has at least one picture, the new one is skipped.
     * - This avoids massive duplicates when a zip import is run twice by mistake.
     *
     * @param inputStream       Picture input stream
     * @param originalFilename  Original filename (used only to extract the extension); can be null
     * @param pictureType       Type of the picture (e.g., INSTRUMENT)
     * @param referenceId       Entity ID associated with this picture (e.g., instrumentId)
     * @return The persisted {@link Picture} metadata, or null if skipped due to existing picture(s)
     * @throws IllegalArgumentException if arguments are invalid
     * @throws RuntimeException if filesystem or database operations fail
     */
    public Picture storePictureIfNotExistsForReference(
            InputStream inputStream,
            String originalFilename,
            PictureType pictureType,
            Long referenceId
    ) {
        validateStoreArgs(pictureType, referenceId);

        List<Picture> existing = pictureRepository.findByReferenceIdAndPictureType(referenceId, pictureType);
        if (existing != null && !existing.isEmpty()) {
            log.info("Skipping picture storage: referenceId={} already has picture(s) of type={}", referenceId, pictureType);
            return null;
        }

        return storePicture(inputStream, originalFilename, pictureType, referenceId);
    }

    /**
     * Returns picture IDs matching a given reference ID and picture type.
     *
     * @param referenceId Entity ID associated with the pictures
     * @param pictureType Type of the pictures to retrieve
     * @return List of picture IDs (possibly empty)
     */
    public List<Long> getPicturesIdByReferenceIdAndPictureType(Long referenceId, PictureType pictureType) {
        List<Picture> pictures = pictureRepository.findByReferenceIdAndPictureType(referenceId, pictureType);
        return pictures.stream().map(Picture::getId).toList();
    }

    /**
     * Returns a mapping of categoryId -> list of pictureIds for the given categories.
     *
     * @param categoryIds List of category IDs
     * @return Map where key = categoryId and value = list of picture IDs
     */
    public Map<Long, List<Long>> getCategoryPicturesBatch(List<Long> categoryIds) {
        return getPicturesBatch(categoryIds, PictureType.CATEGORY);
    }

    /**
     * Returns a mapping of instrumentId -> list of pictureIds for the given instruments.
     *
     * @param instrumentIds List of instrument IDs
     * @return Map where key = instrumentId and value = list of picture IDs
     */
    public Map<Long, List<Long>> getInstrumentPicturesBatch(List<Long> instrumentIds) {
        return getPicturesBatch(instrumentIds, PictureType.INSTRUMENT);
    }

    /**
     * Loads a picture resource (filesystem file) by its database ID.
     *
     * @param pictureId Picture database ID
     * @return A {@link Resource} pointing to the stored file
     * @throws ResourceNotFoundException if the metadata does not exist
     * @throws RuntimeException if the file cannot be read
     */
    public Resource loadPicture(Long pictureId) {
        try {
            Picture metadata = pictureRepository.findById(pictureId)
                    .orElseThrow(() -> new ResourceNotFoundException("Picture not found"));

            Path filePath = Paths.get(uploadDir).resolve(metadata.getFileName());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new RuntimeException("Could not read file");
        } catch (MalformedURLException ex) {
            throw new RuntimeException("Error while loading picture resource", ex);
        }
    }

    /**
     * Deletes a picture file and its metadata by ID.
     *
     * @param pictureId Picture database ID
     * @throws ResourceNotFoundException if the metadata does not exist
     * @throws RuntimeException if filesystem deletion fails
     */
    public void deletePicture(Long pictureId) {
        Picture metadata = pictureRepository.findById(pictureId)
                .orElseThrow(() -> new ResourceNotFoundException("Picture not found"));

        Path filePath = Paths.get(uploadDir).resolve(metadata.getFileName());

        try {
            Files.deleteIfExists(filePath);
            pictureRepository.delete(metadata);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to delete file", ex);
        }
    }

    /**
     * Internal shared logic for storing a picture:
     * - ensures upload directory exists
     * - generates a unique filename
     * - copies the stream to disk
     * - saves metadata
     *
     * @param inputStream      Picture data stream
     * @param originalFilename Original filename used only to extract extension (can be null)
     * @param pictureType      Picture type
     * @param referenceId      Reference ID
     * @return Persisted {@link Picture} metadata
     * @throws IOException if filesystem write fails
     */
    private Picture storePictureInternal(
            InputStream inputStream,
            String originalFilename,
            PictureType pictureType,
            Long referenceId
    ) throws IOException {
        Path uploadPath = ensureUploadDirectory();

        String extension = getFileExtension(originalFilename);
        String fileName = UUID.randomUUID().toString() + extension;

        Path filePath = uploadPath.resolve(fileName);
        Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);

        Picture metadata = new Picture();
        metadata.setFileName(fileName);
        metadata.setPictureType(pictureType);
        metadata.setReferenceId(referenceId);
        metadata.setUploadDate(LocalDateTime.now());

        return pictureRepository.save(metadata);
    }

    /**
     * Ensures the upload directory exists.
     *
     * @return The resolved upload directory path
     * @throws IOException if the directory cannot be created
     */
    private Path ensureUploadDirectory() throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        return uploadPath;
    }

    /**
     * Validates common parameters for picture storage.
     *
     * @param pictureType Picture type
     * @param referenceId Reference ID
     * @throws IllegalArgumentException if parameters are invalid
     */
    private void validateStoreArgs(PictureType pictureType, Long referenceId) {
        Objects.requireNonNull(pictureType, "pictureType cannot be null");
        Objects.requireNonNull(referenceId, "referenceId cannot be null");
        if (referenceId <= 0) {
            throw new IllegalArgumentException("referenceId must be > 0");
        }
    }

    /**
     * Retrieves the file extension from a filename.
     *
     * @param fileName Original filename (can be null)
     * @return Extension including the dot (e.g., ".png"), or empty string if none
     */
    private static String getFileExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex == -1 ? "" : fileName.substring(lastDotIndex);
    }

    /**
     * Generic batch helper used by category/instrument batch endpoints.
     *
     * @param referenceIds List of reference IDs
     * @param pictureType  Picture type
     * @return Map referenceId -> list of picture IDs
     */
    private Map<Long, List<Long>> getPicturesBatch(List<Long> referenceIds, PictureType pictureType) {
        if (referenceIds == null || referenceIds.isEmpty()) {
            return Map.of();
        }

        return pictureRepository.findByReferenceIdsAndPictureType(referenceIds, pictureType)
                .stream()
                .collect(Collectors.groupingBy(
                        Picture::getReferenceId,
                        Collectors.mapping(Picture::getId, Collectors.toList())
                ));
    }
}
