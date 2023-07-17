package eu.attempto.dutch_auction_microservices.db_service.bids;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.attempto.dutch_auction_microservices.db_service.auctions.Auction;
import eu.attempto.dutch_auction_microservices.db_service.users.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(indexes = {@Index(columnList = "userId"), @Index(columnList = "auctionId")})
public class Bid {
  @Id @GeneratedValue private Long id;

  @ManyToOne
  @JoinColumn(name = "userId", nullable = false)
  private User user;

  @ManyToOne
  @JoinColumn(name = "auctionId", nullable = false)
  private Auction auction;

  @JsonIgnore
  @CreationTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  private ZonedDateTime createdAt;
}
