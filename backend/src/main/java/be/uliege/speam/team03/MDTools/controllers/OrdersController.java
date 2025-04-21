package be.uliege.speam.team03.MDTools.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.uliege.speam.team03.MDTools.DTOs.OrderItemDTO;
import be.uliege.speam.team03.MDTools.DTOs.OrdersDTO;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.services.OrdersService;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



/**
 * This controller implements the API endpoints relative to the orders. See the Wiki (>>2. Technical requirements>>API Specifications) for more information.
 */
@AllArgsConstructor
@RestController
@RequestMapping("/api/orders")
public class OrdersController {
    private final OrdersService ordersService;

    /**
     * Retrieves all orders associated with a specific user.
     *
     * @param userId The ID of the user whose orders are to be retrieved
     * @return A ResponseEntity containing a list of OrdersDTO objects and HTTP status 200 (OK)
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrdersDTO>> getOrdersIdOfUser(@PathVariable Long userId) {
        List<OrdersDTO> ordersNames = ordersService.findOrdersOfUser(userId);
        return ResponseEntity.status(HttpStatus.OK).body(ordersNames);
    }

    /**
     * Retrieves all instruments associated with a specific order.
     *
     * @param orderId the ID of the order to retrieve instruments for
     * @return ResponseEntity containing a list of OrderItemDTO representing the instruments of the order
     *         with status code 200 (OK) if successful
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<List<OrderItemDTO>> getInstrumentsOfOrder(@PathVariable Long orderId) {
        List<OrderItemDTO> instruments = ordersService.findInstrumentsOfOrder(orderId);
        return ResponseEntity.status(HttpStatus.OK).body(instruments);
        
    }

    /**
     * Adds an instrument to an existing order.
     * 
     * @param body A Map containing the order and instrument information:
     *             - expected keys may include order ID, instrument ID, quantity, etc.
     * @return ResponseEntity containing a List of OrderItemDTO representing the updated order items
     *         with HTTP status 200 (OK) if successful
     */
    @PostMapping("/add-instrument")
    public ResponseEntity<List<OrderItemDTO>> addInstrToOrder(@RequestBody Map<String, Object> body) {
        List<OrderItemDTO> order = ordersService.addInstrumentToOrder(body);
        return ResponseEntity.status(HttpStatus.OK).body(order);
    }

    /**
     * Removes a specific instrument from an order.
     * 
     * @param orderId The ID of the order from which to remove the instrument
     * @param userId The ID of the user who owns the order
     * @param instrumentId The ID of the instrument to remove from the order
     * @return ResponseEntity containing a list of OrderItemDTO representing the updated order items after removal
     */
    @DeleteMapping("/{orderId}/user/{userId}/remove-instrument/{instrumentId}")
    public ResponseEntity<List<OrderItemDTO>> removeInstrFromOrder(@PathVariable Long orderId, @PathVariable Long userId, @PathVariable Long instrumentId) {
        List<OrderItemDTO> order = ordersService.removeInstrumentFromOrder(orderId, userId, instrumentId);
        return ResponseEntity.status(HttpStatus.OK).body(order);
    }

    /**
     * Creates a new order in the system.
     * 
     * @param body A Map containing the data necessary to create a new order
     * @return ResponseEntity containing a list of OrdersDTO representing the created orders with HTTP status 200 (OK)
     */
    @PostMapping
    public ResponseEntity<List<OrdersDTO>> createOrder(@RequestBody Map<String, Object> body) {
        List<OrdersDTO> orders = ordersService.createNewOrder(body);
        return ResponseEntity.status(HttpStatus.OK).body(orders);
    }

    /**
     * Deletes an order with the specified ID.
     * 
     * @param orderId The ID of the order to be deleted
     * @return A ResponseEntity with no content (204) indicating successful deletion
     */
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Boolean> deleteOrder(@PathVariable Long orderId) {
        ordersService.removeOrder(orderId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * Updates an existing order based on the provided partial information.
     * 
     * @param orderId The unique identifier of the order to update
     * @param body A map containing the fields to update and their new values
     * @return ResponseEntity containing a list of OrderItemDTO representing the updated orders
     * @throws ResourceNotFoundException if the order with the specified ID is not found
     * @throws InvalidRequestException if the update operation fails due to invalid data
     */
    @PatchMapping("/{orderId}")
    public ResponseEntity<List<OrderItemDTO>> updateOrder(@PathVariable Long orderId, @RequestBody Map<String, Object> body){
        List<OrderItemDTO> order = ordersService.editOrder(orderId, body);
        return ResponseEntity.status(HttpStatus.OK).body(order);

    } 

    /**
     * Updates an existing order after it is exported (sets exported to true and sets export date)
     * @param orderId The unique identifier of the order to update
     * @return ResponseEntity containing a list of OrdersDTO representing the updated orders with HTTP status 200 (OK)
     * @throws ResourceNotFoundException if the order doesn't exist.
     */
    @PatchMapping("/{orderId}/exported")
    public ResponseEntity<List<OrdersDTO>> orderIsExported(@PathVariable Long orderId){
        List<OrdersDTO> orders = ordersService.orderExported(orderId);
        return ResponseEntity.status(HttpStatus.OK).body(orders);
    }

}
