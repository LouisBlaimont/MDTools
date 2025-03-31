package be.uliege.speam.team03.MDTools.services;

import be.uliege.speam.team03.MDTools.repositories.AlternativesRepository;
import be.uliege.speam.team03.MDTools.repositories.CategoryRepository;
import be.uliege.speam.team03.MDTools.repositories.InstrumentRepository;
import be.uliege.speam.team03.MDTools.repositories.SupplierRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

import be.uliege.speam.team03.MDTools.DTOs.AlternativeReferenceDTO;
import be.uliege.speam.team03.MDTools.DTOs.InstrumentDTO;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.mapper.InstrumentMapper;
import be.uliege.speam.team03.MDTools.models.Alternatives;
import be.uliege.speam.team03.MDTools.models.Category;
import be.uliege.speam.team03.MDTools.models.Instruments;

@Service
public class AlternativeService {
    private AlternativesRepository alternativesRepository;
    private SupplierRepository supplierRepository;
    private CategoryRepository categoryRepository;
    private InstrumentRepository instrumentRepository;

    public AlternativeService(AlternativesRepository alternativeRepo, SupplierRepository supplierRepo, CategoryRepository categoryRepo, InstrumentRepository instrRepo){
        this.alternativesRepository = alternativeRepo;
        this.supplierRepository = supplierRepo;
        this.categoryRepository = categoryRepo;
        this.instrumentRepository = instrRepo;
    }

    public List<Instruments> findAlternatives(Integer instrId){
        Optional<Instruments> instrumentMaybe = instrumentRepository.findById(instrId);

        if(instrumentMaybe.isEmpty()){
            throw new ResourceNotFoundException("Instrument not found.");
        }

        List<Alternatives> alternatives1 = alternativesRepository.findById_InstrId1(instrId);
        List<Alternatives> alternatives2 = alternativesRepository.findById_InstrId2(instrId);

        List<Instruments> alternatives = new ArrayList<>();

        for (Alternatives alt : alternatives1){
            alternatives.add(alt.getInstr2());
        }

        for (Alternatives alt : alternatives2){
            alternatives.add(alt.getInstr1());
        }

        alternatives.sort(Comparator.comparingInt(instr -> {
            String supplierName = (instr.getSupplier() != null) ? instr.getSupplier().getSupplierName() : "";
            if (supplierName.equals("Medicon") || supplierName.equals("Maganovum")){
                return 0;
            }
            return 1;
        }));

        return alternatives;
    }

    public List<InstrumentDTO> findAlternativesUser(Integer instrId){
        List<Instruments> alternatives = findAlternatives(instrId);
        alternatives.removeIf(instr -> !instr.getSupplier().getSoldByMd());
        InstrumentMapper mapper = new InstrumentMapper(supplierRepository, categoryRepository);
        List<InstrumentDTO> alternativesDTO = mapper.convertToDTO(alternatives);
        return alternativesDTO;
    }

    public List<InstrumentDTO> findAlternativesAdmin(Integer instrId){
        List<Instruments> alternatives = findAlternatives(instrId);
        InstrumentMapper mapper = new InstrumentMapper(supplierRepository, categoryRepository);
        List<InstrumentDTO> alternativesDTO = mapper.convertToDTO(alternatives);
        return alternativesDTO;
    }

    public List<Instruments> findAlternativesOfCategory(Integer categoryId){
        Optional<Category> categoryMaybe = categoryRepository.findById(categoryId);
        if (categoryMaybe.isPresent() == false) {
            return null;
        }
        Category category = categoryMaybe.get();
        Optional<List<Instruments>> instrumentsMaybe = instrumentRepository.findByCategory(category);
        if (instrumentsMaybe.isPresent() == false) {
            return null;
        }
        List<Instruments> instruments = instrumentsMaybe.get();

        List<Instruments> alternativesOfCategory = new ArrayList<>();
        Set<Integer> seenInstrumentIds = new HashSet<>();
        
        for (Instruments instrument : instruments){
            List<Alternatives> alternatives1 = alternativesRepository.findById_InstrId1(instrument.getId());
            List<Alternatives> alternatives2 = alternativesRepository.findById_InstrId2(instrument.getId());
            for (Alternatives alt : alternatives1){
                Instruments instr2 = alt.getInstr2();
                if(!seenInstrumentIds.contains(instr2.getId())){
                    alternativesOfCategory.add(instr2);
                    seenInstrumentIds.add(instr2.getId());
                }
            }
            for (Alternatives alt : alternatives2){
                Instruments instr1 = alt.getInstr1();
                if(!seenInstrumentIds.contains(instr1.getId())){
                    alternativesOfCategory.add(instr1);
                    seenInstrumentIds.add(instr1.getId());
                }
            }  
        }

        alternativesOfCategory.sort(Comparator.comparingInt(instr -> {
            String supplierName = (instr.getSupplier() != null) ? instr.getSupplier().getSupplierName() : "";
            if (supplierName.equals("Medicon") || supplierName.equals("Maganovum")){
                return 0;
            }
            return 1;
        }));

        return alternativesOfCategory;
    }

    public List<InstrumentDTO> findAlternativesOfCategoryUser(Integer categoryId){
        List<Instruments> alternatives = findAlternativesOfCategory(categoryId);
        alternatives.removeIf(instr -> !instr.getSupplier().getSoldByMd());
        InstrumentMapper mapper = new InstrumentMapper(supplierRepository, categoryRepository);
        List<InstrumentDTO> alternativesDTO = mapper.convertToDTO(alternatives);
        return alternativesDTO;    
    }

    public List<InstrumentDTO> findAlternativesOfCategoryAdmin(Integer categoryId){
        List<Instruments> alternatives = findAlternativesOfCategory(categoryId);
        InstrumentMapper mapper = new InstrumentMapper(supplierRepository, categoryRepository);
        List<InstrumentDTO> alternativesDTO = mapper.convertToDTO(alternatives);
        return alternativesDTO;    
    }  

    public List<AlternativeReferenceDTO> findAllAlternativesReferences() {
        return alternativesRepository.findAllAlternativesReferences();
    }
}
