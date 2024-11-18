package be.uliege.speam.team03.MDTools.repositories;

import org.springframework.data.repository.CrudRepository;

import be.uliege.speam.team03.MDTools.compositeKeys.GroupCharacteristicKey;
import be.uliege.speam.team03.MDTools.models.*;

public interface GroupCharacteristicRepository extends CrudRepository<GroupCharacteristic, GroupCharacteristicKey>{
    long countByCharacteristic(Characteristic characteristic);
}

