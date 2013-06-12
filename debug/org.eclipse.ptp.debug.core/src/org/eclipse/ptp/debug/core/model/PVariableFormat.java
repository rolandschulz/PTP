/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.debug.core.model;

import org.eclipse.ptp.internal.debug.core.messages.Messages;

/**
 * Variable formats
 * 
 * @author clement
 * 
 */
public class PVariableFormat {
	public static final PVariableFormat BINARY = new PVariableFormat(Messages.PVariableFormat_2);
	public static final PVariableFormat DECIMAL = new PVariableFormat(Messages.PVariableFormat_1);
	public static final PVariableFormat HEXADECIMAL = new PVariableFormat(Messages.PVariableFormat_4);
	public static final PVariableFormat NATURAL = new PVariableFormat(Messages.PVariableFormat_0);
	public static final PVariableFormat OCTAL = new PVariableFormat(Messages.PVariableFormat_3);

	/**
	 * @param code
	 * @return
	 */
	public static PVariableFormat getFormat(int code) {
		switch (code) {
		case 0:
			return NATURAL;
		case 1:
			return DECIMAL;
		case 2:
			return BINARY;
		case 3:
			return OCTAL;
		case 4:
			return HEXADECIMAL;
		default:
			return DECIMAL;
		}
	}

	private final String fName;

	private PVariableFormat(String name) {
		fName = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return fName;
	}
}
