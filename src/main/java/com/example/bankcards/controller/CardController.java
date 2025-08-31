package com.example.bankcards.controller;

import com.example.bankcards.dto.CardCreateDto;
import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.dto.CardUpdateDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private CardService cardService;
    private UserService userService;

    @Autowired
    public CardController(CardService cardService, UserService userService) {
        this.cardService = cardService;
        this.userService = userService;
    }

    //admin user+
    @GetMapping
    public Page<CardResponseDto> getCards(@RequestParam(required = false, defaultValue = "0") Integer page,
                                    @RequestParam(required = false, defaultValue = "10") Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        User user = userService.getUserFromContext();
        return cardService.getCards(pageable, user);
    }

    //user+
    @GetMapping("/{id}/balance")
    public Integer getBalance(@PathVariable Long id) {
        User user = userService.getUserFromContext();
        return cardService.getBalance(id, user);
    }

    //admin+
    @PostMapping
    public CardResponseDto createCard(@RequestParam Long ownerId) {
        return cardService.createCard(ownerId);
    }

    //admin user+
    @PatchMapping("/{id}/block")
    public CardResponseDto blockCard(@PathVariable Long id) {
        User user = userService.getUserFromContext();
        return cardService.blockCard(id, user);

    }
    //admin+
    @PatchMapping("/{id}/activate")
    public CardResponseDto activateCard(Long id) {
        return cardService.activateCard(id);
    }

    //admin+
    @DeleteMapping("/{id}")
    public void deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
    }

    //user+
    @PatchMapping("/from/{idFrom}/to/{idTo}")
    public CardResponseDto transferCard(@PathVariable Long idFrom, @PathVariable Long idTo, @RequestParam Integer amount) {
        User user = userService.getUserFromContext();
        return cardService.transferCard(idFrom, idTo, amount, user);
    }
    //admin+
    @PutMapping("/{id}")
    public CardResponseDto updateCard(@PathVariable Long id,
                                      @RequestBody CardUpdateDto cardUpdateDto) {
        return cardService.updateCard(id,cardUpdateDto);
    }
}
