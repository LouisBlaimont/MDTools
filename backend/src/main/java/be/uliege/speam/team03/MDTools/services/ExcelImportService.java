package be.uliege.speam.team03.MDTools.services;

import be.uliege.speam.team03.MDTools.DTOs.ImportRequestDTO;
import be.uliege.speam.team03.MDTools.compositeKeys.CategoryCharacteristicKey;
import be.uliege.speam.team03.MDTools.models.CategoryCharacteristic;
import be.uliege.speam.team03.MDTools.models.Characteristic;
import be.uliege.speam.team03.MDTools.models.Instruments;
import be.uliege.speam.team03.MDTools.models.SubGroup;
import be.uliege.speam.team03.MDTools.models.Suppliers;
import be.uliege.speam.team03.MDTools.models.Category;
import be.uliege.speam.team03.MDTools.repositories.InstrumentRepository;
import be.uliege.speam.team03.MDTools.repositories.SubGroupRepository;
import be.uliege.speam.team03.MDTools.repositories.SubGroupCharacteristicRepository;
import be.uliege.speam.team03.MDTools.repositories.CharacteristicRepository;
import be.uliege.speam.team03.MDTools.repositories.SupplierRepository;
import be.uliege.speam.team03.MDTools.repositories.CategoryRepository;
import be.uliege.speam.team03.MDTools.repositories.CategoryCharacteristicRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExcelImportService {

    private static final Logger logger = LoggerFactory.getLogger(ExcelImportService.class);
    private final InstrumentRepository instrumentRepository;
    private final SubGroupRepository subGroupRepository;
    private final SubGroupCharacteristicRepository subGroupCharacteristicRepository;
    private final CharacteristicRepository characteristicRepository;
    private final SupplierRepository supplierRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryCharacteristicRepository categoryCharacteristicRepository;



    public ExcelImportService(
        InstrumentRepository instrumentRepository, 
        SubGroupRepository subGroupRepository,
        SubGroupCharacteristicRepository subGroupCharacteristicRepository,
        CharacteristicRepository characteristicRepository,
        SupplierRepository supplierRepository,
        CategoryRepository categoryRepository,
        CategoryCharacteristicRepository categoryCharacteristicRepository
    ) {
        this.instrumentRepository = instrumentRepository;
        this.subGroupRepository = subGroupRepository;
        this.subGroupCharacteristicRepository = subGroupCharacteristicRepository;
        this.characteristicRepository = characteristicRepository;
        this.supplierRepository = supplierRepository;
        this.categoryRepository = categoryRepository;
        this.categoryCharacteristicRepository = categoryCharacteristicRepository;
    }

    /**
     * Processes the import request based on the selected import type.
     *
     * @param request The import request containing data, type, and category info.
     */
    public void processImport(ImportRequestDTO request) {
        switch (request.getImportType()) {
            case "Importer un sous-groupe":
                processSubGroupImport(request.getGroupName(), request.getSubGroupName(), request.getData());
                break;
            case "Importer des instruments non catégorisés":
                processUncategorizedInstruments(request.getData());
                break;
            case "Importer un catalogue":
                processCatalogImport(request.getData());
                break;
            case "Importer des alternatives":
                processAlternativesImport(request.getData());
                break;
            case "Importer un crossref":
                processCrossrefImport(request.getData());
                break;
            default:
                throw new IllegalArgumentException("Unknown import type: " + request.getImportType());
        }
    }

    /**
     * Processes the import of instruments into a sub-group.
     *
     * @param groupName     The name of the group.
     * @param subGroupName  The name of the sub-group.
     * @param data          The list of instruments to be processed.
     */
    private void processSubGroupImport(String groupName, String subGroupName, List<Map<String, Object>> data) {
        logger.info("🛠 Importing instruments into sub-group: {} -> {}", groupName, subGroupName);

        Optional<SubGroup> subGroupOpt = subGroupRepository.findByName(subGroupName);
        if (subGroupOpt.isEmpty()) {
            logger.warn("⚠ Sub-group '{}' not found. Skipping import.", subGroupName);
            return;
        }
        SubGroup subGroup = subGroupOpt.get();

        // Fetch characteristics using the same approach as in SubGroupService
        List<String> subGroupCharacteristics = subGroup.getSubGroupCharacteristics()
            .stream()
            .map(detail -> detail.getCharacteristic().getName())
            .collect(Collectors.toList());

        logger.info("Characteristics for sub-group '{}': {}", subGroupName, subGroupCharacteristics);

        Set<String> availableColumns = data.stream()
        .flatMap(row -> row.keySet().stream())
        .collect(Collectors.toSet());

        for (Map<String, Object> row : data) {
            String reference = (String) row.get("reference");
            if (reference == null || reference.trim().isEmpty()) {
                logger.warn("Skipping entry due to missing reference.");
                continue;
            }

            Optional<Instruments> existingInstrumentOpt = instrumentRepository.findByReference(reference);
            if (existingInstrumentOpt.isPresent()) {
                logger.info("Instrument with reference {} already exists. Checking for differences...", reference);
                Instruments existingInstrument = existingInstrumentOpt.get();
                // TODO: Implement logic to compare and update existing instrument if needed.
                continue;
            }

            Instruments newInstrument = new Instruments();
            newInstrument.setReference(reference);
            
            if (availableColumns.contains("supplier_name")) {
                String supplierName = (String) row.get("supplier_name");
            
                Optional<Suppliers> supplierOpt = supplierRepository.findByName(supplierName);
                
                supplierOpt.ifPresent(newInstrument::setSupplier);
            }

            if (availableColumns.contains("supplier_description")) {
                newInstrument.setSupplierDescription((String) row.get("supplier_description"));
            }
            if (availableColumns.contains("price")) {
                Object priceObj = row.get("price");
                Float price = (priceObj instanceof Number) ? ((Number) priceObj).floatValue() : 0.0f;
                newInstrument.setPrice(price);
            }
            
            if (availableColumns.contains("obsolete")) {
                newInstrument.setObsolete(row.get("obsolete") != null ? (Boolean) row.get("obsolete") : false);
            }

            // Step 1: Extract characteristic values from JSON
            Map<String, String> instrumentCharacteristics = new HashMap<>();
            for (String characteristic : subGroupCharacteristics) {
                if (row.containsKey(characteristic)) {
                    instrumentCharacteristics.put(characteristic, row.get(characteristic).toString());
                } else {
                    instrumentCharacteristics.put(characteristic, "default"); // Placeholder for missing characteristics
                }
            }
            // Retrieve all existing categories for the given sub-group
            List<Category> existingCategories = categoryRepository.findBySubGroup(subGroup)
            .orElse(new ArrayList<>());

            // Step 2: Find an existing category with matching characteristics
            Category matchedCategory = null;
            for (Category category : existingCategories) {
                boolean match = true;
                for (String characteristic : instrumentCharacteristics.keySet()) {
                    Optional<String> existingValueOpt = categoryRepository.findCharacteristicVal(category.getId(), characteristic);
                    if (existingValueOpt.isEmpty() || !existingValueOpt.get().equals(instrumentCharacteristics.get(characteristic))) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    matchedCategory = category;
                    break;
                }
            }

            // Step 3: If no existing category matches, create a new one
            if (matchedCategory == null) {
                matchedCategory = new Category(subGroup);
                categoryRepository.save(matchedCategory);

                // Link new characteristics to the category
                for (Map.Entry<String, String> entry : instrumentCharacteristics.entrySet()) {
                    String characteristicName = entry.getKey();
                    String characteristicValue = entry.getValue();

                    Optional<Characteristic> characteristicOpt = characteristicRepository.findByName(characteristicName);
                    Characteristic characteristic = characteristicOpt.orElseGet(() -> {
                        Characteristic newCharacteristic = new Characteristic(characteristicName);
                        characteristicRepository.save(newCharacteristic);
                        return newCharacteristic;
                    });

                    // Create composite key for CategoryCharacteristic
                    CategoryCharacteristicKey key = new CategoryCharacteristicKey(matchedCategory.getId(), characteristic.getId());

                    // Create and save the CategoryCharacteristic with both value and abbreviation
                    CategoryCharacteristic categoryCharacteristic = new CategoryCharacteristic(matchedCategory, characteristic, characteristicValue, characteristicValue.substring(0, Math.min(3, characteristicValue.length()))); 
                    categoryCharacteristic.setId(key);
                    categoryCharacteristicRepository.save(categoryCharacteristic);
                }
                existingCategories.add(matchedCategory);
            }

            // Step 4: Link the instrument to the determined category
            newInstrument.setCategory(matchedCategory);


            
            instrumentRepository.save(newInstrument);
            logger.info("New instrument saved: {}", reference);
        }
    }


    private void processUncategorizedInstruments(List<Map<String, Object>> data) {
        logger.info("🛠 Importing uncategorized instruments...");
    }

    private void processCatalogImport(List<Map<String, Object>> data) {
        logger.info("🛠 Importing a catalog...");
    }

    private void processAlternativesImport(List<Map<String, Object>> data) {
        logger.info("🛠 Importing alternatives...");
    }

    private void processCrossrefImport(List<Map<String, Object>> data) {
        logger.info("🛠 Importing cross-reference data...");
    }
}
