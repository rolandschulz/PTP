/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.remotetools.exception;


public class PortForwardingException extends RemoteException {
	private static final long serialVersionUID = -2455858989206677165L;
	
	public static final int REMOTE_PORT_ALREADY_FORWARDED = 1;
	public static final int REMOTE_FORWARDING_FAILED = 2;
	public static final int INVALID_PARAMETERS = 3;
	public static final int REMOTE_FORWARDING_NOT_ATIVE = 4;

	int code;
	
	public PortForwardingException(int code) {
		super();
		this.code = code;
	}
	
	public PortForwardingException(int code, Throwable e) {
		super(e);
		this.code = code;
	}
	
	public String getMessage() {
		switch (code) {
		case REMOTE_PORT_ALREADY_FORWARDED:
			return Messages.getString("PortForwardingException.REMOTE_PORT_ALREADY_FORWARDED"); //$NON-NLS-1$
		case REMOTE_FORWARDING_FAILED:
			return Messages.getString("PortForwardingException.REMOTE_FORWARDING_FAILED"); //$NON-NLS-1$
		case INVALID_PARAMETERS:
			return Messages.getString("PortForwardingException.INVALID_PARAMETERS"); //$NON-NLS-1$
		case REMOTE_FORWARDING_NOT_ATIVE:
			return Messages.getString("PortForwardingException.REMOTE_FORWARDING_NOT_ATIVE"); //$NON-NLS-1$			
		default:
			assert false;
			return null;
		}
	}
}
