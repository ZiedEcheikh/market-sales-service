package com.startup.market.sales.handler.validator;

import com.startup.market.sales.handler.dto.SaleCreateRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

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
