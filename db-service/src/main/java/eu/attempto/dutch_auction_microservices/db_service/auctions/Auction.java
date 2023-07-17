package eu.attempto.dutch_auction_microservices.db_service.auctions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.attempto.dutch_auction_microservices.db_service.auctions.images.Image;
import eu.attempto.dutch_auction_microservices.db_service.users.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(indexes = {@Index(columnList = "author"), @Index(columnList = "title")})
public class Auction {
  @Id @GeneratedValue private Long id;

  @ManyToOne
  @JoinColumn(nullable = false, name = "author")
  private User author;

  @OneToMany(mappedBy = "auction")
  private List<Image> images;

  private String title;

  private String description;

  @Builder.Default private Boolean active = true;

  private ZonedDateTime startTime;

  private ZonedDateTime endTime;

  private BigDecimal price;

  @JsonIgnore
  @CreationTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  private ZonedDateTime createdAt;
}
