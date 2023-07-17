package eu.attempto.dutch_auction_microservices.backend_service.core.users.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {
  private Long id;

  private String email;

  private String name;

  private BigDecimal balance;

  private String role;
}
