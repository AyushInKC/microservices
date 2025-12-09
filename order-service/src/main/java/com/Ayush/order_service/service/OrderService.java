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
   private final WebClient.Builder webClientBuilder;
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
        InventoryResponse[] inventoryResponseArray = webClientBuilder.build()
                .get()
                .uri("http://inventory-service/api/inventory",
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

    private OrderLineItems mapToDto(OrderLineItemsDto dto) {
        OrderLineItems oli = new OrderLineItems();
        oli.setPrice(dto.getPrice());
        oli.setQuantity(dto.getQuantity());
        oli.setSkuCode(dto.getSkuCode());
        return oli;
    }
}
