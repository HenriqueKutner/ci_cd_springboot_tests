package com.example.cashcard;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;


import java.net.URI;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/cashcards")
@Tag(name = "Cashcard", description = "Cashcard controller")
@SecurityRequirement(name = "basicAuth")
public class CashCardController {
    private CashCardRepository cashCardRepository;

    @Autowired
    private void CashCardRepository(CashCardRepository cashCardRepository) {
        this.cashCardRepository = cashCardRepository;
    }


    @GetMapping("/{requestedId}")
    private ResponseEntity<CashCard> findById(@PathVariable Long requestedId, Principal principal) {
        CashCard cashCard = cashCardRepository.findByIdAndOwner(requestedId, principal.getName());
        if (cashCard != null) {
            return ResponseEntity.ok(cashCard);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    private ResponseEntity<List<CashCard>> findAll() {
        List<CashCard> cashCards = cashCardRepository.findAll();
        return ResponseEntity.ok(cashCards);
    }



    @PostMapping
    @Operation(summary = "Add new cashcard")
    @ApiResponse(responseCode = "201", description = "New cashcard saved")
    @ApiResponse(responseCode = "500", description = "Erro")
    private ResponseEntity<Void> createCashCard(@RequestBody CashCard newCashCardRequest, UriComponentsBuilder ucb) {
        try {
            CashCard savedCashCard = cashCardRepository.save(newCashCardRequest);
            URI locationOfNewCashCard = ucb
                    .path("cashcards/{id}")
                    .buildAndExpand(savedCashCard.getId())
                    .toUri();
            return ResponseEntity.created(locationOfNewCashCard).build();
        } catch (Exception e) {
            System.out.println("Teste de erro");
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{requestedId}")
    private ResponseEntity<CashCard> putCashCard(@PathVariable Long requestedId, @RequestBody CashCard cashCardUpdate, Principal principal) {
        CashCard cashCard = cashCardRepository.findByIdAndOwner(requestedId, principal.getName());
        if (null != cashCard) {
            CashCard updatedCashCard = new CashCard(cashCard.getId(), cashCardUpdate.getAmount(), principal.getName());
            cashCardRepository.save(updatedCashCard);
            return ResponseEntity.ok(updatedCashCard);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    private ResponseEntity<Void> deleteCashCard(@PathVariable Long id, Principal principal) {
        if (!cashCardRepository.existsByIdAndOwner(id, principal.getName())) {
            return ResponseEntity.notFound().build();
        }
        cashCardRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
