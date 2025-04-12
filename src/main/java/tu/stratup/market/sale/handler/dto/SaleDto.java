package tu.stratup.market.sale.handler.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SaleDto {
	private String identify;
	private String title;
	private String description;
	private String status;
	private LocalDateTime startDate;
	private LocalDateTime endDate;
}
