package eu.attempto.dutch_auction_microservices.db_service.auctions;

import eu.attempto.dutch_auction_microservices.db_service.auctions.images.Image;
import eu.attempto.dutch_auction_microservices.db_service.users.UserProjection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@Projection(name = "auctionProjection", types = Auction.class)
public interface AuctionProjection {
  @Value("#{target.id}")
  Long getId();

  UserProjection getAuthor();

  List<Image> getImages();

  String getTitle();

  String getDescription();

  Boolean getActive();

  ZonedDateTime getStartTime();

  ZonedDateTime getEndTime();

  BigDecimal getPrice();
}
