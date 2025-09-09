package com.example.bankcards.service;

import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.ForbiddenException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CardService cardService;

    private User adminUser;
    private User regularUser;
    private Card card;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setRole(UserRole.ADMIN);

        regularUser = new User();
        regularUser.setId(2L);
        regularUser.setRole(UserRole.USER);

        card = new Card();
        card.setId(1L);
        card.setOwner(regularUser);
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(1000);
    }

    @Test
    void getCards_AsAdmin_ReturnsAllCards() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> cardPage = new PageImpl<>(Collections.singletonList(card));
        when(cardRepository.findAll(pageable)).thenReturn(cardPage);
        when(modelMapper.map(any(), any())).thenReturn(new CardResponseDto());


        Page<CardResponseDto> result = cardService.getCards(pageable, adminUser);


        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(cardRepository).findAll(pageable);
    }

    @Test
    void getCards_AsUser_ReturnsOwnCards() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> cardPage = new PageImpl<>(Collections.singletonList(card));
        when(cardRepository.findByOwner(regularUser, pageable)).thenReturn(cardPage);
        when(modelMapper.map(any(), any())).thenReturn(new CardResponseDto());

        Page<CardResponseDto> result = cardService.getCards(pageable, regularUser);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(cardRepository).findByOwner(regularUser, pageable);
    }

    @Test
    void createCard_Successfully() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(regularUser));
        when(cardRepository.save(any(Card.class))).thenReturn(card);
        when(modelMapper.map(any(), any())).thenReturn(new CardResponseDto());

        CardResponseDto result = cardService.createCard(2L);

        assertNotNull(result);
        verify(userRepository).findById(2L);
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void getBalance_Successfully() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        Integer balance = cardService.getBalance(1L, regularUser);

        assertEquals(1000, balance);
    }

    @Test
    void getBalance_CardBlocked_ThrowsBadRequest() {
        card.setStatus(CardStatus.BLOCKED);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        assertThrows(BadRequestException.class, () -> cardService.getBalance(1L, regularUser));
    }

    @Test
    void getBalance_Forbidden() {
        User anotherUser = new User();
        anotherUser.setId(3L);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        assertThrows(ForbiddenException.class, () -> cardService.getBalance(1L, anotherUser));
    }

    @Test
    void transferCard_Successfully() {
        Card cardTo = new Card();
        cardTo.setId(2L);
        cardTo.setOwner(regularUser);
        cardTo.setStatus(CardStatus.ACTIVE);
        cardTo.setBalance(500);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(cardTo));
        when(modelMapper.map(any(), any())).thenReturn(new CardResponseDto());


        cardService.transferCard(1L, 2L, 200, regularUser);


        assertEquals(800, card.getBalance());
        assertEquals(700, cardTo.getBalance());
        verify(cardRepository, times(2)).save(any(Card.class));
    }

    @Test
    void transferCard_NotEnoughBalance_ThrowsBadRequest() {
        Card cardTo = new Card();
        cardTo.setId(2L);
        cardTo.setOwner(regularUser);
        cardTo.setStatus(CardStatus.ACTIVE);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(cardTo));

        assertThrows(BadRequestException.class, () -> cardService.transferCard(1L, 2L, 1200, regularUser));
    }
}
