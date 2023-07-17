package eu.attempto.dutch_auction_microservices.db_service.users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.attempto.dutch_auction_microservices.db_service.roles.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
    name = "_users",
    indexes = @Index(columnList = "email"),
    uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class User {
  @Id @GeneratedValue private Long id;

  private String email;

  private String name;

  @Builder.Default private BigDecimal balance = new BigDecimal(0);

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(nullable = false)
  private Role role;

  @JsonIgnore
  @CreationTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  private ZonedDateTime createdAt;
}
