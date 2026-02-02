package be.uliege.speam.team03.MDTools.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.coyote.BadRequestException;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import be.uliege.speam.team03.MDTools.DTOs.InstrumentDTO;
import be.uliege.speam.team03.MDTools.exception.ServerErrorException;
import be.uliege.speam.team03.MDTools.models.Picture;
import be.uliege.speam.team03.MDTools.models.PictureType;
import be.uliege.speam.team03.MDTools.services.InstrumentService;
import be.uliege.speam.team03.MDTools.services.PictureStorageService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

/**
 * REST controller for managing pictures.
 * Provides endpoints for uploading, retrieving, and deleting pictures.
 */
@Log4j2
@RestController
@RequestMapping("/api/pictures")
@AllArgsConstructor
public class PictureController {
   private PictureStorageService storageService;
   private InstrumentService instrumentService;

   /**
    * Uploads a single picture file to the storage service.
    *
    * @param file        the picture file to be uploaded
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
    * Uploads a single picture for an instrument.
    * 
    * @param file        The input stream containing the picture file data
    * @param referenceId The ID of the instrument that the picture belongs to
    * @return The metadata of the stored picture
    */
   private Picture uploadSingleInstrumentPicture(InputStream file, Long referenceId) {
      PictureType type = PictureType.INSTRUMENT;
      Picture metadata = storageService.storePicture(file, type, referenceId);
      return metadata;
   }

   /**
    * Handles the HTTP POST request for uploading a picture.
    *
    * @param file        the picture file to be uploaded
    * @param pictureType the type of the picture (e.g., "JPEG", "PNG")
    * @param referenceId the reference ID associated with the picture
    * @return a ResponseEntity containing the metadata of the uploaded picture
    */
   @PostMapping("/single")
   public ResponseEntity<Picture> uploadPicture(
         @RequestParam("file") MultipartFile file,
         @RequestParam("type") String pictureType,
         @RequestParam("referenceId") Long referenceId) {

      if (file == null || file.isEmpty()) {
         throw new IllegalArgumentException("File cannot be empty");
      }

      MultipartFile fileToSend = file;
      Picture metadataList = this.uploadSinglePicture(fileToSend, pictureType, referenceId);

      return ResponseEntity.ok(metadataList);
   }

   /**
    * Handles the HTTP POST request for uploading a picture.
    *
    * @param files       the multipe picture files to be uploaded
    * @param pictureType the type of the picture (e.g., "JPEG", "PNG")
    * @param referenceId the reference ID associated with the picture
    * @return a ResponseEntity containing the metadata of the uploaded pictures
    */
   @PostMapping("/multiple")
   public ResponseEntity<List<Picture>> uploadPictures(
         @RequestParam("files") List<MultipartFile> files,
         @RequestParam("type") String pictureType,
         @RequestParam("referenceId") Long referenceId) {

      List<Picture> metadataList = new ArrayList<>();
      for (MultipartFile file : files) {
         metadataList.add(this.uploadSinglePicture(file, pictureType, referenceId));
      }
      return ResponseEntity.ok(metadataList);
   }

   /**
    * Handles HTTP GET requests to retrieve a picture by its ID.
    *
    * @param id the ID of the picture to retrieve
    * @return a ResponseEntity containing the picture as a Resource, with the
    *         appropriate
    *         Content-Disposition header to prompt a download with the original
    *         filename
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

   /**
    * Controller method to handle bulk upload of instrument pictures from zip
    * files.
    * 
    * @param zipFiles List of zip files containing instrument pictures
    * @return List of processing results for each picture
    */
   @PostMapping("/instruments/bulk-upload")
   public ResponseEntity<List<PictureProcessResponse>> uploadInstrumentsPictureBulk(
         @NonNull @RequestParam List<MultipartFile> zipFiles) throws BadRequestException {

      List<PictureProcessResponse> results = new ArrayList<>();

      // Validate input
      if (zipFiles.isEmpty()) {
         throw new BadRequestException("No zip files provided");
      }

      Path tempDir = null;
      try {
         // Create a temp directory to extract files
         tempDir = Files.createTempDirectory("image-processing");

         for (MultipartFile zipFile : zipFiles) {
            // Validate the file type
            String contentType = zipFile.getContentType();
            if (contentType == null || !contentType.equals("application/zip")) {
               results.add(new PictureProcessResponse(zipFile.getOriginalFilename(),
                     "Invalid file type. Only zip files are allowed.", false));
               continue;
            }

            processZipFile(zipFile, tempDir, results);
         }

         return ResponseEntity.ok(results);

      } catch (Exception e) {
         log.error("Failed to process zip files", e);
         throw new ServerErrorException("Failed to process zip files: " + e.getMessage(), e);
      } finally {
         cleanupTempDirectory(tempDir);
      }
   }

