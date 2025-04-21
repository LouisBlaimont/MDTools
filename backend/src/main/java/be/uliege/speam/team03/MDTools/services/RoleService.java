package be.uliege.speam.team03.MDTools.services;

import java.util.List;

import org.springframework.stereotype.Service;

import be.uliege.speam.team03.MDTools.models.Authority;
import be.uliege.speam.team03.MDTools.repositories.AuthorityRepository;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class RoleService {
   
   private final AuthorityRepository authoritiesRepository;

   /**
    * getting all the Authorities
    * @return  a list of authority object
    */
   public List<Authority> getAllAuthorities() {
      return authoritiesRepository.findAll();
   }

   /**
    * getting all the names of the authorities
    * @return a list of string (names)
    */
   public List<String> getAllAuthoritiesNames() {
      return authoritiesRepository.findAll().stream().map(Authority::getAuthority).toList();
   }
}
