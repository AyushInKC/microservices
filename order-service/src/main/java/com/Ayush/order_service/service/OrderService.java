package com.Ayush.order_service.service;

import com.Ayush.order_service.dto.OrderLineItemsDto;
import com.Ayush.order_service.dto.OrderRequest;
import com.Ayush.order_service.model.InventoryResponse;
import com.Ayush.order_service.model.Order;
import com.Ayush.order_service.model.OrderLineItems;
import com.Ayush.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
   private final OrderRepository orderRepository;
   private final WebClient webClient;
    public void placeOrder(OrderRequest orderRequest){
        Order order=new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        List<OrderLineItems> orderLineItems= orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .toList();

        order.setOrderLineItemsList(orderLineItems);

        String skuCodes = String.valueOf(order.getOrderLineItemsList()
                .stream()
                .map(OrderLineItems::getSkuCode)
                .toList());


        //Checking for the items in inventory if yes then place the order using WebClient for synchronous communication
        InventoryResponse[] inventoryResponseArray=webClient.get()
                .uri("http://localhost:8082/api/inventory",
                        uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();
        //--> block() used for to make WebFlux synchronous

        assert inventoryResponseArray != null;
        boolean allProductsInStock=Arrays.stream(inventoryResponseArray).allMatch(InventoryResponse::isInStock);

    if(allProductsInStock) {
        orderRepository.save(order);
    }
    else {
        throw new IllegalArgumentException("Product is not in stock! Try again later.");
    }
    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
     OrderLineItems orderLineItems=new OrderLineItems();
     orderLineItems.setPrice(orderLineItems.getPrice());
     orderLineItems.setQuantity(orderLineItems.getQuantity());
     orderLineItems.setSkuCode(orderLineItems.getSkuCode());
     return orderLineItems;
    }
}
