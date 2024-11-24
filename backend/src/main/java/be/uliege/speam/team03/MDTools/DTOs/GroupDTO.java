package be.uliege.speam.team03.MDTools.DTOs;

import java.util.List;

public class GroupDTO {
    private String name;
    private List<String> characteristics;
    private int nbInstr;

    public GroupDTO(String name, List<String> characteristics, int nbInstr) {
        this.name = name;
        this.characteristics = characteristics;
        this.nbInstr = nbInstr;
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
}