package be.uliege.speam.team03.MDTools.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import be.uliege.speam.team03.MDTools.DTOs.InstrumentDTO;
import be.uliege.speam.team03.MDTools.models.Category;
import be.uliege.speam.team03.MDTools.models.Instruments;
import be.uliege.speam.team03.MDTools.models.Supplier;
import be.uliege.speam.team03.MDTools.repositories.AlternativesRepository;
import be.uliege.speam.team03.MDTools.repositories.CategoryRepository;
import be.uliege.speam.team03.MDTools.repositories.SupplierRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Component
public class InstrumentMapper {
    private final SupplierRepository supplierRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Convert an instrument to a DTO.
     * 
     * @param instrument the instrument to convert
     * @return the converted instrument DTO
     */
    public InstrumentDTO convertToDTO(Instruments instrument) {
        InstrumentDTO dto = new InstrumentDTO(
            instrument.getSupplier().getSupplierName(),
            instrument.getCategory().getId(),
            instrument.getReference(),
            instrument.getSupplierDescription(),
            instrument.getPrice(),
            instrument.getObsolete(),
            null,
            instrument.getId()
        );
        return dto;
    }
    /**
     * Convert a list of instruments to DTOs.
     * 
     * @param instruments the list of instruments to convert
     * @return the converted list of instrument DTOs
     */
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
                null,
                instrument.getId()
            );
            instrumentDTOs.add(dto);
        }
        return instrumentDTOs;
    }

    /**
     * Convert an instrument DTO to an entity.
     * 
     * @param instrumentDTO the instrument DTO to convert
     * @return the converted instrument entity
     * @throws IllegalArgumentException if the supplier name is null or empty
     */
    public Instruments convertToEntity(InstrumentDTO instrumentDTO) {
        Instruments instrument = new Instruments();
        instrument.setReference(instrumentDTO.getReference());
        instrument.setId(instrumentDTO.getId());
        instrument.setSupplierDescription(instrumentDTO.getSupplierDescription());
        instrument.setPrice(instrumentDTO.getPrice());
        instrument.setObsolete(instrumentDTO.getObsolete());

        // retrieve supplier based on supplier name
        Optional<Supplier> supplierMaybe = supplierRepository.findBySupplierName(instrumentDTO.getSupplier());
        if (supplierMaybe.isPresent() == false) {
            return null;
        }
        Supplier supplier = supplierMaybe.get();
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
