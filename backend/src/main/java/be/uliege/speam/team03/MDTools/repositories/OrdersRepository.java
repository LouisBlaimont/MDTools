package be.uliege.speam.team03.MDTools.repositories;


import org.springframework.data.repository.CrudRepository;
import be.uliege.speam.team03.MDTools.models.Orders;
import java.util.List;
import java.util.Optional;


public interface OrdersRepository extends CrudRepository<Orders, Integer>{
    List<Orders> findByUserId(Integer userId);
    Optional<Orders> findById(Integer id);
    List<Orders> findByOrderName(String orderName);
}