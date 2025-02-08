package be.uliege.speam.team03.MDTools.services;

import be.uliege.speam.team03.MDTools.models.Category;
import be.uliege.speam.team03.MDTools.models.CategoryPictures;
import be.uliege.speam.team03.MDTools.repositories.CategoryPicturesRepository;
import be.uliege.speam.team03.MDTools.repositories.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class CategoryPictureServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryPicturesRepository categoryPicturesRepository;

    @InjectMocks
    private CategoryPictureService categoryPictureService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
}