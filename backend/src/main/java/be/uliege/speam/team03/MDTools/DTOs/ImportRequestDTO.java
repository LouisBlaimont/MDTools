package be.uliege.speam.team03.MDTools.DTOs;

import java.util.List;
import java.util.Map;

/**
 * DTO representing the import request received from the frontend.
 */
public class ImportRequestDTO {
    private String importType;
    private String groupName;
    private String subGroupName;
    private List<Map<String, Object>> data;

    public String getImportType() {
        return importType;
    }

    public void setImportType(String importType) {
        this.importType = importType;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getSubGroupName() {
        return subGroupName;
    }

    public void setSubGroupName(String subGroupName) {
        this.subGroupName = subGroupName;
    }

    public List<Map<String, Object>> getData() {
        return data;
    }

    public void setData(List<Map<String, Object>> data) {
        this.data = data;
    }
}
