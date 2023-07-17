package eu.attempto.dutch_auction_microservices.backend_service.core.auctions.images.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImageRequest {
  private Long id;

  private String auction;

  private String link;
}
