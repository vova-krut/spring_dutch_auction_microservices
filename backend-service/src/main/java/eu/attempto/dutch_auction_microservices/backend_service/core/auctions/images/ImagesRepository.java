package eu.attempto.dutch_auction_microservices.backend_service.core.auctions.images;

import eu.attempto.dutch_auction_microservices.backend_service.core.auctions.Auction;
import eu.attempto.dutch_auction_microservices.backend_service.core.auctions.images.dto.ImageRequest;
import eu.attempto.dutch_auction_microservices.backend_service.exceptions.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

@Repository
@RequiredArgsConstructor
public class ImagesRepository {
  private final RestTemplate restTemplate;

  public Image save(Image image) {
    var imageRequest = buildImageRequest(image);
    if (imageRequest.getId() != null) {
      restTemplate.put("http://db-service/images/{imageId}", imageRequest, imageRequest.getId());
      return image;
    }

    return restTemplate.postForObject("http://db-service/images", imageRequest, Image.class);
  }

  private ImageRequest buildImageRequest(Image image) {
    var auction = image.getAuction();
    var imageRequest = ImageRequest.builder().id(image.getId()).link(image.getLink()).build();
    if (auction != null) {
      imageRequest.setAuction(getAuctionLink(auction.getId()));
    }

    return imageRequest;
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
}
