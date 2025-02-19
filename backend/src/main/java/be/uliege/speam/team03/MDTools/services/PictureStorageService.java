package be.uliege.speam.team03.MDTools.services;

import java.io.IOException;
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

@Service
public class PictureStorageService {
    @Value("${app.upload.dir:/app/pictures}")
    private String uploadDir;
    
    @Autowired
    private PictureRepository pictureRepository;
    
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

    public List<String> getPicturesIdByReferenceIdAndPictureType(Long referenceId, PictureType pictureType) {
        List<Picture> pictures = pictureRepository.findByReferenceIdAndPictureType(referenceId, pictureType);
        return pictures.stream().map(picture -> picture.getFileName()).toList();
    }
    
    public Resource loadPicture(Long pictureId) {
        try {
            Picture metadata = pictureRepository.findById(pictureId)
                .orElseThrow(() -> new ResourceNotFoundException("Picture not found"));
            
            String fileName = metadata.getFileName();
            Path filePath = Paths.get(uploadDir).resolve(fileName);
            System.out.println(filePath);
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
    
    private String getFileExtension(String fileName) {
        if (fileName == null) return "";
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex == -1 ? "" : fileName.substring(lastDotIndex);
    }

    public void deletePicture(Long pictureId) {
        Picture metadata = pictureRepository.findById(pictureId)
            .orElseThrow(() -> new ResourceNotFoundException("Picture not found"));

        if(metadata == null) {
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
}
