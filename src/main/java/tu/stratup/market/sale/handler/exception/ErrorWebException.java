package tu.stratup.market.sale.handler.exception;


import tu.stratup.market.sale.utils.ExceptionResolver;

import java.util.ArrayList;
import java.util.List;

public class ErrorWebException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private final Integer statusCode;

	private final List<String> errors=new ArrayList<>();

	public ErrorWebException(final String message, final Integer statusCode) {
		super(message);
		this.statusCode = statusCode;
	}

	public ErrorWebException(final String message, final List<String> errors, final Integer statusCode) {
		super(message);
		this.errors.addAll(errors);
		this.statusCode = statusCode;
	}

	public ErrorWebException(final String message, final Throwable exception) {
		super(message);
		this.statusCode = ExceptionResolver.getStatusCode(exception);
	}

	public Integer getStatusCode() {
		return statusCode;
	}

	public List<String> getErrors() {
		return errors;
	}

	public void setErrors(final List<String> errors) {
		this.errors.addAll(errors);
	}
}
