// package be.uliege.speam.team03.MDTools.controllers;

// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.anyLong;
// import static org.mockito.Mockito.when;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.times;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

// import java.sql.Timestamp;
// import java.util.Arrays;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.http.MediaType;
// import org.springframework.test.web.servlet.MockMvc;

// import com.fasterxml.jackson.databind.ObjectMapper;

// import be.uliege.speam.team03.MDTools.DTOs.OrderItemDTO;
// import be.uliege.speam.team03.MDTools.DTOs.OrdersDTO;
// import be.uliege.speam.team03.MDTools.compositeKeys.OrderItemsKey;
// import be.uliege.speam.team03.MDTools.exception.BadRequestException;
// import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
// import be.uliege.speam.team03.MDTools.mapper.OrderItemMapper;
// import be.uliege.speam.team03.MDTools.models.Category;
// import be.uliege.speam.team03.MDTools.models.Group;
// import be.uliege.speam.team03.MDTools.models.Instruments;
// import be.uliege.speam.team03.MDTools.models.OrderItems;
// import be.uliege.speam.team03.MDTools.models.Orders;
// import be.uliege.speam.team03.MDTools.models.SubGroup;
// import be.uliege.speam.team03.MDTools.models.Supplier;
// import be.uliege.speam.team03.MDTools.models.User;
// import be.uliege.speam.team03.MDTools.services.OrdersService;

// @WebMvcTest(OrdersController.class)
// public class OrdersControllerTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @MockBean
//     private OrdersService ordersService;

//     @Autowired
//     private ObjectMapper objectMapper;

//     private User testUser;
//     private Orders testOrder;
//     private Instruments testInstrument;
//     private OrderItems testOrderItem;
//     private OrderItemsKey testOrderItemsKey;
//     private Category testCategory;
//     private Group testGroup;
//     private SubGroup testSubGroup;
//     private Supplier testSupplier;
//     private OrderItemDTO testOrderItemDTO;
    
//     @BeforeEach
//     void setUp() {
//         testUser = new User();
//         testUser.setUserId(1L);
        
//         testOrder = new Orders();
//         testOrder.setId(1L);
//         testOrder.setUserId(1L);
//         testOrder.setOrderName("Test Order");
//         testOrder.setOrderDate(new Timestamp(System.currentTimeMillis()));

//         testGroup = new Group();
//         testGroup.setId(1L);
//         testGroup.setName("Test Group");
        
//         testSubGroup = new SubGroup();
//         testSubGroup.setId(1L);
//         testSubGroup.setName("Test SubGroup");
//         testSubGroup.setGroup(testGroup);

//         testCategory = new Category();
//         testCategory.setId(1L);
//         testCategory.setSubGroup(testSubGroup);

//         testSupplier = new Supplier();
//         testSupplier.setId(1L);
//         testSupplier.setSupplierName("Test Supplier");
        
//         testInstrument = new Instruments();
//         testInstrument.setId(1L);
//         testInstrument.setReference("Test Reference");
//         testInstrument.setCategory(testCategory);
//         testInstrument.setSupplier(testSupplier);
        
//         testOrderItemsKey = new OrderItemsKey(1L, 1L);
        
//         testOrderItem = new OrderItems(testOrder, testInstrument, 2);
//         testOrderItem.setId(testOrderItemsKey);

//         testOrderItemDTO = new OrderItemDTO();
//          testOrderItemDTO.setOrderId(1L);
//          testOrderItemDTO.setUserId(1L);
//          testOrderItemDTO.setInstrumentId(1L);
//          testOrderItemDTO.setOrderName("Test Order");
//          testOrderItemDTO.setReference("Test Reference");
//          testOrderItemDTO.setSupplier("Test Supplier");
//          // testOrderItemDTO.setCategory(testCategory);
//          testOrderItemDTO.setQuantity(2);
//          testOrderItemDTO.setPrice(10.0f);
//          testOrderItemDTO.setTotalPrice(20.0f);
//     }

