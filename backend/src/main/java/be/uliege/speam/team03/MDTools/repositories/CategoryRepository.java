package be.uliege.speam.team03.MDTools.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import be.uliege.speam.team03.MDTools.models.Category;
import be.uliege.speam.team03.MDTools.models.Group;

@Repository
public interface CategoryRepository extends CrudRepository<Category, Integer> {
    Optional<List<Category>> findByGroup(Group group);
}
