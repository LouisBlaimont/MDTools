package be.uliege.speam.team03.MDTools.DTOs;

public class SubGroupSummaryDTO {
    private String name;
    private int nbInstr;

    public SubGroupSummaryDTO(String name, int nbInstr) {
        this.name = name;
        this.nbInstr = nbInstr;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNbInstr(){
        return nbInstr;
    }
    public void setNbInstr(int instr){
        this.nbInstr = instr;
    }
}