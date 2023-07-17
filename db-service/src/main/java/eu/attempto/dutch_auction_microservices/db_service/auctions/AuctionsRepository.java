package eu.attempto.dutch_auction_microservices.db_service.auctions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.time.ZonedDateTime;
import java.util.List;

@RepositoryRestResource(excerptProjection = AuctionProjection.class)
public interface AuctionsRepository extends JpaRepository<Auction, Long> {
  List<Auction> findByActiveTrueAndEndTimeAfter(ZonedDateTime dateTime);

  List<Auction> findByActiveFalseAndStartTimeBefore(ZonedDateTime dateTime);

  List<Auction> findByActiveTrueAndEndTimeLessThanEqual(ZonedDateTime dateTime);

  @Query("DELETE FROM Auction a WHERE a.author.id = :authorId")
  List<Auction> deleteByAuthor(Long authorId);

  @Query("SELECT COUNT(a) FROM Auction a WHERE a.createdAt >= :date")
  Long countAuctionsCreatedAfter(ZonedDateTime date);

  @Query("SELECT COUNT(DISTINCT b.user) FROM Bid b WHERE b.auction.id = :auctionId")
  Long countUsersWhoPlacedBidsOnAuction(Long auctionId);

  @Query("SELECT COUNT(b) FROM Bid b WHERE b.auction.id = :auctionId AND b.createdAt >= :date")
  Long countBidsPlacedOnAuctionAfter(Long auctionId, ZonedDateTime date);

  @Query(
      "SELECT AVG(TIMESTAMPDIFF(SECOND, a.startTime, a.endTime)) FROM Auction a WHERE a.active = false")
  Double averageAuctionDuration();

  @Query(
      "SELECT AVG(COALESCE((SELECT COUNT(b) FROM Bid b WHERE b.auction = a), 0)) FROM Auction a WHERE a.active = false")
  Double averageNumberOfBidsPerAuction();
}
