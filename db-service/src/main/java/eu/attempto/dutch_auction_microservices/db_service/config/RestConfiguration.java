package eu.attempto.dutch_auction_microservices.db_service.config;

import eu.attempto.dutch_auction_microservices.db_service.auctions.Auction;
import eu.attempto.dutch_auction_microservices.db_service.auctions.images.Image;
import eu.attempto.dutch_auction_microservices.db_service.bids.Bid;
import eu.attempto.dutch_auction_microservices.db_service.roles.Role;
import eu.attempto.dutch_auction_microservices.db_service.users.User;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@Configuration
public class RestConfiguration implements RepositoryRestConfigurer {
  @Override
  public void configureRepositoryRestConfiguration(
      RepositoryRestConfiguration config, CorsRegistry cors) {
    config.exposeIdsFor(User.class, Role.class, Auction.class, Image.class, Bid.class);
  }
}
