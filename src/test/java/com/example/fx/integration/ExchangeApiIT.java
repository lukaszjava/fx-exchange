package com.example.fx.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@AutoConfigureMockMvc
class ExchangeApiIT extends BaseIntegrationTest {

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper objectMapper;
  @Autowired CircuitBreakerRegistry circuitBreakerRegistry;

  record CreateReq(String firstName, String lastName, BigDecimal initialPlnBalance) {}

  enum Dir {
    PLN_TO_USD,
    USD_TO_PLN
  }

  record ExchangeReq(Dir direction, BigDecimal amount) {}

  @BeforeEach
  void resetCb() {
    var circuitBreaker = circuitBreakerRegistry.circuitBreaker("nbpRate");
    circuitBreaker.reset();
    circuitBreaker.transitionToClosedState();
  }

  @BeforeEach
  void resetWiremock() {
    wiremock.resetAll();
  }

  @Test
  @DisplayName("POST /accounts/{id}/exchange PLN->USD – happy path")
  void exchange_pln_to_usd_success() throws Exception {
    // given
    UUID id = createAccount("John", "Doe", new BigDecimal("1000.00"));

    BigDecimal rate = new BigDecimal("4.1234");
    stubNbp(rate);

    BigDecimal amount = new BigDecimal("333.33");
    var exchangeRequest = new ExchangeReq(Dir.PLN_TO_USD, amount);

    // when
    MvcResult result =
        mvc.perform(
                post("/accounts/{id}/exchange", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(exchangeRequest)))
            // then
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rate").value(rate.toString()))
            .andExpect(jsonPath("$.rateTimestamp").isNotEmpty())
            .andExpect(jsonPath("$.balances.PLN").value("666.67"))
            .andExpect(jsonPath("$.balances.USD").isNotEmpty())
            .andReturn();

    JsonNode body = objectMapper.readTree(result.getResponse().getContentAsByteArray());
    BigDecimal usdAfter = new BigDecimal(body.at("/balances/USD").asText());
    BigDecimal expectedUsdGain = amount.divide(rate, 4, RoundingMode.HALF_UP);

    assertThat(usdAfter).isEqualByComparingTo(expectedUsdGain);
  }

  @Test
  @DisplayName("POST /accounts/{id}/exchange USD->PLN – happy path")
  void exchange_usd_to_pln_success() throws Exception {
    // given
    UUID id = createAccount("John", "Doe", new BigDecimal("1000.00"));

    stubNbp(new BigDecimal("4.0000"));
    exchange(id, Dir.PLN_TO_USD, new BigDecimal("200.00"));

    BigDecimal rate = new BigDecimal("3.9876");
    stubNbp(rate);

    BigDecimal usdToSell = new BigDecimal("10.0000");

    MvcResult result =
        exchange(id, Dir.USD_TO_PLN, usdToSell)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rate").value(rate.toString()))
            .andReturn();

    JsonNode body = objectMapper.readTree(result.getResponse().getContentAsByteArray());
    BigDecimal pln = new BigDecimal(body.at("/balances/PLN").asText());
    BigDecimal usd = new BigDecimal(body.at("/balances/USD").asText());
    BigDecimal expectedPln =
        new BigDecimal("800.00").add(usdToSell.multiply(rate).setScale(2, RoundingMode.HALF_UP));
    BigDecimal expectedUsd = new BigDecimal("50.0000").subtract(usdToSell);

    assertThat(pln).isEqualByComparingTo(expectedPln);
    assertThat(usd).isEqualByComparingTo(expectedUsd);
  }

  @Test
  @DisplayName("POST /accounts/{id}/exchange -> 409 Conflict when funds are insufficient")
  void exchange_insufficient_conflict() throws Exception {
    UUID id = createAccount("John", "Doe", new BigDecimal("10.00"));
    stubNbp(new BigDecimal("4.00"));

    mvc.perform(
            post("/accounts/{id}/exchange", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new ExchangeReq(Dir.PLN_TO_USD, new BigDecimal("100.00")))))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.type").value("https://example.com/problems/insufficient-funds"))
        .andExpect(jsonPath("$.title").value("Insufficient funds"));
  }

  @Test
  @DisplayName("POST /accounts/{id}/exchange -> 404 when the account does not exist")
  void exchange_not_found() throws Exception {
    stubNbp(new BigDecimal("4.00"));

    mvc.perform(
            post("/accounts/{id}/exchange", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new ExchangeReq(Dir.PLN_TO_USD, new BigDecimal("10.00")))))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.title").value("Account not found"));
  }

  @Test
  @DisplayName(
      "POST /accounts/{id}/exchange -> 400 on validation (amount <= 0 / missing direction)")
  void exchange_validation() throws Exception {
    UUID id = createAccount("John", "Doe", new BigDecimal("100.00"));

    String badPayload1 = """
                  {"amount": 0.00}
                """;

    mvc.perform(
            post("/accounts/{id}/exchange", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(badPayload1))
        .andExpect(status().isBadRequest());

    String badPayload2 =
        """
                  {"direction": "PLN_TO_USD", "amount": 0.00}
                """;
    mvc.perform(
            post("/accounts/{id}/exchange", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(badPayload2))
        .andExpect(status().isBadRequest());
  }

  private UUID createAccount(String first, String last, BigDecimal pln) throws Exception {
    var createRequest = new CreateReq(first, last, pln);
    MvcResult res =
        mvc.perform(
                post("/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn();
    String location = res.getResponse().getHeader("Location");
    assertThat(location).isNotBlank();
    return UUID.fromString(location.substring(location.lastIndexOf('/') + 1));
  }

  private void stubNbp(BigDecimal rate) {
    String body =
        """
                {
                  "table":"A",
                  "currency":"dolar",
                  "code":"USD",
                  "rates":[{"effectiveDate":"2024-01-01","mid":%s}]
                }
                """
            .formatted(rate.toString());

    stubFor(
        get(urlEqualTo("/api/exchangerates/rates/A/USD/?format=json"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(body)));
  }

  private org.springframework.test.web.servlet.ResultActions exchange(
      UUID id, Dir dir, BigDecimal amount) throws Exception {
    return mvc.perform(
        post("/accounts/{id}/exchange", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new ExchangeReq(dir, amount))));
  }
}
