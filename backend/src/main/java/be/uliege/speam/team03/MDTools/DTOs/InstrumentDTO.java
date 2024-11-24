package be.uliege.speam.team03.MDTools.DTOs;

public class InstrumentDTO {

    private Integer instrumentId;
    private String supplierName;
    private String categoryShape;
    private String reference;
    private String supplierDescription;
    private Float price;
    private Boolean obsolete;

    public InstrumentDTO(Integer instrumentId, String supplierName, String categoryShape, String reference, String supplierDescription, Float price, Boolean obsolete) {
        this.instrumentId = instrumentId;
        this.supplierName = supplierName;
        this.categoryShape = categoryShape;
        this.reference = reference;
        this.supplierDescription = supplierDescription;
        this.price = price;
        this.obsolete = obsolete;
    }

    public Integer getInstrumentId() {
        return instrumentId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public String getCategoryShape() {
        return categoryShape;
    }

    public String getReference() {
        return reference;
    }

    public String getSupplierDescription() {
        return supplierDescription;
    }

    public Float getPrice() {
        return price;
    }

    public Boolean isObsolete() {
        return obsolete;
    }
}
