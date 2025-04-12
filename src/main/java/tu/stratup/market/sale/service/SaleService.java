package tu.stratup.market.sale.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tu.stratup.market.sale.constants.Message;
import tu.stratup.market.sale.constants.Status;
import tu.stratup.market.sale.items.SaleItem;
import tu.stratup.market.sale.repositrory.SaleRepository;
import tu.stratup.market.sale.service.exception.BadRequestException;
import tu.stratup.market.sale.service.exception.InternalServerException;
import tu.stratup.market.sale.service.exception.ResourceNotFoundException;
import tu.stratup.market.sale.utils.DateTime;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SaleService {

	private SaleRepository saleRepository;

	public SaleService(final SaleRepository saleRepository) {
		this.saleRepository = saleRepository;
	}

	public Mono<SaleItem> saveNewSale(final SaleItem sale) {
		sale.setCreateAt(DateTime.localDateToISO(LocalDateTime.now()));
		sale.setStatus(Status.DRAFT.name());
		return saleRepository.createSale(sale).
				onErrorMap(this::mapToException);
	}

	public Flux<SaleItem> getSalesByStatus(final String status) {
		final Status saleStatus = Status.findByValue(status);
		final Flux<SaleItem> result;
		if (saleStatus != null) {
			//@formatter:off
			result = saleRepository.retrieveByStatus(saleStatus)
					.map(page -> {
						final List<SaleItem> items = page.items();
						if (items.isEmpty()) {
							throw new ResourceNotFoundException(Message.NO_DATA_FOUND);
						}
						return items.stream().collect(Collectors.toList());
					})
					.flatMap(Flux::fromIterable)
					.onErrorMap(this::mapToException);
			//@formatter:on
		} else {
			result = Flux.error(new BadRequestException("Status sale not valid"));
		}
		return result;
	}

	private RuntimeException mapToException(final Throwable throwable) {
		final RuntimeException exception;

		if (throwable instanceof ResourceNotFoundException) {
			exception = new ResourceNotFoundException(throwable.getMessage());
		} else {
			exception = new InternalServerException(Message.INTERNAL_SERVER_ERROR);
		}
		return exception;
	}
}
