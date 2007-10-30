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
package org.eclipse.ptp.remotetools.exception;


public class RemoteExecutionException extends RemoteException {
	private static final long serialVersionUID = 1L;

	private String resultmessage;

	private String errormessage;

	private int exitCode;

	public RemoteExecutionException(String string) {
		super(string);
	}

	public RemoteExecutionException(String string, Exception e) {
		super(string, e);
	}

	public RemoteExecutionException(Exception e) {
		super(e);
	}

	public RemoteExecutionException(String message, int exitcode, String resultmessage, String errormessage) {
		super(message);
		if (resultmessage.length() == 0)
			resultmessage = null;
		if (errormessage.length() == 0)
			errormessage = null;
		this.resultmessage = resultmessage;
		this.errormessage = errormessage;
		this.exitCode = exitcode;
	}

	public String getResultMessage() {
		return resultmessage;
	}

	public String getErrorMessage() {
		if (errormessage == null) {
			return Messages.getString("RemoteExecutionException.GetErrorMessage_NoErrorInfoAvailable"); //$NON-NLS-1$
		} else {
			return errormessage;
		}
	}

	public int getExitCode() {
		return exitCode;
	}

	public String toString() {
		if (exitCode != 0) {
			return getMessage() + "\n" + Messages.getString("RemoteExecutionException.ToString_ExitCode") + exitCode + (resultmessage != null ? "\n" + resultmessage : "") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					+ (errormessage != null ? "\n" + errormessage : ""); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			return super.toString();
		}
	}
}
