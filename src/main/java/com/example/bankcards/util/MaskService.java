package com.example.bankcards.util;

import com.example.bankcards.entity.Card;
import org.springframework.stereotype.Service;


public class MaskService {

    public static String MagicMask(Card card) {
        String pan = card.getPan();
        String last4 = pan.substring(pan.length() - 4);
       // card.setPan("**** **** **** " + last4);
        return "**** **** **** " + last4;
    }

}
