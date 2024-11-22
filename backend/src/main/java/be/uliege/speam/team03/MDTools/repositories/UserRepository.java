package be.uliege.speam.team03.MDTools.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import be.uliege.speam.team03.MDTools.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{

   public Optional<User> findByEmail(String email);

}