//     @Test
//     void getOrdersIdOfUser_ShouldReturnOrdersList() throws Exception {
//         OrdersDTO orderDTO = new OrdersDTO(testOrder.getId(), testOrder.getOrderName());
//         List<OrdersDTO> ordersList = Arrays.asList(orderDTO);
        
//         when(ordersService.findOrdersOfUser(1L)).thenReturn(ordersList);

//         mockMvc.perform(get("/api/orders/user/1"))
//             .andExpect(status().isOk())
//             .andExpect(jsonPath("$[0].id").value(1))
//             .andExpect(jsonPath("$[0].orderName").value("Test Order"));
            
//         verify(ordersService, times(1)).findOrdersOfUser(1L);
//     }
    
//     @Test
//     void getOrdersIdOfUser_UserNotFound_ShouldThrowException() throws Exception {
//         when(ordersService.findOrdersOfUser(999L)).thenThrow(new ResourceNotFoundException("User not found."));

//         mockMvc.perform(get("/api/orders/user/999"))
//             .andExpect(status().isNotFound());
            
//         verify(ordersService, times(1)).findOrdersOfUser(999L);
//     }
    
//     @Test
//     void getInstrumentsOfOrder_ShouldReturnInstrumentsList() throws Exception {
//         OrderItemDTO orderItemDTO = new OrderItemDTO();
//          orderItemDTO.setInstrumentId(1L);
        
//         List<OrderItemDTO> orderItemsList = Arrays.asList(orderItemDTO);
        
//         when(ordersService.findInstrumentsOfOrder(1L)).thenReturn(orderItemsList);

//         mockMvc.perform(get("/api/orders/1"))
//             .andExpect(status().isOk())
//             .andExpect(jsonPath("$[0].instrumentId").value(1))
//             .andExpect(jsonPath("$[0].orderName").value("Test Order"));
            
//         verify(ordersService, times(1)).findInstrumentsOfOrder(1L);
//     }
    
//     @Test
//     void getInstrumentsOfOrder_OrderNotFound_ShouldThrowException() throws Exception {
//         when(ordersService.findInstrumentsOfOrder(999L)).thenThrow(new ResourceNotFoundException("Order not found."));

//         mockMvc.perform(get("/api/orders/999"))
//             .andExpect(status().isNotFound());
            
//         verify(ordersService, times(1)).findInstrumentsOfOrder(999L);
//     }
    
//     @Test
//     void addInstrToOrder_ShouldReturnUpdatedOrder() throws Exception {
//         Map<String, Object> requestBody = new HashMap<>();
//         requestBody.put("orderId", 1L);
//         requestBody.put("userId", 1L);
//         requestBody.put("instrId", 1L);
//         requestBody.put("quantity", 2);
        
//         OrderItemDTO orderItemDTO = new OrderItemDTO();
//         orderItemDTO.setInstrumentId(1L);
//         orderItemDTO.setQuantity(2);
        
//         List<OrderItemDTO> orderItemsList = Arrays.asList(orderItemDTO);
        
//         when(ordersService.addInstrumentToOrder(any())).thenReturn(orderItemsList);

//         mockMvc.perform(post("/api/orders/add-instrument")
//             .contentType(MediaType.APPLICATION_JSON)
//             .content(objectMapper.writeValueAsString(requestBody)))
//             .andExpect(status().isOk())
//             .andExpect(jsonPath("$[0].instrumentId").value(1))
//             .andExpect(jsonPath("$[0].quantity").value(2));
            
//         verify(ordersService, times(1)).addInstrumentToOrder(any());
//     }
    
//     @Test
//     void addInstrToOrder_ResourceNotFound_ShouldThrowException() throws Exception {
//         Map<String, Object> requestBody = new HashMap<>();
//         requestBody.put("orderId", 999L);
//         requestBody.put("userId", 1L);
//         requestBody.put("instrId", 1L);
//         requestBody.put("quantity", 2);
        
