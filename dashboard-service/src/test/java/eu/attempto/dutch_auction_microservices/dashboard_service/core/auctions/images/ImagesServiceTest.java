package eu.attempto.dutch_auction_microservices.dashboard_service.core.auctions.images;

import eu.attempto.dutch_auction_microservices.backend_service.core.auctions.Auction;
import eu.attempto.dutch_auction_microservices.backend_service.files.FilesService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImagesServiceTest {
  @Mock private ImagesRepository imagesRepository;
  @Mock private FilesService filesService;
  @InjectMocks private ImagesService imagesService;

  @Test
  void saveImage_SavesImageToDiskAndLinkToDb() {
    // Arrange
    var auction = new Auction();
    var file = new MockMultipartFile("test.png", new byte[] {});
    var fileLink = "fileLink";
    when(filesService.saveImage(Mockito.any(MultipartFile.class))).thenReturn(fileLink);
    given(imagesRepository.save(Mockito.any(Image.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    // Act
    var result = imagesService.saveImage(auction, file);

    // Assert
    assertThat(result.getAuction()).isEqualTo(auction);
    assertThat(result.getLink()).isEqualTo(fileLink);
  }
}
