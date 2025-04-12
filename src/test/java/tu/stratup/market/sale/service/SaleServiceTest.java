package tu.stratup.market.sale.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import tu.stratup.market.sale.constants.Message;
import tu.stratup.market.sale.constants.Status;
import tu.stratup.market.sale.items.SaleItem;
import tu.stratup.market.sale.repositrory.SaleRepository;
import tu.stratup.market.sale.service.exception.BadRequestException;
import tu.stratup.market.sale.service.exception.InternalServerException;
import tu.stratup.market.sale.service.exception.ResourceNotFoundException;
import tu.stratup.market.sale.utils.JsonReader;
import tu.stratup.market.sale.utils.ListUtils;

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
		final List<SaleItem> sales = JsonReader.readJsonFileAsList("/data/sale/models/sales.json", SaleItem.class);
		final Page salesPage = Page.builder(SaleItem.class)
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
		final Page salessPage = Page.builder(SaleItem.class)
				.items(sales)
				.build();
		when(saleRepository.retrieveByStatus(Status.DRAFT))
				.thenReturn(Flux.just(salessPage));
		final Flux<SaleItem> salesByStatus = saleService.getSalesByStatus(status);
		StepVerifier
				.create(salesByStatus)
				.expectErrorMatches(
						throwable -> throwable instanceof ResourceNotFoundException
								&& throwable.getMessage().equalsIgnoreCase(Message.NO_DATA_FOUND))
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
						&& throwable.getMessage().equalsIgnoreCase(Message.INTERNAL_SERVER_ERROR))
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
		final SaleItem sale = JsonReader.readJsonFile("/data/sale/models/sale.json", SaleItem.class);
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
		final SaleItem sale = JsonReader.readJsonFile("/data/sale/models/sale.json", SaleItem.class);
		when(saleRepository.createSale(any()))
				.thenReturn(Mono.error(new RuntimeException("Error happened during creating sale")));
		final Mono<SaleItem> savedSale = saleService.saveNewSale(sale);
		StepVerifier
				.create(savedSale)
				.expectErrorMatches(throwable -> throwable instanceof InternalServerException
						&& throwable.getMessage().equalsIgnoreCase(Message.INTERNAL_SERVER_ERROR))
				.verify();
	}

}
