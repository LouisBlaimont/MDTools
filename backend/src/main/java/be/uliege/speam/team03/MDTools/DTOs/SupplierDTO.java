package be.uliege.speam.team03.MDTools.DTOs;

public class SupplierDTO {
    private String name;
    private Integer id;
    private Boolean soldByMd;
    private Boolean closed;

    public SupplierDTO(String name, Integer id, Boolean soldByMD, Boolean closed) {
        this.id = id;
        this.name = name;
        this.soldByMd = soldByMD != null ? soldByMD : true;
        this.closed = closed;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean isSoldByMD() {
        return soldByMd;
    }

    public void setSoldByMD(Boolean soldByMD) {
        this.soldByMd = soldByMD;
    }

    public void setClosed(Boolean closed) {
        this.closed = closed;
    }

    public Boolean isClosed() {
        return closed;
    }
}
