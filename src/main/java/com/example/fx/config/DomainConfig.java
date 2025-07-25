package com.example.fx.config;

import com.example.fx.domain.service.ExchangeService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfig {
  @Bean
  public ExchangeService exchangeService() {
    return new ExchangeService();
  }
}
