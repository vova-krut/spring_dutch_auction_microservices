package eu.attempto.dutch_auction_microservices.dashboard_service.files;

import eu.attempto.dutch_auction_microservices.backend_service.exceptions.BadRequestException;
import eu.attempto.dutch_auction_microservices.backend_service.exceptions.InternalServerException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class FilesServiceTest {
  private FilesService filesService;
  private final String host = "http://example.com/";
  private final String uploadPath = "upload/";

  @BeforeEach
  void setUp() {
    filesService = new FilesService();
    ReflectionTestUtils.setField(filesService, "uploadPath", uploadPath);
    ReflectionTestUtils.setField(filesService, "host", host);
  }

  @AfterEach
  void tearDown() {
    var file = new File(uploadPath);
    file.delete();
  }

  @Test
  void saveImage_ValidImage_ReturnsImageLink() throws IOException {
    // Arrange
    var mockImage = mock(MockMultipartFile.class);
    when(mockImage.isEmpty()).thenReturn(false);
    when(mockImage.getContentType()).thenReturn("image/jpeg");
    when(mockImage.getOriginalFilename()).thenReturn("image.jpg");

    // Act
    var result = filesService.saveImage(mockImage);

    // Assert
    assertThat(result).startsWith(host).endsWith(".jpg");
    verify(mockImage, times(1)).transferTo(Mockito.any(File.class));
  }

  @Test
  void saveImage_NullImage_ThrowsBadRequest() {
    // Act
    var result =
        assertThrows(
            BadRequestException.class,
            () -> filesService.saveImage(null),
            "Expected to throw when trying to save null");

    // Assert
    assertThat(result).hasMessage("Auction requires an image");
  }

  @Test
  void saveImage_ImageIsEmpty_ThrowsBadRequest() {
    // Arrange
    var mockImage = mock(MockMultipartFile.class);
    when(mockImage.isEmpty()).thenReturn(true);

    // Act
    var result =
        assertThrows(
            BadRequestException.class,
            () -> filesService.saveImage(mockImage),
            "Expected to throw when trying to save empty image");

    // Assert
    assertThat(result).hasMessage("Auction requires an image");
  }

  @Test
  void saveImage_FileNotImage_ThrowsBadRequest() {
    // Arrange
    var mockImage = mock(MockMultipartFile.class);
    when(mockImage.isEmpty()).thenReturn(false);
    when(mockImage.getContentType()).thenReturn("text");

    // Act
    var result =
        assertThrows(
            BadRequestException.class,
            () -> filesService.saveImage(mockImage),
            "Expected to throw when trying to save a file that is not an image");

    // Assert
    assertThat(result).hasMessageContaining("Invalid file type");
  }

  @Test
  void saveImage_FileHasNoExtension_ThrowsBadRequest() {
    // Arrange
    var mockImage = mock(MockMultipartFile.class);
    when(mockImage.isEmpty()).thenReturn(false);
    when(mockImage.getContentType()).thenReturn("image/jpeg");
    when(mockImage.getOriginalFilename()).thenReturn("image");

    // Act
    var result =
        assertThrows(
            BadRequestException.class,
            () -> filesService.saveImage(mockImage),
            "Expected to throw when trying to save a file that does not have extension");

    // Assert
    assertThat(result).hasMessage("File does not have extension");
  }

  @Test
  void saveImage_FileHasNoOriginalFilename_ThrowsInternalException() {
    // Arrange
    var mockImage = mock(MockMultipartFile.class);
    when(mockImage.isEmpty()).thenReturn(false);
    when(mockImage.getContentType()).thenReturn("image/jpeg");
    when(mockImage.getOriginalFilename()).thenReturn(null);

    // Act
    var result =
        assertThrows(
            InternalServerException.class,
            () -> filesService.saveImage(mockImage),
            "Expected to throw when trying to save a file that does not have originalFilename");

    // Assert
    assertThat(result).hasMessage("Error while writing a file");
  }

  @Test
  void saveImage_TransferToThrowsIOException_CatchesItAndThrowsInternalException()
      throws IOException {
    // Arrange
    var ioException = "IO exception";
    var mockImage = mock(MockMultipartFile.class);
    when(mockImage.isEmpty()).thenReturn(false);
    when(mockImage.getContentType()).thenReturn("image/jpeg");
    when(mockImage.getOriginalFilename()).thenReturn("image.jpg");
    doThrow(new IOException(ioException)).when(mockImage).transferTo(Mockito.any(File.class));

    // Act
    var result =
        assertThrows(
            InternalServerException.class,
            () -> filesService.saveImage(mockImage),
            "Expected to rethrow InternalServerException when transferTo throws IOException");

    // Assert
    assertThat(result).hasMessage("Error while writing a file");
  }

  @Test
  void saveImage_ExistingFolder_DoesNotCreateOne() {
    // Arrange
    var mockImage = mock(MockMultipartFile.class);
    when(mockImage.isEmpty()).thenReturn(false);
    when(mockImage.getContentType()).thenReturn("image/jpeg");
    when(mockImage.getOriginalFilename()).thenReturn("image.jpg");
    ReflectionTestUtils.setField(filesService, "uploadPath", "target/");

    // Act
    var result = filesService.saveImage(mockImage);

    // Assert
    assertThat(result).startsWith(host).endsWith(".jpg");
  }
}
