package com.startup.market.sales.handler;

import com.startup.market.sales.constants.Message;
import com.startup.market.sales.handler.dto.SaleCreateRequest;
import com.startup.market.sales.handler.dto.SaleDto;
import com.startup.market.sales.handler.exception.ErrorWebException;
import com.startup.market.sales.mapper.SaleMapper;
import com.startup.market.sales.service.SaleService;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Component
public class SaleHandler {

	private SaleService saleService;
	private Validator validator;

	public SaleHandler(final SaleService saleService, final Validator validator) {
		this.saleService = saleService;
		this.validator = validator;
	}

	public Mono<ServerResponse> createSale(final ServerRequest serverRequest) {
		final Mono<SaleCreateRequest> saleCreateRequest = serverRequest.bodyToMono(SaleCreateRequest.class);
		return saleCreateRequest.flatMap(createRequest -> {
			final Errors errors = new BeanPropertyBindingResult(createRequest, "createRequest");
			validator.validate(createRequest, errors);
			if (errors.hasErrors()) {
				throw new ErrorWebException(Message.BAD_REQUEST_ERROR, msgErrors(errors), 400);
			}
			return ServerResponse.ok()
					.body(saleService.saveNewSale(SaleMapper.saleFrom(createRequest))
							.map(SaleMapper::saleDtoFrom)
							.onErrorResume(throwable -> {
								throw new ErrorWebException(throwable.getMessage(), throwable);
							}), SaleDto.class);
		});

	}

	public Mono<ServerResponse> getSalesByStatus(final ServerRequest serverRequest) {
		final String status = serverRequest.pathVariable("status");
		return ServerResponse.ok()
				.body(saleService.getSalesByStatus(status)
						.map(SaleMapper::saleDtoFrom)
						.onErrorResume(throwable -> {
							throw new ErrorWebException(throwable.getMessage(), throwable);
						}), SaleDto.class);
	}

	private List<String> msgErrors(final Errors errors) {
		final List<String> msgErrors = new ArrayList<>();
		for (final ObjectError error : errors.getAllErrors()) {
			msgErrors.add(error.getDefaultMessage());
		}
		return msgErrors;
	}

}
