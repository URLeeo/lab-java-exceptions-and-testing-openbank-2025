package org.example.springbrestapi_lab.controller;

import jakarta.validation.Valid;
import org.example.springbrestapi_lab.exception.MissingApiKeyException;
import org.example.springbrestapi_lab.model.Product;
import org.example.springbrestapi_lab.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private static final String API_KEY = "123456";
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    private void validateApiKey(String apiKey) {
        if (apiKey == null || !apiKey.equals(API_KEY)) {
            throw new MissingApiKeyException("Missing or invalid API-Key header");
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product createProduct(
            @RequestHeader(value = "API-Key", required = false) String apiKey,
            @Valid @RequestBody Product product) {

        validateApiKey(apiKey);
        return productService.addProduct(product);
    }

    @GetMapping
    public List<Product> getAllProducts(
            @RequestHeader(value = "API-Key", required = false) String apiKey) {

        validateApiKey(apiKey);
        return productService.getAllProducts();
    }

    @GetMapping("/name/{name}")
    public Product getProductByName(
            @RequestHeader(value = "API-Key", required = false) String apiKey,
            @PathVariable String name) {

        validateApiKey(apiKey);
        return productService.getProductByName(name);
    }

    @PutMapping("/name/{name}")
    public Product updateProduct(
            @RequestHeader(value = "API-Key", required = false) String apiKey,
            @PathVariable String name,
            @Valid @RequestBody Product product) {

        validateApiKey(apiKey);
        return productService.updateProduct(name, product);
    }

    @DeleteMapping("/name/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(
            @RequestHeader(value = "API-Key", required = false) String apiKey,
            @PathVariable String name) {

        validateApiKey(apiKey);
        productService.deleteProduct(name);
    }

    @GetMapping("/category/{category}")
    public List<Product> getProductsByCategory(
            @RequestHeader(value = "API-Key", required = false) String apiKey,
            @PathVariable String category) {

        validateApiKey(apiKey);
        return productService.getProductsByCategory(category);
    }

    @GetMapping("/price-range")
    public List<Product> getProductsByPriceRange(
            @RequestHeader(value = "API-Key", required = false) String apiKey,
            @RequestParam double min,
            @RequestParam double max) {

        validateApiKey(apiKey);
        return productService.getProductsByPriceRange(min, max);
    }
}