package com.example.fx.config;

import com.example.fx.application.port.out.AccountRepository;
import com.example.fx.application.port.out.ExchangeRateProvider;
import com.example.fx.application.service.AccountService;
import com.example.fx.application.service.ExchangeFundsHandler;
import com.example.fx.domain.service.ExchangeService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

  @Bean
  public AccountService accountService(AccountRepository repo) {
    return new AccountService(repo);
  }

  @Bean
  public ExchangeFundsHandler exchangeFundsHandler(
      AccountRepository repo, ExchangeRateProvider rateProvider, ExchangeService exchanger) {
    return new ExchangeFundsHandler(repo, rateProvider, exchanger);
  }
}
