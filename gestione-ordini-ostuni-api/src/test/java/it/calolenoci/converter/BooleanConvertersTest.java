package it.calolenoci.converter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BooleanConvertersTest {

    @Test
    void converteBooleaniNelFormatoZeroUno() {
        Boolean01Converter converter = new Boolean01Converter();

        assertNull(converter.convertToDatabaseColumn(null));
        assertEquals(1, converter.convertToDatabaseColumn(true));
        assertEquals(0, converter.convertToDatabaseColumn(false));
        assertNull(converter.convertToEntityAttribute(null));
        assertTrue(converter.convertToEntityAttribute(1));
        assertFalse(converter.convertToEntityAttribute(0));
        assertFalse(converter.convertToEntityAttribute(2));
    }

    @Test
    void converteBooleaniNelFormatoTrueFalse() {
        assertStringConverter(new TrueFalseConverter(), "T", "F");
    }

    @Test
    void converteBooleaniNelFormatoYesNo() {
        assertStringConverter(new YesNoConverter(), "Y", "N");
    }

    private void assertStringConverter(jakarta.persistence.AttributeConverter<Boolean, String> converter,
                                       String trueValue, String falseValue) {
        assertNull(converter.convertToDatabaseColumn(null));
        assertEquals(trueValue, converter.convertToDatabaseColumn(true));
        assertEquals(falseValue, converter.convertToDatabaseColumn(false));
        assertNull(converter.convertToEntityAttribute(null));
        assertTrue(converter.convertToEntityAttribute(trueValue));
        assertTrue(converter.convertToEntityAttribute(trueValue.toLowerCase()));
        assertFalse(converter.convertToEntityAttribute(falseValue));
        assertFalse(converter.convertToEntityAttribute("?"));
    }
}
