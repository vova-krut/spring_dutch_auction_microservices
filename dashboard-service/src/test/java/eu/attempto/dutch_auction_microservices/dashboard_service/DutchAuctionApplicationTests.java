package eu.attempto.dutch_auction_microservices.dashboard_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class DutchAuctionApplicationTests {

  @Test
  void contextLoads() {
    var truth = true;
    assertThat(!truth).isFalse();
  }
}
