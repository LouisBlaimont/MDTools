package be.uliege.speam.team03.MDTools.DTOs;

import java.util.List;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class InstrumentDTO {
    private Integer id;
    private String supplier;
    private Integer categoryId;
    private String reference;
    private String supplierDescription;
    private Float price;
    private boolean alt;
    private boolean obsolete;
    private List<Long> picturesId;


    public String getSupplierName() {
        return supplier;
    }

    public void setSupplierName(String supplier) {
        this.supplier = supplier;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getSupplierDescription() {
        return supplierDescription;
    }

    public void setSupplierDescription(String supplierDescription) {
        this.supplierDescription = supplierDescription;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public boolean isAlt() {
        return alt;
    }

    public void setAlt(boolean alt) {
        this.alt = alt;
    }
}
