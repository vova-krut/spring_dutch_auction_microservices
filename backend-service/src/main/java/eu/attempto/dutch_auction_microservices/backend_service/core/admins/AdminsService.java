package eu.attempto.dutch_auction_microservices.backend_service.core.admins;

import eu.attempto.dutch_auction_microservices.backend_service.core.auctions.AuctionsService;
import eu.attempto.dutch_auction_microservices.backend_service.core.auctions.dto.ChangeAuctionDto;
import eu.attempto.dutch_auction_microservices.backend_service.core.users.UsersService;
import eu.attempto.dutch_auction_microservices.backend_service.core.users.dto.CreateUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminsService {
  private final UsersService usersService;
  private final AuctionsService auctionsService;

  public String createUser(CreateUserDto createUserDto) {
    usersService.createUser(createUserDto);
    return "OK";
  }

  public String changeAuction(ChangeAuctionDto changeAuctionDto) {
    return auctionsService.changeAuction(changeAuctionDto);
  }
}
