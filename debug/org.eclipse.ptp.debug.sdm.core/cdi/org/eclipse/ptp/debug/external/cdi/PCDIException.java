package org.eclipse.ptp.debug.external.cdi;

import org.eclipse.cdt.debug.core.cdi.CDIException;

public class PCDIException extends CDIException {
	/**
	 * All serializable objects should have a stable serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	
	public static final int NOT_IMPLEMENTED = 100;
	public static final int COMMAND_TIMEOUT = 101;
	public static final int INVALID_PROCESS_SET = 102;

	private int status;
	
	public PCDIException(int status, String desc) {
		super(desc);
		this.status = status;
	}
	
	public int getStatus() {
		return this.status;
	}
}
