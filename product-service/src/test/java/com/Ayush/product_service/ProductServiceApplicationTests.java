package com.Ayush.product_service;

import com.Ayush.product_service.dto.ProductRequest;
import com.Ayush.product_service.dto.ProductResponse;
import com.Ayush.product_service.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import java.math.BigDecimal;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class ProductServiceApplicationTests {

	@Container
	static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.2");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ProductRepository productRepository;

	static {
		mongoDBContainer.start();
	}

	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
	}

	@BeforeEach
	void cleanupTestProduct() {
		productRepository.deleteByName("iPhone 13");
		productRepository.deleteByName("iPhone 16");
	}

	@Test
	void shouldCreateProduct() throws Exception {
		ProductRequest productRequest = getProductRequest();
		String productRequestString = objectMapper.writeValueAsString(productRequest);

		mockMvc.perform(
						MockMvcRequestBuilders.post("/api/product")
								.contentType(MediaType.APPLICATION_JSON)
								.content(productRequestString))
				.andExpect(status().isCreated());

		Assertions.assertEquals(1, productRepository.findAll().size());
	}

	@Test
	void shouldReturnProducts() throws Exception {
		ProductResponse product = getProductResponse();

		productRepository.save(
				com.Ayush.product_service.model.Product.builder()
						.name(product.getName())
						.description(product.getDescription())
						.price(product.getPrice())
						.build()
		);

		String expectedJson = """
        [
            {
                "name": "iPhone 16",
                "description": "iPhone 13",
                "price": 1200
            }
        ]
        """;

		mockMvc.perform(MockMvcRequestBuilders.get("/api/product"))
				.andExpect(status().isOk())
				.andExpect(content().json(expectedJson, false));
	}

	private ProductResponse getProductResponse() {
		return ProductResponse.builder()
				.name("iPhone 16")
				.description("iPhone 13")
				.price(BigDecimal.valueOf(1200))
				.build();
	}

	private ProductRequest getProductRequest() {
		return ProductRequest.builder()
				.name("iPhone 13")
				.description("iPhone 13")
				.price(BigDecimal.valueOf(120000))
				.build();
	}
}
