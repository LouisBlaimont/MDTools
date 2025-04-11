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

    /**
     * Retrieves the alternatives of an instrument identified by its ID. 
     * The alternatives are sorted such that the one sold my Medicon or Maganovum are displayed first.
     * @param instrId The id of the instrument for which the alternatives have to be retrieved
     * @return A list of the alternatives in the form of Instruments.
     */
    public List<Instruments> findAlternatives(Long instrId){
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

    /**
     * Retrieves the alternatives of an instrument. 
     * The alternatives available on display for users are the one from suppliers sold by MD Medical.
     * @param instrId The id of the instrument.
     * @return A list of alternatives in the form of InstrumentDTO
     */
    public List<InstrumentDTO> findAlternativesUser(Long instrId){
        List<Instruments> alternatives = findAlternatives(instrId);
        alternatives.removeIf(instr -> !instr.getSupplier().getSoldByMd());
        InstrumentMapper mapper = new InstrumentMapper(supplierRepository, categoryRepository, pictureStorageService);
        List<InstrumentDTO> alternativesDTO = mapper.convertToDTO(alternatives);
        return alternativesDTO;
    }

    /**
     * Retrieves the alternatives of an instrument. 
     * All alternatives are available on display for admins.
     * @param instrId The id of the instrument.
     * @return A list of alternatives in the form of InstrumentDTO
     */
    public List<InstrumentDTO> findAlternativesAdmin(Long instrId){
        List<Instruments> alternatives = findAlternatives(instrId);
        InstrumentMapper mapper = new InstrumentMapper(supplierRepository, categoryRepository, pictureStorageService);
        List<InstrumentDTO> alternativesDTO = mapper.convertToDTO(alternatives);
        return alternativesDTO;
    }


    /**
     * Retrieves the alternatives of every instrument belonging to a certain group. 
     * The alternatives are sorted such that the one sold my Medicon or Maganovum are displayed first.
     * @param categoryId The id of the category for which the alternatives have to be retrieved
     * @return A list of the alternatives in the form of Instruments.
     */
    public List<Instruments> findAlternativesOfCategory(Long categoryId){
        Optional<Category> categoryMaybe = categoryRepository.findById(categoryId);
        if (categoryMaybe.isEmpty()) {
            throw new ResourceNotFoundException("No category found for the provided id : " + categoryId);
        }
        Category category = categoryMaybe.get();
        Optional<List<Instruments>> instrumentsMaybe = instrumentRepository.findByCategory(category);
        if (instrumentsMaybe.isEmpty()) {
            return new ArrayList<>();
        }
        List<Instruments> instruments = instrumentsMaybe.get();

        List<Instruments> alternativesOfCategory = new ArrayList<>();
        Set<Long> seenInstrumentIds = new HashSet<>();
        
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

    /**
     * Retrieves the alternatives of every instrument belonging to a category. 
     * The alternatives available on display for users are the one from suppliers sold by MD Medical.
     * @param categoryId The id of the category.
     * @return A list of alternatives in the form of InstrumentDTO.
     */
    public List<InstrumentDTO> findAlternativesOfCategoryUser(Long categoryId){
        List<Instruments> alternatives = findAlternativesOfCategory(categoryId);
        alternatives.removeIf(instr -> !instr.getSupplier().getSoldByMd());
        InstrumentMapper mapper = new InstrumentMapper(supplierRepository, categoryRepository, pictureStorageService);
        List<InstrumentDTO> alternativesDTO = mapper.convertToDTO(alternatives);
        return alternativesDTO;    
    }

    /**
     * Retrieves the alternatives of every instrument belonging to a category. 
     * All alternatives are available on display for admins.
     * @param categoryId The id of the category.
     * @return A list of alternatives in the form of InstrumentDTO.
     */
    public List<InstrumentDTO> findAlternativesOfCategoryAdmin(Long categoryId){
        List<Instruments> alternatives = findAlternativesOfCategory(categoryId);
        InstrumentMapper mapper = new InstrumentMapper(supplierRepository, categoryRepository, pictureStorageService);
        List<InstrumentDTO> alternativesDTO = mapper.convertToDTO(alternatives);
        return alternativesDTO;    
    }  

    /**
     * Retrieves all alternatives.
     * @return A list of objects that contain the references form both instruments of the corresponding alternative.
     */
    public List<AlternativeReferenceDTO> findAllAlternativesReferences() {
        return alternativesRepository.findAllAlternativesReferences();
    } 

    /**
     * Removes an instrument identified by its ID as alternative for all instruments in a specific category. 
     * Every alternatives concerning this instrument and an instrument of the category is deleted.
     * @param categoryId The id of the category in which the instrument must be removed from the alternatives.
     * @param instrId The id of the instrument to remove as alternative.
     * @return A boolean set to true if the deletion worked
     */
    public Boolean removeAlternativeFromCategory(Long categoryId, Long instrId){
        Optional<Category> categoryMaybe = categoryRepository.findById(categoryId);
        Optional<Instruments> instrumentMaybe = instrumentRepository.findById(instrId);
        if (categoryMaybe.isEmpty() || instrumentMaybe.isEmpty()) {
            throw new ResourceNotFoundException("No category found for the provided id : "+ categoryId +" or no instrument found for the provided id : " + instrId);
        }

        Category category = categoryMaybe.get();
        Optional<List<Instruments>> instrumentsMaybe = instrumentRepository.findByCategory(category);
        if (instrumentsMaybe.isEmpty()) {
            return false;
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

    /**
     * Creates a new alternative between two instruments. 
     * @param instrId1 The id of the first instrument.
     * @param instrId2 The id of the second instrument.
     * @return A list of the updated alternatives available in the category of the first instrument. 
     */    
    public List<InstrumentDTO> addAlternative(Long instrId1, Long instrId2){
        Optional<Instruments> instr1Maybe = instrumentRepository.findById(instrId1);
        Optional<Instruments> instr2Maybe = instrumentRepository.findById(instrId2);

        if(instr1Maybe.isEmpty() || instr2Maybe.isEmpty()){
            throw new ResourceNotFoundException("Instrument(s) not found.");
        }

        Instruments instr1 = instr1Maybe.get();
        Instruments instr2 = instr2Maybe.get();
        Long supplierId1 = instr1.getSupplier().getId();
        Long supplierId2 = instr2.getSupplier().getId();
        if (supplierId1 == supplierId2){
            throw new BadRequestException("Alternatives can't have the same supplier");
        }
        if (instr1.getCategory().getSubGroup().getGroup().getId() != instr2.getCategory().getSubGroup().getGroup().getId()){
            throw new BadRequestException("Alternatives must be in the same group");    
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

    /**
     * Deletes an alternative between two instruments.
     * @param instrId1 The id of the first instrument.
     * @param instrId2 The id of the second instrument.
     * @return A list of the updated alternatives available in the category of the first instrument. 
     */
    public List<InstrumentDTO> removeAlternative(Long instrId1, Long instrId2){
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
