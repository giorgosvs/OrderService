package es.merkle.component.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import es.merkle.component.model.Product;
import es.merkle.component.model.ProductStatus;
import es.merkle.component.repository.entity.DbProduct;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(source = "productStatus", target = "productStatus")
    Product mapToProduct(DbProduct dbProduct);

    default ProductStatus mapStatus(String status) {
        return ProductStatus.fromValue(status);
    }
}
