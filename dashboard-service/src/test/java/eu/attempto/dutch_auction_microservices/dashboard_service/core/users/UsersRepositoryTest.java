package eu.attempto.dutch_auction_microservices.dashboard_service.core.users;

import eu.attempto.dutch_auction_microservices.backend_service.core.roles.Role;
import eu.attempto.dutch_auction_microservices.backend_service.core.roles.RolesEnum;
import eu.attempto.dutch_auction_microservices.backend_service.core.roles.RolesRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class UsersRepositoryTest {
  @Autowired private UsersRepository usersRepository;
  @Autowired private RolesRepository rolesRepository;

  @AfterEach
  void tearDown() {
    usersRepository.deleteAll();
    rolesRepository.deleteAll();
  }

  @Test
  void findByEmail_UserExists_ReturnsUser() {
    // Arrange
    var email = "email@test.com";
    var user = User.builder().name("User").email(email).role(getUserRole()).build();
    usersRepository.save(user);

    // Act
    var dbUser = usersRepository.findByEmail(email);

    // Assert
    assertThat(dbUser).isPresent();
  }

  private Role getUserRole() {
    var role = Role.builder().name(RolesEnum.USER).build();
    return rolesRepository.save(role);
  }

  @Test
  void findByEmail_UserDoesNotExist_ReturnsEmptyOptional() {
    // Arrange
    var email = "otherEmail@test.com";

    // Act
    var dbUser = usersRepository.findByEmail(email);

    // Assert
    assertThat(dbUser).isEmpty();
  }
}
