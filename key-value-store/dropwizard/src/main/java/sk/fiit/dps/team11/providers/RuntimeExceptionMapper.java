package sk.fiit.dps.team11.providers;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeExceptionMapper.class);
	
	private static final Map<Integer, BiConsumer<RuntimeException, Response>> statusHandlers;
	static {
		statusHandlers = new HashMap<>();
		statusHandlers.put(404, RuntimeExceptionMapper::handle404);
	}
	
	private static void handle404(RuntimeException e, Response r) {
		LOGGER.error("Not-found: {}", r.getLocation());
	}
	
	public Response toResponse(RuntimeException exception) {
		
		if (!(exception instanceof WebApplicationException)) {
			LOGGER.error("Uncaught runtime exception: ", exception);
		}
		
		Response r = null;
		
		// Try to extract a response
		try {
			Method getResponse = exception.getClass().getMethod("getResponse");
			Object ret = getResponse.invoke(exception);
			if (ret instanceof Response) {
				r = (Response) ret;
			}
		} catch (Exception e) {
			r = Response.serverError().build();
		}

		int status = r.getStatus();
		
		if (statusHandlers.containsKey(status)) {
			statusHandlers.get(status).accept(exception, r);
		}
		
		return r;
	}

}
