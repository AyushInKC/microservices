package com.Ayush.inventory_service;

import com.Ayush.inventory_service.model.Inventory;
import com.Ayush.inventory_service.repository.InventoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class InventoryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InventoryServiceApplication.class, args);
	}
    @Bean

	public CommandLineRunner loadData(InventoryRepository inventoryRepository){
        return args -> {
			Inventory inventory= new Inventory();
           inventory.setSkuCode("iphone_13");
		   inventory.setQuantity(100);

			Inventory inventory1= new Inventory();
			inventory.setSkuCode("iphone_13");
			inventory.setQuantity(0);
			inventoryRepository.save(inventory1);
			inventoryRepository.save(inventory);

		};
	}
}
