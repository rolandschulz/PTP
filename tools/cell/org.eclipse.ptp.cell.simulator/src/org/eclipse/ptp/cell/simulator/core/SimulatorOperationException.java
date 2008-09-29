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

package org.eclipse.ptp.cell.simulator.core;

import org.eclipse.osgi.util.NLS;

/**
* Raised on a {@link ISimulatorControl} method that is waiting for simulator
* operation to complete, but the operation fails.
* 
* @author Daniel Felix Ferber
* 
*/
public class SimulatorOperationException extends SimulatorException {
	private static final long serialVersionUID = -4045023542123850789L;
	
	public static final int UNKNOWN = 0;
	public static final int CREATE_WORKING_DIRECTORY = 1;
	public static final int DEPLOY_FILE = 2;
	public static final int WORKING_DIRECTORY_READONLY = 3;
	public static final int EXISTING_SIMULATOR_INSTANCE = 4;
	public static final int UNEXPECTED_EXCEPTION = 6;

	public static final int PATH_NOT_ABSOLUTE = 10001;
	public static final int PATH_NOT_EXIST = 10002;
	public static final int PATH_NOT_FILE = 10003;
	public static final int PATH_NOT_DIRECTORY = 10004;
	public static final int PATH_NOT_READABLE = 10005;
	public static final int PATH_NOT_WRITABLE = 10006;



	int reason;
	String what;
	
	public SimulatorOperationException(int reason) {
		super(translatedMessage(reason, null));
		this.reason = reason;
	}
	
	public SimulatorOperationException(int reason, Throwable cause) {
		super(translatedMessage(reason, null));
		this.reason = reason;
	}
	
	public SimulatorOperationException(int reason, String what, Throwable cause) {
		super(translatedMessage(reason, what));
		this.reason = reason;
		this.what = what;
	}

	public SimulatorOperationException(int reason, String what) {
		super(translatedMessage(reason, what));
		this.reason = reason;
		this.what = what;
	}

	public int getReason() {
		return reason;
	}
	
	public SimulatorOperationException() {
		super(Messages.SimulatorOperationException_DefaultMessage);
	}

	public SimulatorOperationException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public SimulatorOperationException(String arg0) {
		super(arg0);
	}

	public SimulatorOperationException(Throwable arg0) {
		super(arg0);
	}
	
	private static String translatedMessage(int reason, String what) {
		String message = null;
		String thiswhat = what;
		if (thiswhat == null) {
			thiswhat = Messages.SimulatorOperationException_Unknown;
		}
		switch (reason) {
		case CREATE_WORKING_DIRECTORY:
			message = NLS.bind(Messages.SimulatorOperationException_CreateWorkingDirFailed, thiswhat);
			break;
		case DEPLOY_FILE:
			message = NLS.bind(Messages.SimulatorOperationException_DeployFileFailed, thiswhat);
			break;
		case WORKING_DIRECTORY_READONLY:
			message = NLS.bind(Messages.SimulatorOperationException_WorkingDirNotWriteable, thiswhat);
			break;
		case EXISTING_SIMULATOR_INSTANCE:
			message = NLS.bind(Messages.SimulatorOperationException_WorkingDirInuse, null);
			break;			
		case UNEXPECTED_EXCEPTION:
			message = NLS.bind(Messages.SimulatorOperationException_UnexpectedException, null);
			break;			
		case PATH_NOT_ABSOLUTE:
			message = NLS.bind(Messages.SimulatorOperationException_PathNotAbsolute, thiswhat);
			break;
		case PATH_NOT_EXIST:
			message = NLS.bind(Messages.SimulatorOperationException_PathDoesNotExist, thiswhat);
			break;
		case PATH_NOT_FILE:
			message = NLS.bind(Messages.SimulatorOperationException_PathNotFile, thiswhat);
			break;
		case PATH_NOT_DIRECTORY:
			message = NLS.bind(Messages.SimulatorOperationException_PathNotDir, thiswhat);
			break;
		case PATH_NOT_READABLE:
			message = NLS.bind(Messages.SimulatorOperationException_PathNotReadable, thiswhat);
			break;
		case PATH_NOT_WRITABLE:
			message = NLS.bind(Messages.SimulatorOperationException_PathNotWriteable, thiswhat);
			break;
		}
		return message;
	}
}