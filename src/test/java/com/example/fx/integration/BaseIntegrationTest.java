package com.example.fx.integration;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseIntegrationTest {

  protected static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine").withReuse(true);

  protected static final WireMockServer wiremock = new WireMockServer(options().dynamicPort());

  static {
    POSTGRES.start();
    wiremock.start();
    WireMock.configureFor("localhost", wiremock.port());
  }

  @DynamicPropertySource
  static void registerProps(DynamicPropertyRegistry propertyRegistry) {
    propertyRegistry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    propertyRegistry.add("spring.datasource.username", POSTGRES::getUsername);
    propertyRegistry.add("spring.datasource.password", POSTGRES::getPassword);
    propertyRegistry.add("nbp.api-url", wiremock::baseUrl);
  }
}
