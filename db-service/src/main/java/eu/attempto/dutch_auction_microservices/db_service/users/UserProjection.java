package eu.attempto.dutch_auction_microservices.db_service.users;

import eu.attempto.dutch_auction_microservices.db_service.roles.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import java.math.BigDecimal;

@Projection(name = "userProjection", types = User.class)
public interface UserProjection {
  @Value("#{target.id}")
  Long getId();

  String getEmail();

  String getName();

  BigDecimal getBalance();

  Role getRole();
}
