package be.uliege.speam.team03.MDTools.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import be.uliege.speam.team03.MDTools.models.User;

/**
 * Repository interface for User entity.
 * Extends JpaRepository to provide CRUD operations.
 * 
 * Methods:
 * - Optional<User> findByEmail(String email): Finds a user by their email.
 * - Optional<User> findByResetToken(String resetToken): Finds a user by their reset token.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>{

   /**
    * Retrieves an Optional containing a User entity that matches the given email.
    *
    * @param email the email address to search for
    * @return an Optional containing the User entity if found, or an empty Optional if not found
    */
   public Optional<User> findByEmail(String email);

   /**
    * Finds a user by their reset token.
    *
    * @param resetToken the reset token associated with the user
    * @return an Optional containing the user if found, or an empty Optional if no user is found with the given reset token
    */
   public Optional<User> findByResetToken(String resetToken);

}
