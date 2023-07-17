package eu.attempto.dutch_auction_microservices.dashboard_service.bids;

import eu.attempto.dutch_auction_microservices.dashboard_service.range.TimeRange;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class BidDashboardService {
  private final RestTemplate restTemplate;

  public Long countAuctionsOnWhichUserPlacedBids(Long userId) {
    return restTemplate.getForObject(
        "http://db-service/bids/search/countAuctionsOnWhichUserPlacedBids?userId={userId}",
        Long.class,
        userId);
  }

  public Long countBidsPlacedByUserAfter(Long userId, TimeRange timeRange) {
    return restTemplate.getForObject(
        "http://db-service/bids/search/countBidsPlacedByUserAfter?userId={userId}&date={date}",
        Long.class,
        userId,
        timeRange.getTimeRangeStr());
  }
}
