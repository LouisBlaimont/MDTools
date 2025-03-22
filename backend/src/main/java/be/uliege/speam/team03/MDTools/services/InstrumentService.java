package be.uliege.speam.team03.MDTools.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sound.midi.Instrument;

import org.springframework.stereotype.Service;

import be.uliege.speam.team03.MDTools.DTOs.InstrumentDTO;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.models.Alternatives;
import be.uliege.speam.team03.MDTools.models.Category;
import be.uliege.speam.team03.MDTools.models.Instruments;
<<<<<<< Updated upstream
import be.uliege.speam.team03.MDTools.models.PictureType;
=======
import be.uliege.speam.team03.MDTools.models.SubGroup;
>>>>>>> Stashed changes
import be.uliege.speam.team03.MDTools.models.Suppliers;
import be.uliege.speam.team03.MDTools.repositories.AlternativesRepository;
import be.uliege.speam.team03.MDTools.repositories.CategoryRepository;
import be.uliege.speam.team03.MDTools.repositories.InstrumentRepository;
import be.uliege.speam.team03.MDTools.repositories.SupplierRepository;
<<<<<<< Updated upstream
import be.uliege.speam.team03.MDTools.mapper.InstrumentMapper;
=======
import be.uliege.speam.team03.MDTools.repositories.SubGroupRepository;
>>>>>>> Stashed changes
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class InstrumentService {
    private final InstrumentRepository instrumentRepository;
    private final SupplierRepository supplierRepository;
    private final CategoryRepository categoryRepository;
    private final AlternativesRepository alternativesRepository;
<<<<<<< Updated upstream
    private final InstrumentMapper instrumentMapper;
    private final PictureStorageService pictureStorageService;
=======
    private final SubGroupRepository subGroupRepository;
>>>>>>> Stashed changes

    /**
     * Find all instruments of a specific supplier.
     * 
     * @param supplierName the name of the supplier
     * @return a list of instruments for the specified supplier, or null if no instruments are found
     * @throws IllegalArgumentException if the supplier name is null or empty
     */
    public List<InstrumentDTO> findInstrumentsByReference(String reference) {
        Optional<Instruments> instrumentMaybe = instrumentRepository.findByReference(reference);
        if (!instrumentMaybe.isPresent()) {
            return null;
        }
        Instruments instrument = instrumentMaybe.get();
        List<InstrumentDTO> instrumentDTOs = new ArrayList<>();
        instrumentDTOs.add(new InstrumentDTO(
            instrument.getSupplier().getSupplierName(),
            instrument.getCategory().getId(),
            instrument.getReference(),
            instrument.getSupplierDescription(),
            instrument.getPrice(),
            instrument.getObsolete(),
            !alternativesRepository.findByInstrumentsId1(instrument.getId()).isEmpty(),
            null,
            instrument.getId()
        ));
        return instrumentDTOs;
    }

    /**
     * Find an instrument by its reference.
     * 
     * @param reference the reference of the instrument
     * @return the instrument with the specified reference, or null if no instrument is found
     */
    public InstrumentDTO findByReference(String reference) {
        Optional<Instruments> instrumentMaybe = instrumentRepository.findByReference(reference);
        if (!instrumentMaybe.isPresent()) {
            return null;
        }
        Instruments instrument = instrumentMaybe.get();
        return new InstrumentDTO(
            instrument.getSupplier().getSupplierName(),
            instrument.getCategory().getId(),
            instrument.getReference(),
            instrument.getSupplierDescription(),
            instrument.getPrice(),
            instrument.getObsolete(),
            !alternativesRepository.findByInstrumentsId1(instrument.getId()).isEmpty(),
            null,
            instrument.getId()
        );
    }

    /**
     * Find an instrument by its ID.
     * 
     * @param id the ID of the instrument
     * @return the instrument with the specified ID, or null if no instrument is found
     */
    public InstrumentDTO findById(Integer id) {
        Optional<Instruments> instrumentMaybe = instrumentRepository.findById(id);
        if (!instrumentMaybe.isPresent()) {
            return null;
        }
        Instruments instrument = instrumentMaybe.get();
        return new InstrumentDTO(
            instrument.getSupplier().getSupplierName(),
            instrument.getCategory().getId(),
            instrument.getReference(),
            instrument.getSupplierDescription(),
            instrument.getPrice(),
            instrument.getObsolete(),
            !alternativesRepository.findByInstrumentsId1(instrument.getId()).isEmpty(),
            null,
            instrument.getId()
        );
    }

    /**
     * Find all instruments.
     * 
     * @return a list of all instruments, or null if no instruments are found
     */
    public List<InstrumentDTO> findAll() {
        List<Instruments> instruments = (List<Instruments>) instrumentRepository.findAll();
        return instrumentMapper.convertToDTO(instruments);
    }

    /**
     * Find all instruments of a specific supplier.
     * 
     * @param supplierName the name of the supplier
     * @return a list of instruments for the specified supplier, or null if no instruments are found
     * @throws IllegalArgumentException if the supplier name is null or empty
     */
    public List<InstrumentDTO> findInstrumentsBySupplierName(String supplierName) {
        Optional<Suppliers> supplierMaybe = supplierRepository.findBySupplierName(supplierName);
        if (!supplierMaybe.isPresent()) {
            return null;
        }
        Suppliers supplier = supplierMaybe.get();
        List<Instruments> instruments = instrumentRepository.findBySupplierId(supplier.getId()).orElse(null);
        return instrumentMapper.convertToDTO(instruments);
    }

    /**
     * Find all instruments of a specific supplier.
     * 
     * @param supplierId the ID of the supplier
     * @return a list of instruments for the specified supplier, or null if no instruments are found
     * @throws IllegalArgumentException if the supplier ID is null
     */
    public List<InstrumentDTO> findInstrumentsBySupplierId(Integer supplierId) {
        Optional<Suppliers> supplierMaybe = supplierRepository.findById(supplierId);
        if (!supplierMaybe.isPresent()) {
            return null;
        }
        Suppliers supplier = supplierMaybe.get();
        List<Instruments> instruments = instrumentRepository.findBySupplierId(supplier.getId()).orElse(null);
        return instrumentMapper.convertToDTO(instruments);
    }

    /**
     * Find all instruments of a specific category.
     * 
     * @param categoryId the ID of the category
     * @return a list of instruments for the specified category, or null if no instruments are found
     * @throws IllegalArgumentException if the category ID is null
     */
    public List<InstrumentDTO> findInstrumentsOfCatergory(Integer categoryId) {

        // retrieve category based on categoryId
        Optional<Category> categoryMaybe = categoryRepository.findById(categoryId);
        if (categoryMaybe.isPresent() == false) {
            return null;
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
            Integer instrumentId = instrument.getId();
            List<Alternatives> alternatives = alternativesRepository.findByInstrumentsId1(instrumentId);
            boolean alt = !alternatives.isEmpty();
            
            // get pictures of the instrument
            List<Long> pictures = pictureStorageService.getPicturesIdByReferenceIdAndPictureType((long) instrumentId, PictureType.INSTRUMENT);

            InstrumentDTO instrumentDTO = new InstrumentDTO(supplierName, category.getId(), reference, supplierDescription, price, obsolete, alt, pictures, instrumentId);

            instrumentsDTO.add(instrumentDTO);
        }
        return instrumentsDTO;
    }

<<<<<<< Updated upstream
    /**
     * Find the maximum instrument ID.
     * 
     * @return the maximum instrument ID
     */
    public Integer findMaxInstrumentId() {
        return instrumentRepository.findMaxInstrumentId();
=======

    public InstrumentDTO save(InstrumentDTO instrumentDTO) {
        Instruments instrument = new Instruments();
        instrument.setReference(instrumentDTO.getReference());
        instrument.setSupplierDescription(instrumentDTO.getSupplierDescription());
        instrument.setPrice(instrumentDTO.getPrice());
        instrument.setObsolete(instrumentDTO.isObsolete());

        // retrieve supplier based on supplier name
        Optional<Suppliers> supplierMaybe = supplierRepository.findBySupplierName(instrumentDTO.getSupplier());
        if (supplierMaybe.isPresent() == false) {
            return null;
        }
        Suppliers supplier = supplierMaybe.get();
        instrument.setSupplier(supplier);

        // retrieve category based on category id
        Optional<Category> categoryMaybe = categoryRepository.findById(instrumentDTO.getCategoryId());
        if (categoryMaybe.isPresent() == false) {
            return null;
        }
        Category category = categoryMaybe.get();
        instrument.setCategory(category);

        Instruments savedInstrument = instrumentRepository.save(instrument);
        return new InstrumentDTO(
            savedInstrument.getSupplier().getSupplierName(),
            savedInstrument.getCategory().getId(),
            savedInstrument.getReference(),
            savedInstrument.getSupplierDescription(),
            savedInstrument.getPrice(),
            savedInstrument.getObsolete(),
            !alternativesRepository.findByInstrumentsId1(savedInstrument.getId()).isEmpty()
        );
>>>>>>> Stashed changes
    }

    /**
     * Save an instrument.
     * 
     * @param instrumentDTO the instrument to save
     * @return the saved instrument
     * @throws IllegalArgumentException if the supplier name is null or empty
     */
    public InstrumentDTO save(InstrumentDTO instrumentDTO) {
        if (instrumentDTO.getSupplier() == null || instrumentDTO.getSupplier().isEmpty()) {
            throw new IllegalArgumentException("Supplier name cannot be null or empty");
        }
        if (instrumentDTO.getReference().isEmpty()) {
            throw new IllegalArgumentException("Reference is required to identify an instrument");
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
     */
    public void delete(Integer id) {
        Instruments instrument = instrumentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Instrument not found with id: " + id));
        instrumentRepository.delete(instrument);
    }

    public List<InstrumentDTO> findInstrumentsBySubGroup(String subGroupName) {
        // Fetch the subgroup by name
        Optional<SubGroup> subGroupMaybe = subGroupRepository.findByName(subGroupName);
        if (!subGroupMaybe.isPresent()) {
            return null; // No subgroup found
        }
        SubGroup subGroup = subGroupMaybe.get();

        // Fetch all categories within this subgroup
        Optional<List<Category>> categoriesMaybe = categoryRepository.findBySubGroup(subGroup);
        if (!categoriesMaybe.isPresent() || categoriesMaybe.get().isEmpty()) {
            return null; // No categories found
        }

        List<Category> categories = categoriesMaybe.get();
        List<InstrumentDTO> instrumentsDTO = new ArrayList<>();

        // Fetch instruments for each category
        for (Category category : categories) {
            Optional<List<Instruments>> instrumentsMaybe = instrumentRepository.findByCategory(category);
            if (instrumentsMaybe.isPresent()) {
                List<Instruments> instruments = instrumentsMaybe.get();
                instrumentsDTO.addAll(convertToDTO(instruments)); // Convert to DTO format
            }
        }

        return instrumentsDTO;
    }
}
