package eu.attempto.dutch_auction_microservices.backend_service.core.auctions.images;

import eu.attempto.dutch_auction_microservices.backend_service.core.auctions.Auction;
import eu.attempto.dutch_auction_microservices.backend_service.files.FilesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class ImagesService {
  private final ImagesRepository imagesRepository;
  private final FilesService filesService;

  public Image saveImage(Auction auction, MultipartFile imageFile) {
    var link = filesService.saveImage(imageFile);
    var image = Image.builder().auction(auction).link(link).build();

    return imagesRepository.save(image);
  }
}
