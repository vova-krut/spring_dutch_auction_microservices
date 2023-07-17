package eu.attempto.dutch_auction_microservices.db_service.bids;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@RepositoryRestResource(excerptProjection = BidProjection.class)
public interface BidsRepository extends JpaRepository<Bid, Long> {
  @Query("SELECT b FROM Bid b WHERE b.user.id = :userId")
  List<Bid> findByUser(Long userId);

  @Query("DELETE FROM Bid b WHERE b.user.id = :userId")
  List<Bid> deleteByUser(Long userId);

  @Query("SELECT b FROM Bid b WHERE b.auction.id = :auctionId ORDER BY b.createdAt DESC LIMIT 1")
  Optional<Bid> findLastAuctionBid(Long auctionId);

  @Query("SELECT COUNT(b) FROM Bid b WHERE b.user.id = :userId AND b.createdAt >= :date")
  Long countBidsPlacedByUserAfter(Long userId, ZonedDateTime date);

  @Query("SELECT COUNT(DISTINCT b.auction) FROM Bid b WHERE b.user.id = :userId")
  Long countAuctionsOnWhichUserPlacedBids(Long userId);
}
