package org.example.springbrestapi_lab;

import org.example.springbrestapi_lab.controller.ProductController;
import org.example.springbrestapi_lab.exception.InvalidPriceRangeException;
import org.example.springbrestapi_lab.exception.ResourceNotFoundException;
import org.example.springbrestapi_lab.model.Product;
import org.example.springbrestapi_lab.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    private static final String API_KEY_HEADER = "API-Key";
    private static final String API_KEY_VALUE = "123456";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    private Product createProduct(String name, double price, String category, int quantity) {
        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setCategory(category);
        product.setQuantity(quantity);
        return product;
    }

    @Test
    void addProduct_ShouldAddAndReturnProduct() throws Exception {
        Product product = createProduct("Laptop", 1500.0, "Electronics", 5);

        given(productService.addProduct(any(Product.class))).willReturn(product);

        mockMvc.perform(post("/products")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.price").value(1500.0))
                .andExpect(jsonPath("$.category").value("Electronics"))
                .andExpect(jsonPath("$.quantity").value(5));
    }

    @Test
    void getAllProducts_ShouldReturnAllProducts() throws Exception {
        List<Product> products = List.of(
                createProduct("Laptop", 1500.0, "Electronics", 5),
                createProduct("Phone", 900.0, "Electronics", 10)
        );

        given(productService.getAllProducts()).willReturn(products);

        mockMvc.perform(get("/products")
                        .header(API_KEY_HEADER, API_KEY_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Laptop"))
                .andExpect(jsonPath("$[1].name").value("Phone"));
    }

    @Test
    void getProductByName_ShouldReturnProduct_WhenNameExists() throws Exception {
        Product product = createProduct("Laptop", 1500.0, "Electronics", 5);

        given(productService.getProductByName("Laptop")).willReturn(product);

        mockMvc.perform(get("/products/name/Laptop")
                        .header(API_KEY_HEADER, API_KEY_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.price").value(1500.0));
    }

    @Test
    void getProductByName_ShouldThrowException_WhenProductDoesNotExist() throws Exception {
        given(productService.getProductByName("Tablet"))
                .willThrow(new ResourceNotFoundException("Product not found"));

        mockMvc.perform(get("/products/name/Tablet")
                        .header(API_KEY_HEADER, API_KEY_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateProduct_ShouldUpdateAndReturnProduct_WhenProductExists() throws Exception {
        Product updatedProduct = createProduct("Gaming Laptop", 2000.0, "Gaming", 3);

        given(productService.updateProduct(eq("Laptop"), any(Product.class)))
                .willReturn(updatedProduct);

        mockMvc.perform(put("/products/name/Laptop")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Gaming Laptop"))
                .andExpect(jsonPath("$.price").value(2000.0))
                .andExpect(jsonPath("$.category").value("Gaming"))
                .andExpect(jsonPath("$.quantity").value(3));
    }

    @Test
    void updateProduct_ShouldThrowException_WhenProductDoesNotExist() throws Exception {
        Product updatedProduct = createProduct("Tablet", 500.0, "Electronics", 7);

        given(productService.updateProduct(eq("Missing"), any(Product.class)))
                .willThrow(new ResourceNotFoundException("Product not found"));

        mockMvc.perform(put("/products/name/Missing")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedProduct)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProduct_ShouldRemoveProduct_WhenProductExists() throws Exception {
        willDoNothing().given(productService).deleteProduct("Laptop");

        mockMvc.perform(delete("/products/name/Laptop")
                        .header(API_KEY_HEADER, API_KEY_VALUE))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteProduct_ShouldThrowException_WhenProductDoesNotExist() throws Exception {
        willThrow(new ResourceNotFoundException("Product not found"))
                .given(productService).deleteProduct("Missing");

        mockMvc.perform(delete("/products/name/Missing")
                        .header(API_KEY_HEADER, API_KEY_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProductsByCategory_ShouldReturnMatchingProducts() throws Exception {
        List<Product> products = List.of(
                createProduct("Laptop", 1500.0, "Electronics", 5),
                createProduct("Phone", 900.0, "Electronics", 10)
        );

        given(productService.getProductsByCategory("Electronics")).willReturn(products);

        mockMvc.perform(get("/products/category/Electronics")
                        .header(API_KEY_HEADER, API_KEY_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].category").value("Electronics"))
                .andExpect(jsonPath("$[1].category").value("Electronics"));
    }

    @Test
    void getProductsByCategory_ShouldThrowException_WhenNoProductsFound() throws Exception {
        given(productService.getProductsByCategory("Electronics"))
                .willThrow(new ResourceNotFoundException("No products found in this category"));

        mockMvc.perform(get("/products/category/Electronics")
                        .header(API_KEY_HEADER, API_KEY_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProductsByPriceRange_ShouldReturnMatchingProducts() throws Exception {
        List<Product> products = List.of(
                createProduct("Laptop", 1500.0, "Electronics", 5),
                createProduct("Phone", 900.0, "Electronics", 10)
        );

        given(productService.getProductsByPriceRange(100.0, 1600.0)).willReturn(products);

        mockMvc.perform(get("/products/price-range")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .param("min", "100.0")
                        .param("max", "1600.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getProductsByPriceRange_ShouldThrowInvalidPriceRangeException_WhenMinGreaterThanMax() throws Exception {
        given(productService.getProductsByPriceRange(500.0, 100.0))
                .willThrow(new InvalidPriceRangeException("Minimum price cannot be greater than maximum price"));

        mockMvc.perform(get("/products/price-range")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .param("min", "500.0")
                        .param("max", "100.0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getProductsByPriceRange_ShouldThrowInvalidPriceRangeException_WhenMinIsNegative() throws Exception {
        given(productService.getProductsByPriceRange(-10.0, 100.0))
                .willThrow(new InvalidPriceRangeException("Prices cannot be negative"));

        mockMvc.perform(get("/products/price-range")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .param("min", "-10.0")
                        .param("max", "100.0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getProductsByPriceRange_ShouldThrowInvalidPriceRangeException_WhenMaxIsNegative() throws Exception {
        given(productService.getProductsByPriceRange(10.0, -100.0))
                .willThrow(new InvalidPriceRangeException("Prices cannot be negative"));

        mockMvc.perform(get("/products/price-range")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .param("min", "10.0")
                        .param("max", "-100.0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getProductsByPriceRange_ShouldThrowResourceNotFoundException_WhenNoProductsFound() throws Exception {
        given(productService.getProductsByPriceRange(100.0, 200.0))
                .willThrow(new ResourceNotFoundException("No products found in this price range"));

        mockMvc.perform(get("/products/price-range")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .param("min", "100.0")
                        .param("max", "200.0"))
                .andExpect(status().isNotFound());
    }
}