package be.uliege.speam.team03.MDTools.mapper;

import org.springframework.stereotype.Component;

import be.uliege.speam.team03.MDTools.DTOs.SupplierDTO;
import be.uliege.speam.team03.MDTools.models.Suppliers;
import be.uliege.speam.team03.MDTools.repositories.SupplierRepository;

@Component
public class SupplierMapper {

    private SupplierMapper(SupplierRepository supplierRepository){}

    /**
     * Convert a Suppliers entity to a SupplierDTO.
     * 
     * @param supplier the Suppliers entity to convert
     * @return the converted SupplierDTO
     */
    public SupplierDTO convertToDTO(Suppliers supplier) {
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
    public Suppliers convertToEntity(SupplierDTO supplierDTO) {
        Suppliers supplier = new Suppliers();
        supplier.setSupplierName(supplierDTO.getName());
        supplier.setId(supplierDTO.getId());
        supplier.setSoldByMd(supplierDTO.isSoldByMD() != null ? supplierDTO.isSoldByMD() : true);
        supplier.setClosed(supplierDTO.isClosed());
        return supplier;
    }
}
