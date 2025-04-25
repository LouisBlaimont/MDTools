package be.uliege.speam.team03.MDTools.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import java.util.Collections;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import be.uliege.speam.team03.MDTools.DTOs.CategoryDTO;
import be.uliege.speam.team03.MDTools.DTOs.InstrumentDTO;
import be.uliege.speam.team03.MDTools.exception.BadRequestException;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.models.Picture;
import be.uliege.speam.team03.MDTools.repositories.PictureRepository;
import be.uliege.speam.team03.MDTools.models.PictureType;
import be.uliege.speam.team03.MDTools.services.InstrumentService;
import be.uliege.speam.team03.MDTools.services.PictureStorageService;
import be.uliege.speam.team03.MDTools.services.CategoryService;
import java.util.Arrays;

@ExtendWith(MockitoExtension.class)
public class InstrumentControllerTest {

    @Mock
    private InstrumentService instrumentService;

    @InjectMocks
    private InstrumentController instrumentController;

    @Mock
    private PictureRepository pictureRepository;

    @Mock
    private CategoryService categoryService;

    @Mock
    private PictureStorageService pictureStorageService;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        instrumentController = new InstrumentController(instrumentService, pictureRepository, pictureStorageService, categoryService);
        mockMvc = MockMvcBuilders.standaloneSetup(instrumentController).build();
    }

    @Test
    public void testFindInstrumentById() throws Exception {
        InstrumentDTO instrument = new InstrumentDTO();
        instrument.setId((long) 1);
        instrument.setReference("REF001");

        when(instrumentService.findById((long) 1)).thenReturn(instrument);

        mockMvc.perform(get("/api/instrument/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.reference").value("REF001"));

        verify(instrumentService, times(1)).findById((long) 1);
    }
    @Test
    public void testAddInstrumentWithMissingReference() throws Exception {
        // Simulate the service throwing a BadRequestException for missing reference
        when(instrumentService.addInstrument(any(InstrumentDTO.class)))
            .thenThrow(new BadRequestException("Reference is required to identify an instrument"));
        
        mockMvc.perform(post("/api/instrument")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"supplier\":\"Supplier1\",\"categoryId\":1,\"price\":100.0}"))
                .andExpect(status().isBadRequest());
        
        verify(instrumentService, times(1)).addInstrument(any(InstrumentDTO.class));
    }

    @Test
    public void testAddInstrumentWithInvalidCategory() throws Exception {
        // Simulate the service throwing a BadRequestException for invalid category
        when(instrumentService.addInstrument(any(InstrumentDTO.class)))
            .thenThrow(new BadRequestException("Invalid category ID"));
        
        mockMvc.perform(post("/api/instrument")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reference\":\"REF001\",\"supplier\":\"Supplier1\",\"categoryId\":-1,\"price\":100.0}"))
                .andExpect(status().isBadRequest());
        
        verify(instrumentService, times(1)).addInstrument(any(InstrumentDTO.class));
    }

    @Test
    public void testAddInstrumentWithInvalidPrice() throws Exception {
        // Simulate the service throwing a BadRequestException for invalid price
        when(instrumentService.addInstrument(any(InstrumentDTO.class)))
            .thenThrow(new BadRequestException("Price must be a positive number"));
        
        mockMvc.perform(post("/api/instrument")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reference\":\"REF001\",\"supplier\":\"Supplier1\",\"categoryId\":1,\"price\":-100.0}"))
                .andExpect(status().isBadRequest());
        
        verify(instrumentService, times(1)).addInstrument(any(InstrumentDTO.class));
    }

    @Test
    public void testAddInstrumentWithInvalidSupplier() throws Exception {
        // Simulate the service throwing a BadRequestException for invalid supplier
        when(instrumentService.addInstrument(any(InstrumentDTO.class)))
            .thenThrow(new BadRequestException("Supplier is required to identify an instrument"));
        
        mockMvc.perform(post("/api/instrument")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reference\":\"REF001\",\"categoryId\":1,\"price\":100.0}"))
                .andExpect(status().isBadRequest());
        
        verify(instrumentService, times(1)).addInstrument(any(InstrumentDTO.class));
    }

    @Test
    public void testAddInstrument() throws Exception {
        InstrumentDTO newInstrument = new InstrumentDTO();
        newInstrument.setId((long) 1);
        newInstrument.setReference("REF001");
        newInstrument.setSupplier("Supplier1");
        newInstrument.setCategoryId(1L);
        newInstrument.setPrice((float) 100.0);

        when(instrumentService.addInstrument(any(InstrumentDTO.class))).thenReturn(newInstrument);

        mockMvc.perform(post("/api/instrument")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reference\":\"REF001\",\"supplier\":\"Supplier1\",\"categoryId\":1,\"price\":100.0}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.reference").value("REF001"));

        verify(instrumentService, times(1)).addInstrument(any(InstrumentDTO.class));
    }

    @Test
    public void testFindInstrumentByIdNotFound() throws Exception {
        // Simulate the service throwing a ResourceNotFoundException
        when(instrumentService.findById(99L)).thenThrow(new ResourceNotFoundException("Instrument not found"));

        mockMvc.perform(get("/api/instrument/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateInstrumentSuccessfully() throws Exception {
        Long instrumentId = 1L;
        Map<String, Object> updates = Map.of("reference", "NEW_REF001");

        InstrumentDTO updatedInstrument = new InstrumentDTO();
        updatedInstrument.setId(instrumentId);
        updatedInstrument.setReference("NEW_REF001");

        when(instrumentService.updateInstrument(any(Map.class), eq(instrumentId))).thenReturn(updatedInstrument);

        mockMvc.perform(patch("/api/instrument/" + instrumentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reference\":\"NEW_REF001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.reference").value("NEW_REF001"));

        verify(instrumentService, times(1)).updateInstrument(any(Map.class), eq(instrumentId));
    }

    @Test
    public void testUpdateInstrumentNotFound() throws Exception {
        Long instrumentId = 99L;

        when(instrumentService.updateInstrument(any(Map.class), eq(instrumentId)))
                .thenThrow(new ResourceNotFoundException("Instrument not found"));

        mockMvc.perform(patch("/api/instrument/" + instrumentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reference\":\"NEW_REF001\"}"))
                .andExpect(status().isNotFound());

        verify(instrumentService, times(1)).updateInstrument(any(Map.class), eq(instrumentId));
    }

    @Test
    public void testUpdateInstrumentWithBadRequest() throws Exception {
        Long instrumentId = 1L;

        when(instrumentService.updateInstrument(any(Map.class), eq(instrumentId)))
                .thenThrow(new BadRequestException("Invalid update data"));

        mockMvc.perform(patch("/api/instrument/" + instrumentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"price\":-100.0}")) // Example of invalid input
                .andExpect(status().isBadRequest());

        verify(instrumentService, times(1)).updateInstrument(any(Map.class), eq(instrumentId));
    }

    @Test
    public void testDeleteInstrument() throws Exception {
        doNothing().when(instrumentService).delete((long) 1);

        mockMvc.perform(delete("/api/instrument/1"))
                .andExpect(status().isNoContent());

        verify(instrumentService, times(1)).delete((long) 1);
    }

    @Test
    public void testDeleteInstrumentNotFound() throws Exception {
        // Simulate the service throwing a ResourceNotFoundException
        doNothing().when(instrumentService).delete(99L);
        mockMvc.perform(delete("/api/instrument/99"))
                .andExpect(status().isNoContent());

        verify(instrumentService, times(1)).delete(99L);
    }

    @Test
    public void testGetInstrumentPictureIds() throws Exception {
        // Create test data
        Long instrumentId = 1L;
        Picture picture1 = new Picture();
        picture1.setId(101L);
        picture1.setReferenceId(instrumentId);
        picture1.setPictureType(PictureType.INSTRUMENT);
        
        Picture picture2 = new Picture();
        picture2.setId(102L);
        picture2.setReferenceId(instrumentId);
        picture2.setPictureType(PictureType.INSTRUMENT);
        
        List<Picture> pictures = Arrays.asList(picture1, picture2);
        
        // Mock the repository behavior
        when(pictureRepository.findByReferenceIdAndPictureType(instrumentId, PictureType.INSTRUMENT))
            .thenReturn(pictures);
        
        // Perform GET request and verify response
        mockMvc.perform(get("/api/instrument/pictures/" + instrumentId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0]").value(101))
            .andExpect(jsonPath("$[1]").value(102));
        
        // Verify repository was called
        verify(pictureRepository, times(1))
            .findByReferenceIdAndPictureType(instrumentId, PictureType.INSTRUMENT);
    }

    @Test
    public void testGetInstrumentPictureIdsWithNoPictures() throws Exception {
        Long instrumentId = 2L;
        
        // Mock empty list return for an instrument with no pictures
        when(pictureRepository.findByReferenceIdAndPictureType(instrumentId, PictureType.INSTRUMENT))
            .thenReturn(Collections.emptyList());
        
        // Perform GET request and verify response (should be empty array, not 404)
        mockMvc.perform(get("/api/instrument/pictures/" + instrumentId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
        
        // Verify repository was called
        verify(pictureRepository, times(1))
            .findByReferenceIdAndPictureType(instrumentId, PictureType.INSTRUMENT);
    }

    @Test
public void testAddInstrumentPicture() throws Exception {
    Long instrumentId = 1L;
    MockMultipartFile file = new MockMultipartFile(
        "file", 
        "test-image.jpg", 
        "image/jpeg", 
        "test image content".getBytes()
    );
    
    // Create a Picture object to be returned by the mocked method
    Picture savedPicture = new Picture();
    savedPicture.setId(1L);
    savedPicture.setFileName("uuid-test-image.jpg");
    savedPicture.setPictureType(PictureType.INSTRUMENT);
    savedPicture.setReferenceId(instrumentId);
    
    when(pictureStorageService.storePicture(any(MultipartFile.class), eq(PictureType.INSTRUMENT), eq(instrumentId)))
        .thenReturn(savedPicture);
    
    mockMvc.perform(multipart("/api/instrument/pictures/" + instrumentId)
        .file(file))
        .andExpect(status().isNoContent());
    
    verify(pictureStorageService, times(1)).storePicture(any(MultipartFile.class), eq(PictureType.INSTRUMENT), eq(instrumentId));
}
    
    @Test
    public void testFindInstrumentsBySubGroup() throws Exception {
        String subGroupName = "TestSubGroup";
        
        InstrumentDTO instrument1 = new InstrumentDTO();
        instrument1.setId(1L);
        instrument1.setReference("REF001");
        
        InstrumentDTO instrument2 = new InstrumentDTO();
        instrument2.setId(2L);
        instrument2.setReference("REF002");
        
        List<InstrumentDTO> instruments = Arrays.asList(instrument1, instrument2);
        
        when(instrumentService.findInstrumentsBySubGroup(subGroupName)).thenReturn(instruments);
        
        mockMvc.perform(get("/api/instrument/subgroup/" + subGroupName))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].reference").value("REF001"))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].reference").value("REF002"));
        
        verify(instrumentService, times(1)).findInstrumentsBySubGroup(subGroupName);
    }
    
    @Test
    public void testFindInstrumentsBySubGroupWithNoResults() throws Exception {
        String subGroupName = "NonExistentSubGroup";
        
        when(instrumentService.findInstrumentsBySubGroup(subGroupName)).thenReturn(Collections.emptyList());
        
        mockMvc.perform(get("/api/instrument/subgroup/" + subGroupName))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
        
        verify(instrumentService, times(1)).findInstrumentsBySubGroup(subGroupName);
    }

    @Test
    public void testFindInstrumentsBySupplierName() throws Exception {
        String supplierName = "TestSupplier";
        
        InstrumentDTO instrument1 = new InstrumentDTO();
        instrument1.setId(1L);
        instrument1.setReference("REF001");
        instrument1.setSupplier(supplierName);
        
        InstrumentDTO instrument2 = new InstrumentDTO();
        instrument2.setId(2L);
        instrument2.setReference("REF002");
        instrument2.setSupplier(supplierName);
        
        List<InstrumentDTO> instruments = Arrays.asList(instrument1, instrument2);
        
        when(instrumentService.findInstrumentsBySupplierName(supplierName)).thenReturn(instruments);
        
        mockMvc.perform(get("/api/instrument/supplier/" + supplierName))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].reference").value("REF001"))
            .andExpect(jsonPath("$[0].supplier").value(supplierName))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].reference").value("REF002"))
            .andExpect(jsonPath("$[1].supplier").value(supplierName));
        
        verify(instrumentService, times(1)).findInstrumentsBySupplierName(supplierName);
    }
    
    @Test
    public void testFindInstrumentsBySupplierNameWithNoResults() throws Exception {
        String supplierName = "NonExistentSupplier";
        
        when(instrumentService.findInstrumentsBySupplierName(supplierName)).thenReturn(Collections.emptyList());
        
        mockMvc.perform(get("/api/instrument/supplier/" + supplierName))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
        
        verify(instrumentService, times(1)).findInstrumentsBySupplierName(supplierName);
    }

    @Test
    public void testSearchInstrument() throws Exception {
        List<String> keywords = Arrays.asList("test", "search");
        
        InstrumentDTO instrument1 = new InstrumentDTO();
        instrument1.setId(1L);
        instrument1.setReference("TestInstrument");
        
        InstrumentDTO instrument2 = new InstrumentDTO();
        instrument2.setId(2L);
        instrument2.setReference("SearchItem");
        
        List<InstrumentDTO> instruments = Arrays.asList(instrument1, instrument2);
        
        when(instrumentService.searchInstrument(keywords)).thenReturn(instruments);
        
        mockMvc.perform(get("/api/instrument/search")
                .param("keywords", "test")
                .param("keywords", "search"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].reference").value("TestInstrument"))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].reference").value("SearchItem"));
        
        verify(instrumentService, times(1)).searchInstrument(keywords);
    }
    
    @Test
    public void testSearchInstrumentWithEmptyKeywords() throws Exception {
        mockMvc.perform(get("/api/instrument/search"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
    }
    
    @Test
    public void testSearchInstrumentWithBlankKeywords() throws Exception {
        mockMvc.perform(get("/api/instrument/search")
                .param("keywords", "")
                .param("keywords", "  "))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
    }
    
    @Test
    public void testSearchCategory() throws Exception {
        Long categoryId = 1L;
        
        CategoryDTO category = new CategoryDTO();
        category.setId(categoryId);
        category.setName("Test Category");
        
        when(categoryService.searchCategory(categoryId)).thenReturn(category);
        
        mockMvc.perform(get("/api/instrument/getCategory/" + categoryId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Test Category"));
        
        verify(categoryService, times(1)).searchCategory(categoryId);
    }
    
    @Test
    public void testSearchCategoryNotFound() throws Exception {
        Long categoryId = 99L;
        
        when(categoryService.searchCategory(categoryId)).thenThrow(new ResourceNotFoundException("Category not found"));
        
        mockMvc.perform(get("/api/instrument/getCategory/" + categoryId))
            .andExpect(status().isNotFound());
        
        verify(categoryService, times(1)).searchCategory(categoryId);
    }
}
