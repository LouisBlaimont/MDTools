package be.uliege.speam.team03.MDTools.services;

import be.uliege.speam.team03.MDTools.DTOs.ImportRequestDTO;
import be.uliege.speam.team03.MDTools.compositeKeys.CategoryCharacteristicKey;
import be.uliege.speam.team03.MDTools.models.*;
import be.uliege.speam.team03.MDTools.repositories.*;
import lombok.AllArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.text.Normalizer;

@AllArgsConstructor
@Service
public class ExcelImportService {

    private static final Logger logger = LoggerFactory.getLogger(ExcelImportService.class);

    private final InstrumentRepository instrumentRepository;
    private final SubGroupRepository subGroupRepository;
    private final CharacteristicRepository characteristicRepository;
    private final SupplierRepository supplierRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryCharacteristicRepository categoryCharacteristicRepository;
    private final CharacteristicAbbreviationService abbreviationService;

    /**
     * Processes an import request based on its type.
     * @param request The import request containing data and type.
     */
    public void processImport(ImportRequestDTO request) {
        switch (request.getImportType()) {
            case "SubGroup":
                processSubGroupImport(request.getGroupName(), request.getSubGroupName(), request.getData());
                break;
            case "NonCategorized":
                processUncategorizedInstruments(request.getData());
                break;
            case "Catalogue":
                processCatalogImport(request.getSupplier(), request.getData());
                break;
            case "Alternatives":
                //processAlternativesImport(request.getData());
                break;
            case "Crossref":
                processCrossrefImport(request.getData());
                break;
            default:
                throw new IllegalArgumentException("Unknown import type: " + request.getImportType());
        }
    }

