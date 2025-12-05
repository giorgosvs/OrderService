package es.merkle.component.service;

import es.merkle.component.mapper.ProductMapper;
import es.merkle.component.model.Product;
import es.merkle.component.repository.ProductRepository;
import es.merkle.component.repository.entity.DbProduct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductMapper productMapper;

    public List<Product> getAllProducts() {
        List<DbProduct> entities = productRepository.findAll();

        // Convert DbProduct entities to DTO and Return
        return entities.stream()
                .map(productMapper::mapToProduct)
                .collect(Collectors.toList());
    }

    public Product getProductById(String id) {
        DbProduct entity = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));

        return productMapper.mapToProduct(entity);
    }
}
