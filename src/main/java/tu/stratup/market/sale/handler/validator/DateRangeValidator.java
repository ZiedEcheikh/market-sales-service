package tu.stratup.market.sale.handler.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import tu.stratup.market.sale.handler.dto.SaleCreateRequest;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, SaleCreateRequest> {

	@Override
	public boolean isValid(final SaleCreateRequest request, final ConstraintValidatorContext context) {
		boolean isValid = true;

		if (request.getStartDate() != null && request.getEndDate() != null) {
			isValid = request.getStartDate().isBefore(request.getEndDate());
		}
		return isValid;
	}
}
