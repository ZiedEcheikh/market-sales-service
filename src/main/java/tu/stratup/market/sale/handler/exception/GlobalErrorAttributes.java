package tu.stratup.market.sale.handler.exception;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.HashMap;
import java.util.Map;

@Component
public class GlobalErrorAttributes extends DefaultErrorAttributes {


    @Override
    public Map<String, Object> getErrorAttributes(final ServerRequest request, final ErrorAttributeOptions options) {
		final Map<String, Object> errorMap = new HashMap<>();
		final ErrorWebException error = (ErrorWebException) getError(request);
		errorMap.put("message", error.getMessage());
		errorMap.put("status", error.getStatusCode());
		if(error.getErrors() != null && !error.getErrors().isEmpty()){
			errorMap.put("errors", error.getErrors());
		}
		return errorMap;
    }
}
