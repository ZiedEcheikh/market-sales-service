package com.startup.market.sales.utils;


import com.startup.market.sales.service.exception.BadRequestException;
import com.startup.market.sales.service.exception.InternalServerException;
import com.startup.market.sales.service.exception.ResourceNotFoundException;

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
