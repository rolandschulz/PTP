package org.eclipse.ptp.rm.jaxb.core.exceptions;

public class PropertyResolutionException extends Exception {
	private static final long serialVersionUID = 1031L;

	public PropertyResolutionException() {
	}

	public PropertyResolutionException(String message) {
		super(message);
	}

	public PropertyResolutionException(String message, Throwable cause) {
		super(message, cause);
	}

	public PropertyResolutionException(Throwable cause) {
		super(cause);
	}

}
