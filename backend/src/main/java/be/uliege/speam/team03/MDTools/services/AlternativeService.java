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
import be.uliege.speam.team03.MDTools.exception.BadRequestException;
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
    private PictureStorageService pictureStorageService;

    public AlternativeService(AlternativesRepository alternativeRepo, SupplierRepository supplierRepo, CategoryRepository categoryRepo, InstrumentRepository instrRepo, PictureStorageService pictureStorageService){
        this.alternativesRepository = alternativeRepo;
        this.supplierRepository = supplierRepo;
        this.categoryRepository = categoryRepo;
        this.instrumentRepository = instrRepo;
        this.pictureStorageService = pictureStorageService;
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
        InstrumentMapper mapper = new InstrumentMapper(supplierRepository, categoryRepository, pictureStorageService);
        List<InstrumentDTO> alternativesDTO = mapper.convertToDTO(alternatives);
        return alternativesDTO;
    }

    public List<InstrumentDTO> findAlternativesAdmin(Integer instrId){
        List<Instruments> alternatives = findAlternatives(instrId);
        InstrumentMapper mapper = new InstrumentMapper(supplierRepository, categoryRepository, pictureStorageService);
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
        InstrumentMapper mapper = new InstrumentMapper(supplierRepository, categoryRepository, pictureStorageService);
        List<InstrumentDTO> alternativesDTO = mapper.convertToDTO(alternatives);
        return alternativesDTO;    
    }

    public List<InstrumentDTO> findAlternativesOfCategoryAdmin(Integer categoryId){
        List<Instruments> alternatives = findAlternativesOfCategory(categoryId);
        InstrumentMapper mapper = new InstrumentMapper(supplierRepository, categoryRepository, pictureStorageService);
        List<InstrumentDTO> alternativesDTO = mapper.convertToDTO(alternatives);
        return alternativesDTO;    
    }  

    public List<AlternativeReferenceDTO> findAllAlternativesReferences() {
        return alternativesRepository.findAllAlternativesReferences();
    } 

    public Boolean removeAlternativeFromCategory(Integer categoryId, Integer instrId){
        Optional<Category> categoryMaybe = categoryRepository.findById(categoryId);
        if (categoryMaybe.isPresent() == false) {
            return null;
        }
        Category category = categoryMaybe.get();
        Optional<List<Instruments>> instrumentsMaybe = instrumentRepository.findByCategory(category);
        if (instrumentsMaybe.isPresent() == false) {
            return null;
        }
        List<Instruments> instrumentsOfCat = instrumentsMaybe.get();

        Boolean isRemoved = false;

        for (Instruments instrument : instrumentsOfCat){
            List<Alternatives> alternatives1 = alternativesRepository.findById_InstrId1(instrument.getId());
            List<Alternatives> alternatives2 = alternativesRepository.findById_InstrId2(instrument.getId());
            for (Alternatives alt : alternatives1){
                Instruments instr2 = alt.getInstr2();
                if(instr2.getId() == instrId){
                    alternativesRepository.delete(alt);
                    isRemoved = true;
                }
            }
            for (Alternatives alt : alternatives2){
                Instruments instr1 = alt.getInstr1();
                if(instr1.getId() == instrId){
                    alternativesRepository.delete(alt);
                    isRemoved = true;
                }
            }  
        }
        return isRemoved;
    }

    public List<InstrumentDTO> addAlternative(Integer instrId1, Integer instrId2){
        Optional<Instruments> instr1Maybe = instrumentRepository.findById(instrId1);
        Optional<Instruments> instr2Maybe = instrumentRepository.findById(instrId2);

        if(instr1Maybe.isEmpty() || instr2Maybe.isEmpty()){
            throw new ResourceNotFoundException("Instrument(s) not found.");
        }

        Instruments instr1 = instr1Maybe.get();
        Instruments instr2 = instr2Maybe.get();
        Integer supplierId1 = instr1.getSupplier().getId();
        Integer supplierId2 = instr2.getSupplier().getId();
        if (supplierId1 == supplierId2){
            throw new BadRequestException("Alternatives can't have the same supplier");
        }

        Optional<Alternatives> existingAlternative = alternativesRepository.findByInstr1AndInstr2(instr1, instr2);
        if (existingAlternative.isPresent()){
            throw new BadRequestException("This alternative already exists.");
        }

        Alternatives newAlternative = new Alternatives(instr1, instr2);
        alternativesRepository.save(newAlternative);

        List<InstrumentDTO> instrumentsOfCatInstr1 = findAlternativesOfCategoryAdmin(instr1.getCategory().getId());
        return instrumentsOfCatInstr1;
    }

    public List<InstrumentDTO> removeAlternative(Integer instrId1, Integer instrId2){
        Optional<Instruments> instr1Maybe = instrumentRepository.findById(instrId1);
        Optional<Instruments> instr2Maybe = instrumentRepository.findById(instrId2);

        if(instr1Maybe.isEmpty() || instr2Maybe.isEmpty()){
            throw new ResourceNotFoundException("Instrument(s) not found.");
        }

        Instruments instr1 = instr1Maybe.get();
        Instruments instr2 = instr2Maybe.get();
        Optional<Alternatives> alternativeMaybe = alternativesRepository.findByInstr1AndInstr2(instr1, instr2);
        if (alternativeMaybe.isEmpty()){
            Optional<Alternatives> alternativeOtherSideMaybe = alternativesRepository.findByInstr1AndInstr2(instr2, instr1);
            if( alternativeOtherSideMaybe.isEmpty()){
                throw new BadRequestException("This alternative doesn't exist.");
            }
            else{
                alternativeMaybe = alternativeOtherSideMaybe;
            }
        }
        Alternatives alternative = alternativeMaybe.get();
        alternativesRepository.delete(alternative);

        List<InstrumentDTO> instrumentsOfCatInstr1 = findAlternativesOfCategoryAdmin(instr1.getCategory().getId());
        return instrumentsOfCatInstr1;
    }
}
