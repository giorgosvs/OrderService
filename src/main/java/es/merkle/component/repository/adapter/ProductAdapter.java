package es.merkle.component.repository.adapter;

import es.merkle.component.mapper.ProductMapper;
import es.merkle.component.repository.ProductRepository;
import es.merkle.component.repository.entity.DbProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductAdapter {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductMapper productMapper;

    public DbProduct getReqProductById (String id) {
        return productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
    }
}
