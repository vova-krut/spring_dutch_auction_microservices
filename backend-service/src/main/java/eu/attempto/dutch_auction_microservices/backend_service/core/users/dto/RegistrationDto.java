package eu.attempto.dutch_auction_microservices.backend_service.core.users.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationDto {
  private String email;

  private String name;

  private String password;
}
