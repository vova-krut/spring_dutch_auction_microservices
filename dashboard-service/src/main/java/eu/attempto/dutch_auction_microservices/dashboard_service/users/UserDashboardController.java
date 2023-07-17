package eu.attempto.dutch_auction_microservices.dashboard_service.users;

import eu.attempto.dutch_auction_microservices.dashboard_service.range.TimeRange;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "UserDashboard")
@RequestMapping("/users")
public class UserDashboardController {
  private final UserDashboardService userDashboardService;

  @GetMapping("/registered")
  public Long getCountOfUsersRegistered(@RequestParam TimeRange timeRange) {
    return userDashboardService.countUsersCreated(timeRange);
  }

  @GetMapping("/created-auctions")
  public Long getCountOfUsersWhoCreatedAuctions(@RequestParam TimeRange timeRange) {
    return userDashboardService.countUsersWhoCreatedAuctions(timeRange);
  }

  @GetMapping("/placed-bids")
  public Long getCountOfUsersWhoPlacedBids(@RequestParam TimeRange timeRange) {
    return userDashboardService.countUsersWhoPlacedBids(timeRange);
  }
}
