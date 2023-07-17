package eu.attempto.dutch_auction_microservices.backend_service.core.auctions.dto;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChangeAuctionDto {
  @Positive private Long id;

  @Positive private BigDecimal price;
}