   @PostMapping("/instruments/upload-with-reference")
   public ResponseEntity<Void> uploadInstrumentsPictureBulkFormData(
         @NonNull @RequestParam MultipartFile picture, @NonNull @RequestParam String reference) throws BadRequestException {

      // Search for the instrument by reference
      InstrumentDTO instrument = instrumentService.findByReference(reference);

      // Add picture to the instrument
      uploadSinglePicture(picture, "INSTRUMENT", instrument.getId());

      return ResponseEntity.ok().build();
   }

   /**
    * Process a single zip file, extracting and processing all contained images
    */
   private void processZipFile(MultipartFile zipFile, Path tempDir, List<PictureProcessResponse> results) {
      log.info("Processing zip file: " + zipFile.getOriginalFilename());

      try (ZipInputStream zipInputStream = new ZipInputStream(zipFile.getInputStream())) {
         ZipEntry zipEntry;

         // Extract and process each file in the zip
         while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            try {
               // Skip directories
               if (zipEntry.isDirectory()) {
                  continue;
               }

               // Process the file
               String filename = zipEntry.getName();

               processExtractedFile(zipInputStream, filename, tempDir, results);

            } finally {
               zipInputStream.closeEntry();
            }
         }
      } catch (IOException e) {
         results.add(new PictureProcessResponse(zipFile.getOriginalFilename(),
               "Error processing zip file: " + e.getMessage(), false));
         log.error("Failed to process zip file: {}", zipFile.getOriginalFilename(), e);
      }
   }

   /**
    * Process an individual file extracted from the zip
    */
   private void processExtractedFile(ZipInputStream zipInputStream, String filename,
         Path tempDir, List<PictureProcessResponse> results) {
      try {
         // Skip hidden files or files without an extension
         if (filename.startsWith(".") || !filename.contains(".")) {
            results.add(new PictureProcessResponse(filename,
                  "Skipping file: " + filename + " - Invalid filename format", false));
            return;
         }

         // Create a temp file to store the extracted file
         Path tempFile = tempDir.resolve(filename);

         // Copy the zip entry to the temp file
         Files.copy(zipInputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);

         // Extract the instrument reference (filename without extension)
         String instrumentRef = getInstrumentReference(filename);

         // Check if the reference is valid
         if (instrumentRef.isEmpty()) {
            results.add(new PictureProcessResponse(filename,
                  "Invalid instrument reference extracted from filename", false));
            return;
         }

         // Find the instrument by reference
         InstrumentDTO instrument = instrumentService.findByReference(instrumentRef);
         if (instrument == null) {
            results.add(new PictureProcessResponse(filename,
                  "Instrument with reference '" + instrumentRef + "' not found", false));
            return;
         }

         // Upload the picture
         Picture stored = storageService.storePictureIfNotExistsForReference(
            Files.newInputStream(tempFile),
            filename,
            PictureType.INSTRUMENT,
            instrument.getId().longValue()
         );

         if (stored == null) {
            results.add(new PictureProcessResponse(filename,
                  "Skipped: instrument already has picture(s)", true));
            return;
         }

         results.add(new PictureProcessResponse(filename, "Uploaded successfully", true));

      } catch (IOException e) {
         results.add(new PictureProcessResponse(filename, "Error processing file: " + e.getMessage(), false));
         log.error("Failed to process file: {}", filename, e);
      }
   }

   /**
    * Extract the instrument reference from filename
    */
   private String getInstrumentReference(String filename) {
      int lastDotIndex = filename.lastIndexOf('.');
      return lastDotIndex > 0 ? filename.substring(0, lastDotIndex) : "";
   }

   /**
    * Clean up the temporary directory
    */
   private void cleanupTempDirectory(Path tempDir) {
      if (tempDir != null) {
         try {
            FileSystemUtils.deleteRecursively(tempDir);
         } catch (IOException e) {
            log.warn("Failed to delete temp directory: {}", tempDir, e);
            // Don't throw exception here to avoid masking the primary error
         }
      }
   }

   @AllArgsConstructor
   @Getter
   public class PictureProcessResponse {
      private String fileName;
      private String message;
      private boolean success;
   }

}
