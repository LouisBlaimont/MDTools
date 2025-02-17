package be.uliege.speam.team03.MDTools.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import be.uliege.speam.team03.MDTools.DTOs.InstrumentDTO;
import be.uliege.speam.team03.MDTools.models.Alternatives;
import be.uliege.speam.team03.MDTools.models.Category;
import be.uliege.speam.team03.MDTools.models.Instruments;
import be.uliege.speam.team03.MDTools.models.Suppliers;
import be.uliege.speam.team03.MDTools.repositories.AlternativesRepository;
import be.uliege.speam.team03.MDTools.repositories.CategoryRepository;
import be.uliege.speam.team03.MDTools.repositories.InstrumentRepository;
import be.uliege.speam.team03.MDTools.repositories.SupplierRepository;

@Service
public class InstrumentService {
    private final InstrumentRepository instrumentRepository;
    private final SupplierRepository supplierRepository;
    private final CategoryRepository categoryRepository;
    private final AlternativesRepository alternativesRepository;

    public InstrumentService(CategoryRepository categoryRepo, InstrumentRepository instrumentRepo, AlternativesRepository alternativesRepo, SupplierRepository supplierRepo) {
        this.categoryRepository = categoryRepo;
        this.instrumentRepository = instrumentRepo;
        this.alternativesRepository = alternativesRepo;
        this.supplierRepository = supplierRepo;
    }

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
            instrument.isObsolete(),
            !alternativesRepository.findByInstrumentsId1(instrument.getId()).isEmpty()
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
            instrument.getSupplier().getSupplierName(),
            instrument.getCategory().getId(),
            instrument.getReference(),
            instrument.getSupplierDescription(),
            instrument.getPrice(),
            instrument.isObsolete(),
            !alternativesRepository.findByInstrumentsId1(instrument.getId()).isEmpty()
        );
    }

    public List<InstrumentDTO> findAll() {
        List<Instruments> instruments = (List<Instruments>) instrumentRepository.findAll();
        return convertToDTO(instruments);
    }

    public List<InstrumentDTO> findInstrumentsBySupplierName(String supplierName) {
        Optional<Suppliers> supplierMaybe = supplierRepository.findByName(supplierName);
        if (!supplierMaybe.isPresent()) {
            return null;
        }
        Suppliers supplier = supplierMaybe.get();
        List<Instruments> instruments = instrumentRepository.findBySupplierId(supplier.getSupplierId()).orElse(null);
        return convertToDTO(instruments);
    }

    public List<InstrumentDTO> findInstrumentsBySupplierId(Integer supplierId) {
        Optional<Suppliers> supplierMaybe = supplierRepository.findById(supplierId);
        if (!supplierMaybe.isPresent()) {
            return null;
        }
        Suppliers supplier = supplierMaybe.get();
        List<Instruments> instruments = instrumentRepository.findBySupplierId(supplier.getSupplierId()).orElse(null);
        return convertToDTO(instruments);
    }

    private List<InstrumentDTO> convertToDTO(List<Instruments> instruments) {
        List<InstrumentDTO> instrumentDTOs = new ArrayList<>();
        for (Instruments instrument : instruments) {
            InstrumentDTO dto = new InstrumentDTO(
                instrument.getSupplier().getSupplierName(),
                instrument.getCategory().getId(),
                instrument.getReference(),
                instrument.getSupplierDescription(),
                instrument.getPrice(),
                instrument.isObsolete(),
                !alternativesRepository.findByInstrumentsId1(instrument.getId()).isEmpty()
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
        Optional<Suppliers> supplierMaybe = supplierRepository.findByName(instrumentDTO.getSupplier());
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
            boolean obsolete = instrument.isObsolete();

            // retrieve alternatives? 
            Integer instrumentId = instrument.getId();
            List<Alternatives> alternatives = alternativesRepository.findByInstrumentsId1(instrumentId);
            boolean alt = !alternatives.isEmpty();            

            InstrumentDTO instrumentDTO = new InstrumentDTO(supplierName, category.getId(), reference, supplierDescription, price, obsolete, alt);

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
        Optional<Suppliers> supplierMaybe = supplierRepository.findByName(instrumentDTO.getSupplierName());
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
            savedInstrument.isObsolete(),
            !alternativesRepository.findByInstrumentsId1(savedInstrument.getId()).isEmpty()
        );
    }

    public void delete(Integer id) {
        instrumentRepository.deleteById(id);
    }
}
