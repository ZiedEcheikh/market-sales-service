package com.startup.market.sales.handler.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = DateRangeValidator.class)
@Target({ElementType.TYPE}) // Applies to class level
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDateRange {
	String message() default "Start date must be before end date";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
}
