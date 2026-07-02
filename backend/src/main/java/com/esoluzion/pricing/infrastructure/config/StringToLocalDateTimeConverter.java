package com.esoluzion.pricing.infrastructure.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class StringToLocalDateTimeConverter implements Converter<String, LocalDateTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public LocalDateTime convert(String source) {
        String sanitized = source.endsWith("Z") || source.endsWith("z")
            ? source.substring(0, source.length() - 1)
            : source;
        return LocalDateTime.parse(sanitized, FORMATTER);
    }
}
