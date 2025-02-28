package be.uliege.speam.team03.MDTools.DTOs;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * DTO representing the import request received from the frontend.
 */
@Getter
@Setter
public class ImportRequestDTO {
    private String importType;
    private String groupName;
    private String subGroupName;
    private String supplier; // Added supplier field for catalog imports
    private List<Map<String, Object>> data;
}
