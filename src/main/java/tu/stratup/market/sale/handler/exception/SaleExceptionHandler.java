package tu.stratup.market.sale.handler.exception;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public final class SaleExceptionHandler extends AbstractErrorWebExceptionHandler {

    private final ServerCodecConfigurer serverCodecConfigurer;

    public SaleExceptionHandler(final ErrorAttributes errorAttributes,
            final WebProperties.Resources resources,
            final ApplicationContext applicationContext,
            final ServerCodecConfigurer serverCodecConfigurer) {
        super(errorAttributes, resources, applicationContext);
        this.serverCodecConfigurer = serverCodecConfigurer;
        initializeMessageReaders();
    }

    private void initializeMessageReaders() {
        super.setMessageReaders(this.serverCodecConfigurer.getReaders());
        super.setMessageWriters(this.serverCodecConfigurer.getWriters());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(final ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderException);
    }

    private Mono<ServerResponse> renderException(final ServerRequest serverRequest) {
        final Map<String, Object> error = this.getErrorAttributes(serverRequest, ErrorAttributeOptions.defaults());
        return ServerResponse
                .status(HttpStatus.valueOf((Integer) error.get("status")))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(error));
    }
}