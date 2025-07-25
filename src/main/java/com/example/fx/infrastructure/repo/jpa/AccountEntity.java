package com.example.fx.infrastructure.repo.jpa;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountEntity {

  @Id private UUID id;

  @Column(name = "first_name", nullable = false, length = 100)
  private String firstName;

  @Column(name = "last_name", nullable = false, length = 100)
  private String lastName;

  @Column(nullable = false, precision = 18, scale = 2)
  private BigDecimal pln;

  @Column(nullable = false, precision = 18, scale = 4)
  private BigDecimal usd;

  @Version private long version;
}
