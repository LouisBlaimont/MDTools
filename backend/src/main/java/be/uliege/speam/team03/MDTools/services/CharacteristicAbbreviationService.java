package be.uliege.speam.team03.MDTools.services;

import java.util.Optional;

import org.springframework.stereotype.Service;

import be.uliege.speam.team03.MDTools.models.CharacteristicValueAbbreviation;
import be.uliege.speam.team03.MDTools.repositories.CharacteristicValueAbbreviationRepository;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class CharacteristicAbbreviationService {
   private final CharacteristicValueAbbreviationRepository repository;


   public Optional<String> getAbbreviation(String characteristicValue) {
      return repository.findByValue(characteristicValue).map(CharacteristicValueAbbreviation::getAbbreviation);
  }

   public void addAbbreviation(String characteristicValue, String abbreviation) {
      repository.save(new CharacteristicValueAbbreviation(characteristicValue, abbreviation));
   }

   public void updateAbbreviation(String characteristicValue, String abbreviation) {
      repository.findByValue(characteristicValue).ifPresent(abbrev -> {
         abbrev.setAbbreviation(abbreviation);
         repository.save(abbrev);
      });
   }

   public void deleteAbbreviation(String characteristicValue) {
      repository.deleteByValue(characteristicValue);
   }
}
