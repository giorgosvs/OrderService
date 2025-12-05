package es.merkle.component.model;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum ProductStatus {
    AVAILABLE,
    NOT_AVAILABLE("NOT AVAILABLE"),
    VIP;

    private String value;

    @Override
    public String toString() {
        return Optional.ofNullable(getValue()).orElse(name());
    }

    public static ProductStatus fromValue(String value) {
        for (ProductStatus status : values()) {
            if (status.toString().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("No enum constant for value: " + value);
    }
}
