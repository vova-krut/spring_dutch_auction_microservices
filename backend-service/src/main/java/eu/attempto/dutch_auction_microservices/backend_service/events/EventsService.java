package eu.attempto.dutch_auction_microservices.backend_service.events;

import eu.attempto.dutch_auction_microservices.backend_service.core.auctions.Auction;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EventsService {
  private final Map<Long, Sinks.Many<Auction>> eventMap = new ConcurrentHashMap<>();

  public void addEvent(Auction event) {
    var auctionId = event.getId();
    Sinks.Many<Auction> subject = getOrCreateSubject(auctionId);
    subject.tryEmitNext(event);
  }

  public Flux<Auction> sendEvents(Long auctionId) {
    Sinks.Many<Auction> subject = getOrCreateSubject(auctionId);
    return subject.asFlux();
  }

  private Sinks.Many<Auction> getOrCreateSubject(Long auctionId) {
    return eventMap.computeIfAbsent(auctionId, k -> Sinks.many().replay().limit(100));
  }

  public Flux<Auction> addEventAndSubscribe(Auction event) {
    if (!eventMap.containsKey(event.getId())) {
      addEvent(event);
    }

    return sendEvents(event.getId());
  }
}
