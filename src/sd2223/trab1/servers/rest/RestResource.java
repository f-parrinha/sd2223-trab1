package sd2223.trab1.servers.rest;

import sd2223.trab1.api.java.Result;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;

import java.util.concurrent.atomic.AtomicLong;

public class RestResource {

	/** Constants */
	public static AtomicLong num_seq = new AtomicLong(0);

	/**
	 * Given a Result<T>, either returns the value, or throws the JAX-WS Exception
	 * matching the error code...
	 */
	protected <T> T fromJavaResult(Result<T> result) {
		if (result.isOK()) {
			num_seq.addAndGet(1);
			return result.value();
		}
		else {
			throw new WebApplicationException(statusCodeFrom(result));
		}
	}

	/**
	 * Translates a Result<T> to a HTTP Status code
	 */
	private static Status statusCodeFrom(Result<?> result) {
		return switch (result.error()) {
			case CONFLICT -> Status.CONFLICT;
			case NOT_FOUND -> Status.NOT_FOUND;
			case FORBIDDEN -> Status.FORBIDDEN;
			case TIMEOUT, BAD_REQUEST -> Status.BAD_REQUEST;
			case NOT_IMPLEMENTED -> Status.NOT_IMPLEMENTED;
			case INTERNAL_ERROR -> Status.INTERNAL_SERVER_ERROR;
			case OK -> result.value() == null ? Status.NO_CONTENT : Status.OK;
			default -> Status.INTERNAL_SERVER_ERROR;
		};
	}

}