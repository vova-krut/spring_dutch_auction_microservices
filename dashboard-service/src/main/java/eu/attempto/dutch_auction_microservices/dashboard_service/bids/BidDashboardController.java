package eu.attempto.dutch_auction_microservices.dashboard_service.bids;

import eu.attempto.dutch_auction_microservices.dashboard_service.range.TimeRange;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "BidDashboard")
@RequestMapping("/bids")
public class BidDashboardController {
  private final BidDashboardService bidDashboardService;

  @GetMapping("/{userId}/auctions")
  public Long countAuctionsOnWhichUserPlacedBids(@PathVariable Long userId) {
    return bidDashboardService.countAuctionsOnWhichUserPlacedBids(userId);
  }

  @GetMapping("/{userId}/bids")
  public Long countBidsPlacedByUserAfter(
      @PathVariable Long userId, @RequestParam TimeRange timeRange) {
    return bidDashboardService.countBidsPlacedByUserAfter(userId, timeRange);
  }
}
