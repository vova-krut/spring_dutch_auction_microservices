package eu.attempto.dutch_auction_microservices.db_service.users;

import eu.attempto.dutch_auction_microservices.db_service.roles.Role;
import eu.attempto.dutch_auction_microservices.db_service.roles.RolesEnum;
import eu.attempto.dutch_auction_microservices.db_service.roles.RolesRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

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
    Assertions.assertThat(dbUser).isPresent();
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
    Assertions.assertThat(dbUser).isEmpty();
  }
}
