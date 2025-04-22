package be.uliege.speam.team03.MDTools.repositories;


import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;

import be.uliege.speam.team03.MDTools.models.Orders;


public interface OrdersRepository extends CrudRepository<Orders, Long>{
    List<Orders> findByUserId(Long userId);

    @NonNull
    @Override
    Optional<Orders> findById(@NonNull Long id);

    List<Orders> findByOrderNameIgnoreCase(String orderName);
}