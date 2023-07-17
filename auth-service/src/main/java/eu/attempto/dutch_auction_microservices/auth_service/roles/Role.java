package eu.attempto.dutch_auction_microservices.auth_service.roles;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Role {
  private Long id;

  private RolesEnum name;
}
