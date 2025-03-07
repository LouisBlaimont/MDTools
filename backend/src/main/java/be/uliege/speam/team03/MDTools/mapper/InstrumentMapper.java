package be.uliege.speam.team03.MDTools.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import be.uliege.speam.team03.MDTools.DTOs.InstrumentDTO;
import be.uliege.speam.team03.MDTools.models.Category;
import be.uliege.speam.team03.MDTools.models.Instruments;
import be.uliege.speam.team03.MDTools.models.Suppliers;
import be.uliege.speam.team03.MDTools.repositories.AlternativesRepository;
import be.uliege.speam.team03.MDTools.repositories.CategoryRepository;
import be.uliege.speam.team03.MDTools.repositories.SupplierRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Component
public class InstrumentMapper {
    private final AlternativesRepository alternativesRepository;
    private final SupplierRepository supplierRepository;
    private final CategoryRepository categoryRepository;

    public List<InstrumentDTO> convertToDTO(List<Instruments> instruments) {
        List<InstrumentDTO> instrumentDTOs = new ArrayList<>();
        for (Instruments instrument : instruments) {
            InstrumentDTO dto = new InstrumentDTO(
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
}