    /**
     * Processes uncategorized instruments and updates or inserts them into the database.
     * @param data The list of instrument data.
     */
    void processUncategorizedInstruments(List<Map<String, Object>> data) {    
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
                Instruments existingInstrument = existingInstrumentOpt.get();
                boolean updated = updateExistingInstrument(existingInstrument, row, null, availableColumns, new ArrayList<>(), false);
                if (updated) {
                    instrumentRepository.save(existingInstrument);
                }
            } else {
                processInstrumentRow(row, null, availableColumns, new ArrayList<>(), false, null);
            }
        }
    }

    /**
     * Processes catalog import by inserting instruments linked to a supplier.
     * @param supplierName The supplier's name.
     * @param data The list of instrument data.
     */
    void processCatalogImport(String supplierName, List<Map<String, Object>> data) {
        Set<String> importedReferences = data.stream()
                .map(row -> (String) row.get("reference"))
                .filter(Objects::nonNull)
                .map(String::trim)
                .collect(Collectors.toSet());
    
        Supplier supplier = getOrCreateSupplier(Collections.emptyMap(), Collections.emptySet(), supplierName);
        if (supplier == null) {
            logger.warn("Supplier not found or could not be created.");
            return;
        }
    
        // Step 1: Mark obsolete instruments
        List<Instruments> existingInstruments = instrumentRepository.findAllBySupplier(supplier);
        for (Instruments instrument : existingInstruments) {
            if (!importedReferences.contains(instrument.getReference())) {
                if (Boolean.FALSE.equals(instrument.getObsolete())) {
                    instrument.setObsolete(true);
                    instrumentRepository.save(instrument);
                }
            }
        }
    
        // Step 2: Import instruments and mark as not obsolete
        Set<String> availableColumns = data.stream()
                .flatMap(row -> row.keySet().stream())
                .collect(Collectors.toSet());
    
        for (Map<String, Object> row : data) {
            processInstrumentRow(row, null, availableColumns, new ArrayList<>(), false, supplierName);
    
            // Check if it exists and obsolete = true → make obsolete = false
            String reference = (String) row.get("reference");
            if (reference != null) {
                Optional<Instruments> existing = instrumentRepository.findByReference(reference.trim());
                existing.ifPresent(instrument -> {
                    if (Boolean.TRUE.equals(instrument.getObsolete())) {
                        instrument.setObsolete(false);
                        instrumentRepository.save(instrument);
                    }
                });
            }
        }
    }
     
    
    /**
     * Processes subgroup import and associates instruments with the subgroup.
     * @param groupName The name of the group.
     * @param subGroupName The name of the subgroup.
     * @param data The list of instrument data.
     */
    void processSubGroupImport(String groupName, String subGroupName, List<Map<String, Object>> data) {
    
        Optional<SubGroup> subGroupOpt = subGroupRepository.findByName(subGroupName);
        if (subGroupOpt.isEmpty()) {
            return;
        }
        SubGroup subGroup = subGroupOpt.get();

        List<String> subGroupCharacteristics = subGroup.getSubGroupCharacteristics()
                .stream()
                .map(detail -> detail.getCharacteristic().getName())
                .toList();

        Set<String> availableColumns = data.stream()
                .flatMap(row -> row.keySet().stream())
                .collect(Collectors.toSet());

        for (Map<String, Object> row : data) {
            processInstrumentRow(row, subGroup, availableColumns, subGroupCharacteristics, true, null);
        }
    }

    /**
     * Processes a single instrument row and adds or updates it in the database.
     * @param row The data of the instrument.
     * @param subGroup The subgroup of the instrument.
     * @param availableColumns The available columns in the dataset.
     * @param subGroupCharacteristics The characteristics of the subgroup.
     * @param manageCategories Whether to manage categories.
     * @param supplier The supplier of the instrument.
     */
    void processInstrumentRow(Map<String, Object> row, SubGroup subGroup, Set<String> availableColumns, List<String> subGroupCharacteristics, boolean manageCategories, String supplier) {
        String reference = (String) row.get("reference");
        if (reference == null || reference.trim().isEmpty()) {
            logger.warn("Skipping entry due to missing reference.");
            return;
        }
    
        Optional<Instruments> existingInstrumentOpt = instrumentRepository.findByReference(reference);
        if (existingInstrumentOpt.isPresent()) {
            Instruments existingInstrument = existingInstrumentOpt.get();

            if (updateExistingInstrument(existingInstrument, row, subGroup, availableColumns, subGroupCharacteristics, manageCategories)) {
                instrumentRepository.save(existingInstrument);
            }
            return;
        }
    
        Instruments newInstrument = new Instruments();
        newInstrument.setReference(reference);
        newInstrument.setSupplier(getOrCreateSupplier(row, availableColumns, supplier));
        newInstrument.setSupplierDescription((String) row.getOrDefault("supplier_description", ""));
        newInstrument.setPrice(getPrice(row, availableColumns));
        newInstrument.setObsolete(getObsoleteValue(row, availableColumns));
    
        if (manageCategories) {
            newInstrument.setCategory(getOrCreateCategory(subGroup, row, subGroupCharacteristics));
        }
    
        instrumentRepository.save(newInstrument);
    }
    
    /**
     * Retrieves an existing supplier or creates a new one.
     * @param row The row data containing supplier information.
     * @param availableColumns The available columns in the dataset.
     * @param supplierName The supplier's name.
     * @return The supplier object.
     */
    Supplier getOrCreateSupplier(Map<String, Object> row, Set<String> availableColumns, String supplierName) {
        // Use the provided supplier name if not null, otherwise extract from available columns
        if (supplierName == null || supplierName.trim().isEmpty()) {
            supplierName = availableColumns.contains("supplier") ? (String) row.get("supplier") : null;
        }

        // If still null or empty after extraction, return null (do not create an "Unknown Supplier")
        if (supplierName == null || supplierName.trim().isEmpty()) {
            return null;
        }

        // Normalize supplier name for case-insensitive and accent-free comparison
        String normalizedSupplierName = normalizeString(supplierName);
    
        // Fetch all existing suppliers from the database
        List<Supplier> existingSuppliers = supplierRepository.findAll();
        for (Supplier existingSupplier : existingSuppliers) {
            // Compare normalized names to check if a similar supplier already exists
            if (normalizeString(existingSupplier.getSupplierName()).equals(normalizedSupplierName)) {
                return existingSupplier; // Return the existing supplier to avoid duplicates
            }
        }
    
        // If no similar supplier exists, create a new one
        Supplier newSupplier = new Supplier();
        newSupplier.setSupplierName(supplierName);
        
        newSupplier.setSoldByMd(getSoldByMdValue(row, availableColumns));
        newSupplier.setClosed(getClosedValue(row, availableColumns));
    
        // Save the new supplier in the database
        supplierRepository.save(newSupplier);
        
        return newSupplier;
    }
    /**
     * Extracts the price from the given data.
     * @param row The instrument data.
     * @param availableColumns The available columns in the data.
     * @return The extracted price, or 0.0 if not available.
     */
    Float getPrice(Map<String, Object> row, Set<String> availableColumns) {
        if (!availableColumns.contains("price")) return 0.0f;

        Object priceObj = row.get("price");
        return (priceObj instanceof Number) ? ((Number) priceObj).floatValue() : 0.0f;
    }

    /**
     * Determines whether an instrument is obsolete.
     * @param row The instrument data.
     * @param availableColumns The available columns in the data.
     * @return True if the instrument is obsolete, false otherwise.
     */
    Boolean getObsoleteValue(Map<String, Object> row, Set<String> availableColumns) {
        return getBooleanValue(row, availableColumns, "obsolete");
    }
    

    /**
     * Determines whether the instrument is sold by MD.
     * @param row The instrument data.
     * @param availableColumns The available columns in the data.
     * @return True if the instrument is sold by MD, false otherwise.
     */
    Boolean getSoldByMdValue(Map<String, Object> row, Set<String> availableColumns) {
        if (!availableColumns.contains("sold_by_md") || row.get("sold_by_md") == null || row.get("sold_by_md").toString().trim().isEmpty()) {
            return false; // Default to false if the column is missing or empty
        }
        return getBooleanValue(row, availableColumns, "sold_by_md");
    }
    
    /**
     * Determines whether the supplier is closed.
     * @param row The instrument data.
     * @param availableColumns The available columns in the data.
     * @return True if the supplier is closed, false otherwise.
     */
    Boolean getClosedValue(Map<String, Object> row, Set<String> availableColumns) {
        if (!availableColumns.contains("closed") || row.get("closed") == null || row.get("closed").toString().trim().isEmpty()) {
            return false; // Default to false if the column is missing or empty
        }
        return getBooleanValue(row, availableColumns, "closed");
    }
    
    /**
     * Extracts a boolean value from the given data.
     * @param row The instrument data.
     * @param availableColumns The available columns in the data.
     * @param key The key for the boolean value.
     * @return The extracted boolean value, or false if not found.
     */
    Boolean getBooleanValue(Map<String, Object> row, Set<String> availableColumns, String key) {
        if (!availableColumns.contains(key)) return false;
    
        Object value = row.get(key);
        if (value instanceof Boolean) return (Boolean) value;
    
        if (value instanceof String) {
            String strValue = ((String) value).trim().toLowerCase();
            Set<String> trueValues = Set.of("true", "vrai", "ja", "yes", "oui", "marque propre", "obsolete");
            Set<String> falseValues = Set.of("false", "faux", "nee", "non", "no");
    
            if (trueValues.contains(strValue)) return true;
            if (falseValues.contains(strValue)) return false;
        }
        return false;
    }
    
    /**
     * Retrieves or creates a category based on subgroup characteristics.
     * @param subGroup The subgroup associated with the category.
     * @param row The instrument data.
     * @param subGroupCharacteristics The list of subgroup characteristics.
     * @return The category entity.
     */
    Category getOrCreateCategory(SubGroup subGroup, Map<String, Object> row, List<String> subGroupCharacteristics) {
        Map<String, String> instrumentCharacteristics = extractCharacteristics(row, subGroupCharacteristics);
    
        List<Category> existingCategories = categoryRepository.findBySubGroup(subGroup).orElse(new ArrayList<>());
    
        for (Category category : existingCategories) {
            if (matchCategory(category, instrumentCharacteristics)) return category;
        }
        
        return createNewCategory(subGroup, instrumentCharacteristics, row.keySet(), row);
    }

    /**
     * Extracts instrument characteristics based on the given subgroup characteristics.
     * @param row The instrument data.
     * @param subGroupCharacteristics The list of subgroup characteristics.
     * @return A map of characteristic names and values.
     */
    Map<String, String> extractCharacteristics(Map<String, Object> row, List<String> subGroupCharacteristics) {
        Map<String, String> characteristics = new HashMap<>();
        for (String characteristic : subGroupCharacteristics) {
            Object characteristicValue = row.get(characteristic);
            characteristics.put(characteristic, (characteristicValue != null) ? characteristicValue.toString() : "");
        }
        return characteristics;
    }

    /**
     * Checks if the given instrument characteristics match an existing category.
     * @param category The category to compare against.
     * @param instrumentCharacteristics The characteristics of the instrument.
     * @return True if the category matches, false otherwise.
     */
    boolean matchCategory(Category category, Map<String, String> instrumentCharacteristics) {
        for (String characteristic : instrumentCharacteristics.keySet()) {
            Optional<String> existingValueOpt = categoryRepository.findCharacteristicVal(category.getId().longValue(), characteristic);
            if (existingValueOpt.isEmpty() || !existingValueOpt.get().equals(instrumentCharacteristics.get(characteristic))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Creates a new category with the given characteristics.
     * @param subGroup The subgroup associated with the category.
     * @param instrumentCharacteristics The characteristics of the instrument.
     * @param availableColumns The available columns in the data.
     * @param row The instrument data.
     * @return The created category entity.
     */
    Category createNewCategory(SubGroup subGroup, Map<String, String> instrumentCharacteristics, Set<String> availableColumns, Map<String, Object> row) {
        Category newCategory = new Category(subGroup);
        categoryRepository.save(newCategory);
    
        for (Map.Entry<String, String> entry : instrumentCharacteristics.entrySet()) {
            String characteristicName = entry.getKey();
            String characteristicValue = entry.getValue();
    
            // Normalize the characteristic value for comparison
            String normalizedValue = normalizeString(characteristicValue);
    
            // Check if an abbreviation column exists for this characteristic
            String abbreviationColumn = "abbreviation_" + characteristicName;
            String abbreviation = (availableColumns.contains(abbreviationColumn) && row.get(abbreviationColumn) != null)
                    ? row.get(abbreviationColumn).toString()
                    : null;
    
            // Retrieve the characteristic from the database or create a new one if it does not exist
            Characteristic characteristic = characteristicRepository.findByName(characteristicName)
                    .orElseGet(() -> {
                        Characteristic newChar = new Characteristic(characteristicName);
                        characteristicRepository.save(newChar);
                        return newChar;
                    });
    
            // Check if the characteristic value already exists in the database
            Optional<CategoryCharacteristic> existingCategoryCharacteristic = categoryCharacteristicRepository
                    .findByCharacteristicId(characteristic.getId())
                    .stream()
                    .filter(cc -> normalizeString(cc.getVal()).equals(normalizedValue))
                    .findFirst();
    
            if (existingCategoryCharacteristic.isPresent()) {
                // If the characteristic value exists, check if an abbreviation is already defined
                String existingAbbreviation = abbreviationService.getAbbreviation(existingCategoryCharacteristic.get().getVal()).orElse(null);
                if (existingAbbreviation == null && abbreviation != null) {
                    // If no abbreviation exists and an abbreviation is provided in the import file, add it
                    abbreviationService.addAbbreviation(existingCategoryCharacteristic.get().getVal(), abbreviation);
                }
            } else {
                // If the characteristic is new, add the abbreviation if available
                String existingAbbreviation = abbreviationService
                    .getAbbreviation(characteristicValue)
                    .orElse(null);

                if (existingAbbreviation == null && abbreviation != null) {
                    abbreviationService.addAbbreviation(characteristicValue, abbreviation);
                }
            }
    
            // Create the association between the category and the characteristic
            CategoryCharacteristic categoryCharacteristic = new CategoryCharacteristic(newCategory, characteristic, characteristicValue);
            categoryCharacteristic.setId(new CategoryCharacteristicKey(newCategory.getId(), characteristic.getId()));
            categoryCharacteristicRepository.save(categoryCharacteristic);
        }
        return newCategory;
    }    

    /**
     * Updates an existing instrument with new data.
     * @param instrument The existing instrument.
     * @param row The new instrument data.
     * @param subGroup The subgroup associated with the instrument.
     * @param availableColumns The available columns in the data.
     * @param subGroupCharacteristics Characteristics associated with the subgroup.
     * @param manageCategories Whether to manage category creation.
     * @return True if the instrument was updated, false otherwise.
     */
    boolean updateExistingInstrument(Instruments instrument, Map<String, Object> row, SubGroup subGroup, Set<String> availableColumns, List<String> subGroupCharacteristics, Boolean manageCategories) {
        boolean isUpdated = false;
    
        // Check Supplier
        Supplier newSupplier = getOrCreateSupplier(row, availableColumns, null);
        if (newSupplier != null && !Objects.equals(instrument.getSupplier(), newSupplier)) {
            instrument.setSupplier(newSupplier);
            isUpdated = true;
        }
    
        // Check Supplier Description
        String newDescription = (String) row.get("supplier_description");
        if (newDescription != null && !Objects.equals(instrument.getSupplierDescription(), newDescription)) {
            instrument.setSupplierDescription(newDescription);
            isUpdated = true;
        }
    
        // Check Price
        Float newPrice = getPrice(row, availableColumns);
        if (newPrice != null && !Objects.equals(instrument.getPrice(), newPrice)) {
            instrument.setPrice(newPrice);
            isUpdated = true;
        }
    
        // Check Obsolete Status
        Boolean newObsolete = getObsoleteValue(row, availableColumns);
        if (newObsolete != null && !Objects.equals(instrument.getObsolete(), newObsolete)) {
            instrument.setObsolete(newObsolete);
            isUpdated = true;
        }
    
        // Check Category
        if (manageCategories) {
            Category newCategory = getOrCreateCategory(subGroup, row, subGroupCharacteristics);
            if (newCategory != null && !Objects.equals(instrument.getCategory(), newCategory)) {
                instrument.setCategory(newCategory);
                isUpdated = true;
            }
        }
    
        return isUpdated;
    }
       
    /**
     * Normalizes a string by converting to lowercase and removing accents.
     * @param input The input string.
     * @return The normalized string.
     */
    String normalizeString(String input) {
        if (input == null) return "";
        
        return Normalizer.normalize(input.trim().toLowerCase(), Normalizer.Form.NFD)
                         .replaceAll("\\p{M}", ""); // Remove accents
    }

    /**
     * Processes the Crossref import by ensuring instruments in the same row
     * share the same category and updating missing suppliers.
     * 
     * @param data The list of instrument data rows.
     */
    void processCrossrefImport(List<Map<String, Object>> data) {
        Set<String> availableColumns = data.stream()
                .flatMap(row -> row.keySet().stream())
                .collect(Collectors.toSet());
    
        for (Map<String, Object> row : data) {
            processCrossrefRow(row, availableColumns);
        }
    }
    
    /**
     * Processes a single row of the Crossref import, ensuring all instruments
     * in the row are linked to the same category and updating suppliers if needed.
     * 
     * @param row The instrument data row.
     * @param availableColumns The available columns in the dataset.
     */
    void processCrossrefRow(Map<String, Object> row, Set<String> availableColumns) {
        List<String> references = row.entrySet().stream()
                .filter(entry -> entry.getValue() != null && !entry.getValue().toString().trim().isEmpty())
                .map(Map.Entry::getValue)
                .map(Object::toString)
                .collect(Collectors.toList());
    
        if (references.isEmpty()) {
            logger.warn("Skipping row due to no valid references.");
            return;
        }
    
        // Step 1: Find an existing instrument with a category
        Category sharedCategory = null;
        for (String reference : references) {
            Optional<Instruments> existingInstrumentOpt = instrumentRepository.findByReference(reference);
            if (existingInstrumentOpt.isPresent()) {
                Instruments existingInstrument = existingInstrumentOpt.get();
                if (existingInstrument.getCategory() != null) {
                    sharedCategory = existingInstrument.getCategory();
                    break; // Found an instrument with a category, use this for all
                }
            }
        }
    
        // Step 2: Process each reference
        for (String reference : references) {
            Optional<Instruments> existingInstrumentOpt = instrumentRepository.findByReference(reference);
            if (existingInstrumentOpt.isPresent()) {
                Instruments existingInstrument = existingInstrumentOpt.get();
    
                // Update category if null and a shared category exists
                if (existingInstrument.getCategory() == null && sharedCategory != null) {
                    existingInstrument.setCategory(sharedCategory);
                    instrumentRepository.save(existingInstrument);
                }
    
                // Update supplier if missing but do not change if different
                Supplier newSupplier = getOrCreateSupplier(row, availableColumns, null);
                if (existingInstrument.getSupplier() == null && newSupplier != null) {
                    existingInstrument.setSupplier(newSupplier);
                    instrumentRepository.save(existingInstrument);
                }
    
            } else {
                // Create a new instrument with the shared category and supplier
                Instruments newInstrument = new Instruments();
                newInstrument.setReference(reference);
                newInstrument.setSupplier(getOrCreateSupplier(row, availableColumns, null));
                newInstrument.setCategory(sharedCategory);
                newInstrument.setPrice (getPrice(row, availableColumns));
                instrumentRepository.save(newInstrument);
            }
        }
    }    
}
