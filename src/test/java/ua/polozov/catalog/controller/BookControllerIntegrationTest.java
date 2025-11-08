package ua.polozov.catalog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ua.polozov.catalog.service.RateService;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BookControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RateService rateService;

    @Test
    void createBook_returns201AndLocation_andGetReturns201Json() throws Exception {
        when(rateService.getCurrentRate()).thenReturn(new BigDecimal("25.00"));

        String reqJson = "{\n" +
                "  \"isbn\": \"1234567890123\",\n" +
                "  \"title\": \"Integration Test Book\",\n" +
                "  \"author\": \"Tester\",\n" +
                "  \"publicationYear\": 2024,\n" +
                "  \"price\": { \"uah\": 100.00 }\n" +
                "}";

        var result = mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqJson))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.matchesPattern(".*/api/v1/books/\\d+$")))
                .andReturn();

        String location = result.getResponse().getHeader("Location");
        // GET the created resource and verify JSON contract
        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isbn").value("1234567890123"))
                .andExpect(jsonPath("$.title").value("Integration Test Book"))
                .andExpect(jsonPath("$.price.uah").value(100.00))
                .andExpect(jsonPath("$.price.eur").isNumber());
    }
}

