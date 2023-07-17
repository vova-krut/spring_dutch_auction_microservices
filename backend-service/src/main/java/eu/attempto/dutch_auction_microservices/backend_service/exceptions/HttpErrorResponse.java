package eu.attempto.dutch_auction_microservices.backend_service.exceptions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HttpErrorResponse {
  private int statusCode;
  private String message;
}
