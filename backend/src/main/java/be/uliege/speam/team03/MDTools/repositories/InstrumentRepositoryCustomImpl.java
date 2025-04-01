package be.uliege.speam.team03.MDTools.repositories;

import java.util.List;

import org.springframework.stereotype.Repository;

import be.uliege.speam.team03.MDTools.models.Instruments;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;


@Repository
public class InstrumentRepositoryCustomImpl implements InstrumentRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;
    // String createCustomQuery(List<String> names) {
    //     StringBuilder query = new StringBuilder("SELECT d.id, d.reference, d.supplierDescription, d.supplier,");
    
    //     // Building the GREATEST similarity score calculation
    //     query.append("GREATEST(");
    //     for (int i = 0; i < names.size(); i++) {
    //         if (i > 0) {
    //             query.append(", ");
    //         }
    //         query.append("similarity(d.reference, '").append(names.get(i)).append("')");
    //         query.append(", similarity(d.supplierDescription, '").append(names.get(i)).append("')");
    //     }
    //     query.append(") AS sim_score FROM instruments d WHERE ");
    
    //     // Building the WHERE condition
    //     for (int i = 0; i < names.size(); i++) {
    //         query.append("(");
            
    //         // ILIKE for case-insensitive matching
    //         query.append("d.reference ILIKE '%").append(names.get(i)).append("%' OR ");
    //         query.append("d.supplierDescription ILIKE '%").append(names.get(i)).append("%' OR ");
    
    //         // Trigram similarity matching (WITHOUT threshold)
    //         query.append("d.reference % '").append(names.get(i)).append("' OR ");
    //         query.append("d.supplierDescription % '").append(names.get(i)).append("'");
    
    //         query.append(")");
    
    //         // Append AND if it's not the last name
    //         if (i != names.size() - 1) {
    //             query.append(" AND ");
    //         }
    //     }
    
    //     // Order by the highest similarity score
    //     query.append(" ORDER BY sim_score DESC;");
    
    //     return query.toString();
    // }
    
    // @SuppressWarnings("unchecked")
    // @Override
    // public List<Instruments> searchByKeywords(List<String> keywords) {
    //     String queryString = createCustomQuery(keywords);
    //     System.out.println("queryy " + queryString);
    //     return entityManager.createQuery(queryString).getResultList();        
    // }  
    @SuppressWarnings("unchecked")
    @Override
    public List<Instruments> searchByKeywords(List<String> keywords) {
        StringBuilder sql = new StringBuilder("SELECT d.\"instrument_id\" AS id, " +
        "d.reference AS reference, " +
        "d.supplier_description AS supplier_description, " +
        "d.supplier_id AS supplier, ");

        // Build GREATEST function for similarity scoring
        sql.append("GREATEST(");
        for (int i = 0; i < keywords.size(); i++) {
            if (i > 0) sql.append(", ");
            sql.append("similarity(d.reference, :keyword").append(i).append(")");
            sql.append(", similarity(d.supplier_description, :keyword").append(i).append(")");
        }
        sql.append(") AS sim_score FROM instruments d WHERE ");

        // Add conditions for each keyword
        for (int i = 0; i < keywords.size(); i++) {
            sql.append("(");
            sql.append("d.reference ILIKE CONCAT('%', :keyword").append(i).append(", '%') OR ");
            sql.append("d.supplier_description ILIKE CONCAT('%', :keyword").append(i).append(", '%') OR ");
            sql.append("d.reference % :keyword").append(i).append(" OR ");
            sql.append("d.supplier_description % :keyword").append(i);
            sql.append(")");

            if (i < keywords.size() - 1) {
                sql.append(" AND ");
            }
        }

        sql.append(" ORDER BY sim_score DESC");

        Query query = entityManager.createNativeQuery(sql.toString(), Instruments.class);

        // Set parameters dynamically
        for (int i = 0; i < keywords.size(); i++) {
            query.setParameter("keyword" + i, keywords.get(i));
        }

        System.out.println("queryy: " + query.getResultList());
        return query.getResultList();
    }
}
    


