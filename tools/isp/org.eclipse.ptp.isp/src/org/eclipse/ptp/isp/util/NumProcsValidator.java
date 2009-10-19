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

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.ptp.isp.messages.Messages;

/**
 * A simple validator for the NumProcs InputDialog.
 */
public class NumProcsValidator implements IInputValidator {

	/**
	 * Checks if the new value for number of procs is valid.
	 * 
	 * @param newText
	 *            The new value that we will check.
	 * @return String The current Value which is the newText unless it was
	 *         invalid.
	 */
	public String isValid(String newText) {
		int num = -1;
		try {
			num = Integer.parseInt(newText);

		} catch (NumberFormatException nfe) {
			return Messages.NumProcsValidator_0;
		}
		if (num < 1) {
			return Messages.NumProcsValidator_1;
		}
		if (num > 64) {
			return Messages.NumProcsValidator_2;
		} else {
			return null;
		}
	}

}