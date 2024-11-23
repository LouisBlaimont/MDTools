package be.uliege.speam.team03.MDTools.services;

import be.uliege.speam.team03.MDTools.models.Instruments;
import be.uliege.speam.team03.MDTools.models.InstrumentPictures;
import be.uliege.speam.team03.MDTools.repositories.InstrumentPicturesRepository;
import be.uliege.speam.team03.MDTools.repositories.InstrumentRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

/**
 * Service responsible for processing new instrument pictures.
 * This includes validating directories, associating images with instruments,
 * and moving processed files to the main folder.
 */
@Service
public class InstrumentPictureService {

    // Path to the main folder for instrument pictures
    @Value("${instrument.pictures.folder:/app/pictures/instrument_pictures}")
    private String picturesFolder;

    // Path to the folder for newly uploaded pictures
    @Value("${instrument.pictures.new.folder:/app/pictures/new_instrument_pictures}")
    private String newPicturesFolder;

    private final InstrumentRepository instrumentRepository;
    private final InstrumentPicturesRepository instrumentPicturesRepository;

    /**
     * Constructor to inject dependencies.
     *
     * @param instrumentRepository Repository for accessing instruments.
     * @param instrumentPicturesRepository Repository for managing instrument pictures.
     */
    public InstrumentPictureService(InstrumentRepository instrumentRepository, InstrumentPicturesRepository instrumentPicturesRepository) {
        this.instrumentRepository = instrumentRepository;
        this.instrumentPicturesRepository = instrumentPicturesRepository;
    }

    /**
     * Processes new instrument pictures by validating paths, associating images
     * with instruments, and moving the files to the main folder.
     */
    public void processNewInstrumentPictures() {
        try {
            // Resolve folder paths
            Path newFolderPath = resolvePath(newPicturesFolder);
            Path mainFolderPath = resolvePath(picturesFolder);

            // Validate folder paths
            validateFolder(newFolderPath, "Invalid new pictures folder path: ");
            validateFolder(mainFolderPath, "Invalid main pictures folder path: ");

            // Process each PNG file in the new pictures folder
            File newFolder = newFolderPath.toFile();
            for (File file : newFolder.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".png")) {
                    processFile(file, mainFolderPath.toFile());
                }
            }
        } catch (Exception e) {
            System.err.println("An error occurred while processing images: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalStateException("Error while processing images: " + e.getMessage(), e);
        }
    }

    /**
     * Resolves the path for a given folder. Checks if the path exists and is a directory.
     *
     * @param folderPath Path to resolve.
     * @return Resolved Path object.
     * @throws IOException If the path does not exist or is inaccessible.
     */
    private Path resolvePath(String folderPath) throws IOException {
        File folder = new File(folderPath);
        if (folder.exists() && folder.isDirectory()) {
            System.out.println("Path found: " + folder.getAbsolutePath());
            return folder.toPath();
        } else {
            throw new IOException("The path " + folderPath + " is not found or inaccessible.");
        }
    }

    /**
     * Validates if a given path exists and is a directory.
     *
     * @param folderPath Path to validate.
     * @param errorMessage Error message to display if validation fails.
     */
    private void validateFolder(Path folderPath, String errorMessage) {
        File folder = folderPath.toFile();
        if (!folder.exists() || !folder.isDirectory()) {
            throw new IllegalArgumentException(errorMessage + folder.getAbsolutePath());
        }
    }

    /**
     * Processes a single image file: associates it with an instrument, saves the association
     * in the database, and moves the file to the main folder.
     *
     * @param file Image file to process.
     * @param mainFolder Main folder where the file should be moved.
     */
    private void processFile(File file, File mainFolder) {
        String reference = file.getName().replace(".png", ""); // Extract reference from file name

        Optional<Instruments> instrumentOpt = instrumentRepository.findByReference(reference);
        if (instrumentOpt.isPresent()) {
            Instruments instrument = instrumentOpt.get();

            // Check if the picture already exists in the database
            if (!instrumentPicturesRepository.existsByInstrumentAndPicturePath(instrument, "pictures/instrument_pictures/" + file.getName())) {
                // Save the new picture to the database
                InstrumentPictures newPicture = new InstrumentPictures(instrument, "pictures/instrument_pictures/" + file.getName());
                instrumentPicturesRepository.save(newPicture);

                // Move the file to the main folder
                moveFileToMainFolder(file, mainFolder);
            }
        } else {
            System.out.println("No instrument found for reference: " + reference);
        }
    }

    /**
     * Moves a file from its current location to the main folder.
     *
     * @param file File to move.
     * @param mainFolder Destination folder.
     */
    private void moveFileToMainFolder(File file, File mainFolder) {
        try {
            Path source = file.toPath();
            Path target = mainFolder.toPath().resolve(file.getName());
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("Error while moving the file: " + file.getName());
            e.printStackTrace();
        }
    }
}
