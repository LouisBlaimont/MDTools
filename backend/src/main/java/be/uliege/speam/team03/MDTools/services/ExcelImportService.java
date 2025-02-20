package be.uliege.speam.team03.MDTools.services;

import be.uliege.speam.team03.MDTools.DTOs.ImportRequestDTO;
import be.uliege.speam.team03.MDTools.compositeKeys.CategoryCharacteristicKey;
import be.uliege.speam.team03.MDTools.models.*;
import be.uliege.speam.team03.MDTools.repositories.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.text.Normalizer;

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

    public void processImport(ImportRequestDTO request) {
        switch (request.getImportType()) {
            case "SubGroup":
                processSubGroupImport(request.getGroupName(), request.getSubGroupName(), request.getData());
                break;
            case "NonCategorized":
                processUncategorizedInstruments(request.getData());
                break;
            case "Catalogue":
                //processCatalogImport(request.getData());
                break;
            case "Alternatives":
                //processAlternativesImport(request.getData());
                break;
            case "Crossref":
                //processCrossrefImport(request.getData());
                break;
            default:
                throw new IllegalArgumentException("Unknown import type: " + request.getImportType());
        }
    }

    void processUncategorizedInstruments(List<Map<String, Object>> data) {
        logger.info("🛠 Importing uncategorized instruments...");
    
        Set<String> availableColumns = data.stream()
                .flatMap(row -> row.keySet().stream())
                .collect(Collectors.toSet());
    
        for (Map<String, Object> row : data) {
            String reference = (String) row.get("reference");
            if (reference == null || reference.trim().isEmpty()) {
                logger.warn("⚠ Skipping entry due to missing reference.");
                continue;
            }
    
            Optional<Instruments> existingInstrumentOpt = instrumentRepository.findByReference(reference);
            if (existingInstrumentOpt.isPresent()) {
                logger.info("🔄 Instrument {} already exists. Updating...", reference);
                Instruments existingInstrument = existingInstrumentOpt.get();
                boolean updated = updateExistingInstrument(existingInstrument, row, null, availableColumns, new ArrayList<>(), false);
                if (updated) {
                    instrumentRepository.save(existingInstrument);
                    logger.info("✅ Instrument {} updated.", reference);
                }
            } else {
                processInstrumentRow(row, null, availableColumns, new ArrayList<>(), false);
                logger.info("📌 New uncategorized instrument saved: {}", reference);
            }
        }
    }
    
    void processSubGroupImport(String groupName, String subGroupName, List<Map<String, Object>> data) {
        logger.info("🛠 Importing instruments into sub-group: {} -> {}", groupName, subGroupName);
    
        Optional<SubGroup> subGroupOpt = subGroupRepository.findByName(subGroupName);
        if (subGroupOpt.isEmpty()) {
            logger.warn("⚠ Sub-group '{}' not found. Skipping import.", subGroupName);
            return;
        }
        SubGroup subGroup = subGroupOpt.get();

        List<String> subGroupCharacteristics = subGroup.getSubGroupCharacteristics()
                .stream()
                .map(detail -> detail.getCharacteristic().getName())
                .collect(Collectors.toList());

        logger.info("Characteristics for sub-group '{}': {}", subGroupName, subGroupCharacteristics);

        Set<String> availableColumns = data.stream()
                .flatMap(row -> row.keySet().stream())
                .collect(Collectors.toSet());

        for (Map<String, Object> row : data) {
            processInstrumentRow(row, subGroup, availableColumns, subGroupCharacteristics, true);
        }
    }

    void processInstrumentRow(Map<String, Object> row, SubGroup subGroup, Set<String> availableColumns, List<String> subGroupCharacteristics, boolean manageCategories) {
        String reference = (String) row.get("reference");
        if (reference == null || reference.trim().isEmpty()) {
            logger.warn("Skipping entry due to missing reference.");
            return;
        }
    
        Optional<Instruments> existingInstrumentOpt = instrumentRepository.findByReference(reference);
        if (existingInstrumentOpt.isPresent()) {
            logger.info("Instrument with reference {} already exists. Checking for differences...", reference);
            Instruments existingInstrument = existingInstrumentOpt.get();

            if (updateExistingInstrument(existingInstrument, row, subGroup, availableColumns, subGroupCharacteristics, manageCategories)) {
                instrumentRepository.save(existingInstrument);
                logger.info("✅ Instrument {} updated successfully.", reference);
            }
            return;
        }
    
        Instruments newInstrument = new Instruments();
        newInstrument.setReference(reference);
        newInstrument.setSupplier(getOrCreateSupplier(row, availableColumns));
        newInstrument.setSupplierDescription((String) row.getOrDefault("supplier_description", ""));
        newInstrument.setPrice(getPrice(row, availableColumns));
        newInstrument.setObsolete(getObsoleteValue(row, availableColumns));
    
        if (manageCategories) {
            newInstrument.setCategory(getOrCreateCategory(subGroup, row, subGroupCharacteristics));
        }
    
        instrumentRepository.save(newInstrument);
        logger.info("New instrument saved: {}", reference);
    }
    

    Suppliers getOrCreateSupplier(Map<String, Object> row, Set<String> availableColumns) {
        // Retrieve supplier name from the provided data or set a default value
        String supplierName = availableColumns.contains("supplier_name") ? (String) row.get("supplier_name") : "Unknown Supplier";
        
        if (supplierName == null || supplierName.trim().isEmpty()) {
            supplierName = "Unknown Supplier";
        }
        
        // Normalize supplier name for case-insensitive and accent-free comparison
        String normalizedSupplierName = normalizeString(supplierName);
    
        // Fetch all existing suppliers from the database
        List<Suppliers> existingSuppliers = supplierRepository.findAll();
        for (Suppliers existingSupplier : existingSuppliers) {
            // Compare normalized names to check if a similar supplier already exists
            if (normalizeString(existingSupplier.getSupplierName()).equals(normalizedSupplierName)) {
                return existingSupplier; // Return the existing supplier to avoid duplicates
            }
        }
    
        // If no similar supplier exists, create a new one
        Suppliers newSupplier = new Suppliers();
        newSupplier.setSupplierName(supplierName);
        
        newSupplier.setSoldByMd(getSoldByMdValue(row, availableColumns));
        newSupplier.setClosed(getClosedValue(row, availableColumns));
    
        // Save the new supplier in the database
        supplierRepository.save(newSupplier);
        logger.info("Created new supplier: {}", supplierName);
        
        return newSupplier;
    }

    Float getPrice(Map<String, Object> row, Set<String> availableColumns) {
        if (!availableColumns.contains("price")) return 0.0f;

        Object priceObj = row.get("price");
        return (priceObj instanceof Number) ? ((Number) priceObj).floatValue() : 0.0f;
    }

    Boolean getObsoleteValue(Map<String, Object> row, Set<String> availableColumns) {
        return getBooleanValue(row, availableColumns, "obsolete");
    }
    
    Boolean getSoldByMdValue(Map<String, Object> row, Set<String> availableColumns) {
        if (!availableColumns.contains("sold_by_md") || row.get("sold_by_md") == null || row.get("sold_by_md").toString().trim().isEmpty()) {
            return false; // Default to false if the column is missing or empty
        }
        return getBooleanValue(row, availableColumns, "sold_by_md");
    }
    
    Boolean getClosedValue(Map<String, Object> row, Set<String> availableColumns) {
        if (!availableColumns.contains("closed") || row.get("closed") == null || row.get("closed").toString().trim().isEmpty()) {
            return false; // Default to false if the column is missing or empty
        }
        return getBooleanValue(row, availableColumns, "closed");
    }
    
    

    Boolean getBooleanValue(Map<String, Object> row, Set<String> availableColumns, String key) {
        if (!availableColumns.contains(key)) return false;
    
        Object value = row.get(key);
        if (value instanceof Boolean) return (Boolean) value;
    
        if (value instanceof String) {
            String strValue = ((String) value).trim().toLowerCase();
            Set<String> trueValues = Set.of("true", "vrai", "ja", "yes", "oui");
            Set<String> falseValues = Set.of("false", "faux", "nee", "non", "no");
    
            if (trueValues.contains(strValue)) return true;
            if (falseValues.contains(strValue)) return false;
        }
        return false;
    }
    

    Category getOrCreateCategory(SubGroup subGroup, Map<String, Object> row, List<String> subGroupCharacteristics) {
        Map<String, String> instrumentCharacteristics = extractCharacteristics(row, subGroupCharacteristics);
    
        List<Category> existingCategories = categoryRepository.findBySubGroup(subGroup).orElse(new ArrayList<>());
    
        for (Category category : existingCategories) {
            if (matchCategory(category, instrumentCharacteristics)) return category;
        }
        
        return createNewCategory(subGroup, instrumentCharacteristics, row.keySet(), row);
    }

    Map<String, String> extractCharacteristics(Map<String, Object> row, List<String> subGroupCharacteristics) {
        Map<String, String> characteristics = new HashMap<>();
        for (String characteristic : subGroupCharacteristics) {
            Object characteristicValue = row.get(characteristic);
            characteristics.put(characteristic, (characteristicValue != null) ? characteristicValue.toString() : "");
        }
        return characteristics;
    }

    boolean matchCategory(Category category, Map<String, String> instrumentCharacteristics) {
        for (String characteristic : instrumentCharacteristics.keySet()) {
            Optional<String> existingValueOpt = categoryRepository.findCharacteristicVal(category.getId().longValue(), characteristic);
            if (existingValueOpt.isEmpty() || !existingValueOpt.get().equals(instrumentCharacteristics.get(characteristic))) {
                return false;
            }
        }
        return true;
    }


    Category createNewCategory(SubGroup subGroup, Map<String, String> instrumentCharacteristics, Set<String> availableColumns, Map<String, Object> row) {
        Category newCategory = new Category(subGroup);
        categoryRepository.save(newCategory);
    
        for (Map.Entry<String, String> entry : instrumentCharacteristics.entrySet()) {
            String characteristicName = entry.getKey();
            String characteristicValue = entry.getValue();
    
            // Normalize characteristic value for comparison
            String normalizedValue = normalizeString(characteristicValue);
    
            // Check if characteristic exists in the database
            Characteristic characteristic = characteristicRepository.findByName(characteristicName)
                    .orElseGet(() -> {
                        Characteristic newChar = new Characteristic(characteristicName);
                        characteristicRepository.save(newChar);
                        return newChar;
                    });
    
            // Look for an existing characteristic value in the database
            Optional<CategoryCharacteristic> existingCategoryCharacteristic = categoryCharacteristicRepository
                    .findByCharacteristicId(characteristic.getId())
                    .stream()
                    .filter(cc -> normalizeString(cc.getVal()).equals(normalizedValue))
                    .findFirst();
    
            String abbreviation;
            if (existingCategoryCharacteristic.isPresent()) {
                // If the value already exists, use its abbreviation
                abbreviation = existingCategoryCharacteristic.get().getValAbrev();
            } else {
    
                // If no abbreviation exists, check if there's a column for abbreviation
                abbreviation = (availableColumns.contains("abbreviation") && row.get("abbreviation") != null)
                        ? row.get("abbreviation").toString()
                        : null;
            }
    
            // Create new CategoryCharacteristic
            CategoryCharacteristic categoryCharacteristic = new CategoryCharacteristic(newCategory, characteristic, characteristicValue, abbreviation);
            categoryCharacteristic.setId(new CategoryCharacteristicKey(newCategory.getId(), characteristic.getId()));
            categoryCharacteristicRepository.save(categoryCharacteristic);
        }
        return newCategory;
    }

    boolean updateExistingInstrument(Instruments instrument, Map<String, Object> row, SubGroup subGroup, Set<String> availableColumns, List<String> subGroupCharacteristics, Boolean manageCategories) {
        boolean isUpdated = false;
    
        // Check Supplier
        Suppliers newSupplier = getOrCreateSupplier(row, availableColumns);
        if (!Objects.equals(instrument.getSupplier(), newSupplier)) {
            instrument.setSupplier(newSupplier);
            isUpdated = true;
        }
    
        // Check Supplier Description
        String newDescription = (String) row.getOrDefault("supplier_description", "");
        if (!Objects.equals(instrument.getSupplierDescription(), newDescription)) {
            instrument.setSupplierDescription(newDescription);
            isUpdated = true;
        }
    
        // Check Price
        Float newPrice = getPrice(row, availableColumns);
        if (!Objects.equals(instrument.getPrice(), newPrice)) {
            instrument.setPrice(newPrice);
            isUpdated = true;
        }
    
        // Check Obsolete Status
        Boolean newObsolete = getObsoleteValue(row, availableColumns);
        if (!Objects.equals(instrument.getObsolete(), newObsolete)) {
            instrument.setObsolete(newObsolete);
            isUpdated = true;
        }
    
        // Check Category
        if (manageCategories) {
            Category newCategory = getOrCreateCategory(subGroup, row, subGroupCharacteristics);
            if (!Objects.equals(instrument.getCategory(), newCategory)) {
                instrument.setCategory(newCategory);
                isUpdated = true;
            }
            logger.info("📌 Sub-group '{}' updated with {} characteristics.", subGroup.getName(), subGroupCharacteristics.size());
        }
    
        return isUpdated;
    }    
    
    
    String normalizeString(String input) {
        if (input == null) return "";
        
        return Normalizer.normalize(input.trim().toLowerCase(), Normalizer.Form.NFD)
                         .replaceAll("\\p{M}", ""); // Remove accents
    }
}
