package tu.stratup.market.sale.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {

	@Bean
	public CorsWebFilter corsWebFilter() {
		final CorsConfiguration corsConfig = new CorsConfiguration();
		corsConfig.setAllowedOrigins(Arrays.asList("http://localhost:4200")); // Allow requests from this origin
		corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")); // Allowed HTTP methods
		corsConfig.setAllowedHeaders(Arrays.asList("*")); // Allowed headers
		corsConfig.setAllowCredentials(true); // Allow credentials (e.g., cookies)
		corsConfig.setMaxAge(3600L); // Max age for CORS preflight requests

		final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", corsConfig); // Apply to all endpoints

		return new CorsWebFilter(source);
	}
}
