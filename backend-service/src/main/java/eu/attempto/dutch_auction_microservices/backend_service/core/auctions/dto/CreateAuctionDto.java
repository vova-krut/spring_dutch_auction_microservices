package eu.attempto.dutch_auction_microservices.backend_service.core.auctions.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateAuctionDto {
  private String title;

  private String description;

  private ZonedDateTime startTime;

  private ZonedDateTime endTime;

  private BigDecimal price;
}
