package eu.attempto.dutch_auction_microservices.dashboard_service.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    var moduleName = "Dashboard for Dutch Auction";
    var apiVersion = "1.0.0";
    var apiTitle = String.format("%s API", StringUtils.capitalize(moduleName));
    return new OpenAPI()
        .components(new Components())
        .info(new Info().title(apiTitle).version(apiVersion));
  }
}
