package eu.attempto.dutch_auction_microservices.backend_service.core.auctions.dto;

import jakarta.annotation.Nullable;
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
public class AuctionRequest {
  private Long id;

  private String author;

  private String title;

  private String description;

  private Boolean active;

  @Nullable private ZonedDateTime startTime;

  private ZonedDateTime endTime;

  private BigDecimal price;
}
