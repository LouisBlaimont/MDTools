package be.uliege.speam.team03.MDTools.services;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import be.uliege.speam.team03.MDTools.DTOs.OrderItemDTO;
import be.uliege.speam.team03.MDTools.DTOs.OrdersDTO;
import be.uliege.speam.team03.MDTools.compositeKeys.OrderItemsKey;
import be.uliege.speam.team03.MDTools.exception.BadRequestException;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.mapper.OrderItemMapper;
import be.uliege.speam.team03.MDTools.models.Instruments;
import be.uliege.speam.team03.MDTools.models.OrderItems;
import be.uliege.speam.team03.MDTools.models.Orders;
import be.uliege.speam.team03.MDTools.models.User;
import be.uliege.speam.team03.MDTools.repositories.CategoryRepository;
import be.uliege.speam.team03.MDTools.repositories.InstrumentRepository;
import be.uliege.speam.team03.MDTools.repositories.OrderItemsRepository;
import be.uliege.speam.team03.MDTools.repositories.OrdersRepository;
import be.uliege.speam.team03.MDTools.repositories.UserRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class OrdersService {
    private final OrdersRepository ordersRepository;
    private final CategoryRepository categoryRepository;
    private final OrderItemsRepository orderItemsRepository;
    private final UserRepository userRepository;
    private final InstrumentRepository instrumentRepository;

    /**
     * Gets the instruments of the order given by id
     * @param id
     * @return List of OrdersDTO
     */
    public List<OrderItemDTO> findInstrumentsOfOrder(Integer id){
        Optional<Orders> orderMaybe = ordersRepository.findById(id);
        if(orderMaybe.isEmpty()){
            throw new ResourceNotFoundException("Order not found.");  
        }
        List<OrderItems> orderItems = orderItemsRepository.findOrderItemsByOrderId(id);

        List<OrderItemDTO> orderItemsDTO = new ArrayList<>();
        
        for (OrderItems orderItem : orderItems ){
            OrderItemMapper orderItemMapper = new OrderItemMapper(categoryRepository);
            OrderItemDTO orderItemDTO = orderItemMapper.mapToOrderItemDTO(orderItem);
            orderItemsDTO.add(orderItemDTO);
        }
        return orderItemsDTO;
    }

    /**
     * Gets the names of the orders of the user given by id.
     * @param id
     * @return List of String (names of orders)
     */
    public List<OrdersDTO> findOrdersOfUser(Integer id){
        Optional<User> userMaybe = userRepository.findByUserId(id);
        if(userMaybe.isEmpty()){
            throw new ResourceNotFoundException("User not found.");
        }
        List<OrdersDTO> orders = ordersRepository.findByUserId(id).stream().map(order -> new OrdersDTO(order.getId(), order.getOrderName())).collect(Collectors.toList());
        return orders;
    }

    /**
     * Add an instrument to an order or increases the quantity of this instrument if already present in the order
     * @param body contains the instrument id, the order id, the user id and the quantity
     * @return List of OrderItemDTO
     */
    public List<OrderItemDTO> addInstrumentToOrder(Map<String, Object> body){
        Integer orderId = (Integer) body.get("orderId");
        Integer userId = (Integer) body.get("userId");
        Integer instrumentId = (Integer) body.get("instrId");

        Optional<User> userMaybe = userRepository.findByUserId(userId);
        Optional<Orders> orderMaybe = ordersRepository.findById(orderId);
        Optional<Instruments> instrumentMaybe = instrumentRepository.findById(instrumentId);

        if(userMaybe.isEmpty() || orderMaybe.isEmpty() || instrumentMaybe.isEmpty()){
            throw new ResourceNotFoundException("User, order or instrument not found.");
        }
        
        Orders order = orderMaybe.get();
        if(order.getUserId() != userId){
            throw new ResourceNotFoundException("Order doesn't exist for this user.");
        }
        Instruments instrument = instrumentMaybe.get();

        OrderItemsKey orderItemsKey = new OrderItemsKey(orderId, instrumentId);

        Optional<OrderItems> orderItemMaybe = orderItemsRepository.findById(orderItemsKey);

        if(orderItemMaybe.isPresent()){
            OrderItems orderItem = orderItemMaybe.get();
            if (body.get("quantity") != null){
                Integer quantity = (Integer) body.get("quantity");
                orderItem.setQuantity(orderItem.getQuantity() + quantity);
                orderItemsRepository.save(orderItem);
                if(orderItem.getQuantity()<=0){
                    List<OrderItemDTO> orderItemDTOs = removeInstrumentFromOrder(orderId, userId, instrumentId);
                    return orderItemDTOs;
                }
            }
        }
        else{
            if(body.get("quantity") != null){
                Integer quantity = (Integer) body.get("quantity");
                OrderItems newOrderItem = new OrderItems(order, instrument, quantity);
                newOrderItem.setId(orderItemsKey);
                orderItemsRepository.save(newOrderItem);
            }
        }

        List<OrderItemDTO> orderItemsDTO = new ArrayList<>();
        List<OrderItems> orderItems = orderItemsRepository.findOrderItemsByOrderId(orderId);
        for (OrderItems orderItem : orderItems ){
            OrderItemMapper orderItemMapper = new OrderItemMapper(categoryRepository);
            OrderItemDTO orderItemDTO = orderItemMapper.mapToOrderItemDTO(orderItem);
            orderItemsDTO.add(orderItemDTO);
        }
        return orderItemsDTO;
    }

    /**
     * Removes instrument from specific order
     * @param orderId Id of the order
     * @param userId Id of the user
     * @param instrumentId Id of the instrument to be removed
     * @return True if instrument was successfully removed, else throws an error
     */
    public List<OrderItemDTO> removeInstrumentFromOrder(Integer orderId, Integer userId, Integer instrumentId){
        Optional<User> userMaybe = userRepository.findByUserId(userId);
        Optional<Orders> orderMaybe = ordersRepository.findById(orderId);
        Optional<Instruments> instrumentMaybe = instrumentRepository.findById(instrumentId);

        if(userMaybe.isEmpty() || orderMaybe.isEmpty() || instrumentMaybe.isEmpty()){
            throw new ResourceNotFoundException("User, order or instrument not found.");
        }
        if(orderMaybe.get().getUserId() != userId){
            throw new ResourceNotFoundException("Order doesn't exist for this user.");
        }
        OrderItemsKey orderItemsKey = new OrderItemsKey(orderId, instrumentId);

        Optional<OrderItems> orderItemMaybe = orderItemsRepository.findById(orderItemsKey);

        if(orderItemMaybe.isPresent()){
            orderItemsRepository.deleteById(orderItemsKey);
            List<OrderItemDTO> orderItemsDTO = new ArrayList<>();
            List<OrderItems> orderItems = orderItemsRepository.findOrderItemsByOrderId(orderId);
            for (OrderItems orderItem : orderItems ){
                OrderItemMapper orderItemMapper = new OrderItemMapper(categoryRepository);
                OrderItemDTO orderItemDTO = orderItemMapper.mapToOrderItemDTO(orderItem);
                orderItemsDTO.add(orderItemDTO);
            }
            return orderItemsDTO;
        }
        else{
            throw new ResourceNotFoundException("Instrument not found in order.");
        }
    }

    /**
     * Create new empty order for the user provided in the body with the name provided in the body
     * @param body
     * @return True if successfully created
     */
    public List<OrdersDTO> createNewOrder(Map<String, Object> body){
        Integer userId = (Integer) body.get("userId");
        String orderName = (String) body.get("orderName");

        Optional<User> userMaybe = userRepository.findByUserId(userId);
        if(userMaybe.isEmpty()){
            throw new ResourceNotFoundException("User doesn't exist.");
        }

        List<Orders> sameOrders = ordersRepository.findByOrderName(orderName);
        for (Orders order : sameOrders){
            if(order.getUserId() == userId){
                throw new BadRequestException("Order with that name already exists for that user");
            }
        }

        Orders newOrder = new Orders();
        newOrder.setUserId(userId);
        newOrder.setOrderDate(new Timestamp(System.currentTimeMillis()));
        newOrder.setOrderName(orderName);
        ordersRepository.save(newOrder);
        return findOrdersOfUser(userId); 
    }

    /**
     * Delete order given by orderId
     * @param orderId
     * @return true if order successfully deleted
     */
    public Boolean removeOrder(Integer orderId){
        Optional<Orders> orderMaybe = ordersRepository.findById(orderId);

        if(orderMaybe.isEmpty()){
            throw new ResourceNotFoundException("Order not found.");
        }
        
        //cascade deletion done in DB normally
        List<OrderItems> orderItems = orderItemsRepository.findOrderItemsByOrderId(orderId);
        if (orderItems.isEmpty() != true) {
            orderItemsRepository.deleteAll(orderItems);
        }
        ordersRepository.deleteById(orderId);
        return true;
    }

    /**
     * Changes the name of the order with order Id
     * @param orderId
     * @param body
     * @return
     */
    public List<OrderItemDTO> editOrder(Integer orderId, Map<String, Object> body){
        Optional<Orders> orderMaybe = ordersRepository.findById(orderId);

        if(orderMaybe.isEmpty()){
            throw new ResourceNotFoundException("Order not found.");
        }

        Orders order = orderMaybe.get();
        Integer userId = order.getUserId();

        String newName = (String) body.get("orderName");
        if(newName == null){
            throw new ResourceNotFoundException("Name is required");
        }

        List<Orders> sameOrders = ordersRepository.findByOrderName(newName);
        for (Orders sameOrder : sameOrders){
            if(sameOrder.getUserId() == userId){
                throw new BadRequestException("Order with that name already exists for that user");
            }
        }

        order.setOrderName(newName);
        ordersRepository.save(order);
        List<OrderItemDTO> orderItemsDTO = new ArrayList<>();
        List<OrderItems> orderItems = orderItemsRepository.findOrderItemsByOrderId(orderId);
        for (OrderItems orderItem : orderItems ){
            OrderItemMapper orderItemMapper = new OrderItemMapper(categoryRepository);
            OrderItemDTO orderItemDTO = orderItemMapper.mapToOrderItemDTO(orderItem);
            orderItemsDTO.add(orderItemDTO);
        }
        return orderItemsDTO;
    }
    
}
