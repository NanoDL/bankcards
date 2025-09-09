package com.example.bankcards.service;

import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.dto.CardUpdateDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.ForbiddenException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.MaskService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Random;

@Service
public class CardService {

    private CardRepository cardRepository;
    private UserRepository userRepository;
    private ModelMapper modelMapper;
    @Autowired
    public CardService(CardRepository cardRepository, UserRepository userRepository, ModelMapper modelMapper) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    public Page<CardResponseDto> getCards(Pageable pageable, User user) {
        Page<Card> page = null;
        if (user.getRole().equals(UserRole.ADMIN)) {
            page = cardRepository.findAll(pageable);
            return page.map(this::toCardResponseDto);
        } else {
            page = cardRepository.findByOwner(user, pageable);
            return page.map(this::toCardResponseDto);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public CardResponseDto createCard(Long ownerId) {
        LocalDate date = LocalDate.now().plusYears(10).with(TemporalAdjusters.lastDayOfMonth());
        User user = userRepository.findById(ownerId).orElseThrow(() -> new UserNotFoundException("User not found"));

        Random random = new Random();
        Card card = new Card();
        card.setPan(String.format("%04d-%04d-%04d-%04d", random.nextInt(10000), random.nextInt(10000), random.nextInt(10000), random.nextInt(10000)));
        card.setExpirationDate(date);
        card.setBalance(0);
        card.setStatus(CardStatus.ACTIVE);
        card.setOwner(user);
        cardRepository.save(card);
        return toCardResponseDto(card);
    }

    public Integer getBalance(Long id, User user) {
        Card card = cardRepository.findById(id).orElseThrow(() -> new UserNotFoundException("Card not found"));
        if (card.getStatus().equals(CardStatus.EXPIRED) || card.getStatus().equals(CardStatus.BLOCKED)) {
            throw new BadRequestException("Card is expired or blocked");
        }
        if (user.getId().equals(card.getOwner().getId())) {
            return card.getBalance();
        } else {
            throw new ForbiddenException("You don't have access to this card");
        }
    }

    @Transactional
    public void deleteCard(Long id) {
        Card card = cardRepository.findById(id).orElseThrow(() -> new CardNotFoundException("Card not found"));
        cardRepository.delete(card);
    }

    @Transactional
    public CardResponseDto blockCard(Long id, User user) {
        Card card = cardRepository.findById(id).orElseThrow(() -> new CardNotFoundException("Card not found"));

        if (user.getId().equals(card.getOwner().getId()) || user.getRole().equals(UserRole.ADMIN)) {
            card.setStatus(CardStatus.BLOCKED);
            cardRepository.save(card);
            return toCardResponseDto(card);
        } else {
            throw new ForbiddenException("You don't have access to this card");
        }


    }

    @Transactional
    public CardResponseDto activateCard(Long id) {
        Card card = cardRepository.findById(id).orElseThrow(() -> new CardNotFoundException("Card not found"));
        if (card.getStatus().equals(CardStatus.BLOCKED)) {
            card.setStatus(CardStatus.ACTIVE);
            cardRepository.save(card);
            return toCardResponseDto(card);
        } else {
            throw new BadRequestException("Card is already active");
        }

    }

    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void updateExpiredCardStatuses(){
        System.out.println("Запуск задачи по обновлению статусов просроченных карт...");

        List<Card> expiredCards = cardRepository.findAllByStatusAndExpirationDateBefore(CardStatus.ACTIVE, LocalDate.now());

        for (Card card : expiredCards) {
            card.setStatus(CardStatus.EXPIRED);
        }

        cardRepository.saveAll(expiredCards);

        System.out.println("Обновлено статусов: " + expiredCards.size());
    }

    private CardResponseDto toCardResponseDto(Card card) {
        CardResponseDto cardResponseDto = modelMapper.map(card, CardResponseDto.class);
        cardResponseDto.setOwnerName(card.getOwner().getUsername());
        cardResponseDto.setPan(MaskService.MagicMask(card));
        return cardResponseDto;
    }

    @Transactional
    public CardResponseDto transferCard(Long idFrom, Long idTo, Integer amount, User user) {
        Card cardFrom = cardRepository.findById(idFrom).orElseThrow(() -> new CardNotFoundException("Card not found"));
        Card cardTo = cardRepository.findById(idTo).orElseThrow(() -> new CardNotFoundException("Card not found"));
        if (!user.getId().equals(cardFrom.getOwner().getId()) || !user.getId().equals(cardTo.getOwner().getId())) {
            throw new ForbiddenException("You don't have access to this card");
        }
        if (cardFrom.getStatus().equals(CardStatus.EXPIRED) || cardTo.getStatus().equals(CardStatus.EXPIRED)) {
            throw new BadRequestException("Card is expired");
        }
        if (cardFrom.getStatus().equals(CardStatus.BLOCKED) || cardTo.getStatus().equals(CardStatus.BLOCKED)) {
            throw new BadRequestException("Card is blocked");
        }
        if (cardFrom.getBalance() < amount) {
            throw new BadRequestException("Not enough balance");
        }
        if (amount < 0) {
            throw new BadRequestException("Amount must be positive");
        }
        cardFrom.setBalance(cardFrom.getBalance() - amount);
        cardTo.setBalance(cardTo.getBalance() + amount);
        cardRepository.save(cardFrom);
        cardRepository.save(cardTo);
        return toCardResponseDto(cardTo);
    }
    @Transactional
    public CardResponseDto updateCard(Long id, CardUpdateDto cardUpdateDto) {
        Card card = cardRepository.findById(id).orElseThrow(() -> new CardNotFoundException("Card not found"));
        card.setOwner(cardUpdateDto.getOwner());
        card.setStatus(cardUpdateDto.getStatus());
        cardRepository.save(card);
        return toCardResponseDto(card);
    }
}
