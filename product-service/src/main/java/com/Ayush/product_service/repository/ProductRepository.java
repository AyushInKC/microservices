package com.Ayush.product_service.repository;

import com.Ayush.product_service.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product,String> {

    void deleteByName(String name);

}
