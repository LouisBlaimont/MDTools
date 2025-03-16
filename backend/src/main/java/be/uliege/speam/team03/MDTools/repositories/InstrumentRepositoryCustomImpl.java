package be.uliege.speam.team03.MDTools.repositories;

import java.util.List;

import org.springframework.stereotype.Repository;

import be.uliege.speam.team03.MDTools.models.Instruments;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class InstrumentRepositoryCustomImpl implements InstrumentRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    String createCustomQuery(List<String> names){
        StringBuilder query = new StringBuilder("SELECT d FROM instruments d WHERE ");
        int size = names.size();
        for(int i = 0; i < size; i++){
            query.append("d.supplierDescription ILIKE '%").append(names.get(i)).append("%'");
            if(i != size-1){
                query.append(" AND ");
            }
        }
        return query.toString();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<Instruments> searchByKeywords(List<String> keywords) {
        String queryString = createCustomQuery(keywords);
        return entityManager.createQuery(queryString).getResultList();
    }   
}

