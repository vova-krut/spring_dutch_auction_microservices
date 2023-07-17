package eu.attempto.dutch_auction_microservices.backend_service.core.auctions;

import eu.attempto.dutch_auction_microservices.backend_service.core.auctions.dto.CreateAuctionDto;
import eu.attempto.dutch_auction_microservices.backend_service.core.users.User;
import eu.attempto.dutch_auction_microservices.backend_service.validators.FutureDate;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@RequiredArgsConstructor
@RestController
@Validated
@Tag(name = "Auctions")
@RequestMapping("/auctions")
public class AuctionsController {
  private final AuctionsService auctionsService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Auction createAuction(
      Authentication authentication,
      @Valid @RequestParam("title") @NotBlank String title,
      @Valid @RequestParam("description") @NotBlank String description,
      @Valid @RequestParam(value = "startTime", required = false) @FutureDate Long startTime,
      @Valid @RequestParam("endTime") @FutureDate Long endTime,
      @Valid @RequestParam("price") @Positive BigDecimal price,
      @RequestParam("images") MultipartFile[] imageFiles) {
    return auctionsService.createAuction(
        (User) authentication.getPrincipal(),
        imageFiles,
        buildCreateAuctionDto(title, description, startTime, endTime, price));
  }

  private CreateAuctionDto buildCreateAuctionDto(
      String title, String description, Long startTime, Long endTime, BigDecimal price) {
    var auctionDto =
        CreateAuctionDto.builder()
            .title(title)
            .description(description)
            .endTime(ZonedDateTime.ofInstant(Instant.ofEpochMilli(endTime), ZoneId.systemDefault()))
            .price(price);

    if (startTime != null) {
      auctionDto.startTime(
          ZonedDateTime.ofInstant(Instant.ofEpochMilli(startTime), ZoneId.systemDefault()));
    } else {
      auctionDto.startTime(ZonedDateTime.now());
    }

    return auctionDto.build();
  }

  @GetMapping
  public Page<Auction> getAuctions(Pageable pageable) {
    return auctionsService.getAuctions(pageable);
  }

  @GetMapping(value = "/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<Auction> getAuction(@PathVariable Long id) {
    return auctionsService.subscribeToAuction(id);
  }
}
