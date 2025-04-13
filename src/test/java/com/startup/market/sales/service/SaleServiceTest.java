package com.startup.market.sales.service;

import com.startup.market.sales.constants.Message;
import com.startup.market.sales.constants.Status;
import com.startup.market.sales.items.SaleItem;
import com.startup.market.sales.repositrory.SaleRepository;
import com.startup.market.sales.service.exception.BadRequestException;
import com.startup.market.sales.service.exception.InternalServerException;
import com.startup.market.sales.service.exception.ResourceNotFoundException;
import com.startup.market.sales.utils.JsonReader;
import com.startup.market.sales.utils.ListUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaleServiceTest {

	@Mock
	private SaleRepository saleRepository;

	@InjectMocks
	private SaleService saleService;

	@Test
	void shouldGetSalesByStatus() {
		final String status = "draft";
		final List<SaleItem> sales = JsonReader.readJsonFileAsList("/data/sales/models/sales.json", SaleItem.class);
		final Page<SaleItem> salesPage = Page.builder(SaleItem.class)
				.items(sales).build();
		when(saleRepository.retrieveByStatus(Status.DRAFT))
				.thenReturn(Flux.just(salesPage));
		final Flux<SaleItem> salesByStatus = saleService.getSalesByStatus(status);
		StepVerifier
				.create(salesByStatus)
				.expectNextMatches(saleByStatus -> ListUtils.existsInList(sales, sale -> sale.getIdentify().equals(saleByStatus.getIdentify())))
				.expectNextMatches(saleByStatus -> ListUtils.existsInList(sales, sale -> sale.getIdentify().equals(saleByStatus.getIdentify())))
				.expectNextMatches(saleByStatus -> ListUtils.existsInList(sales, sale -> sale.getIdentify().equals(saleByStatus.getIdentify())))
				.verifyComplete();
	}

	@Test
	void shouldGetSalesByStatusNoElements() {
		final String status = "draft";
		final List<SaleItem> sales = new ArrayList<>();
		final Page<SaleItem> salessPage = Page.builder(SaleItem.class)
				.items(sales)
				.build();
		when(saleRepository.retrieveByStatus(Status.DRAFT))
				.thenReturn(Flux.just(salessPage));
		final Flux<SaleItem> salesByStatus = saleService.getSalesByStatus(status);
		StepVerifier
				.create(salesByStatus)
				.expectErrorMatches(
						throwable -> throwable instanceof ResourceNotFoundException
								&& Message.NO_DATA_FOUND.equalsIgnoreCase(throwable.getMessage()))
				.verify();
	}

	@Test
	void shouldGetSalesByStatusSeverError() {
		final String status = "draft";
		when(saleRepository.retrieveByStatus(Status.DRAFT))
				.thenReturn(Flux.error(new RuntimeException("Error happened during retrieve sales by status" + status)));
		final Flux<SaleItem> salesByStatus = saleService.getSalesByStatus(status);
		StepVerifier
				.create(salesByStatus)
				.expectErrorMatches(throwable -> throwable instanceof InternalServerException
						&& Message.INTERNAL_SERVER_ERROR.equalsIgnoreCase(throwable.getMessage()))
				.verify();
	}

	@Test
	void shouldGetSalesByStatusBadRequestException() {
		final String status = "finished";
		final Flux<SaleItem> salesByStatus = saleService.getSalesByStatus(status);
		StepVerifier
				.create(salesByStatus)
				.expectErrorMatches(throwable -> throwable instanceof BadRequestException
						&& "Status sale not valid".equalsIgnoreCase(throwable.getMessage()))
				.verify();
	}

	@Test
	void shouldSaveNewSale() {
		final SaleItem sale = JsonReader.readJsonFile("/data/sales/models/sale.json", SaleItem.class);
		when(saleRepository.createSale(any()))
				.thenReturn(Mono.just(sale));
		final Mono<SaleItem> futureSavedSale = saleService.saveNewSale(sale);
		StepVerifier
				.create(futureSavedSale)
				.expectNextMatches(savedSale -> savedSale.getTitle().equals(sale.getTitle()))
				.verifyComplete();
	}

	@Test
	void shouldSaveNewSaleServerError() {
		final SaleItem sale = JsonReader.readJsonFile("/data/sales/models/sale.json", SaleItem.class);
		when(saleRepository.createSale(any()))
				.thenReturn(Mono.error(new RuntimeException("Error happened during creating sale")));
		final Mono<SaleItem> savedSale = saleService.saveNewSale(sale);
		StepVerifier
				.create(savedSale)
				.expectErrorMatches(throwable -> throwable instanceof InternalServerException
						&& Message.INTERNAL_SERVER_ERROR.equalsIgnoreCase(throwable.getMessage()))
				.verify();
	}

}
