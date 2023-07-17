package eu.attempto.dutch_auction_microservices.db_service.auctions.images;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.attempto.dutch_auction_microservices.db_service.auctions.Auction;
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
@Table(
    indexes = @Index(columnList = "auctionId"),
    uniqueConstraints = @UniqueConstraint(columnNames = "link"))
public class Image {
  @Id @GeneratedValue private Long id;

  @ManyToOne
  @JoinColumn(nullable = false, name = "auctionId")
  private Auction auction;

  private String link;

  @JsonIgnore
  @CreationTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  private ZonedDateTime createdAt;
}
