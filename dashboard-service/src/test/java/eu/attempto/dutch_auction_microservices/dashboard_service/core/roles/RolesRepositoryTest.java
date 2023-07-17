package eu.attempto.dutch_auction_microservices.dashboard_service.core.roles;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class RolesRepositoryTest {
  @Autowired private RolesRepository rolesRepository;

  @AfterEach
  void tearDown() {
    rolesRepository.deleteAll();
  }

  @Test
  void findByName_RoleExists() {
    // Arrange
    var roleName = RolesEnum.USER;
    var role = Role.builder().name(roleName).build();
    rolesRepository.save(role);

    // Act
    var dbRole = rolesRepository.findByName(roleName);

    // Assert
    assertThat(dbRole).isPresent();
  }

  @Test
  void findByName_RoleDoesNotExist() {
    // Arrange
    var roleName = RolesEnum.USER;

    // Act
    var dbRole = rolesRepository.findByName(roleName);

    // Assert
    assertThat(dbRole).isEmpty();
  }
}
