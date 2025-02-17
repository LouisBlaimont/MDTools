package be.uliege.speam.team03.MDTools.controllers;

import org.springframework.beans.factory.annotation.Autowired;
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

@RestController
@RequestMapping("/api/pictures")
public class PictureController {
   @Autowired
   private PictureStorageService storageService;

   @PostMapping
   public ResponseEntity<Picture> uploadPicture(
         @RequestParam("file") MultipartFile file,
         @RequestParam("type") String pictureType,
         @RequestParam("referenceId") Long referenceId) {
      
      pictureType = pictureType.toUpperCase();
      PictureType type = PictureType.valueOf(pictureType);
      Picture metadata = storageService.storePicture(file, type, referenceId);
      return ResponseEntity.ok(metadata);
   }

   @GetMapping("/{id}")
   public ResponseEntity<Resource> getPicture(@PathVariable Long id) {
      Resource file = storageService.loadPicture(id);
      return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
            .body(file);
   }

   @DeleteMapping("/{id}")
   public ResponseEntity<?> deletePicture(@PathVariable Long id) {
      storageService.deletePicture(id);
      return ResponseEntity.noContent().build();
   }
}
