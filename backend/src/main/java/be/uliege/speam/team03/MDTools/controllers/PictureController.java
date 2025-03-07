package be.uliege.speam.team03.MDTools.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import be.uliege.speam.team03.MDTools.models.Picture;
import be.uliege.speam.team03.MDTools.models.PictureType;
import be.uliege.speam.team03.MDTools.services.PictureStorageService;
import lombok.AllArgsConstructor;

/**
 * REST controller for managing pictures.
 * Provides endpoints for uploading, retrieving, and deleting pictures.
 */
@RestController
@RequestMapping("/api/pictures")
@AllArgsConstructor
public class PictureController {
   private PictureStorageService storageService;


   /**
    * Uploads a single picture file to the storage service.
    *
    * @param file the picture file to be uploaded
    * @param pictureType the type of the picture (e.g., "INSTRUMENT", "GROUP")
    * @param referenceId the reference ID associated with the picture
    * @return the metadata of the uploaded picture
    */
   private Picture uploadSinglePicture(MultipartFile file, String pictureType, Long referenceId) {
      pictureType = pictureType.toUpperCase();
      PictureType type = PictureType.valueOf(pictureType);
      Picture metadata = storageService.storePicture(file, type, referenceId);
      return metadata;
   }

   /**
    * Handles the HTTP POST request for uploading a picture.
    *
    * @param file the picture file to be uploaded
    * @param pictureType the type of the picture (e.g., "JPEG", "PNG")
    * @param referenceId the reference ID associated with the picture
    * @return a ResponseEntity containing the metadata of the uploaded picture
    */
   @PostMapping
   public ResponseEntity<Picture> uploadPicture(
         @RequestParam("file") MultipartFile file,
         @RequestParam("type") String pictureType,
         @RequestParam("referenceId") Long referenceId) {
      
      return ResponseEntity.ok(this.uploadSinglePicture(file, pictureType, referenceId));
   }

   /**
    * Handles the HTTP POST request for uploading a picture.
    *
    * @param files the multipe picture files to be uploaded
    * @param pictureType the type of the picture (e.g., "JPEG", "PNG")
    * @param referenceId the reference ID associated with the picture
    * @return a ResponseEntity containing the metadata of the uploaded picture
    */
   @PostMapping("/multiple")
   public ResponseEntity<List<Picture>> uploadPictures(
         @RequestParam("files") List<MultipartFile> files,
         @RequestParam("type") String pictureType,
         @RequestParam("referenceId") Long referenceId) {

      List<Picture> metadataList = new ArrayList<>();
      for(MultipartFile file : files) {
         metadataList.add(this.uploadSinglePicture(file, pictureType, referenceId));
      }
      return ResponseEntity.ok(metadataList);
   }

   /**
    * Handles HTTP GET requests to retrieve a picture by its ID.
    *
    * @param id the ID of the picture to retrieve
    * @return a ResponseEntity containing the picture as a Resource, with the appropriate
    *         Content-Disposition header to prompt a download with the original filename
    */
   @GetMapping("/{id}")
   public ResponseEntity<Resource> getPicture(@PathVariable Long id) {
      Resource file = storageService.loadPicture(id);
      return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
            .body(file);
   }

   /**
    * Deletes a picture with the specified ID.
    *
    * @param id the ID of the picture to be deleted
    * @return a ResponseEntity with no content if the deletion is successful
    */
   @DeleteMapping("/{id}")
   public ResponseEntity<Void> deletePicture(@PathVariable Long id) {
      storageService.deletePicture(id);
      return ResponseEntity.noContent().build();
   }
}
