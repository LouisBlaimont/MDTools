package be.uliege.speam.team03.MDTools.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class InstrumentDTO {
    private String supplier;
    private Integer categoryId;
    private String reference;
    private String supplierDescription;
    private Float price;
    private Boolean alt;
    private Boolean obsolete;
    private List<Long> picturesId;

    private Integer id;

    /**
     * Get the supplier of the instrument.
     * 
     * @return the supplier of the instrument
     */
    public String getSupplier() {
        return supplier;
    }

    /**
     * Set the supplier of the instrument.
     * 
     * @param supplier the supplier of the instrument
     */
    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    /**
     * Get the category ID of the instrument.
     * 
     * @return the category ID of the instrument
     */
    public Integer getCategoryId() {
        return categoryId;
    }

    /**
     * Set the category ID of the instrument.
     * 
     * @param categoryId the category ID of the instrument
     */
    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    /**
     * Get the reference of the instrument.
     * 
     * @return the reference of the instrument
     */
    public String getReference() {
        return reference;
    }

    /**
     * Set the reference of the instrument.
     * 
     * @param reference the reference of the instrument
     */
    public void setReference(String reference) {
        this.reference = reference;
    }

    /**
     * Get the supplier description of the instrument.
     * 
     * @return the supplier description of the instrument
     */
    public String getSupplierDescription() {
        return supplierDescription;
    }

    /**
     * Set the supplier description of the instrument.
     * 
     * @param supplierDescription the supplier description of the instrument
     */
    public void setSupplierDescription(String supplierDescription) {
        this.supplierDescription = supplierDescription;
    }

    /**
     * Get the price of the instrument.
     * 
     * @return the price of the instrument
     */
    public Float getPrice() {
        return price;
    }

    /**
     * Set the price of the instrument.
     * 
     * @param price the price of the instrument
     */
    public void setPrice(Float price) {
        this.price = price;
    }

    /**
     * Get the alternative status of the instrument.
     * 
     * @return the alternative status of the instrument
     */
    public boolean isAlt() {
        return alt;
    }

    /**
     * Set the alternative status of the instrument.
     * 
     * @param alt the alternative status of the instrument
     */
    public void setAlt(boolean alt) {
        this.alt = alt;
    }

    /**
     * Get the obsolete status of the instrument.
     * 
     * @return the obsolete status of the instrument
     */
    public boolean isObsolete() {
        return obsolete;
    }

    /**
     * Set the obsolete status of the instrument.
     * 
     * @param obsolete the obsolete status of the instrument
     */
    public void setObsolete(boolean obsolete) {
        this.obsolete = obsolete;
    }

    /**
     * Get the pictures ID of the instrument.
     * 
     * @return the pictures ID of the instrument
     */
    public List<Long> getPicturesId() {
        return picturesId;
    }

    /**
     * Set the pictures ID of the instrument.
     * 
     * @param picturesId the pictures ID of the instrument
     */
    public void setPicturesId(List<Long> picturesId) {
        this.picturesId = picturesId;
    }

    /**
     * Get the ID of the instrument.
     * 
     * @return the ID of the instrument
     */
    public Integer getId() {
        return id;
    }

    /**
     * Set the ID of the instrument.
     * 
     * @param id the ID of the instrument
     */
    public void setId(Integer id) {
        this.id = id;
    }

}
