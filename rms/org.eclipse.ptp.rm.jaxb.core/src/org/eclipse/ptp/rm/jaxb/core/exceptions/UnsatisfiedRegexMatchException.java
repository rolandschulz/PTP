package org.eclipse.ptp.rm.jaxb.core.exceptions;

public class UnsatisfiedRegexMatchException extends Exception {

	private static final long serialVersionUID = 4521238998263940220L;

	public UnsatisfiedRegexMatchException() {
	}

	public UnsatisfiedRegexMatchException(String message) {
		super(message);
	}

	public UnsatisfiedRegexMatchException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnsatisfiedRegexMatchException(Throwable cause) {
		super(cause);
	}
}
