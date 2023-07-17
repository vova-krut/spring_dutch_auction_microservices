package eu.attempto.dutch_auction_microservices.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateBackendUserDto {
  private String email;

  private String name;

  private boolean isAdmin;
}
