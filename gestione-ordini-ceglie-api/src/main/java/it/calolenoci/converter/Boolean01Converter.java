package it.calolenoci.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class Boolean01Converter implements AttributeConverter<Boolean, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Boolean value) {
        if (value == null) return null;
        return value ? 1 : 0;
    }

    @Override
    public Boolean convertToEntityAttribute(Integer value) {
        if (value == null) return null;
        return value == 1;
    }
}
