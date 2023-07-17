package eu.attempto.dutch_auction_microservices.backend_service.core.roles;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Role implements Serializable {
  private Long id;

  private RolesEnum name;
}
