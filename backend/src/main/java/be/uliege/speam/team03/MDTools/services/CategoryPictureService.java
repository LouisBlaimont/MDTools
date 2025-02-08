package be.uliege.speam.team03.MDTools.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import be.uliege.speam.team03.MDTools.models.Category;
import be.uliege.speam.team03.MDTools.models.CategoryPictures;
import be.uliege.speam.team03.MDTools.repositories.CategoryPicturesRepository;
import be.uliege.speam.team03.MDTools.repositories.CategoryRepository;

/**
 * Service responsible for processing new category pictures.
 * This includes validating directories, associating images with category,
 * and moving processed files to the main folder.
 */
@Service
public class CategoryPictureService {

    // Path to the main folder for category pictures
    @Value("${category.pictures.folder:/app/pictures/category_pictures}")
    private String picturesFolder;

    // Path to the folder for newly uploaded pictures
    @Value("${category.pictures.new.folder:/app/pictures/new_category_pictures}")
    private String newPicturesFolder;

    private final CategoryRepository categoryRepository;
    private final CategoryPicturesRepository categoryPicturesRepository;

    /**
     * Constructor to inject dependencies.
     *
     * @param categoryRepository Repository for accessing category.
     * @param categoryPicturesRepository Repository for managing category pictures.
     */
    public CategoryPictureService(CategoryRepository categoryRepository, CategoryPicturesRepository categoryPicturesRepository) {
        this.categoryRepository = categoryRepository;
        this.categoryPicturesRepository = categoryPicturesRepository;
    }

    /**
     * Processes new category pictures by validating paths, associating images
     * with category, and moving the files to the main folder.
     */
    public void processNewCategoryPictures() {
        try {
            // Resolve folder paths
            Path newFolderPath = resolvePath(newPicturesFolder);
            Path mainFolderPath = resolvePath(picturesFolder);

            // Validate folder paths
            validateFolder(newFolderPath, "Invalid new category pictures folder path: ");
            validateFolder(mainFolderPath, "Invalid category pictures folder path: ");

            // Process each PNG file in the new pictures folder
            File newFolder = newFolderPath.toFile();
            for (File file : newFolder.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".png")) {
                    processFile(file, mainFolderPath.toFile());
                }
            }
        } catch (Exception e) {
            System.err.println("An error occurred while processing category pictures: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalStateException("Error while processing category pictures: " + e.getMessage(), e);
        }
    }

    /**
     * Resolves the path for a given folder. Checks if the path exists and is a directory.
     *
     * @param folderPath Path to resolve.
     * @return Resolved Path object.
     * @throws IOException If the path does not exist or is inaccessible.
     */
    protected Path resolvePath(String folderPath) throws IOException {
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
    protected void validateFolder(Path folderPath, String errorMessage) {
        File folder = folderPath.toFile();
        if (!folder.exists() || !folder.isDirectory()) {
            throw new IllegalArgumentException(errorMessage + folder.getAbsolutePath());
        }
    }

    /**
     * Processes a single image file: associates it with a category, saves the association
     * in the database, and moves the file to the main folder.
     *
     * @param file Image file to process.
     * @param mainFolder Main folder where the file should be moved.
     */
    private void processFile(File file, File mainFolder) {
        // Extract category ID from the file name (e.g., "12.png" -> category_id = 12)
        String categoryIdStr = file.getName().replace(".png", "");
        Integer categoryId;

        try {
            categoryId = Integer.parseInt(categoryIdStr); // Convert to Integer
        } catch (NumberFormatException e) {
            System.err.println("Invalid file name format for category ID: " + file.getName());
            return; // Skip processing this file
        }

        // Fetch category by ID
        Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
        if (categoryOpt.isPresent()) {
            Category category = categoryOpt.get();

            // Check if the picture already exists in the database
            if (!categoryPicturesRepository.existsByCategoryAndPicturePath(category, "pictures/category_pictures/" + file.getName())) {
                // Save the new picture to the database
                CategoryPictures newPicture = new CategoryPictures(category, "pictures/category_pictures/" + file.getName());
                categoryPicturesRepository.save(newPicture);

                // Move the file to the main folder
                moveFileToMainFolder(file, mainFolder);
            }
        } else {
            System.out.println("No category found for ID: " + categoryId);
        }
    }

    /**
     * Moves a file from its current location to the main folder.
     *
     * @param file File to move.
     * @param mainFolder Destination folder.
     */
    protected void moveFileToMainFolder(File file, File mainFolder) {
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
