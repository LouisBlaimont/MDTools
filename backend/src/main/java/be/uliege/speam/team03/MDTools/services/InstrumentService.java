package be.uliege.speam.team03.MDTools.services;

import java.util.*;

import org.springframework.stereotype.Service;

import be.uliege.speam.team03.MDTools.DTOs.InstrumentDTO;
import be.uliege.speam.team03.MDTools.models.*;
import be.uliege.speam.team03.MDTools.repositories.*;

@Service
public class InstrumentService {
    private CategoryRepository categoryRepository;
    private InstrumentRepository instrumentRepository;
    private AlternativesRepository alternativesRepository;

    public InstrumentService(CategoryRepository categoryRepo, InstrumentRepository instrumentRepo, AlternativesRepository alternativesRepo) {
        this.categoryRepository = categoryRepo;
        this.instrumentRepository = instrumentRepo;
        this.alternativesRepository = alternativesRepo;
    }

    public List<InstrumentDTO> findInstrumentsOfCatergory(Integer categoryId) {

        // retrieve category based on categoryId
        Optional<Category> categoryMaybe = categoryRepository.findByCategoryId(categoryId);
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
            List<Alternatives> alternativesMaybe = alternativesRepository.findByInstrumentId(instrumentId);
            boolean alt = false;
            if (alternativesMaybe.isPresent() == false) {
                return null;
            }
            List<Alternatives> alternative = alternativesMaybe.get();
            if (alternative.isEmpty() == false) {
                alt = true;
            }

            // add to list
            InstrumentDTO instrumentDTO = new InstrumentDTO(reference, supplierName, supplierDescription, price, alt, obsolete);
            instrumentsDTO.add(instrumentDTO);
        }
        return instrumentsDTO;
    }
}