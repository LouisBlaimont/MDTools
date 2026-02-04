package be.uliege.speam.projections;

public interface CategoryFlatProjection {
    Long getId();
    String getGroupName();
    String getSubGroupName();

    String getExternalCode();
    String getFunction();
    String getAuthor();
    String getName();
    String getDesign();

    String getShape();

    String getDimOrig();
    String getLenAbrv();
}
