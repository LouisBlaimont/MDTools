package be.uliege.speam.team03.MDTools.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import be.uliege.speam.team03.MDTools.models.Log;

/**
 * Repository interface for managing {@link Log} entities.
 * Extends {@link JpaRepository} to provide CRUD operations and additional JPA functionalities.
 * 
 * @see JpaRepository
 * @see Log
 */
public interface LogRepository extends JpaRepository<Log, Long> {
   
}
