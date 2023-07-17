package eu.attempto.dutch_auction_microservices.backend_service.core.bids;

import eu.attempto.dutch_auction_microservices.backend_service.core.auctions.Auction;
import eu.attempto.dutch_auction_microservices.backend_service.core.bids.dto.BidRequest;
import eu.attempto.dutch_auction_microservices.backend_service.core.users.User;
import eu.attempto.dutch_auction_microservices.backend_service.exceptions.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BidsRepository {
  private final RestTemplate restTemplate;

  public Bid save(Bid bid) {
    var bidRequest = buildBidRequest(bid);
    if (bidRequest.getId() != null) {
      restTemplate.put("http://db-service/bids/{bidId}", bidRequest, bidRequest.getId());
      return bid;
    }

    return restTemplate.postForObject("http://db-service/bids", bidRequest, Bid.class);
  }

  private BidRequest buildBidRequest(Bid bid) {
    var auction = bid.getAuction();
    var user = bid.getUser();
    var bidRequest = BidRequest.builder().id(bid.getId()).build();
    if (auction != null) {
      bidRequest.setAuction(getAuctionLink(auction.getId()));
    }
    if (user != null) {
      bidRequest.setUser(getUserLink(user.getId()));
    }

    return bidRequest;
  }

  private String getAuctionLink(Long auctionId) {
    var auctionEntityModel =
        restTemplate
            .exchange(
                "http://db-service/auctions/{id}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<EntityModel<Auction>>() {},
                auctionId)
            .getBody();

    if (auctionEntityModel == null) {
      throw new BadRequestException("Auction with this id was not found");
    }

    return auctionEntityModel.getRequiredLink("self").getHref();
  }

  private String getUserLink(Long userId) {
    var userEntityModel =
        restTemplate
            .exchange(
                "http://db-service/users/{id}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<EntityModel<User>>() {},
                userId)
            .getBody();

    if (userEntityModel == null) {
      throw new BadRequestException("User with this id was not found");
    }

    return userEntityModel.getRequiredLink("self").getHref();
  }

  public List<Bid> findByUser(User user) {
    var collectionModel =
        restTemplate
            .exchange(
                "http://db-service/bids/search/findByUser?projection=bidProjection&userId={userId}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<CollectionModel<Bid>>() {},
                user.getId())
            .getBody();

    if (collectionModel == null) {
      return List.of();
    }
    return collectionModel.getContent().stream().toList();
  }

  public List<Bid> deleteByUser(User user) {
    var collectionModel =
        restTemplate
            .exchange(
                "http://db-service/bids/search/deleteByUser?projection=bidProjection&userId={userId}",
                HttpMethod.DELETE,
                null,
                new ParameterizedTypeReference<CollectionModel<Bid>>() {},
                user.getId())
            .getBody();

    if (collectionModel == null) {
      return List.of();
    }
    return collectionModel.getContent().stream().toList();
  }

  public Optional<Bid> findLastAuctionBid(Auction auction) {
    var bid =
        restTemplate
            .exchange(
                "http://db-service/bids/search/findLastAuctionBid?projection=bidProjection&auctionId={auctionId}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Bid>() {},
                auction.getId())
            .getBody();

    return Optional.ofNullable(bid);
  }
}
