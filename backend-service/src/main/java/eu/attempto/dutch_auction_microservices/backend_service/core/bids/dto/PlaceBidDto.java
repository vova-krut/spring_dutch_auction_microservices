package eu.attempto.dutch_auction_microservices.backend_service.core.bids.dto;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlaceBidDto {
  @Positive private Long auctionId;
}
