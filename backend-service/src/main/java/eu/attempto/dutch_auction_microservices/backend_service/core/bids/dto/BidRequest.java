package eu.attempto.dutch_auction_microservices.backend_service.core.bids.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BidRequest {
  private Long id;

  private String user;

  private String auction;
}