//         when(ordersService.addInstrumentToOrder(any())).thenThrow(new ResourceNotFoundException("User, order or instrument not found."));

//         mockMvc.perform(post("/api/orders/add-instrument")
//             .contentType(MediaType.APPLICATION_JSON)
//             .content(objectMapper.writeValueAsString(requestBody)))
//             .andExpect(status().isNotFound());
            
//         verify(ordersService, times(1)).addInstrumentToOrder(any());
//     }
    
//     @Test
//     void removeInstrFromOrder_ShouldReturnUpdatedOrder() throws Exception {
//         OrderItemDTO orderItemDTO = new OrderItemDTO();
//         orderItemDTO.setInstrumentId(2L);
//         orderItemDTO.setQuantity(3);
        
//         List<OrderItemDTO> orderItemsList = Arrays.asList(orderItemDTO);
        
//         when(ordersService.removeInstrumentFromOrder(1L, 1L, 1L)).thenReturn(orderItemsList);

//         mockMvc.perform(delete("/api/orders/1/user/1/remove-instrument/1"))
//             .andExpect(status().isOk())
//             .andExpect(jsonPath("$[0].instrumentId").value(2))
//             .andExpect(jsonPath("$[0].quantity").value(3));
            
//         verify(ordersService, times(1)).removeInstrumentFromOrder(1L, 1L, 1L);
//     }
    
//     @Test
//     void removeInstrFromOrder_ResourceNotFound_ShouldThrowException() throws Exception {
//         when(ordersService.removeInstrumentFromOrder(1L, 1L, 999L)).thenThrow(new ResourceNotFoundException("Instrument not found in order."));

//         mockMvc.perform(delete("/api/orders/1/user/1/remove-instrument/999"))
//             .andExpect(status().isNotFound());
            
//         verify(ordersService, times(1)).removeInstrumentFromOrder(1L, 1L, 999L);
//     }
    
//     @Test
//     void createOrder_ShouldReturnOrdersList() throws Exception {
//         Map<String, Object> requestBody = new HashMap<>();
//         requestBody.put("userId", 1L);
//         requestBody.put("orderName", "New Test Order");
        
//         OrdersDTO orderDTO = new OrdersDTO(2L, "New Test Order");
//         List<OrdersDTO> ordersList = Arrays.asList(orderDTO);
        
//         when(ordersService.createNewOrder(any())).thenReturn(ordersList);

//         mockMvc.perform(post("/api/orders")
//             .contentType(MediaType.APPLICATION_JSON)
//             .content(objectMapper.writeValueAsString(requestBody)))
//             .andExpect(status().isOk())
//             .andExpect(jsonPath("$[0].id").value(2))
//             .andExpect(jsonPath("$[0].orderName").value("New Test Order"));
            
//         verify(ordersService, times(1)).createNewOrder(any());
//     }
    
//     @Test
//     void createOrder_UserNotFound_ShouldThrowException() throws Exception {
//         Map<String, Object> requestBody = new HashMap<>();
//         requestBody.put("userId", 999L);
//         requestBody.put("orderName", "New Test Order");
        
//         when(ordersService.createNewOrder(any())).thenThrow(new ResourceNotFoundException("User doesn't exist."));

//         mockMvc.perform(post("/api/orders")
//             .contentType(MediaType.APPLICATION_JSON)
//             .content(objectMapper.writeValueAsString(requestBody)))
//             .andExpect(status().isNotFound());
            
//         verify(ordersService, times(1)).createNewOrder(any());
//     }
    
//     @Test
//     void createOrder_DuplicateOrderName_ShouldThrowException() throws Exception {
//         Map<String, Object> requestBody = new HashMap<>();
//         requestBody.put("userId", 1L);
//         requestBody.put("orderName", "Test Order");
        
//         when(ordersService.createNewOrder(any())).thenThrow(new BadRequestException("Order with that name already exists for that user"));

