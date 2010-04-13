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

import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.remotetools.core.messages.Messages;


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
			return Messages.RemoteExecutionException_0;
		} else {
			return errormessage;
		}
	}

	public int getExitCode() {
		return exitCode;
	}

	public String toString() {
		if (exitCode != 0) {
			String msg = NLS.bind(Messages.RemoteExecutionException_1, new Object[] {getMessage(), exitCode});
			if (resultmessage != null) {
				msg += "\n" + resultmessage; //$NON-NLS-1$
			}
			if (errormessage != null ) {
				msg += "\n" + errormessage; //$NON-NLS-1$
			}
			return msg;
		} else {
			return super.toString();
		}
	}
}
