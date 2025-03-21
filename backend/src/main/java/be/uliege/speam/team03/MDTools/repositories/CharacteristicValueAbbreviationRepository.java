package be.uliege.speam.team03.MDTools.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import be.uliege.speam.team03.MDTools.models.CharacteristicValueAbbreviation;


public interface CharacteristicValueAbbreviationRepository extends JpaRepository<CharacteristicValueAbbreviation, Long> {
   Optional<CharacteristicValueAbbreviation> findByValue(String characteristicValue);

   void deleteByValue(String characteristicValue);
}
