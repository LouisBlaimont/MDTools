package be.uliege.speam.team03.MDTools.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.uliege.speam.team03.MDTools.DTOs.GroupDTO;
import be.uliege.speam.team03.MDTools.DTOs.OrderItemDTO;
import be.uliege.speam.team03.MDTools.exception.BadRequestException;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.services.OrdersService;

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
@RestController
@RequestMapping("/api/orders")
public class OrdersController {
    private final OrdersService ordersService;

    public OrdersController(OrdersService ordersService){
        this.ordersService = ordersService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getOrdersIdOfUser(@PathVariable Integer userId) {
        List<String> ordersNames = ordersService.findOrdersOfUser(userId);
        if (ordersNames == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cannot find user id");
        }
        return ResponseEntity.status(HttpStatus.OK).body(ordersNames);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getInstrumentsOfOrder(@PathVariable Integer orderId) {
        List<OrderItemDTO> instruments = ordersService.findInstrumentsOfOrder(orderId);
        if(instruments == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cannot find order id");
        }
        return ResponseEntity.status(HttpStatus.OK).body(instruments);
        
    }

    @PostMapping("/add-instrument")
    public ResponseEntity<?> addInstrToOrder(@RequestBody Map<String, Object> body) {
        List<OrderItemDTO> order = ordersService.addInstrumentToOrder(body);
        if (order == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Cannot add instrument to order");
        }
        return ResponseEntity.status(HttpStatus.OK).body(order);
    }

    @DeleteMapping("/{orderId}/user/{userId}/remove-instrument/{instrumentId}")
    public ResponseEntity<?> removeInstrFromOrder(@PathVariable Integer orderId, @PathVariable Integer userId, @PathVariable Integer instrumentId) {
        List<OrderItemDTO> order = ordersService.removeInstrumentFromOrder(orderId, userId, instrumentId);
        if (order == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Cannot remove instrument from order");
        }
        return ResponseEntity.status(HttpStatus.OK).body(order);
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> body) {
        Boolean creation = ordersService.createNewOrder(body);
        if (creation != true) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Cannot create group");
        }
        return ResponseEntity.status(HttpStatus.OK).body(creation);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> deleteOrder(@PathVariable Integer orderId) {
        Boolean deleted = ordersService.removeOrder(orderId);
        if (deleted != true) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Cannot delete order");
        }
        return ResponseEntity.status(HttpStatus.OK).body(deleted);
    }

    /* To test after adding a column orderName in db */
    @PatchMapping("/{orderId}")
    public ResponseEntity<?> updateOrder(@PathVariable Integer orderId, @RequestBody Map<String, Object> body){
        List<OrderItemDTO> order = ordersService.editOrder(orderId, body);
        return ResponseEntity.status(HttpStatus.OK).body(order);

    }
    
    
    
      
}
