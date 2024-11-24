package be.uliege.speam.team03.MDTools.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import be.uliege.speam.team03.MDTools.models.Log;

public interface LogRepository extends JpaRepository<Log, Long> {
   
}
