package be.uliege.speam.team03.MDTools.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import be.uliege.speam.team03.MDTools.compositeKeys.SubGroupCharacteristicKey;
import be.uliege.speam.team03.MDTools.models.Characteristic;
import be.uliege.speam.team03.MDTools.models.SubGroupCharacteristic;

public interface SubGroupCharacteristicRepository extends CrudRepository<SubGroupCharacteristic, SubGroupCharacteristicKey>{
    long countByCharacteristic(Characteristic characteristic);

    @Query("SELECT COUNT(gc) FROM SubGroupCharacteristic gc WHERE gc.subgroup.id = :subgroupId")
    int countBySubGroupId(Integer subgroupId);
}

