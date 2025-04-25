package be.uliege.speam.team03.MDTools.services;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import be.uliege.speam.team03.MDTools.DTOs.InstrumentDTO;
import be.uliege.speam.team03.MDTools.DTOs.SupplierDTO;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.exception.BadRequestException;
import be.uliege.speam.team03.MDTools.mapper.InstrumentMapper;
import be.uliege.speam.team03.MDTools.models.Category;
import be.uliege.speam.team03.MDTools.models.Instruments;
import be.uliege.speam.team03.MDTools.models.PictureType;
import be.uliege.speam.team03.MDTools.models.SubGroup;
import be.uliege.speam.team03.MDTools.models.Supplier;
import be.uliege.speam.team03.MDTools.repositories.CategoryRepository;
import be.uliege.speam.team03.MDTools.repositories.InstrumentRepository;
import be.uliege.speam.team03.MDTools.repositories.SubGroupRepository;
import be.uliege.speam.team03.MDTools.repositories.SupplierRepository;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class InstrumentService {
    private final InstrumentRepository instrumentRepository;
    private final SupplierRepository supplierRepository;
    private final CategoryRepository categoryRepository;
    private final SubGroupRepository subGroupRepository;
    private final InstrumentMapper instrumentMapper;
    private final PictureStorageService pictureStorageService;
    private final SupplierService supplierService;

    /**
     * Find instruments by their reference.
     * 
     * @param reference the reference of the instrument
     * @return a list of instruments matching the reference, or null if no instruments are found
     */
    public List<InstrumentDTO> findInstrumentsByReference(String reference) {
        Optional<Instruments> instrumentMaybe = instrumentRepository.findByReferenceIgnoreCase(reference);
        if (!instrumentMaybe.isPresent()) {
            return null;
        }
        Instruments instrument = instrumentMaybe.get();
        List<InstrumentDTO> instrumentDTOs = new ArrayList<>();
        instrumentDTOs.add(new InstrumentDTO(
            instrument.getSupplier().getSupplierName(),
            instrument.getCategory().getSubGroup().getGroup().getId(),
            instrument.getCategory().getSubGroup().getId(),
            instrument.getCategory().getId(),
            instrument.getReference(),
            instrument.getSupplierDescription(),
            instrument.getPrice(),
            instrument.getObsolete(),
            null,
            instrument.getPriceDate(),
            instrument.getId()
        ));
        return instrumentDTOs;
    }

    /**
     * Find an instrument by its reference.
     * 
     * @param reference the reference of the instrument
     * @return the instrument DTO with the specified reference, or null if no instrument is found
     */
    public InstrumentDTO findByReference(String reference) {
        Optional<Instruments> instrumentMaybe = instrumentRepository.findByReferenceIgnoreCase(reference);
        if (!instrumentMaybe.isPresent()) {
            return null;
        }
        Instruments instrument = instrumentMaybe.get();
        InstrumentDTO dto = new InstrumentDTO();
        dto.setSupplier(instrument.getSupplier() != null ? instrument.getSupplier().getSupplierName() : null);
        dto.setCategoryId(instrument.getCategory() != null ? instrument.getCategory().getId() : null);
        dto.setGroupId(instrument.getCategory() != null ? instrument.getCategory().getSubGroup().getGroup().getId() : null);
        dto.setSubGroupId(instrument.getCategory() != null ? instrument.getCategory().getSubGroup().getId() : null);
        dto.setReference(instrument.getReference());
        dto.setSupplierDescription(instrument.getSupplierDescription());
        dto.setPrice(instrument.getPrice());
        dto.setObsolete(instrument.getObsolete());
        dto.setPicturesId(pictureStorageService.getPicturesIdByReferenceIdAndPictureType((long) instrument.getId(), PictureType.INSTRUMENT));
        dto.setId(instrument.getId());
        dto.setPriceDate(instrument.getPriceDate());
        
        return dto;
    }

    /**
     * Find an instrument by its ID.
     * 
     * @param id the ID of the instrument
     * @return the instrument DTO with the specified ID
     * @throws ResourceNotFoundException if no instrument is found with the given ID
     */
    public InstrumentDTO findById(Long id) throws ResourceNotFoundException {
        Optional<Instruments> instrumentMaybe = instrumentRepository.findById(id);
        if (!instrumentMaybe.isPresent()) {
            throw new ResourceNotFoundException("Instrument not found with ID: " + id);
        }
        return instrumentMapper.convertToDTO(instrumentMaybe.get()); 
    }

    /**
     * Find all instruments.
     * 
     * @return a list of all instrument DTOs
     */
    public List<InstrumentDTO> findAll() {
        List<Instruments> instruments = (List<Instruments>) instrumentRepository.findAll();
        return instrumentMapper.convertToDTO(instruments);
    }

    /**
     * Find all instruments of a specific supplier by their name.
     * 
     * @param supplierName the name of the supplier
     * @return a list of instrument DTOs for the specified supplier
     * @throws ResourceNotFoundException if the supplier is not found
     */
    public List<InstrumentDTO> findInstrumentsBySupplierName(String supplierName) {
        Optional<Supplier> supplierMaybe = supplierRepository.findBySupplierName(supplierName);
        if (supplierMaybe.isEmpty()) {
            throw new ResourceNotFoundException("Supplier not found with name: " + supplierName);
        }
        Supplier supplier = supplierMaybe.get();
        List<Instruments> instruments = instrumentRepository.findBySupplierId(supplier.getId()).orElse(null);
        return instrumentMapper.convertToDTO(instruments);
    }

    /**
     * Find all instruments of a specific supplier by their ID.
     * 
     * @param supplierId the ID of the supplier
     * @return a list of instrument DTOs for the specified supplier, or null if no instruments are found
     * @throws ResourceNotFoundException if the supplier is not found
     * @throws BadRequestException if the supplier ID is invalid
     */
    public List<InstrumentDTO> findInstrumentsBySupplierId(Long supplierId) throws ResourceNotFoundException, BadRequestException {
        Optional<Supplier> supplierMaybe = supplierRepository.findById(supplierId);
        if (!supplierMaybe.isPresent()) {
            return null;
        }
        Supplier supplier = supplierMaybe.get();
        List<Instruments> instruments = instrumentRepository.findBySupplierId(supplier.getId()).orElse(null);
        return instrumentMapper.convertToDTO(instruments);
    }

    /**
     * Update an instrument with the given data.
     * 
     * @param body the data to update the instrument
     * @param id the ID of the instrument to update
     * @return the updated instrument DTO
     * @throws BadRequestException if the data is invalid
     * @throws ResourceNotFoundException if the instrument is not found
     */
    public InstrumentDTO updateInstrument(Map<String, Object> body, Long id) {
        if (body == null || body.isEmpty()) {
            throw new BadRequestException("No data provided to update the instrument.");
        }
        Optional<Instruments> instrumentMaybe = instrumentRepository.findById(id);
        if (!instrumentMaybe.isPresent()) {
            throw new ResourceNotFoundException("Instrument not found with ID: " + id);
        }
        Instruments instrument = instrumentMaybe.get();
        String reference = (String) body.get("reference");
        String supplier = (String) body.get("supplier");
        Long categoryId = ((Number) body.get("categoryId")).longValue();
        String supplierDescription = (String) body.get("supplierDescription");
        Number priceValue = (Number) body.get("price");
        Float price = priceValue != null ? priceValue.floatValue() : null;
        boolean obsolete = (boolean) body.get("obsolete");

        Float previousPrice = instrument.getPrice();

        Optional<Instruments> instrumentByReference = instrumentRepository.findByReferenceIgnoreCase(reference);
        if (instrumentByReference.isPresent() && instrumentByReference.get().getId() != id) {
            throw new BadRequestException("An instrument with this reference already exists.");
        }

        instrument.setReference(reference);
        instrument.setSupplierDescription(supplierDescription);
        instrument.setPrice(price);
        instrument.setObsolete(obsolete);
        instrument.setSupplier(supplierRepository.findBySupplierName(supplier).orElse(null));
        instrument.setCategory(categoryRepository.findById(categoryId).orElse(null));

        if(price != null && previousPrice != price){
            instrument.setPriceDate(new Timestamp(System.currentTimeMillis()));
        }

        Instruments updatedInstrument = instrumentRepository.save(instrument);

        return instrumentMapper.convertToDTO(updatedInstrument);
    }

    /**
     * Find all instruments of a specific category.
     * 
     * @param categoryId the ID of the category
     * @return a list of instrument DTOs for the specified category
     * @throws ResourceNotFoundException if the category is not found
     */
    public List<InstrumentDTO> findInstrumentsOfCatergory(Long categoryId) {

        // retrieve category based on categoryId
        Optional<Category> categoryMaybe = categoryRepository.findById(categoryId);
        if (categoryMaybe.isEmpty()) {
            throw new ResourceNotFoundException("Category with the id " + categoryId + " doesn't exist");
        }
        Category category = categoryMaybe.get();

        // retrieve instruments that matches found category 
        Optional<List<Instruments>> instrumentsMaybe = instrumentRepository.findByCategory(category);
        // is it correct to do so, like can we use the category if we do not have the category name and we just have the id, normally yes, but need to test to be sure
        if (instrumentsMaybe.isPresent() == false) {
            return null;
        }

        List<Instruments> instruments = instrumentsMaybe.get();

        List<InstrumentDTO> instrumentsDTO = new ArrayList<>();

        for (Instruments instrument : instruments) {
            String reference = instrument.getReference();
            String supplierName = instrument.getSupplier().getSupplierName();
            String supplierDescription = instrument.getSupplierDescription();
            Float price = instrument.getPrice();
            boolean obsolete = instrument.getObsolete();

            // retrieve alternatives? 
            Long instrumentId = instrument.getId();
            
            // get pictures of the instrument
            List<Long> pictures = pictureStorageService.getPicturesIdByReferenceIdAndPictureType((long) instrumentId, PictureType.INSTRUMENT);

            InstrumentDTO instrumentDTO = new InstrumentDTO(supplierName, category.getSubGroup().getGroup().getId(), category.getSubGroup().getId(), category.getId(), reference, supplierDescription, price, obsolete, pictures, instrument.getPriceDate(),instrumentId);

            instrumentsDTO.add(instrumentDTO);
        }
        return instrumentsDTO;
    }

    /**
     * Save a new instrument.
     * 
     * @param instrumentDTO the instrument DTO to save
     * @return the saved instrument DTO
     * @throws IllegalArgumentException if required fields are missing
     */
    public InstrumentDTO save(InstrumentDTO instrumentDTO) {
        if (instrumentDTO.getReference() == null || instrumentDTO.getReference().isEmpty()) {
            throw new IllegalArgumentException("Reference is required to identify an instrument");
        }
        if (instrumentDTO.getSupplier() == null || instrumentDTO.getSupplier().isEmpty()) {
            throw new IllegalArgumentException("Supplier name cannot be null or empty");
        }
        if (instrumentDTO.getCategoryId() == null) {
            throw new IllegalArgumentException("Category ID is required to identify an instrument");
        }
        Instruments savedInstrument = instrumentRepository.save(instrumentMapper.convertToEntity(instrumentDTO));
        return instrumentMapper.convertToDTO(savedInstrument);
    }

    /**
     * Delete an instrument by its ID.
     * 
     * @param id the ID of the instrument to delete
     * @throws ResourceNotFoundException if the instrument is not found
     */
    public void delete(Long id) {
        Instruments instrument = instrumentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Instrument not found with id: " + id));
        instrumentRepository.delete(instrument);
    }


    /**
     * Find all instruments linked to a specific sub-group by its name.
     * 
     * @param subGroupName the name of the sub-group
     * @return a list of instrument DTOs for the specified sub-group
     * @throws ResourceNotFoundException if the sub-group is not found
     */
    public List<InstrumentDTO> findInstrumentsBySubGroup(String subGroupName) {
        // Fetch the subgroup by name
        Optional<SubGroup> subGroupMaybe = subGroupRepository.findByName(subGroupName);
        if (subGroupMaybe.isEmpty()) {
            throw new ResourceNotFoundException("SubGroup not found with name: " + subGroupName);
        }
        SubGroup subGroup = subGroupMaybe.get();

        // Fetch all categories within this subgroup
        List<Category> categories = categoryRepository.findBySubGroup(subGroup, Sort.by("subGroupName", "id"));

        List<InstrumentDTO> instrumentsDTO = new ArrayList<>();

        // Fetch instruments for each category
        for (Category category : categories) {
            Optional<List<Instruments>> instrumentsMaybe = instrumentRepository.findByCategory(category);
            if (instrumentsMaybe.isPresent()) {
                List<Instruments> instruments = instrumentsMaybe.get();
                instrumentsDTO.addAll(instrumentMapper.convertToDTO(instruments));
            }
        }

        return instrumentsDTO;
    }

    /**
     * Search for instruments matching the given keywords.
     * 
     * @param keywords a list of keywords to search for
     * @return a list of instrument DTOs matching the keywords, or null if no matches are found
     */
    public List<InstrumentDTO> searchInstrument(List<String> keywords) {
        if (keywords.isEmpty()) {
            return null;
        }
        List<Instruments> instruments = instrumentRepository.searchByKeywords(keywords);
        return instrumentMapper.convertToDTO(instruments);
    }

    /**
     * Add a new instrument.
     * 
     * @param newInstrument the instrument DTO to add
     * @return the saved instrument DTO
     * @throws BadRequestException if required fields are missing or invalid
     */
    public InstrumentDTO addInstrument(InstrumentDTO newInstrument) throws BadRequestException {
        if (newInstrument.getReference() == null || newInstrument.getReference().isEmpty()) {
            throw new BadRequestException("Reference is required to identify an instrument");
        }

        Optional<Instruments> existingInstruments = instrumentRepository.findByReferenceIgnoreCase(newInstrument.getReference());

        if(existingInstruments.isPresent()) {
            throw new BadRequestException("An instrument with this reference already exists.");
        }
        if(newInstrument.getSupplier() == null || newInstrument.getSupplier().isEmpty()) {
            throw new BadRequestException("Supplier name cannot be null or empty");
        }
        if(newInstrument.getCategoryId() == null) {
            throw new BadRequestException("Category ID is required to identify an instrument");
        }
        if(newInstrument.getPrice() == null) {
            newInstrument.setPrice(0F);
        }

        // Check if the supplier exists
        SupplierDTO supplier = supplierService.findSupplierByName(newInstrument.getSupplier());
        if (supplier == null) {
            throw new BadRequestException("Supplier not found: " + newInstrument.getSupplier());
        }

        // Check if the category exists
        Optional<Category> categoryMaybe = categoryRepository.findById(newInstrument.getCategoryId());
        if (categoryMaybe.isEmpty()) {
            throw new BadRequestException("Category not found with ID: " + newInstrument.getCategoryId());
        }

        newInstrument.setPriceDate(new Timestamp(System.currentTimeMillis()));

        return this.save(newInstrument);
    }
}
