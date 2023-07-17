package eu.attempto.dutch_auction_microservices.db_service.bids;

import eu.attempto.dutch_auction_microservices.db_service.auctions.AuctionProjection;
import eu.attempto.dutch_auction_microservices.db_service.users.UserProjection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

@Projection(name = "bidProjection", types = Bid.class)
public interface BidProjection {
  @Value("#{target.id}")
  Long getId();

  UserProjection getUser();

  AuctionProjection getAuction();
}
