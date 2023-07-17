package eu.attempto.dutch_auction_microservices.db_service.auctions.images;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ImagesRepository extends JpaRepository<Image, Long> {}
