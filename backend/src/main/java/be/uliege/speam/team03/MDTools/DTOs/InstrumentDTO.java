package be.uliege.speam.team03.MDTools.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;
import java.util.List;

@Getter
@Setter
public class InstrumentDTO {
    private String supplier;
    private Integer categoryId;
    private String reference;
    private String supplierDescription;
    private Float price;
    private boolean alt;
    private boolean obsolete;
    private List<Long> picturesId;

    //@JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer id;

    public InstrumentDTO(String supplier, Integer categoryId, String reference, String supplierDescription, Float price, boolean alt, boolean obsolete, List<Long> picturesId, Integer id) {
        this.supplier = supplier;
        this.categoryId = categoryId;
        this.reference = reference;
        this.supplierDescription = supplierDescription;
        this.price = price;
        this.alt = alt;
        this.obsolete = obsolete;
        this.picturesId = picturesId;
        this.id = id;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
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

    public boolean isObsolete() {
        return obsolete;
    }

    public void setObsolete(boolean obsolete) {
        this.obsolete = obsolete;
    }

    public List<Long> getPicturesId() {
        return picturesId;
    }

    public void setPicturesId(List<Long> picturesId) {
        this.picturesId = picturesId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

}
