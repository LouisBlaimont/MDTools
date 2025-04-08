package be.uliege.speam.team03.MDTools.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import be.uliege.speam.team03.MDTools.DTOs.InstrumentDTO;
import be.uliege.speam.team03.MDTools.models.Category;
import be.uliege.speam.team03.MDTools.models.Instruments;
import be.uliege.speam.team03.MDTools.models.PictureType;
import be.uliege.speam.team03.MDTools.models.Supplier;
import be.uliege.speam.team03.MDTools.repositories.CategoryRepository;
import be.uliege.speam.team03.MDTools.repositories.SupplierRepository;
import be.uliege.speam.team03.MDTools.services.PictureStorageService;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Component
public class InstrumentMapper {
    private final SupplierRepository supplierRepository;
    private final CategoryRepository categoryRepository;
    private final PictureStorageService pictureStorageService;

    /**
     * Convert an instrument to a DTO.
     * 
     * @param instrument the instrument to convert
     * @return the converted instrument DTO
     */
    public InstrumentDTO convertToDTO(Instruments instrument) {
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
        instrument.setSupplier(null);

        // retrieve supplier based on supplier name
        Optional<Supplier> supplierMaybe = supplierRepository.findBySupplierName(instrumentDTO.getSupplier());
        if (supplierMaybe.isPresent() == false) {
            return null;
        }
        Supplier supplier = supplierMaybe.get();
        instrument.setSupplier(supplier);

        // retrieve category based on category id
        Optional<Category> categoryMaybe = categoryRepository.findById(instrumentDTO.getCategoryId());
        if (categoryMaybe.isEmpty()) {
            return null;
        }
        Category category = categoryMaybe.get();
        instrument.setCategory(category);

        return instrument;
    }
}
