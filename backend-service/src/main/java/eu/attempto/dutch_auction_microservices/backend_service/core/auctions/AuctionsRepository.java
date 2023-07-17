package eu.attempto.dutch_auction_microservices.backend_service.core.auctions;

import eu.attempto.dutch_auction_microservices.backend_service.core.auctions.dto.AuctionRequest;
import eu.attempto.dutch_auction_microservices.backend_service.core.users.User;
import eu.attempto.dutch_auction_microservices.backend_service.exceptions.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AuctionsRepository {
  private final RestTemplate restTemplate;

  public Auction save(Auction auction) {
    var auctionRequest = buildAuctionRequest(auction);
    if (auctionRequest.getId() != null) {
      restTemplate.put(
          "http://db-service/auctions/{auctionId}?projection=auctionProjection",
          auctionRequest,
          auctionRequest.getId());
      return auction;
    }

    return restTemplate.postForObject("http://db-service/auctions", auctionRequest, Auction.class);
  }

  private AuctionRequest buildAuctionRequest(Auction auction) {
    var author = auction.getAuthor();
    var auctionRequest =
        AuctionRequest.builder()
            .id(auction.getId())
            .title(auction.getTitle())
            .description(auction.getDescription())
            .active(auction.getActive())
            .startTime(auction.getStartTime())
            .endTime(auction.getEndTime())
            .price(auction.getPrice())
            .build();
    if (author != null) {
      auctionRequest.setAuthor(getAuthorLink(author.getId()));
    }

    return auctionRequest;
  }

  private String getAuthorLink(Long userId) {
    var userEntityModel =
        restTemplate
            .exchange(
                "http://db-service/users/{id}?projection=userProjection",
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

  public Page<Auction> findAll(Pageable pageable) {
    var uriVars = new HashMap<String, Object>();
    uriVars.put("page", pageable.getPageNumber());
    uriVars.put("size", pageable.getPageSize());
    uriVars.put("sort", pageable.getSort());
    var pagedModel =
        restTemplate
            .exchange(
                "http://db-service/auctions?page={page}&size={size}&sort={sort}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<PagedModel<Auction>>() {},
                uriVars)
            .getBody();

    if (pagedModel == null || pagedModel.getMetadata() == null) {
      return Page.empty();
    }
    return new PageImpl<>(
        pagedModel.getContent().stream().toList(),
        pageable,
        pagedModel.getMetadata().getTotalElements());
  }

  public Page<Auction> findAll() {
    var pagedModel =
        restTemplate
            .exchange(
                "http://db-service/auctions",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<PagedModel<Auction>>() {})
            .getBody();

    if (pagedModel == null || pagedModel.getMetadata() == null) {
      return Page.empty();
    }
    return new PageImpl<>(
        pagedModel.getContent().stream().toList(),
        Pageable.unpaged(),
        pagedModel.getMetadata().getTotalElements());
  }

  public List<Auction> deleteByAuthor(User author) {
    var collectionModel =
        restTemplate
            .exchange(
                "http://db-service/auctions/search/deleteByAuthor?authorId={authorId}",
                HttpMethod.DELETE,
                null,
                new ParameterizedTypeReference<CollectionModel<Auction>>() {},
                author.getId())
            .getBody();

    if (collectionModel == null) {
      return List.of();
    }
    return collectionModel.getContent().stream().toList();
  }

  public Optional<Auction> findById(Long id) {
    var auction =
        restTemplate.getForObject(
            "http://db-service/auctions/{id}?projection=auctionProjection", Auction.class, id);

    return Optional.ofNullable(auction);
  }

  public List<Auction> findByActiveTrueAndEndTimeAfter(ZonedDateTime dateTime) {
    var collectionModel =
        restTemplate
            .exchange(
                "http://db-service/auctions/search/findByActiveTrueAndEndTimeAfter?dateTime={dateTime}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<CollectionModel<Auction>>() {},
                dateTime)
            .getBody();

    if (collectionModel == null) {
      return List.of();
    }
    return collectionModel.getContent().stream().toList();
  }

  public List<Auction> findByActiveFalseAndStartTimeBefore(ZonedDateTime dateTime) {
    var collectionModel =
        restTemplate
            .exchange(
                "http://db-service/auctions/search/findByActiveFalseAndStartTimeBefore?dateTime={dateTime}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<CollectionModel<Auction>>() {},
                dateTime)
            .getBody();

    if (collectionModel == null) {
      return List.of();
    }
    return collectionModel.getContent().stream().toList();
  }

  public List<Auction> findByActiveTrueAndEndTimeLessThanEqual(ZonedDateTime dateTime) {
    var collectionModel =
        restTemplate
            .exchange(
                "http://db-service/auctions/search/findByActiveTrueAndEndTimeLessThanEqual?dateTime={dateTime}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<CollectionModel<Auction>>() {},
                dateTime)
            .getBody();

    if (collectionModel == null) {
      return List.of();
    }
    return collectionModel.getContent().stream().toList();
  }
}
