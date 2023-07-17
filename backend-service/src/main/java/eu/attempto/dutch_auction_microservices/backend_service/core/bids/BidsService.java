package eu.attempto.dutch_auction_microservices.backend_service.core.bids;

import eu.attempto.dutch_auction_microservices.backend_service.core.auctions.Auction;
import eu.attempto.dutch_auction_microservices.backend_service.core.auctions.AuctionsService;
import eu.attempto.dutch_auction_microservices.backend_service.core.bids.dto.PlaceBidDto;
import eu.attempto.dutch_auction_microservices.backend_service.core.users.User;
import eu.attempto.dutch_auction_microservices.backend_service.core.users.UsersRepository;
import eu.attempto.dutch_auction_microservices.backend_service.events.EventsService;
import eu.attempto.dutch_auction_microservices.backend_service.exceptions.BadRequestException;
import eu.attempto.dutch_auction_microservices.backend_service.schedules.ScheduleType;
import eu.attempto.dutch_auction_microservices.backend_service.schedules.SchedulesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class BidsService {
  private final BidsRepository bidsRepository;
  private final UsersRepository usersRepository;
  private final AuctionsService auctionsService;
  private final SchedulesService schedulesService;
  private final EventsService eventsService;

  private final BigDecimal bidPrice = new BigDecimal(1);

  public String placeBid(User user, PlaceBidDto placeBidDto) {
    var auction = auctionsService.getAuctionById(placeBidDto.getAuctionId());

    validateBid(user, auction);
    schedulesService.removeAuctionSchedule(auction.getId(), ScheduleType.FINISH);

    var bid = Bid.builder().user(user).auction(auction).build();

    var future1 = CompletableFuture.supplyAsync(() -> payForBid(user));
    var future2 = CompletableFuture.supplyAsync(() -> auctionsService.placeBid(auction));
    var future3 = CompletableFuture.supplyAsync(() -> bidsRepository.save(bid));

    // Wait for all tasks to complete
    CompletableFuture.allOf(future1, future2, future3).join();

    schedulesService.scheduleAuctionFinish(auction, user);
    eventsService.addEvent(auction);

    return "OK";
  }

  private void validateBid(User user, Auction auction) {
    if (!auction.getActive()) {
      throw new BadRequestException("This auction is not active");
    }

    if (Objects.equals(user.getId(), auction.getAuthor().getId())) {
      throw new BadRequestException("You can't bid on your own auction");
    }

    var comparisonValue = auction.getPrice().multiply(new BigDecimal(2)).add(bidPrice);

    if (user.getBalance().compareTo(comparisonValue) < 0) {
      throw new BadRequestException(
          "You don't have enough coins to make a bid and pay for the lot");
    }
  }

  private User payForBid(User user) {
    user.setBalance(user.getBalance().subtract(bidPrice));
    return usersRepository.save(user);
  }

  public List<Bid> getUserBids(User user) {
    return bidsRepository.findByUser(user);
  }

  public List<Bid> deleteUserBids(User user) {
    return bidsRepository.deleteByUser(user);
  }
}
