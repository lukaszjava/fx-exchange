package com.example.fx.infrastructure.nbp;

import com.example.fx.application.port.out.ExchangeRateProvider;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class NbpExchangeRateProvider implements ExchangeRateProvider {

  private final NbpClient client;

  public NbpExchangeRateProvider(NbpClient client) {
    this.client = client;
  }

  @Override
  @Retry(name = "nbpRate")
  @CircuitBreaker(name = "nbpRate")
  public BigDecimal getPlnUsdRate() {
    return client.usdRate().rates().getFirst().mid();
  }
}
