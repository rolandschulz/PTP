/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.launch.data;

/**
 * Stores information about how the remote launch finished execution.
 * @author Daniel Felix Ferber
 */
public class ExecutionResult {
	int exitValue = 0;
	int status = UNDEFINED;
	
	// Execution result status for remote application
	/** The execution of application has noy yet finished. The execution resulta has not meaning yest. */
	public static final int UNDEFINED = 0;
	/** The execution of application has finished succesfully with zero as exit value. */
	public static final int SUCCESS = 1;
	/** The execution of application has finished succesfully, but with nonzero exit value. */
	public static final int SUCCESS_WITH_CODE = 2;
	/** The execution of the application was cancelled because the the user terminated/killed the process. */
	public static final int CANCELLED = 3;
	/** The execution of the application was terminated by the remote operating system. */
	public static final int EXCEPTION = 4;
	/** The command used to launch the application was not accepted by the remote operating system. */
	public static final int COMMAND_ERROR = 5;
	/** The execution of application failed, but no information is available. */
	public static final int UNKNOWN = 6;
	/** The launcher failed due an internal error. */
	public static final int ERROR = 7;
	
	public int getExitValue() {
		return exitValue;
	}

	public void setExitValue(int exitValue) {
		this.exitValue = exitValue;
	}
	
	public int getStatus() {
		return status;
	}
	
	public void setStatus(int status) {
		this.status = status;
	}
}
