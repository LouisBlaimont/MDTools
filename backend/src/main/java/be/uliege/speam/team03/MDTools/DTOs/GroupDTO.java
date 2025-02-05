package be.uliege.speam.team03.MDTools.DTOs;

import java.util.List;

public class GroupDTO {
    private String name;
    private List<SubGroupDTO> subGroups;
    private List<String> characteristics;
    private int nbInstr;

    public GroupDTO(String name, List<SubGroupDTO> subgroups, int nbInstr) {
        this.name = name;
        this.characteristics = characteristics;
        this.nbInstr = nbInstr;
        this.subGroups = subgroups;
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

    public int getNbInstr(){
        return nbInstr;
    }
    public void setNbInstr(int instr){
        this.nbInstr = instr;
    }

    public List<SubGroupDTO> getSubGroups(){
        return subGroups;
    }
    public void setSubGroups(List<SubGroupDTO> subgroups){
        this.subGroups = subgroups;
    }
}