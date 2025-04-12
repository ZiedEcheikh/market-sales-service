package tu.stratup.market.sale.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity // Enables @PreAuthorize and @PostAuthorize
@Profile("!local")
public class SecurityConfig {

	@Bean
	SecurityWebFilterChain springSecurityFilterChain(final ServerHttpSecurity http) {
		http
				.authorizeExchange(exchanges ->
						exchanges
								.pathMatchers("/actuator/health").permitAll()
								.anyExchange().authenticated()
				)
				.oauth2ResourceServer(oauth2ResourceServer ->
						oauth2ResourceServer
								.jwt(withDefaults())
				);
		return http.build();
	}
}

