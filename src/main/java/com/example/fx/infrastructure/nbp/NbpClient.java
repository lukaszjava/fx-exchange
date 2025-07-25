package com.example.fx.infrastructure.nbp;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "nbp", url = "${nbp.api-url:https://api.nbp.pl}")
public interface NbpClient {
  @GetMapping("/api/exchangerates/rates/A/USD/?format=json")
  NbpRateResponse usdRate();
}
