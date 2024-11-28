package be.uliege.speam.team03.MDTools.repositories;

import org.springframework.data.repository.CrudRepository;

import be.uliege.speam.team03.MDTools.compositeKeys.CategoryCharacteristicKey;
import be.uliege.speam.team03.MDTools.models.CategoryCharacteristic;

public interface CategoryCharacteristicRepository extends CrudRepository<CategoryCharacteristic, CategoryCharacteristicKey> {
}