//         mockMvc.perform(post("/api/orders")
//             .contentType(MediaType.APPLICATION_JSON)
//             .content(objectMapper.writeValueAsString(requestBody)))
//             .andExpect(status().isBadRequest());
            
//         verify(ordersService, times(1)).createNewOrder(any());
//     }
    
//     @Test
//     void deleteOrder_ShouldReturnNoContent() throws Exception {
//         when(ordersService.removeOrder(1L)).thenReturn(true);

//         mockMvc.perform(delete("/api/orders/1"))
//             .andExpect(status().isNoContent());
            
//         verify(ordersService, times(1)).removeOrder(1L);
//     }
    
//     @Test
//     void deleteOrder_OrderNotFound_ShouldThrowException() throws Exception {
//         when(ordersService.removeOrder(999L)).thenThrow(new ResourceNotFoundException("Order not found."));

//         mockMvc.perform(delete("/api/orders/999"))
//             .andExpect(status().isNotFound());
            
//         verify(ordersService, times(1)).removeOrder(999L);
//     }
    
//     @Test
//     void updateOrder_ShouldReturnUpdatedOrder() throws Exception {
//         Map<String, Object> requestBody = new HashMap<>();
//         requestBody.put("orderName", "Updated Order Name");
        
//         OrderItemDTO orderItemDTO = new OrderItemDTO();
//         orderItemDTO.setInstrumentId(1L);
//         orderItemDTO.setQuantity(2);
        
//         List<OrderItemDTO> orderItemsList = Arrays.asList(orderItemDTO);
        
//         when(ordersService.editOrder(anyLong(), any())).thenReturn(orderItemsList);

//         mockMvc.perform(patch("/api/orders/1")
//             .contentType(MediaType.APPLICATION_JSON)
//             .content(objectMapper.writeValueAsString(requestBody)))
//             .andExpect(status().isOk())
//             .andExpect(jsonPath("$[0].instrumentId").value(1))
//             .andExpect(jsonPath("$[0].quantity").value(2));
            
//         verify(ordersService, times(1)).editOrder(anyLong(), any());
//     }
    
//     @Test
//     void updateOrder_OrderNotFound_ShouldThrowException() throws Exception {
//         Map<String, Object> requestBody = new HashMap<>();
//         requestBody.put("orderName", "Updated Order Name");
        
//         when(ordersService.editOrder(anyLong(), any())).thenThrow(new ResourceNotFoundException("Order not found."));

//         mockMvc.perform(patch("/api/orders/999")
//             .contentType(MediaType.APPLICATION_JSON)
//             .content(objectMapper.writeValueAsString(requestBody)))
//             .andExpect(status().isNotFound());
            
//         verify(ordersService, times(1)).editOrder(anyLong(), any());
//     }
    
//     @Test
//     void updateOrder_DuplicateOrderName_ShouldThrowException() throws Exception {
//         Map<String, Object> requestBody = new HashMap<>();
//         requestBody.put("orderName", "Existing Order Name");
        
//         when(ordersService.editOrder(anyLong(), any())).thenThrow(new BadRequestException("Order with that name already exists for that user"));

//         mockMvc.perform(patch("/api/orders/1")
//             .contentType(MediaType.APPLICATION_JSON)
//             .content(objectMapper.writeValueAsString(requestBody)))
//             .andExpect(status().isBadRequest());
            
//         verify(ordersService, times(1)).editOrder(anyLong(), any());
//     }
    
//     @Test
//     void updateOrder_MissingName_ShouldThrowException() throws Exception {
//         Map<String, Object> requestBody = new HashMap<>();
//         // No orderName provided
        
//         when(ordersService.editOrder(anyLong(), any())).thenThrow(new ResourceNotFoundException("Name is required"));

//         mockMvc.perform(patch("/api/orders/1")
//             .contentType(MediaType.APPLICATION_JSON)
//             .content(objectMapper.writeValueAsString(requestBody)))
//             .andExpect(status().isNotFound());
            
//         verify(ordersService, times(1)).editOrder(anyLong(), any());
//     }
// }