package eu.attempto.dutch_auction_microservices.dashboard_service.auctions;

import eu.attempto.dutch_auction_microservices.dashboard_service.range.TimeRange;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "AuctionDashboard")
@RequestMapping("/auctions")
public class AuctionDashboardController {
  private final AuctionDashboardService auctionDashboardService;

  @GetMapping("/average-duration")
  public Double getAverageAuctionDuration() {
    return auctionDashboardService.getAverageAuctionDuration();
  }

  @GetMapping("/average-bids")
  public Double getAverageNumberOfBidsPerAuction() {
    return auctionDashboardService.getAverageNumberOfBidsPerAuction();
  }

  @GetMapping
  public Long countAuctionsCreatedAfter(@RequestParam TimeRange timeRange) {
    return auctionDashboardService.countAuctionsCreatedAfter(timeRange);
  }

  @GetMapping("/{auctionId}/bids")
  public Long countBidsPlacedOnAuctionAfter(
      @PathVariable Long auctionId, @RequestParam TimeRange timeRange) {
    return auctionDashboardService.countBidsPlacedOnAuctionAfter(auctionId, timeRange);
  }

  @GetMapping("/{auctionId}")
  public Long countUsersWhoPlacedBidsOnAuction(@PathVariable Long auctionId) {
    return auctionDashboardService.countUsersWhoPlacedBidsOnAuction(auctionId);
  }
}
