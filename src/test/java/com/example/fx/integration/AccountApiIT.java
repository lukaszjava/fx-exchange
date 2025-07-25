package com.example.fx.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@AutoConfigureMockMvc
class AccountApiIT extends BaseIntegrationTest {

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper objectMapper;

  record CreateReq(String firstName, String lastName, BigDecimal initialPlnBalance) {}

  @Test
  @DisplayName("POST /accounts -> 201 Created + Location + body")
  void create_account_success() throws Exception {
    // given
    var createRequest = new CreateReq("John", "Doe", new BigDecimal("123.45"));

    // when
    MvcResult result =
        mvc.perform(
                post("/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
            // then
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", startsWith("/accounts/")))
            .andExpect(jsonPath("$.accountId").exists())
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.balances.PLN").value("123.45"))
            .andExpect(jsonPath("$.balances.USD").value(0))
            .andReturn();

    // and when+then (GET)
    String location = result.getResponse().getHeader("Location");
    assertThat(location).isNotBlank();

    mvc.perform(get(location))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accountId").value(extractId(location).toString()));
  }

  static Object[][] invalidCreatePayloads() {
    return new Object[][] {
      {new CreateReq("", "Doe", new BigDecimal("10.00")), "firstName"},
      {new CreateReq("John", "", new BigDecimal("10.00")), "lastName"},
      {new CreateReq("John", "Doe", new BigDecimal("-0.01")), "initialPlnBalance"},
    };
  }

  @ParameterizedTest(name = "invalid create payload -> 400, field={1}")
  @MethodSource("invalidCreatePayloads")
  void create_account_validation_error(CreateReq payload, String expectedField) throws Exception {
    mvc.perform(
            post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title").value("Bad Request"))
        .andExpect(jsonPath("$.type").value("https://example.com/problems/validation-error"))
        .andExpect(jsonPath("$.errors", not(empty())))
        .andExpect(jsonPath("$.errors[*].field", hasItem(expectedField)));
  }

  @Test
  @DisplayName("GET /accounts/{id} -> 404 when not exists")
  void get_notFound() throws Exception {
    mvc.perform(get("/accounts/{id}", UUID.randomUUID()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("title").value("Account not found"))
        .andExpect(jsonPath("type").value("https://example.com/problems/account-not-found"));
  }

  private static UUID extractId(String locationHeader) {
    return UUID.fromString(locationHeader.substring(locationHeader.lastIndexOf('/') + 1));
  }
}
