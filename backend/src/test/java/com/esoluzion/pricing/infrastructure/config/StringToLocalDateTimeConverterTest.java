package com.esoluzion.pricing.infrastructure.config;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

class StringToLocalDateTimeConverterTest {

    private final StringToLocalDateTimeConverter converter = new StringToLocalDateTimeConverter();

    @Test
    @DisplayName("Debe convertir fecha sin Z")
    void shouldConvertDateWithoutZ() {
        var result = converter.convert("2020-06-14T10:00:00");
        assertThat(result).isEqualTo(LocalDateTime.of(2020, 6, 14, 10, 0, 0));
    }

    @Test
    @DisplayName("Debe convertir fecha con Z mayúscula")
    void shouldConvertDateWithUpperCaseZ() {
        var result = converter.convert("2020-06-14T10:00:00Z");
        assertThat(result).isEqualTo(LocalDateTime.of(2020, 6, 14, 10, 0, 0));
    }

    @Test
    @DisplayName("Debe convertir fecha con z minúscula")
    void shouldConvertDateWithLowerCaseZ() {
        var result = converter.convert("2020-06-14T10:00:00z");
        assertThat(result).isEqualTo(LocalDateTime.of(2020, 6, 14, 10, 0, 0));
    }

    @Test
    @DisplayName("Debe lanzar excepción para formato inválido")
    void shouldThrowExceptionForInvalidFormat() {
        assertThatThrownBy(() -> converter.convert("not-a-date"))
            .isInstanceOf(java.time.format.DateTimeParseException.class);
    }
}
