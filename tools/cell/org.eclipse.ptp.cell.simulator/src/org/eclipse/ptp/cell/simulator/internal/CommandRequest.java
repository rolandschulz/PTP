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
package org.eclipse.ptp.cell.simulator.internal;

public class CommandRequest implements ITSimRequest {
	String command = null;
	
	public CommandRequest(String command) {
		super();
		this.command = command;
	}

	public String getQuery() {
		return command;
	}

	public void parseResponse(String[] response) {
		// Do nothing
	}

	public static CommandRequest stopCommand() {
		return new CommandRequest("simstop"); //$NON-NLS-1$
	}
}
