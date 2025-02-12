package be.uliege.speam.team03.MDTools.DTOs;

public class InstrumentDTO {
    private String supplier;
    private String reference;
    private String supplierDescription;
    private Float price;
    private boolean alt;
    private boolean obsolete;


    public InstrumentDTO(String reference, String supplier, String supplierDescription, Float price, boolean alt, boolean obsolete) {
        this.reference = reference;
        this.supplier = supplier;
        this.supplierDescription = supplierDescription;
        this.price = price;
        this.alt = alt;
        this.obsolete = obsolete;
    }
}