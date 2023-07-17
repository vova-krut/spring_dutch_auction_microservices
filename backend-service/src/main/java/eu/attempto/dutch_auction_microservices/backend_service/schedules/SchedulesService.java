package eu.attempto.dutch_auction_microservices.backend_service.schedules;

import eu.attempto.dutch_auction_microservices.backend_service.core.auctions.Auction;
import eu.attempto.dutch_auction_microservices.backend_service.core.auctions.AuctionsRepository;
import eu.attempto.dutch_auction_microservices.backend_service.core.bids.Bid;
import eu.attempto.dutch_auction_microservices.backend_service.core.bids.BidsRepository;
import eu.attempto.dutch_auction_microservices.backend_service.core.users.User;
import eu.attempto.dutch_auction_microservices.backend_service.core.users.UsersRepository;
import eu.attempto.dutch_auction_microservices.backend_service.events.EventsService;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;

@Component
@RequiredArgsConstructor
public class SchedulesService {
  private final TaskScheduler taskScheduler;
  private final UsersRepository usersRepository;
  private final AuctionsRepository auctionsRepository;
  private final BidsRepository bidsRepository;
  private final EventsService eventsService;
  private final Map<String, ScheduledFuture<?>> scheduledTasks;

  public void scheduleAuctionStart(Auction auction) {
    var startTime = auction.getStartTime();
    if (startTime == null) {
      return;
    }

    Runnable callback =
        () -> {
          startAuction(auction);
          eventsService.addEvent(auction);
        };

    var scheduledTask = taskScheduler.schedule(callback, startTime.toInstant());

    scheduledTasks.put(auction.getId() + "-" + ScheduleType.START, scheduledTask);
  }

  public void scheduleAuctionFinish(Auction auction, @Nullable User user) {
    Runnable callback =
        () -> {
          finishAuction(auction);
          if (user != null) {
            payToAuctionAuthor(user, auction);
          }
          eventsService.addEvent(auction);
        };

    var endTime = auction.getEndTime().toInstant();
    var scheduledTask = taskScheduler.schedule(callback, endTime);

    scheduledTasks.put(auction.getId() + "-" + ScheduleType.FINISH, scheduledTask);
  }

  public void removeAuctionSchedule(Long auctionId, ScheduleType type) {
    var key = auctionId + "-" + type;
    var scheduledTask = scheduledTasks.get(key);
    if (scheduledTask != null) {
      scheduledTask.cancel(true);
      scheduledTasks.remove(key);
    }
  }

  public void onApplicationStart() {
    var future1 = CompletableFuture.supplyAsync(this::scheduleActiveAuctions);
    var future2 = CompletableFuture.supplyAsync(this::scheduleStartingAuctions);
    var future3 = CompletableFuture.supplyAsync(this::deactivateExpiredAuctions);
    // Wait for all tasks to complete
    CompletableFuture.allOf(future1, future2, future3).join();
  }

  private String scheduleActiveAuctions() {
    var activeAuctions = getActiveAuctions();
    for (var activeAuction : activeAuctions) {
      var bid = bidsRepository.findLastAuctionBid(activeAuction);
      scheduleAuctionFinish(activeAuction, bid.map(Bid::getUser).orElse(null));
    }

    return "OK";
  }

  private List<Auction> getActiveAuctions() {
    return auctionsRepository.findByActiveTrueAndEndTimeAfter(ZonedDateTime.now());
  }

  private String scheduleStartingAuctions() {
    var auctionsToStart = getAuctionsToStart();
    for (var auctionToStart : auctionsToStart) {
      scheduleAuctionStart(auctionToStart);
    }

    return "OK";
  }

  private List<Auction> getAuctionsToStart() {
    return auctionsRepository.findByActiveFalseAndStartTimeBefore(ZonedDateTime.now());
  }

  private String deactivateExpiredAuctions() {
    var expiredAuctions = getExpiredAuctions();
    for (var expiredAuction : expiredAuctions) {
      expiredAuction.setActive(false);
      auctionsRepository.save(expiredAuction);
    }

    return "OK";
  }

  private List<Auction> getExpiredAuctions() {
    return auctionsRepository.findByActiveTrueAndEndTimeLessThanEqual(ZonedDateTime.now());
  }

  private void startAuction(Auction auction) {
    auction.setActive(true);
    auctionsRepository.save(auction);
  }

  private void finishAuction(Auction auction) {
    auction.setActive(false);
    auctionsRepository.save(auction);
  }

  private void payToAuctionAuthor(User user, Auction auction) {
    var totalSum = auction.getPrice().multiply(new BigDecimal(2));
    user.setBalance(user.getBalance().subtract(totalSum));
    auction.getAuthor().setBalance(auction.getAuthor().getBalance().add(totalSum));

    var future1 = CompletableFuture.supplyAsync(() -> usersRepository.save(user));
    var future2 = CompletableFuture.supplyAsync(() -> usersRepository.save(auction.getAuthor()));

    // Wait for all tasks to complete
    CompletableFuture.allOf(future1, future2).join();
  }
}
