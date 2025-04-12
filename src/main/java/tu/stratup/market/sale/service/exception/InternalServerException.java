package tu.stratup.market.sale.service.exception;

public class InternalServerException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public InternalServerException(final String message) {
		super(message);
	}
}
