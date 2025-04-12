package tu.stratup.market.sale.handler.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import tu.stratup.market.sale.handler.validator.ValidDateRange;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@ValidDateRange
public class SaleCreateRequest {
	@NotBlank(message = "Title is required")
	private String title;
	@NotBlank(message = "Description is required")
	private String description;
	@NotNull(message = "Start date is required")
	private LocalDateTime startDate;
	@NotNull(message = "End date is required")
	private LocalDateTime endDate;
}
