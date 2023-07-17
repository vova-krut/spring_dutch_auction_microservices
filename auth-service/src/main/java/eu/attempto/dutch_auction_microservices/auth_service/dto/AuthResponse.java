package eu.attempto.dutch_auction_microservices.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
  private String access_token;
  private Integer expires_in;
  private String refresh_token;
  private Integer refresh_expires_in;
}
