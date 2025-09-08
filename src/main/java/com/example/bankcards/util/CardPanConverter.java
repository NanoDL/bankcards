package com.example.bankcards.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Converter
@Component
public class CardPanConverter implements AttributeConverter<String, String> {

    private final CardEncryptionService cardEncryptionService;

    @Autowired
    public CardPanConverter(CardEncryptionService cardEncryptionService) {
        this.cardEncryptionService = cardEncryptionService;
    }


    @Override
    public String convertToDatabaseColumn(String attribute) {
        return attribute == null ? null : cardEncryptionService.encryptCardNumber(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return dbData == null ? null : cardEncryptionService.decryptCardNumber(dbData);
    }
}
