package be.uliege.speam.team03.MDTools.mapper;

import java.util.*;

import org.springframework.stereotype.Component;

import be.uliege.speam.team03.MDTools.DTOs.SupplierDTO;
import be.uliege.speam.team03.MDTools.models.Supplier;

@Component
public class SupplierMapper {

    /**
     * Convert a Suppliers entity to a SupplierDTO.
     * 
     * @param supplier the Suppliers entity to convert
     * @return the converted SupplierDTO
     */
    public SupplierDTO convertToDTO(Supplier supplier) {
        return new SupplierDTO(
            supplier.getSupplierName(),
            supplier.getId(),
            supplier.getSoldByMd(),
            supplier.getClosed()
        );
    }

    /**
     * Convert a SupplierDTO to a Suppliers entity.
     * 
     * @param supplierDTO the SupplierDTO to convert
     * @return the converted Suppliers entity
     */
    public Supplier convertToEntity(SupplierDTO supplierDTO) {
        Supplier supplier = new Supplier();
        supplier.setSupplierName(supplierDTO.getName());
        supplier.setId(supplierDTO.getId());
        supplier.setSoldByMd(supplierDTO.isSoldByMd());
        supplier.setClosed(supplierDTO.isClosed());
        return supplier;
    }

    /**
     * Convert a list of Suppliers entities to a list of SupplierDTOs.
     * 
     * @param suppliers the list of Suppliers entities to convert
     * @return the converted list of SupplierDTOs
     */
    public List<SupplierDTO> convertToDTOList(List<Supplier> suppliers) {
        List<SupplierDTO> supplierDTOs = new ArrayList<>();
        for (Supplier supplier : suppliers) {
            supplierDTOs.add(convertToDTO(supplier));
        }
        return supplierDTOs;
    }
}
