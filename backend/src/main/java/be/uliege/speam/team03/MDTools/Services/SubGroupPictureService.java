package be.uliege.speam.team03.MDTools.Services;

import be.uliege.speam.team03.MDTools.models.SubGroups;
import be.uliege.speam.team03.MDTools.models.SubGroupPictures;
import be.uliege.speam.team03.MDTools.repositories.SubGroupRepository;
import be.uliege.speam.team03.MDTools.repositories.SubGroupPicturesRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

/**
 * Service responsible for processing new sub-group pictures.
 * This includes validating directories, associating images with sub-groups,
 * and moving processed files to the main folder.
 */
@Service
public class SubGroupPictureService {

    // Path to the main folder for sub-group pictures
    @Value("${subgroup.pictures.folder:/app/pictures/sub_group_pictures}")
    private String picturesFolder;

    // Path to the folder for newly uploaded pictures
    @Value("${subgroup.pictures.new.folder:/app/pictures/new_sub_group_pictures}")
    private String newPicturesFolder;

    private final SubGroupRepository subGroupRepository;
    private final SubGroupPicturesRepository subGroupPicturesRepository;

    /**
     * Constructor to inject dependencies.
     *
     * @param subGroupRepository Repository for accessing sub-groups.
     * @param subGroupPicturesRepository Repository for managing sub-group pictures.
     */
    public SubGroupPictureService(SubGroupRepository subGroupRepository, SubGroupPicturesRepository subGroupPicturesRepository) {
        this.subGroupRepository = subGroupRepository;
        this.subGroupPicturesRepository = subGroupPicturesRepository;
    }

    /**
     * Processes new sub-group pictures by validating paths, associating images
     * with sub-groups, and moving the files to the main folder.
     */
    public void processNewSubGroupPictures() {
        try {
            // Resolve folder paths
            Path newFolderPath = resolvePath(newPicturesFolder);
            Path mainFolderPath = resolvePath(picturesFolder);

            // Validate folder paths
            validateFolder(newFolderPath, "Invalid new sub-group pictures folder path: ");
            validateFolder(mainFolderPath, "Invalid sub-group pictures folder path: ");

            // Process each PNG file in the new pictures folder
            File newFolder = newFolderPath.toFile();
            for (File file : newFolder.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".png")) {
                    processFile(file, mainFolderPath.toFile());
                }
            }
        } catch (Exception e) {
            System.err.println("An error occurred while processing sub-group pictures: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalStateException("Error while processing sub-group pictures: " + e.getMessage(), e);
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
     * Processes a single image file: associates it with a sub-group, saves the association
     * in the database, and moves the file to the main folder.
     *
     * @param file Image file to process.
     * @param mainFolder Main folder where the file should be moved.
     */
    private void processFile(File file, File mainFolder) {
        // Extract sub-group ID from the file name (e.g., "12.png" -> sub_group_id = 12)
        String subGroupIdStr = file.getName().replace(".png", "");
        Integer subGroupId;

        try {
            subGroupId = Integer.parseInt(subGroupIdStr); // Convert to Integer
        } catch (NumberFormatException e) {
            System.err.println("Invalid file name format for sub-group ID: " + file.getName());
            return; // Skip processing this file
        }

        // Fetch sub-group by ID
        Optional<SubGroups> subGroupOpt = subGroupRepository.findById(subGroupId);
        if (subGroupOpt.isPresent()) {
            SubGroups subGroup = subGroupOpt.get();

            // Check if the picture already exists in the database
            if (!subGroupPicturesRepository.existsBySubGroupAndPicturePath(subGroup, "pictures/sub_group_pictures/" + file.getName())) {
                // Save the new picture to the database
                SubGroupPictures newPicture = new SubGroupPictures(subGroup, "pictures/sub_group_pictures/" + file.getName());
                subGroupPicturesRepository.save(newPicture);

                // Move the file to the main folder
                moveFileToMainFolder(file, mainFolder);
            }
        } else {
            System.out.println("No sub-group found for ID: " + subGroupId);
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
