package eu.attempto.dutch_auction_microservices.backend_service.core.bids;

import eu.attempto.dutch_auction_microservices.backend_service.core.auctions.Auction;
import eu.attempto.dutch_auction_microservices.backend_service.core.users.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Bid {
  private Long id;

  private User user;

  private Auction auction;
}
