package eu.attempto.dutch_auction_microservices.dashboard_service.events;

import eu.attempto.dutch_auction_microservices.backend_service.core.auctions.Auction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class EventsServiceTest {
  private EventsService eventsService;

  @BeforeEach
  void setUp() {
    eventsService = new EventsService();
  }

  @Test
  void testAddEvent() {
    // Arrange
    var auction = Auction.builder().id(1L).title("Auction 1").build();
    eventsService.addEvent(auction);

    // Act
    var events = eventsService.sendEvents(auction.getId());

    // Assert
    StepVerifier.create(events).expectNext(auction).thenCancel().verify();
  }

  @Test
  void testSendEvents() {
    // Arrange
    var auction1 = Auction.builder().id(1L).title("Auction 1").build();
    eventsService.addEvent(auction1);
    var auction2 = Auction.builder().id(1L).title("Auction 2").build();
    eventsService.addEvent(auction2);

    // Act
    var events = eventsService.sendEvents(auction1.getId());

    // Assert
    StepVerifier.create(events).expectNext(auction1).expectNext(auction2).thenCancel().verify();
  }

  @Test
  void testAddEventAndSubscribe() {
    // Arrange
    var auction = Auction.builder().id(1L).title("Auction 1").build();

    // Act
    var events = eventsService.addEventAndSubscribe(auction);

    // Assert
    StepVerifier.create(events).expectNext(auction).thenCancel().verify();
  }

  @Test
  void testAddEventAndSubscribeWithExistingEvent() {
    // Arrange
    var auction = Auction.builder().id(1L).title("Auction 1").build();
    eventsService.addEvent(auction);

    // Act
    var events = eventsService.addEventAndSubscribe(auction);

    // Assert
    StepVerifier.create(events).expectNext(auction).thenCancel().verify();
  }
}
