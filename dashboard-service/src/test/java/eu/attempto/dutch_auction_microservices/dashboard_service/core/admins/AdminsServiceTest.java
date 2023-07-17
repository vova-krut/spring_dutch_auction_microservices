package eu.attempto.dutch_auction_microservices.dashboard_service.core.admins;

import eu.attempto.dutch_auction_microservices.backend_service.auth.AuthService;
import eu.attempto.dutch_auction_microservices.backend_service.auth.dto.RegistrationDto;
import eu.attempto.dutch_auction_microservices.backend_service.core.auctions.AuctionsService;
import eu.attempto.dutch_auction_microservices.backend_service.core.auctions.dto.ChangeAuctionDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminsServiceTest {
  @Mock private AuthService authService;
  @Mock private AuctionsService auctionsService;
  @InjectMocks private AdminsService adminsService;

  @Test
  void createAdmin_ReturnsOk() {
    // Arrange
    var registrationDto =
        RegistrationDto.builder().name("Name").email("email@test.com").password("password").build();
    when(authService.registration(registrationDto, true)).thenReturn(null);

    // Act
    var result = adminsService.createAdmin(registrationDto);

    // Assert
    assertThat(result).isEqualTo("OK");
    verify(authService, times(1)).registration(registrationDto, true);
  }

  @Test
  void changeAuction_ReturnsOk() {
    // Arrange
    var changeAuctionDto = ChangeAuctionDto.builder().id(1L).price(BigDecimal.ONE).build();
    when(auctionsService.changeAuction(changeAuctionDto)).thenReturn("OK");

    // Act
    var result = adminsService.changeAuction(changeAuctionDto);

    // Assert
    assertThat(result).isEqualTo("OK");
    verify(auctionsService, times(1)).changeAuction(changeAuctionDto);
  }
}
