package eu.attempto.dutch_auction_microservices.dashboard_service.schedules;

import eu.attempto.dutch_auction_microservices.backend_service.core.auctions.Auction;
import eu.attempto.dutch_auction_microservices.backend_service.core.auctions.AuctionsRepository;
import eu.attempto.dutch_auction_microservices.backend_service.core.bids.Bid;
import eu.attempto.dutch_auction_microservices.backend_service.core.bids.BidsRepository;
import eu.attempto.dutch_auction_microservices.backend_service.core.users.User;
import eu.attempto.dutch_auction_microservices.backend_service.core.users.UsersRepository;
import eu.attempto.dutch_auction_microservices.backend_service.events.EventsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchedulesServiceTest {
  @Mock private TaskScheduler taskScheduler;
  @Mock private UsersRepository usersRepository;
  @Mock private AuctionsRepository auctionsRepository;
  @Mock private BidsRepository bidsRepository;
  @Mock private EventsService eventsService;
  @Mock private Map<String, ScheduledFuture<?>> scheduledTasks;
  @InjectMocks private SchedulesService schedulesService;

  @Test
  void scheduleAuctionStart_ValidStartTime_SchedulesAuctionStart() {
    // Arrange
    var auction =
        Auction.builder()
            .id(1L)
            .title("Auction")
            .startTime(ZonedDateTime.now().plusMinutes(2))
            .active(false)
            .build();
    var scheduledTask = mock(ScheduledFuture.class);
    var callbackCaptor = ArgumentCaptor.forClass(Runnable.class);
    when(taskScheduler.schedule(any(Runnable.class), any(Instant.class))).thenReturn(scheduledTask);

    // Act
    schedulesService.scheduleAuctionStart(auction);

    // Assert
    assert auction.getStartTime() != null;
    verify(taskScheduler, times(1))
        .schedule(callbackCaptor.capture(), eq(auction.getStartTime().toInstant()));
    verify(scheduledTasks, times(1)).put("1-START", scheduledTask);

    // Arrange
    var callback = callbackCaptor.getValue();

    // Act
    callback.run();

    // Assert
    verify(auctionsRepository, times(1)).save(auction);
    verify(eventsService, times(1)).addEvent(auction);
  }

  @Test
  void scheduleAuctionStart_NoStartTime_DoesNothing() {
    // Arrange
    var auction = Auction.builder().id(1L).title("Auction").active(true).build();

    // Act
    schedulesService.scheduleAuctionStart(auction);

    // Assert
    verifyNoInteractions(taskScheduler);
    verifyNoInteractions(scheduledTasks);
  }

  @Test
  void scheduleAuctionFinish_IncludingUser_AddsPayingToAuctionAuthor() {
    // Arrange
    var author = new User();
    var auction =
        Auction.builder()
            .id(1L)
            .title("Auction")
            .endTime(ZonedDateTime.now().plusMinutes(2))
            .price(BigDecimal.ONE)
            .author(author)
            .active(true)
            .build();
    var user = User.builder().name("User").balance(BigDecimal.TEN).build();
    var scheduledTask = mock(ScheduledFuture.class);
    var callbackCaptor = ArgumentCaptor.forClass(Runnable.class);
    when(taskScheduler.schedule(any(Runnable.class), any(Instant.class))).thenReturn(scheduledTask);

    // Act
    schedulesService.scheduleAuctionFinish(auction, user);

    // Assert
    verify(taskScheduler, times(1))
        .schedule(callbackCaptor.capture(), eq(auction.getEndTime().toInstant()));
    verify(scheduledTasks, times(1)).put("1-FINISH", scheduledTask);

    // Arrange
    var userCaptor = ArgumentCaptor.forClass(User.class);
    var callback = callbackCaptor.getValue();

    // Act
    callback.run();

    // Assert
    verify(auctionsRepository, times(1)).save(auction);
    verify(usersRepository, times(2)).save(userCaptor.capture());
    verify(eventsService, times(1)).addEvent(auction);
    assertThat(userCaptor.getAllValues().get(0)).isEqualTo(user);
    assertThat(userCaptor.getAllValues().get(1)).isEqualTo(author);
    assertThat(user.getBalance()).isEqualTo(new BigDecimal(8));
    assertThat(author.getBalance()).isEqualTo(new BigDecimal(2));
  }

  @Test
  void scheduleAuctionFinish_NoUser_NoPayingToAuctionAuthor() {
    // Arrange
    var auction =
        Auction.builder()
            .id(1L)
            .title("Auction")
            .endTime(ZonedDateTime.now().plusMinutes(2))
            .active(true)
            .build();
    var scheduledTask = mock(ScheduledFuture.class);
    var callbackCaptor = ArgumentCaptor.forClass(Runnable.class);
    when(taskScheduler.schedule(any(Runnable.class), any(Instant.class))).thenReturn(scheduledTask);

    // Act
    schedulesService.scheduleAuctionFinish(auction, null);

    // Assert
    verify(taskScheduler, times(1))
        .schedule(callbackCaptor.capture(), eq(auction.getEndTime().toInstant()));
    verify(scheduledTasks, times(1)).put("1-FINISH", scheduledTask);

    // Arrange
    var callback = callbackCaptor.getValue();

    // Act
    callback.run();

    // Assert
    verify(auctionsRepository, times(1)).save(auction);
    verifyNoInteractions(usersRepository);
    verify(eventsService, times(1)).addEvent(auction);
  }

  @Test
  void removeAuctionSchedule_ExistingSchedule_RemovesItFromTasksTable() {
    // Arrange
    var id = 1L;
    var type = ScheduleType.FINISH;
    var key = id + "-" + type;
    var scheduledTask = mock(ScheduledFuture.class);
    when(scheduledTasks.get(key)).thenReturn(scheduledTask);

    // Act
    schedulesService.removeAuctionSchedule(id, type);

    // Assert
    verify(scheduledTasks, times(1)).remove(key);
    verify(scheduledTask, times(1)).cancel(true);
  }

  @Test
  void removeAuctionSchedule_NonExistingSchedule_DoesNothing() {
    // Arrange
    var id = 1L;
    var type = ScheduleType.FINISH;
    var key = id + "-" + type;
    when(scheduledTasks.get(key)).thenReturn(null);

    // Act
    schedulesService.removeAuctionSchedule(id, type);

    // Assert
    verify(scheduledTasks, never()).remove(key);
  }

  @Test
  void onApplicationStart_ScheduleActiveAuctions() {
    // Arrange
    var onGoingAuctions =
        List.of(
            Auction.builder().id(1L).endTime(ZonedDateTime.now().plusMinutes(1)).build(),
            Auction.builder().id(2L).endTime(ZonedDateTime.now().plusMinutes(2)).build());
    var testAuctionBids =
        List.of(Bid.builder().auction(onGoingAuctions.get(0)).user(new User()).build());

    var scheduledStartOnGoingAuctionsTask = mock(ScheduledFuture.class);

    var taskSchedulerEndTimeCaptor = ArgumentCaptor.forClass(Instant.class);
    var scheduledTasksKeysCaptor = ArgumentCaptor.forClass(String.class);

    when(auctionsRepository.findByActiveTrueAndEndTimeAfter(any())).thenReturn(onGoingAuctions);
    when(bidsRepository.findByAuctionOrderByCreatedAtDesc(onGoingAuctions.get(0)))
        .thenReturn(testAuctionBids);
    when(bidsRepository.findByAuctionOrderByCreatedAtDesc(onGoingAuctions.get(1)))
        .thenReturn(List.of());
    when(taskScheduler.schedule(any(Runnable.class), any(Instant.class)))
        .thenReturn(scheduledStartOnGoingAuctionsTask);

    // Act
    schedulesService.onApplicationStart();

    // Assert
    verify(taskScheduler, times(onGoingAuctions.size()))
        .schedule(any(Runnable.class), taskSchedulerEndTimeCaptor.capture());
    verify(scheduledTasks, times(onGoingAuctions.size()))
        .put(scheduledTasksKeysCaptor.capture(), any());

    for (var i = 0; i < taskSchedulerEndTimeCaptor.getAllValues().size(); i++) {
      assertThat(taskSchedulerEndTimeCaptor.getAllValues().get(i))
          .isEqualTo(onGoingAuctions.get(i).getEndTime().toInstant());
    }
    for (var i = 0; i < scheduledTasksKeysCaptor.getAllValues().size(); i++) {
      assertThat(scheduledTasksKeysCaptor.getAllValues().get(i))
          .isEqualTo(onGoingAuctions.get(i).getId() + "-" + ScheduleType.FINISH);
    }
  }

  @Test
  void onApplicationStart_ScheduleStartingAuctions() {
    // Arrange
    var startingAuctions =
        List.of(
            Auction.builder().id(1L).startTime(ZonedDateTime.now().plusMinutes(1)).build(),
            Auction.builder().id(2L).startTime(ZonedDateTime.now().plusMinutes(2)).build());

    var scheduledStartAuctionsTask = mock(ScheduledFuture.class);

    var taskSchedulerStartTimeCaptor = ArgumentCaptor.forClass(Instant.class);
    var scheduledTasksKeysCaptor = ArgumentCaptor.forClass(String.class);

    when(auctionsRepository.findByActiveFalseAndStartTimeBefore(any()))
        .thenReturn(startingAuctions);
    when(taskScheduler.schedule(any(Runnable.class), any(Instant.class)))
        .thenReturn(scheduledStartAuctionsTask);

    // Act
    schedulesService.onApplicationStart();

    // Assert
    verify(taskScheduler, times(startingAuctions.size()))
        .schedule(any(Runnable.class), taskSchedulerStartTimeCaptor.capture());
    verify(scheduledTasks, times(startingAuctions.size()))
        .put(scheduledTasksKeysCaptor.capture(), any());
    for (var i = 0; i < taskSchedulerStartTimeCaptor.getAllValues().size(); i++) {
      assert startingAuctions.get(i).getStartTime() != null;
      assertThat(taskSchedulerStartTimeCaptor.getAllValues().get(i))
          .isEqualTo(startingAuctions.get(i).getStartTime().toInstant());
    }
    for (var i = 0; i < scheduledTasksKeysCaptor.getAllValues().size(); i++) {
      assertThat(scheduledTasksKeysCaptor.getAllValues().get(i))
          .isEqualTo(startingAuctions.get(i).getId() + "-" + ScheduleType.START);
    }
  }

  @Test
  void onApplicationStart_DeactivateExpiredAuctions() {
    // Arrange
    var expiredAuctions =
        List.of(
            Auction.builder().id(1L).endTime(ZonedDateTime.now().minusMinutes(1)).build(),
            Auction.builder().id(2L).endTime(ZonedDateTime.now().minusMinutes(2)).build());
    var savedAuctionsCaptor = ArgumentCaptor.forClass(Auction.class);
    when(auctionsRepository.findByActiveTrueAndEndTimeLessThanEqual(any()))
        .thenReturn(expiredAuctions);

    // Act
    schedulesService.onApplicationStart();

    // Assert
    verify(auctionsRepository, times(expiredAuctions.size())).save(savedAuctionsCaptor.capture());
    for (var i = 0; i < savedAuctionsCaptor.getAllValues().size(); i++) {
      assertThat(savedAuctionsCaptor.getAllValues().get(i)).isEqualTo(expiredAuctions.get(i));
    }
  }
}
