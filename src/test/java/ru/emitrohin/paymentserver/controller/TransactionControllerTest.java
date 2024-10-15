package ru.emitrohin.paymentserver.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.emitrohin.paymentserver.model.Transaction;
import ru.emitrohin.paymentserver.service.TransactionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Test
    @WithMockUser(username = "1234567890")
    void getAllTransactions_ShouldReturnTransactionList() throws Exception {
        // Мокируем транзакции
        var transaction1 = new Transaction();
        transaction1.setAmount(new BigDecimal("100"));
        transaction1.setDateTime(LocalDateTime.now());
        transaction1.setCurrency("RUB");

        var transaction2 = new Transaction();
        transaction2.setAmount(new BigDecimal("200"));
        transaction2.setDateTime(LocalDateTime.now().minusDays(1));
        transaction2.setCurrency("RUB");

        when(transactionService.getAllTransactions(1234567890L)).thenReturn(List.of(transaction1, transaction2));

        mockMvc.perform(get("/transactions"))
                .andExpect(status().isOk()) // Ожидаем успешный ответ
                .andExpect(jsonPath("$[0].amount").value("100"))
                .andExpect(jsonPath("$[0].currency").value("RUB"))
                .andExpect(jsonPath("$[1].amount").value("200"))
                .andExpect(jsonPath("$[1].currency").value("RUB"));
    }

    @Test
    @WithMockUser(username = "1234567890")
    void getAllTransactions_NoTransactions_ShouldReturnEmptyList() throws Exception {
        // Мокируем ситуацию, когда нет транзакций
        when(transactionService.getAllTransactions(1234567890L)).thenReturn(List.of());

        mockMvc.perform(get("/transactions"))
                .andExpect(status().isOk()) // Ожидаем успешный ответ
                .andExpect(jsonPath("$").isEmpty()); // Ожидаем пустой список
    }
}
