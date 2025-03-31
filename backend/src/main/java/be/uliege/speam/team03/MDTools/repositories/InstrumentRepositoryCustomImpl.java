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
    String createCustomQuery(List<String> names, float similarityThreshold){
        StringBuilder query = new StringBuilder("SELECT d FROM instruments d WHERE ");
        int size = names.size();
        
        for(int i = 0; i < size; i++){
            query.append("(");
            
            // ILIKE for case-insensitive matching
            // query.append("d.reference ILIKE '%").append(names.get(i)).append("%' OR ");
            // query.append("d.supplierDescription ILIKE '%").append(names.get(i)).append("%'");
    
            // Trigram similarity matching with threshold
            query.append(" CAST(similarity(d.reference, '").append(names.get(i)).append("') AS float  > ")
                 .append(similarityThreshold).append(" AND d.reference % '").append(names.get(i)).append("')");
    
            query.append(" OR CAST(similarity(d.supplierDescription, '").append(names.get(i)).append("') AS float > ")
                 .append(similarityThreshold).append(" AND d.supplierDescription % '").append(names.get(i)).append("')");
    
            query.append(")");
    
            // Append AND if it's not the last name
            if(i != size - 1){
                query.append(" OR "); // to change with AND when it works
            }
        }    

        return query.toString();
    }  

    
    @SuppressWarnings("unchecked")
    @Override
    public List<Instruments> searchByKeywords(List<String> keywords) {
        float threshold = 0.15f;
        String queryString = createCustomQuery(keywords, threshold);
        System.out.println("queryy " + queryString);
        return entityManager.createQuery(queryString).getResultList();        
    }  
}

