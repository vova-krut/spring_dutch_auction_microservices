package eu.attempto.dutch_auction_microservices.backend_service.core.bids;

import eu.attempto.dutch_auction_microservices.backend_service.core.bids.dto.PlaceBidDto;
import eu.attempto.dutch_auction_microservices.backend_service.core.users.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Bids")
@RequestMapping("/bids")
public class BidsController {
  private final BidsService bidsService;

  @PostMapping
  public String placeBid(
      Authentication authentication, @Valid @RequestBody PlaceBidDto placeBidDto) {
    return bidsService.placeBid((User) authentication.getPrincipal(), placeBidDto);
  }

  @GetMapping
  public List<Bid> getMyBids(Authentication authentication) {
    return bidsService.getUserBids((User) authentication.getPrincipal());
  }
}
