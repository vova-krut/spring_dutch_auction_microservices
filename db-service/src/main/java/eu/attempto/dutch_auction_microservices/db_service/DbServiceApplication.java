package eu.attempto.dutch_auction_microservices.db_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class DbServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(DbServiceApplication.class, args);
  }
}
