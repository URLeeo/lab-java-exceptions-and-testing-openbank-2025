package org.example.springbrestapi_lab.service;

import org.example.springbrestapi_lab.exception.InvalidPriceRangeException;
import org.example.springbrestapi_lab.exception.ResourceNotFoundException;
import org.example.springbrestapi_lab.model.Product;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    private final List<Product> products = new ArrayList<>();

    public Product addProduct(Product product) {
        products.add(product);
        return product;
    }

    public List<Product> getAllProducts() {
        return products;
    }

    public Product getProductByName(String name) {
        return products.stream()
                .filter(product -> product.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with name: " + name));
    }

    public Product updateProduct(String name, Product updatedProduct) {
        Product existingProduct = getProductByName(name);

        existingProduct.setName(updatedProduct.getName());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setCategory(updatedProduct.getCategory());
        existingProduct.setQuantity(updatedProduct.getQuantity());

        return existingProduct;
    }

    public void deleteProduct(String name) {
        Product product = getProductByName(name);
        products.remove(product);
    }

    public List<Product> getProductsByCategory(String category) {
        List<Product> filteredProducts = products.stream()
                .filter(product -> product.getCategory().equalsIgnoreCase(category))
                .toList();

        if (filteredProducts.isEmpty()) {
            throw new ResourceNotFoundException("No products found in category: " + category);
        }

        return filteredProducts;
    }

    public List<Product> getProductsByPriceRange(double min, double max) {
        if (min > max || min < 0 || max < 0) {
            throw new InvalidPriceRangeException("Invalid price range: min must be less than or equal to max and both must be positive");
        }

        List<Product> filteredProducts = products.stream()
                .filter(product -> product.getPrice() >= min && product.getPrice() <= max)
                .toList();

        if (filteredProducts.isEmpty()) {
            throw new ResourceNotFoundException("No products found in price range: " + min + " - " + max);
        }

        return filteredProducts;
    }
}