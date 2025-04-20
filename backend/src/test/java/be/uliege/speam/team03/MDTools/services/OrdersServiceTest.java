package be.uliege.speam.team03.MDTools.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.sql.Timestamp;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import be.uliege.speam.team03.MDTools.DTOs.OrderItemDTO;
import be.uliege.speam.team03.MDTools.DTOs.OrdersDTO;
import be.uliege.speam.team03.MDTools.compositeKeys.OrderItemsKey;
import be.uliege.speam.team03.MDTools.exception.BadRequestException;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.models.Category;
import be.uliege.speam.team03.MDTools.models.Group;
import be.uliege.speam.team03.MDTools.models.Instruments;
import be.uliege.speam.team03.MDTools.models.OrderItems;
import be.uliege.speam.team03.MDTools.models.Orders;
import be.uliege.speam.team03.MDTools.models.SubGroup;
import be.uliege.speam.team03.MDTools.models.Supplier;
import be.uliege.speam.team03.MDTools.models.User;
import be.uliege.speam.team03.MDTools.repositories.CategoryRepository;
import be.uliege.speam.team03.MDTools.repositories.InstrumentRepository;
import be.uliege.speam.team03.MDTools.repositories.OrderItemsRepository;
import be.uliege.speam.team03.MDTools.repositories.OrdersRepository;
import be.uliege.speam.team03.MDTools.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
public class OrdersServiceTest {

    @Mock
    private OrdersRepository ordersRepository;
    
    @Mock
    private CategoryRepository categoryRepository;
    
    @Mock
    private OrderItemsRepository orderItemsRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private InstrumentRepository instrumentRepository;
    
    @InjectMocks
    private OrdersService ordersService;
    
    private User testUser;
    private Orders testOrder;
    private Instruments testInstrument;
    private OrderItems testOrderItem;
    private OrderItemsKey testOrderItemsKey;
    private Category testCategory;
    private Group testGroup;
    private SubGroup testSubGroup;
    private Supplier testSupplier;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        
        testOrder = new Orders();
        testOrder.setId(1L);
        testOrder.setUserId(1L);
        testOrder.setOrderName("Test Order");
        testOrder.setCreationDate(new Timestamp(System.currentTimeMillis()));
        testOrder.setIsExported(false);
        testOrder.setExportDate(null);

        testGroup = new Group();
        testGroup.setId(1L);
        testGroup.setName("Test Group");
        
        testSubGroup = new SubGroup();
        testSubGroup.setId(1L);
        testSubGroup.setName("Test SubGroup");
        testSubGroup.setGroup(testGroup);

        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setSubGroup(testSubGroup);

        testSupplier = new Supplier();
        testSupplier.setId(1L);
        testSupplier.setSupplierName("Test Supplier");
        
        testInstrument = new Instruments();
        testInstrument.setId(1L);
        testInstrument.setReference("Test Reference");
        testInstrument.setCategory(testCategory);
        testInstrument.setSupplier(testSupplier);
        
        
        testOrderItemsKey = new OrderItemsKey(1L, 1L);
        
