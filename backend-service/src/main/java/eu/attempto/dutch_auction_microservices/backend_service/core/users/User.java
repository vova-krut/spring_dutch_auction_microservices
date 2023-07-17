package eu.attempto.dutch_auction_microservices.backend_service.core.users;

import eu.attempto.dutch_auction_microservices.backend_service.core.roles.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
  private Long id;

  private String email;

  private String name;

  @Builder.Default private BigDecimal balance = new BigDecimal(0);

  private Role role;
}
