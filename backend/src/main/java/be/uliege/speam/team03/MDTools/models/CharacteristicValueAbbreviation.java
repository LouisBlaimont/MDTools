package be.uliege.speam.team03.MDTools.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Getter;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "category_characteristic_abbreviations")
public class CharacteristicValueAbbreviation {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   @Column(name = "id")
   Long id;

   @Column(name = "characteristic_value", nullable = false, unique = true)
   String value;

   @Column(name = "value_abreviation", nullable = false)
   String abbreviation;

   public CharacteristicValueAbbreviation(String value, String abbreviation) {
      this.value = value;
      this.abbreviation = abbreviation;
   }
}
