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
    @SuppressWarnings("unchecked")
    @Override
    public List<Instruments> searchByKeywords(List<String> keywords) {
        StringBuilder sql = new StringBuilder("SELECT d.instrument_id, " +
                                                    "d.reference, " +
                                                    "d.supplier_description, " +
                                                    "d.supplier_id, " + 
                                                    "d.category_id, " + 
                                                    "d.price, " + "d.obsolete, " + "d.price_date, "
        );

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
    


