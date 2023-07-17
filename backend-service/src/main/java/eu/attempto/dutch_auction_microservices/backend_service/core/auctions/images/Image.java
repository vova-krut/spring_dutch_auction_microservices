package eu.attempto.dutch_auction_microservices.backend_service.core.auctions.images;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.attempto.dutch_auction_microservices.backend_service.core.auctions.Auction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Image {
  private Long id;

  @JsonIgnore private Auction auction;

  private String link;
}
