package be.uliege.speam.team03.MDTools.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import be.uliege.speam.team03.MDTools.models.Authority;

@Repository
public interface AuthorityRepository extends JpaRepository<Authority, String> {}
