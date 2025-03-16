package be.uliege.speam.team03.MDTools.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import be.uliege.speam.team03.MDTools.DTOs.InstrumentDTO;
import be.uliege.speam.team03.MDTools.models.Alternatives;
import be.uliege.speam.team03.MDTools.models.Category;
import be.uliege.speam.team03.MDTools.models.Instruments;
import be.uliege.speam.team03.MDTools.models.PictureType;
import be.uliege.speam.team03.MDTools.models.Suppliers;
import be.uliege.speam.team03.MDTools.repositories.AlternativesRepository;
import be.uliege.speam.team03.MDTools.repositories.CategoryRepository;
import be.uliege.speam.team03.MDTools.repositories.InstrumentRepository;
import be.uliege.speam.team03.MDTools.repositories.SupplierRepository;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class InstrumentService {
    private final InstrumentRepository instrumentRepository;
    private final SupplierRepository supplierRepository;
    private final CategoryRepository categoryRepository;
    private final AlternativesRepository alternativesRepository;
    private final PictureStorageService pictureStorageService;


    public List<InstrumentDTO> findInstrumentsByReference(String reference) {
        Optional<Instruments> instrumentMaybe = instrumentRepository.findByReference(reference);
        if (!instrumentMaybe.isPresent()) {
            return null;
        }
        Instruments instrument = instrumentMaybe.get();
        List<InstrumentDTO> instrumentDTOs = new ArrayList<>();
        instrumentDTOs.add(new InstrumentDTO(
            instrument.getId(),
            instrument.getSupplier().getSupplierName(),
            instrument.getCategory().getId(),
            instrument.getReference(),
            instrument.getSupplierDescription(),
            instrument.getPrice(),
            instrument.getObsolete(),
            !alternativesRepository.findByInstrumentsId1(instrument.getId()).isEmpty(),
            null
        ));
        return instrumentDTOs;
    }

    public InstrumentDTO findById(Integer id) {
        Optional<Instruments> instrumentMaybe = instrumentRepository.findById(id);
        if (!instrumentMaybe.isPresent()) {
            return null;
        }
        Instruments instrument = instrumentMaybe.get();
        return new InstrumentDTO(
            instrument.getId(),
            instrument.getSupplier().getSupplierName(),
            instrument.getCategory().getId(),
            instrument.getReference(),
            instrument.getSupplierDescription(),
            instrument.getPrice(),
            instrument.getObsolete(),
            !alternativesRepository.findByInstrumentsId1(instrument.getId()).isEmpty(),
            null
        );
    }

    public List<InstrumentDTO> findAll() {
        List<Instruments> instruments = (List<Instruments>) instrumentRepository.findAll();
        return convertToDTO(instruments);
    }

    public List<InstrumentDTO> findInstrumentsBySupplierName(String supplierName) {
        Optional<Suppliers> supplierMaybe = supplierRepository.findBySupplierName(supplierName);
        if (!supplierMaybe.isPresent()) {
            return null;
        }
        Suppliers supplier = supplierMaybe.get();
        List<Instruments> instruments = instrumentRepository.findBySupplierId(supplier.getId()).orElse(null);
        return convertToDTO(instruments);
    }

    public List<InstrumentDTO> findInstrumentsBySupplierId(Integer supplierId) {
        Optional<Suppliers> supplierMaybe = supplierRepository.findById(supplierId);
        if (!supplierMaybe.isPresent()) {
            return null;
        }
        Suppliers supplier = supplierMaybe.get();
        List<Instruments> instruments = instrumentRepository.findBySupplierId(supplier.getId()).orElse(null);
        return convertToDTO(instruments);
    }

    private List<InstrumentDTO> convertToDTO(List<Instruments> instruments) {
        List<InstrumentDTO> instrumentDTOs = new ArrayList<>();
        for (Instruments instrument : instruments) {
            InstrumentDTO dto = new InstrumentDTO(
                instrument.getId(),
                instrument.getSupplier().getSupplierName(),
                instrument.getCategory().getId(),
                instrument.getReference(),
                instrument.getSupplierDescription(),
                instrument.getPrice(),
                instrument.getObsolete(),
                !alternativesRepository.findByInstrumentsId1(instrument.getId()).isEmpty(),
                null
            );
            instrumentDTOs.add(dto);
        }
        return instrumentDTOs;
    }

    public Instruments convertToEntity(InstrumentDTO instrumentDTO) {
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

        return instrument;
    }

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

            InstrumentDTO instrumentDTO = new InstrumentDTO(instrument.getId(), supplierName, category.getId(), reference, supplierDescription, price, obsolete, alt, pictures);

            instrumentsDTO.add(instrumentDTO);
        }
        return instrumentsDTO;
    }


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
            savedInstrument.getId(),
            savedInstrument.getSupplier().getSupplierName(),
            savedInstrument.getCategory().getId(),
            savedInstrument.getReference(),
            savedInstrument.getSupplierDescription(),
            savedInstrument.getPrice(),
            savedInstrument.getObsolete(),
            !alternativesRepository.findByInstrumentsId1(savedInstrument.getId()).isEmpty(),
            null
        );
    }

    public void delete(Integer id) {
        instrumentRepository.deleteById(id);
    }

    public List<InstrumentDTO> searchInstrument(List<String> keywords) {
        List<Instruments> instruments = instrumentRepository.searchByKeywords(keywords);
        return convertToDTO(instruments);
    }
}

