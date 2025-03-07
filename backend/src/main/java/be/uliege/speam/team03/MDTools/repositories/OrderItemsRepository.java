package be.uliege.speam.team03.MDTools.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import be.uliege.speam.team03.MDTools.compositeKeys.OrderItemsKey;
import be.uliege.speam.team03.MDTools.models.Instruments;
import be.uliege.speam.team03.MDTools.models.OrderItems;

public interface OrderItemsRepository extends CrudRepository<OrderItems, OrderItemsKey>{
    @Query("SELECT oi FROM OrderItems oi WHERE oi.order.id = :orderId")
    List<OrderItems> findOrderItemsByOrderId(Integer orderId);
}
