/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.mpi.openmpi.core.rtsystem;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author Greg Watson
 *
 */
public class OpenMPIErrorParser {
	private OpenMPIErrorParser() {
		// Do not allow instances.
	}

	private final static Pattern pattern = Pattern.compile("(\\*\\*\\* MPI_ERRORS_ARE_FATAL.*)"); //$NON-NLS-1$
	
	private static String errorMessage;

	/**
	 * Parse output line for error messages.
	 * 
	 * @param line output line to parse
	 * @return true if error detected
	 */
	public static boolean parse(String line) {
		Matcher matcher = pattern.matcher(line);
		if (matcher.matches()) {
			setErrorMessage(matcher.group(1));
			return true;
		}
		
		return false;
	}
	
	public static String getErrorMessage() {
		return errorMessage;
	}
	
	public static void setErrorMessage(String message) {
		errorMessage = message;
	}
}

