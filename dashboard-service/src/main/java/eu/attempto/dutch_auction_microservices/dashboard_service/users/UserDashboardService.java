package eu.attempto.dutch_auction_microservices.dashboard_service.users;

import eu.attempto.dutch_auction_microservices.dashboard_service.range.TimeRange;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class UserDashboardService {
  private final RestTemplate restTemplate;

  public Long countUsersCreated(TimeRange timeRange) {
    return restTemplate.getForObject(
        "http://db-service/users/search/countUsersCreatedAfter?date={date}",
        Long.class,
        timeRange.getTimeRangeStr());
  }

  public Long countUsersWhoCreatedAuctions(TimeRange timeRange) {
    return restTemplate.getForObject(
        "http://db-service/users/search/countUsersWhoCreatedAuctionsAfter?date={date}",
        Long.class,
        timeRange.getTimeRangeStr());
  }

  public Long countUsersWhoPlacedBids(TimeRange timeRange) {
    return restTemplate.getForObject(
        "http://db-service/users/search/countUsersWhoPlacedBidsAfter?date={date}",
        Long.class,
        timeRange.getTimeRangeStr());
  }
}
