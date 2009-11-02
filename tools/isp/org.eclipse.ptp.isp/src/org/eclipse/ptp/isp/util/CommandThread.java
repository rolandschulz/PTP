/*******************************************************************************
 * Copyright (c) 2009 University of Utah School of Computing
 * 50 S Central Campus Dr. 3190 Salt Lake City, UT 84112
 * http://www.cs.utah.edu/formal_verification/
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alan Humphrey - Initial API and implementation
 *    Christopher Derrick - Initial API and implementation
 *    Prof. Ganesh Gopalakrishnan - Project Advisor
 *******************************************************************************/

package org.eclipse.ptp.isp.util;

import java.io.IOException;

import org.eclipse.ptp.isp.messages.Messages;

public class CommandThread implements Runnable {

	private String command;

	/**
	 * Sets the command to be whatever was passed in
	 * 
	 * @param String
	 *            cmd The new command String
	 */
	public CommandThread(String cmd) {
		this.command = cmd;
	}

	public void run() {
		try {
			Runtime.getRuntime().exec(this.command);
		} catch (IOException ioe) {
			IspUtilities.showExceptionDialog(Messages.CommandThread_0, ioe);
			IspUtilities.logError(Messages.CommandThread_1, ioe);
		}

	}

}
