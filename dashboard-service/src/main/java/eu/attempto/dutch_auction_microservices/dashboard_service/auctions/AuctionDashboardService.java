package eu.attempto.dutch_auction_microservices.dashboard_service.auctions;

import eu.attempto.dutch_auction_microservices.dashboard_service.range.TimeRange;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class AuctionDashboardService {
  private final RestTemplate restTemplate;

  public Double getAverageAuctionDuration() {
    return restTemplate.getForObject(
        "http://db-service/auctions/search/averageAuctionDuration", Double.class);
  }

  public Double getAverageNumberOfBidsPerAuction() {
    return restTemplate.getForObject(
        "http://db-service/auctions/search/averageNumberOfBidsPerAuction", Double.class);
  }

  public Long countAuctionsCreatedAfter(TimeRange timeRange) {
    return restTemplate.getForObject(
        "http://db-service/auctions/search/countAuctionsCreatedAfter?date={date}",
        Long.class,
        timeRange.getTimeRangeStr());
  }

  public Long countBidsPlacedOnAuctionAfter(Long auctionId, TimeRange timeRange) {
    return restTemplate.getForObject(
        "http://db-service/auctions/search/countBidsPlacedOnAuctionAfter?auctionId={auctionId}&date={date}",
        Long.class,
        auctionId,
        timeRange.getTimeRangeStr());
  }

  public Long countUsersWhoPlacedBidsOnAuction(Long auctionId) {
    return restTemplate.getForObject(
        "http://db-service/auctions/search/countBidsPlacedOnAuctionAfter?auctionId={auctionId}",
        Long.class,
        auctionId);
  }
}
