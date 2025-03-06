package be.uliege.speam.team03.MDTools.mapper;

import be.uliege.speam.team03.MDTools.DTOs.CategoryDTO;
import be.uliege.speam.team03.MDTools.DTOs.OrderItemDTO;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.models.Category;
import be.uliege.speam.team03.MDTools.models.Instruments;
import be.uliege.speam.team03.MDTools.models.OrderItems;
import be.uliege.speam.team03.MDTools.models.Orders;
import be.uliege.speam.team03.MDTools.repositories.CategoryRepository;

public class OrderItemMapper {
    private CategoryRepository categoryRepository;

    public OrderItemMapper(CategoryRepository categoryRepository){
        this.categoryRepository = categoryRepository;
    }

    public OrderItemDTO mapToOrderItemDTO(OrderItems orderItem){
        Instruments instrument = orderItem.getInstrument();
        Orders order = orderItem.getOrder();
        if(instrument!=null && order!=null){
                Category category = instrument.getCategory();
                CategoryMapper categoryMapper = new CategoryMapper(categoryRepository);
                CategoryDTO categoryDTO = categoryMapper.mapToCategoryDto(category); 
                OrderItemDTO orderItemDTO = new OrderItemDTO(order.getId(), instrument.getId(),order.getOrderName(), instrument.getReference(), instrument.getSupplier().getSupplierName(), categoryDTO,  orderItem.getQuantity(), instrument.getPrice());
                orderItemDTO.setTotalPrice();
                return orderItemDTO;
        }
        else{
            throw new ResourceNotFoundException("Instrument not found.");
        }
    }
}
