package tu.stratup.market.sale.mapper;

import tu.stratup.market.sale.handler.dto.SaleCreateRequest;
import tu.stratup.market.sale.handler.dto.SaleDto;
import tu.stratup.market.sale.items.SaleItem;

import java.time.LocalDateTime;

public class SaleMapper {

	public static SaleItem saleFrom(final SaleCreateRequest saleCreateRequest) {
		final SaleItem sale = new SaleItem();
		sale.setTitle(saleCreateRequest.getTitle());
		sale.setDescription(saleCreateRequest.getDescription());
		if (saleCreateRequest.getStartDate() != null) {
			sale.setStartDate(saleCreateRequest.getStartDate().toString());
		}
		if (saleCreateRequest.getEndDate() != null) {
			sale.setEndDate(saleCreateRequest.getEndDate().toString());
		}

		return sale;
	}

	public static SaleDto saleDtoFrom(final SaleItem sale) {
		return SaleDto.builder()
				.identify(sale.getIdentify())
				.title(sale.getTitle())
				.description(sale.getDescription())
				.status(sale.getStatus())
				.startDate(LocalDateTime.parse(sale.getStartDate()))
				.endDate(LocalDateTime.parse(sale.getEndDate()))
				.build();
	}
}
