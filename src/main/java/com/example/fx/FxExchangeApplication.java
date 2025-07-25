package com.example.fx;

import com.example.fx.infrastructure.nbp.NbpClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(clients = NbpClient.class)
public class FxExchangeApplication {
  public static void main(String[] args) {
    SpringApplication.run(FxExchangeApplication.class, args);
  }
}
