package tu.stratup.market.sale.router;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import tu.stratup.market.sale.handler.SaleHandler;

@Component
public class SaleRouter {
	private SaleHandler saleHandler;

	public SaleRouter(final SaleHandler saleHandler) {
		this.saleHandler = saleHandler;
	}

	@Bean
	public WebProperties.Resources resources() {
		return new WebProperties.Resources();
	}

	@Bean
	public RouterFunction<ServerResponse> salesRoutes() {
		return RouterFunctions
				.route(RequestPredicates.GET("/sales/status/{status}"),
						saleHandler::getSalesByStatus)
				.andRoute(RequestPredicates.POST("/sales"),
						saleHandler::createSale);
	}
}