        testOrderItem = new OrderItems(testOrder, testInstrument, 2);
        testOrderItem.setId(testOrderItemsKey);
    }
    
    @Test
    void findInstrumentsOfOrder_Success() {
        when(ordersRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderItemsRepository.findOrderItemsByOrderId(1L)).thenReturn(Collections.singletonList(testOrderItem));
        
        List<OrderItemDTO> result = ordersService.findInstrumentsOfOrder(1L);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(ordersRepository).findById(1L);
        verify(orderItemsRepository).findOrderItemsByOrderId(1L);
    }
    
    @Test
    void findInstrumentsOfOrder_OrderNotFound() {
        when(ordersRepository.findById(1L)).thenReturn(Optional.empty());
        
        assertThrows(ResourceNotFoundException.class, () -> {
            ordersService.findInstrumentsOfOrder(1L);
        });
        
        verify(ordersRepository).findById(1L);
        verify(orderItemsRepository, never()).findOrderItemsByOrderId(anyLong());
    }
    
    @Test
    void findOrdersOfUser_Success() {
        when(userRepository.findByUserId(1L)).thenReturn(Optional.of(testUser));
        when(ordersRepository.findByUserId(1L)).thenReturn(Collections.singletonList(testOrder));
        
        List<OrdersDTO> result = ordersService.findOrdersOfUser(1L);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Order", result.get(0).getName());
        verify(userRepository).findByUserId(1L);
        verify(ordersRepository).findByUserId(1L);
    }
    
    @Test
    void findOrdersOfUser_UserNotFound() {
        when(userRepository.findByUserId(1L)).thenReturn(Optional.empty());
        
        assertThrows(ResourceNotFoundException.class, () -> {
            ordersService.findOrdersOfUser(1L);
        });
        
        verify(userRepository).findByUserId(1L);
        verify(ordersRepository, never()).findByUserId(anyLong());
    }
    
    @Test
    void addInstrumentToOrder_NewInstrument_Success() {
        Map<String, Object> body = new HashMap<>();
        body.put("orderId", 1L);
        body.put("userId", 1L);
        body.put("instrId", 1L);
        body.put("quantity", 2);
        
        when(userRepository.findByUserId(1L)).thenReturn(Optional.of(testUser));
        when(ordersRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(instrumentRepository.findById(1L)).thenReturn(Optional.of(testInstrument));
        when(orderItemsRepository.findById(testOrderItemsKey)).thenReturn(Optional.empty());
        when(orderItemsRepository.findOrderItemsByOrderId(1L)).thenReturn(Collections.singletonList(testOrderItem));
        
        List<OrderItemDTO> result = ordersService.addInstrumentToOrder(body);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderItemsRepository).save(any(OrderItems.class));
    }
    
    @Test
    void addInstrumentToOrder_ExistingInstrument_Success() {
        Map<String, Object> body = new HashMap<>();
        body.put("orderId", 1L);
        body.put("userId", 1L);
        body.put("instrId", 1L);
        body.put("quantity", 2);
        
        when(userRepository.findByUserId(1L)).thenReturn(Optional.of(testUser));
        when(ordersRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(instrumentRepository.findById(1L)).thenReturn(Optional.of(testInstrument));
        when(orderItemsRepository.findById(testOrderItemsKey)).thenReturn(Optional.of(testOrderItem));
        when(orderItemsRepository.findOrderItemsByOrderId(1L)).thenReturn(Collections.singletonList(testOrderItem));
        
        List<OrderItemDTO> result = ordersService.addInstrumentToOrder(body);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderItemsRepository).save(testOrderItem);
    }
    
    @Test
    void addInstrumentToOrder_ResourceNotFound() {
        Map<String, Object> body = new HashMap<>();
        body.put("orderId", 1L);
        body.put("userId", 1L);
        body.put("instrId", 1L);
        body.put("quantity", 2);
        
        when(userRepository.findByUserId(1L)).thenReturn(Optional.empty());
        
        assertThrows(ResourceNotFoundException.class, () -> {
            ordersService.addInstrumentToOrder(body);
        });
    }
    
    @Test
    void addInstrumentToOrder_OrderDoesNotBelongToUser() {
        Map<String, Object> body = new HashMap<>();
        body.put("orderId", 1L);
        body.put("userId", 2L); // Different from order's userId
        body.put("instrId", 1L);
        body.put("quantity", 2);
        
        Orders orderWithDifferentUser = new Orders();
        orderWithDifferentUser.setId(1L);
        orderWithDifferentUser.setUserId(1L); // Different from userId in body
        
        when(userRepository.findByUserId(2L)).thenReturn(Optional.of(testUser));
        when(ordersRepository.findById(1L)).thenReturn(Optional.of(orderWithDifferentUser));
        when(instrumentRepository.findById(1L)).thenReturn(Optional.of(testInstrument));
        
        assertThrows(ResourceNotFoundException.class, () -> {
            ordersService.addInstrumentToOrder(body);
        });
    }
    
    @Test
    void addInstrumentToOrder_RemoveInstrumentWhenQuantityIsZeroOrLess() {
        Map<String, Object> body = new HashMap<>();
        body.put("orderId", 1L);
        body.put("userId", 1L);
        body.put("instrId", 1L);
        body.put("quantity", -2); // Negative quantity
        
        OrderItems orderItemWithQuantity = new OrderItems(testOrder, testInstrument, 1); // Current quantity is 1
        orderItemWithQuantity.setId(testOrderItemsKey);
        
        when(userRepository.findByUserId(1L)).thenReturn(Optional.of(testUser));
        when(ordersRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(instrumentRepository.findById(1L)).thenReturn(Optional.of(testInstrument));
        when(orderItemsRepository.findById(testOrderItemsKey)).thenReturn(Optional.of(orderItemWithQuantity));
        
        // Mock the removeInstrumentFromOrder method indirectly
        when(orderItemsRepository.findOrderItemsByOrderId(1L)).thenReturn(Collections.emptyList());
        
        List<OrderItemDTO> result = ordersService.addInstrumentToOrder(body);
        
        assertNotNull(result);
        verify(orderItemsRepository).deleteById(testOrderItemsKey);
    }
    
    @Test
    void removeInstrumentFromOrder_Success() {
        when(userRepository.findByUserId(1L)).thenReturn(Optional.of(testUser));
        when(ordersRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(instrumentRepository.findById(1L)).thenReturn(Optional.of(testInstrument));
        when(orderItemsRepository.findById(testOrderItemsKey)).thenReturn(Optional.of(testOrderItem));
        when(orderItemsRepository.findOrderItemsByOrderId(1L)).thenReturn(Collections.emptyList());
        
        List<OrderItemDTO> result = ordersService.removeInstrumentFromOrder(1L, 1L, 1L);
        
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(orderItemsRepository).deleteById(testOrderItemsKey);
    }
    
    @Test
    void removeInstrumentFromOrder_ResourceNotFound() {
        when(userRepository.findByUserId(1L)).thenReturn(Optional.empty());
        
        assertThrows(ResourceNotFoundException.class, () -> {
            ordersService.removeInstrumentFromOrder(1L, 1L, 1L);
        });
    }
    
    @Test
    void removeInstrumentFromOrder_OrderDoesNotBelongToUser() {
        Orders orderWithDifferentUser = new Orders();
        orderWithDifferentUser.setId(1L);
        orderWithDifferentUser.setUserId(2L); // Different from userId in parameters
        
        when(userRepository.findByUserId(1L)).thenReturn(Optional.of(testUser));
        when(ordersRepository.findById(1L)).thenReturn(Optional.of(orderWithDifferentUser));
        when(instrumentRepository.findById(1L)).thenReturn(Optional.of(testInstrument));
        
        assertThrows(ResourceNotFoundException.class, () -> {
            ordersService.removeInstrumentFromOrder(1L, 1L, 1L);
        });
    }
    
    @Test
    void removeInstrumentFromOrder_InstrumentNotInOrder() {
        when(userRepository.findByUserId(1L)).thenReturn(Optional.of(testUser));
        when(ordersRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(instrumentRepository.findById(1L)).thenReturn(Optional.of(testInstrument));
        when(orderItemsRepository.findById(testOrderItemsKey)).thenReturn(Optional.empty());
        
        assertThrows(ResourceNotFoundException.class, () -> {
            ordersService.removeInstrumentFromOrder(1L, 1L, 1L);
        });
    }
    
    @Test
    void createNewOrder_Success() {
        Map<String, Object> body = new HashMap<>();
        body.put("userId", 1L);
        body.put("orderName", "New Order");
        
        when(userRepository.findByUserId(1L)).thenReturn(Optional.of(testUser));
        when(ordersRepository.findByOrderNameIgnoreCase("New Order")).thenReturn(Collections.emptyList());
        when(ordersRepository.findByUserId(1L)).thenReturn(Collections.singletonList(testOrder));
        
        List<OrdersDTO> result = ordersService.createNewOrder(body);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(ordersRepository).save(any(Orders.class));
    }
    
    @Test
    void createNewOrder_UserNotFound() {
        Map<String, Object> body = new HashMap<>();
        body.put("userId", 1L);
        body.put("orderName", "New Order");
        
        when(userRepository.findByUserId(1L)).thenReturn(Optional.empty());
        
        assertThrows(ResourceNotFoundException.class, () -> {
            ordersService.createNewOrder(body);
        });
    }
    
    @Test
    void createNewOrder_OrderNameExists() {
        Map<String, Object> body = new HashMap<>();
        body.put("userId", 1L);
        body.put("orderName", "Existing Order");
        
        Orders existingOrder = new Orders();
        existingOrder.setId(2L);
        existingOrder.setUserId(1L);
        existingOrder.setOrderName("Existing Order");
        existingOrder.setIsExported(false);
        
        when(userRepository.findByUserId(1L)).thenReturn(Optional.of(testUser));
        when(ordersRepository.findByOrderNameIgnoreCase("Existing Order")).thenReturn(Collections.singletonList(existingOrder));
        
        assertThrows(BadRequestException.class, () -> {
            ordersService.createNewOrder(body);
        });
    }
    
    @Test
    void removeOrder_Success() {
        when(ordersRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderItemsRepository.findOrderItemsByOrderId(1L)).thenReturn(Collections.singletonList(testOrderItem));
        
        Boolean result = ordersService.removeOrder(1L);
        
        assertTrue(result);
        verify(orderItemsRepository).deleteAll(anyList());
        verify(ordersRepository).deleteById(1L);
    }
    
    @Test
    void removeOrder_EmptyOrderItems_Success() {
        when(ordersRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderItemsRepository.findOrderItemsByOrderId(1L)).thenReturn(Collections.emptyList());
        
        Boolean result = ordersService.removeOrder(1L);
        
        assertTrue(result);
        verify(orderItemsRepository, never()).deleteAll(anyList());
        verify(ordersRepository).deleteById(1L);
    }
    
    @Test
    void removeOrder_OrderNotFound() {
        when(ordersRepository.findById(1L)).thenReturn(Optional.empty());
        
        assertThrows(ResourceNotFoundException.class, () -> {
            ordersService.removeOrder(1L);
        });
    }
    
    @Test
    void editOrder_Success() {
        Map<String, Object> body = new HashMap<>();
        body.put("orderName", "Updated Order Name");
        
        when(ordersRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(ordersRepository.findByOrderNameIgnoreCase("Updated Order Name")).thenReturn(Collections.emptyList());
        when(orderItemsRepository.findOrderItemsByOrderId(1L)).thenReturn(Collections.singletonList(testOrderItem));
        
        List<OrderItemDTO> result = ordersService.editOrder(1L, body);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(ordersRepository).save(testOrder);
        assertEquals("Updated Order Name", testOrder.getOrderName());
    }
    
    @Test
    void editOrder_OrderNotFound() {
        Map<String, Object> body = new HashMap<>();
        body.put("orderName", "Updated Order Name");
        
        when(ordersRepository.findById(1L)).thenReturn(Optional.empty());
        
        assertThrows(ResourceNotFoundException.class, () -> {
            ordersService.editOrder(1L, body);
        });
    }
    
    @Test
    void editOrder_NameNotProvided() {
        Map<String, Object> body = new HashMap<>();
        // No orderName provided
        
        when(ordersRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        
        assertThrows(ResourceNotFoundException.class, () -> {
            ordersService.editOrder(1L, body);
        });
    }
    
    @Test
    void editOrder_NameExists() {
        Map<String, Object> body = new HashMap<>();
        body.put("orderName", "Existing Order");
        
        Orders existingOrder = new Orders();
        existingOrder.setId(2L);
        existingOrder.setUserId(1L);
        existingOrder.setOrderName("Existing Order");
        existingOrder.setIsExported(false);
        
        when(ordersRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(ordersRepository.findByOrderNameIgnoreCase("Existing Order")).thenReturn(Collections.singletonList(existingOrder));
        
        assertThrows(BadRequestException.class, () -> {
            ordersService.editOrder(1L, body);
        });
    }
}