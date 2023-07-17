package eu.attempto.dutch_auction_microservices.dashboard_service.core.roles;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RolesServiceTest {
  @Mock private RolesRepository rolesRepository;
  @InjectMocks private RolesService rolesService;

  @Test
  void getRole_ExistingName_ReturnsExistingRole() {
    // Arrange
    var roleName = RolesEnum.USER;
    var role = Role.builder().id(1L).name(roleName).build();
    when(rolesRepository.findByName(Mockito.any(RolesEnum.class))).thenReturn(Optional.of(role));

    // Act
    var result = rolesService.getRole(roleName);

    // Assert
    assertThat(result).isEqualTo(role);
    verify(rolesRepository, times(1)).findByName(roleName);
  }

  @Test
  void getRole_NonExistingName_ReturnsCreatedRole() {
    // Arrange
    var roleName = RolesEnum.USER;
    when(rolesRepository.findByName(Mockito.any(RolesEnum.class))).thenReturn(Optional.empty());
    given(rolesRepository.save(Mockito.any(Role.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    // Act
    var result = rolesService.getRole(roleName);

    // Assert
    assertThat(result.getName()).isEqualTo(roleName);
    verify(rolesRepository, times(1)).save(Mockito.any(Role.class));
  }
}
