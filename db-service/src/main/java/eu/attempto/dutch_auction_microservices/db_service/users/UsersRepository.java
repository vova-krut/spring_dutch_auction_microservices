package eu.attempto.dutch_auction_microservices.db_service.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.time.ZonedDateTime;
import java.util.Optional;

@RepositoryRestResource(excerptProjection = UserProjection.class)
public interface UsersRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String email);

  @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :date")
  Long countUsersCreatedAfter(ZonedDateTime date);

  @Query("SELECT COUNT(DISTINCT b.user) FROM Bid b WHERE b.createdAt >= :date")
  Long countUsersWhoPlacedBidsAfter(ZonedDateTime date);

  @Query("SELECT COUNT(DISTINCT a.author) FROM Auction a WHERE a.createdAt >= :date")
  Long countUsersWhoCreatedAuctionsAfter(ZonedDateTime date);
}
