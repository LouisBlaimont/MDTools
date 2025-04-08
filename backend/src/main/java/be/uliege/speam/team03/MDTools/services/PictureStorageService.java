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
import java.util.UUID;

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
 * Service for handling picture storage operations.
 */
@Log4j2
@Service
public class PictureStorageService {

    /**
     * Directory where pictures will be uploaded.
     */
    @Value("${app.upload.dir:/app/pictures}")
    private String uploadDir;

    @Autowired
    private PictureRepository pictureRepository;

    /**
     * Stores a picture file and its metadata.
     *
     * @param file        the picture file to store
     * @param pictureType the type of the picture
     * @param referenceId the reference ID associated with the picture
     * @return the stored picture metadata
     * @throws RuntimeException if the file storage fails
     */
    public Picture storePicture(MultipartFile file, PictureType pictureType, Long referenceId) {
        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String fileName = UUID.randomUUID().toString() +
                    getFileExtension(file.getOriginalFilename());

            // Save file to filesystem
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Create and save metadata
            Picture metadata = new Picture();
            metadata.setFileName(fileName);

            metadata.setPictureType(pictureType);
            metadata.setReferenceId(referenceId);
            metadata.setUploadDate(LocalDateTime.now());

            return pictureRepository.save(metadata);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to store file", ex);
        }
    }

    public Picture storePicture(InputStream file, PictureType pictureType, Long referenceId) {
        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            log.info("Storing " + file.toString());
            // Generate unique filename
            String fileName = UUID.randomUUID().toString() +
                    getFileExtension(file.toString());

            log.info("File name: " + fileName);

            // Save file to filesystem
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file, filePath, StandardCopyOption.REPLACE_EXISTING);

            // Create and save metadata
            Picture metadata = new Picture();
            metadata.setFileName(fileName);

            metadata.setPictureType(pictureType);
            metadata.setReferenceId(referenceId);
            metadata.setUploadDate(LocalDateTime.now());

            return pictureRepository.save(metadata);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to store file", ex);
        }
    }

    /**
     * Retrieves a list of picture file names by reference ID and picture type.
     *
     * @param referenceId the ID of the reference to which the pictures belong
     * @param pictureType the type of pictures to retrieve
     * @return a list of picture file names that match the given reference ID and picture type
     */
    public List<Long> getPicturesIdByReferenceIdAndPictureType(Long referenceId, PictureType pictureType) {
        List<Picture> pictures = pictureRepository.findByReferenceIdAndPictureType(referenceId, pictureType);
        return pictures.stream().map(picture -> picture.getId()).toList();
    }

    /**
     * Loads a picture resource by its ID.
     *
     * @param pictureId the ID of the picture to load
     * @return the picture resource
     * @throws ResourceNotFoundException if the picture is not found
     * @throws RuntimeException if the file could not be read or if there is a URL error
     */
    public Resource loadPicture(Long pictureId) {
        try {
            Picture metadata = pictureRepository.findById(pictureId)
                    .orElseThrow(() -> new ResourceNotFoundException("Picture not found"));

            String fileName = metadata.getFileName();
            Path filePath = Paths.get(uploadDir).resolve(fileName);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read file");
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("Error: " + ex.getMessage());
        }
    }

    /**
     * Retrieves the file extension from the given file name.
     *
     * @param fileName the name of the file from which to extract the extension
     * @return the file extension, including the dot (e.g., ".txt"), or an empty string if the file name is null or does not contain a dot
     */
    private static String getFileExtension(String fileName) {
        if (fileName == null)
            return "";
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex == -1 ? "" : fileName.substring(lastDotIndex);
    }

    /**
     * Deletes a picture with the given ID from the storage and the repository.
     *
     * @param pictureId the ID of the picture to be deleted
     * @throws ResourceNotFoundException if the picture with the given ID is not found
     * @throws RuntimeException if there is an error deleting the file from the storage
     */
    public void deletePicture(Long pictureId) {
        Picture metadata = pictureRepository.findById(pictureId)
                .orElseThrow(() -> new ResourceNotFoundException("Picture not found"));

        if (metadata == null) {
            throw new ResourceNotFoundException("Picture not found");
        }

        String fileName = metadata.getFileName();
        Path filePath = Paths.get(uploadDir).resolve(fileName);

        try {
            Files.delete(filePath);
            pictureRepository.delete(metadata);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to delete file", ex);
        }
    }

    // This is just an idea to implement the deletion of all pictures of a certain type

    // /**
    //  * Deletes pictures by reference ID and picture type.
    //  *
    //  * @param referenceId the ID of the reference to which the pictures belong
    //  * @param pictureType the type of pictures to delete
    //  */
    // public void deletePicturesByReferenceIdAndPictureType(Long referenceId, PictureType pictureType) {
    //     List<Picture> pictures = pictureRepository.findByReferenceIdAndPictureType(referenceId, pictureType);
    //     pictureRepository.deleteAll(pictures);
    // }
}
