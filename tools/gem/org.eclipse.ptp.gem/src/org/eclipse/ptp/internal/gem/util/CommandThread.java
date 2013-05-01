/*******************************************************************************
 * Copyright (c) 2009, 2013 University of Utah School of Computing
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

package org.eclipse.ptp.internal.gem.util;

import java.io.IOException;

/**
 * This class serves to execute the specified string as a command via the
 * java.lang.Runtime instance.
 */
public class CommandThread implements Runnable {

	private final String command;

	/**
	 * Sets the command to be whatever was passed in. This thread executes the
	 * specified command via the java.lang.Runtime instance.
	 * 
	 * @param cmd
	 *            The new command String.
	 */
	public CommandThread(String cmd) {
		this.command = cmd;
	}

	public void run() {
		try {
			Runtime.getRuntime().exec(this.command);
		} catch (final IOException e) {
			GemUtilities.logExceptionDetail(e);
		}
	}

}
