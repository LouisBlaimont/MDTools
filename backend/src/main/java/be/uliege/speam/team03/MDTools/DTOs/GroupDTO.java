package be.uliege.speam.team03.MDTools.DTOs;

import java.util.List;

public class GroupDTO {
    private String name;
    private List<String> characteristics;

    public GroupDTO(String name, List<String> characteristics) {
        this.name = name;
        this.characteristics = characteristics;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getCharacteristics() {
        return characteristics;
    }

    public void setCharacteristics(List<String> characteristics) {
        this.characteristics = characteristics;
    }
}