package eu.attempto.dutch_auction_microservices.backend_service.core.auctions;

import eu.attempto.dutch_auction_microservices.backend_service.core.auctions.images.Image;
import eu.attempto.dutch_auction_microservices.backend_service.core.users.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Auction {
  private Long id;

  private User author;

  private List<Image> images;

  private String title;

  private String description;

  @Builder.Default private Boolean active = true;

  private ZonedDateTime startTime;

  private ZonedDateTime endTime;

  private BigDecimal price;
}
