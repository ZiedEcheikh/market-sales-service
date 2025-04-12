package tu.stratup.market.sale.utils;


import tu.stratup.market.sale.service.exception.BadRequestException;
import tu.stratup.market.sale.service.exception.InternalServerException;
import tu.stratup.market.sale.service.exception.ResourceNotFoundException;

public class ExceptionResolver {

    public static Integer getStatusCode(final Throwable throwable) {
        return switch (throwable) {
            case BadRequestException bad -> 400;
            case InternalServerException internal -> 500;
            case ResourceNotFoundException notFound -> 204;
            default -> 501;
        };
    }
}
