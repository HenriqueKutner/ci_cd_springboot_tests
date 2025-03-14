package com.example.cashcard;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


//@SpringBootTest
//@AutoConfigureMockMvc
@WebMvcTest({CashCardController.class, SecurityConfig.class})
public class CashCardControllerTests {

    @MockitoBean
    CashCardRepository cashCardRepository;


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private CashCard cashCard;
    private List<CashCard> cashCardList;
    private Principal mockPrincipal;

    @BeforeEach
    void setUp() {
        cashCard = new CashCard(1L, 100.0, "sarah");
        cashCardList = Arrays.asList(
                new CashCard(1L, 100.0, "sarah"),
                new CashCard(2L, 200.0, "sarah")
        );


    }



    @Test
    @WithMockUser(username = "sarah", roles = {"CARD-OWNER"})
    void shouldReturnOkAndEmptyList() throws Exception {
        when(cashCardRepository.findAll()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/cashcards"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }


    @Test
    @WithMockUser(username = "sarah", roles = {"CARD-OWNER"})
    void shouldReturnAllCashCards() throws Exception {
        when(cashCardRepository.findAll()).thenReturn(cashCardList);

        mockMvc.perform(get("/cashcards"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(cashCardList)));
    }


    @Test
    @WithMockUser(username = "sarah", roles = {"CARD-OWNER"})
    void shouldReturnCashCardById() throws Exception{
        when(cashCardRepository.findByIdAndOwner(1L, "sarah")).thenReturn(cashCard);

        mockMvc.perform(get("/cashcards/99"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(cashCard)));
    }

    @Test
    @WithMockUser(username = "sarah", roles = {"CARD-OWNER"})
    void shouldReturn404WhenCashCardNotFound() throws Exception {
        when(cashCardRepository.findByIdAndOwner(1L, "sarah")).thenReturn(null);
        mockMvc.perform(get("/cashcards/99")).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "sarah", roles = {"CARD-OWNER"})
    void shouldReturn404WhenAcessingOtherUserCashCard() throws Exception {
        // Card exists but belongs to another user
        CashCard johnsCard = new CashCard(5L, 500.0, "john");
        when(cashCardRepository.findByIdAndOwner(5L, "sarah")).thenReturn(null);

        mockMvc.perform(get("/cashcards/5"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }


    @Test
    @WithMockUser(username = "sarah1", roles = {"CARD-OWNER"})
    void shouldCreateNewCashCard() throws Exception {
        CashCard newCard = new CashCard(null, 250.0, "sarah");
        CashCard savedCard = new CashCard(1L, 250.0, "sarah");

        when(cashCardRepository.save(any(CashCard.class))).thenReturn(savedCard);

        mockMvc.perform(post("/cashcards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCard)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/cashcards/1"));

        /*
        When used in verify(cashCardRepository).save(any(CashCard.class)),
        it's checking that the save method was called exactly once with any
        CashCard object as a parameter.
        */
        verify(cashCardRepository).save(any(CashCard.class));
    }

    /*
    any(CashCard.class) is a Mockito argument matcher that means
    "match any CashCard object" - it's basically a wildcard for
    method parameters.
    */

    @Test
    @WithMockUser(username = "sarah", roles = {"CARD-OWNER"})
    void shouldReturnBadRequestWhenBodyIsEmpty() throws Exception {
        mockMvc.perform(post("/cashcards"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "sarah", roles = {"CARD-OWNER"})
    void shouldUpdateExistingCashCard() throws Exception {
        CashCard existingCard = new CashCard(1L, 100.0, "sarah");
        CashCard updatedCard = new CashCard(1L, 150.0, "sarah");
        CashCard updateRequest = new CashCard(null, 150.0, null);

        when(cashCardRepository.findByIdAndOwner(1L, "sarah")).thenReturn(existingCard);
        when(cashCardRepository.save(any(CashCard.class))).thenReturn(updatedCard);

        mockMvc.perform(put("/cashcards/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(updatedCard)));

        verify(cashCardRepository).save(any(CashCard.class));
    }

    @Test
    @WithMockUser(username = "sarah", roles = {"CARD-OWNER"})
    void shouldReturn404WhenUpdatingNonExistentCashCard() throws Exception {
        CashCard updateRequest = new CashCard(null, 150.0, null);

        when(cashCardRepository.findByIdAndOwner(99L, "sarah")).thenReturn(null);

        mockMvc.perform(put("/cashcards/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "sarah", roles = {"CARD-OWNER"})
    void shouldDeleteCashCard() throws Exception {
        when(cashCardRepository.existsByIdAndOwner(1L, "sarah")).thenReturn(true);
        doNothing().when(cashCardRepository).deleteById(1L);

        mockMvc.perform(delete("/cashcards/1"))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(cashCardRepository).deleteById(1L);
    }

    @Test
    @WithMockUser(username = "sarah", roles = {"CARD-OWNER"})
    void shouldReturn404WhenDeletingNonExistentCashCard() throws Exception {
        when(cashCardRepository.existsByIdAndOwner(99L, "sarah")).thenReturn(false);
        mockMvc.perform(delete("/cashcards/99"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }


}


