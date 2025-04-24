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
    private final PictureStorageService pictureStorageService;

    /**
     * Gets the instruments of the provided order
     * @param id The unique identifier of the order
     * @return List of OrderItemDTO representing the instruments within the order
     */
    public List<OrderItemDTO> findInstrumentsOfOrder(Long id){
        Optional<Orders> orderMaybe = ordersRepository.findById(id);
        if(orderMaybe.isEmpty()){
            throw new ResourceNotFoundException("Order not found.");  
        }
        List<OrderItems> orderItems = orderItemsRepository.findOrderItemsByOrderId(id);

        List<OrderItemDTO> orderItemsDTO = new ArrayList<>();
        
        for (OrderItems orderItem : orderItems ){
            OrderItemMapper orderItemMapper = new OrderItemMapper(categoryRepository, pictureStorageService);
            OrderItemDTO orderItemDTO = orderItemMapper.mapToOrderItemDTO(orderItem);
            orderItemsDTO.add(orderItemDTO);
        }
        return orderItemsDTO;
    }

    /**
     * Gets the informations about all orders of a specific user
     * @param id The unique identifier of the user
     * @return List of OrdersDTO representing the existing orders
     */
    public List<OrdersDTO> findOrdersOfUser(Long id){
        Optional<User> userMaybe = userRepository.findByUserId(id);
        if(userMaybe.isEmpty()){
            throw new ResourceNotFoundException("User not found.");
        }
        List<OrdersDTO> orders = ordersRepository.findByUserId(id).stream().map(order -> new OrdersDTO(order.getId(), order.getOrderName(), order.getIsExported(), order.getCreationDate(), order.getExportDate())).collect(Collectors.toList());
        return orders;
    }

    /**
     * Add an instrument to an order or increases the quantity of this instrument if already present in the order
     * @param body contains the instrument identifier, the order identifier, the user identifier and the quantity
     * @return List of OrderItemDTO representing the instrument within the updated order
     */
    public List<OrderItemDTO> addInstrumentToOrder(Map<String, Object> body){
        Long orderId = ((Number) body.get("orderId")).longValue();
        Long userId = ((Number) body.get("userId")).longValue();
        Long instrumentId = ((Number) body.get("instrId")).longValue();

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
            OrderItemMapper orderItemMapper = new OrderItemMapper(categoryRepository, pictureStorageService);
            OrderItemDTO orderItemDTO = orderItemMapper.mapToOrderItemDTO(orderItem);
            orderItemsDTO.add(orderItemDTO);
        }
        return orderItemsDTO;
    }

    /**
     * Removes instrument from specific order
     * @param orderId The unique identifier of the order
     * @param userId The unique identifier of the user
     * @param instrumentId The unique identifier of the instrument to be removed
     * @return True if instrument was successfully removed
     */
    public List<OrderItemDTO> removeInstrumentFromOrder(Long orderId, Long userId, Long instrumentId){
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
                OrderItemMapper orderItemMapper = new OrderItemMapper(categoryRepository, pictureStorageService);
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
     * @param body Contains the name of the new order
     * @return True if successfully created
     */
    public List<OrdersDTO> createNewOrder(Map<String, Object> body){
        Long userId = ((Number) body.get("userId")).longValue();
        String orderName = (String) body.get("orderName");

        Optional<User> userMaybe = userRepository.findByUserId(userId);
        if(userMaybe.isEmpty()){
            throw new ResourceNotFoundException("User doesn't exist.");
        }

        List<Orders> sameOrders = ordersRepository.findByOrderNameIgnoreCase(orderName);
        for (Orders order : sameOrders){
            if(order.getUserId() == userId && order.getIsExported() == false){
                throw new BadRequestException("Order with that name already exists for that user");
            }
        }

        Orders newOrder = new Orders();
        newOrder.setUserId(userId);
        newOrder.setCreationDate(new Timestamp(System.currentTimeMillis()));
        newOrder.setOrderName(orderName);
        newOrder.setIsExported(false);
        ordersRepository.save(newOrder);
        return findOrdersOfUser(userId); 
    }

    /**
     * Delete order given by orderId
     * @param orderId
     * @return true if order successfully deleted
     */
    public Boolean removeOrder(Long orderId){
        Optional<Orders> orderMaybe = ordersRepository.findById(orderId);

        if(orderMaybe.isEmpty()){
            throw new ResourceNotFoundException("Order not found.");
        }
        
        ordersRepository.deleteById(orderId);
        return true;
    }

    /**
     * Changes the name of a specific order
     * @param orderId The unique identifier of the order
     * @param body Contains the new name of the order
     * @return List of OrderItemDTO representing the updated order
     */
    public List<OrderItemDTO> editOrder(Long orderId, Map<String, Object> body){
        Optional<Orders> orderMaybe = ordersRepository.findById(orderId);

        if(orderMaybe.isEmpty()){
            throw new ResourceNotFoundException("Order not found.");
        }

        Orders order = orderMaybe.get();
        Long userId = order.getUserId();

        String newName = (String) body.get("orderName");
        if(newName == null){
            throw new ResourceNotFoundException("Name is required");
        }

        List<Orders> sameOrders = ordersRepository.findByOrderNameIgnoreCase(newName);
        for (Orders sameOrder : sameOrders){
            if(sameOrder.getUserId() == userId && sameOrder.getIsExported() == false){
                throw new BadRequestException("Order with that name already exists for that user");
            }
        }

        order.setOrderName(newName);
        ordersRepository.save(order);
        List<OrderItemDTO> orderItemsDTO = new ArrayList<>();
        List<OrderItems> orderItems = orderItemsRepository.findOrderItemsByOrderId(orderId);
        for (OrderItems orderItem : orderItems ){
            OrderItemMapper orderItemMapper = new OrderItemMapper(categoryRepository, pictureStorageService);
            OrderItemDTO orderItemDTO = orderItemMapper.mapToOrderItemDTO(orderItem);
            orderItemsDTO.add(orderItemDTO);
        }
        return orderItemsDTO;
    }

    /**
     * Updates information about a newly exported order :
     * - sets isExported to true
     * - sets exportDate
     * @param orderId The unique identifier of the order
     * @return List of OrdersDTO representing the updated orders
     */
    public List<OrdersDTO> orderExported(Long orderId){
        Optional<Orders> orderMaybe = ordersRepository.findById(orderId);

        if(orderMaybe.isEmpty()){
            throw new ResourceNotFoundException("Order not found.");
        }

        Orders order = orderMaybe.get();
        Long userId = order.getUserId();
        order.setIsExported(true);
        order.setExportDate(new Timestamp(System.currentTimeMillis()));
        ordersRepository.save(order);
        return findOrdersOfUser(userId);
    }
    
}
