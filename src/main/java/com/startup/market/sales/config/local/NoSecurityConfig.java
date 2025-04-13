package com.startup.market.sales.config.local;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@Profile("local")
public class NoSecurityConfig {

	@Bean
	public SecurityWebFilterChain securityWebFilterChain(final ServerHttpSecurity http) {
		return http
				.authorizeExchange(exchanges -> exchanges
						.anyExchange().permitAll()
				)
				.csrf(ServerHttpSecurity.CsrfSpec::disable)
				.build();
	}
}
