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
package org.eclipse.ptp.internal.debug.core;

import java.math.BigInteger;

/**
 * @author Clement chu
 * 
 */
public class ExtFormat {
	public final static int HEXADECIMAL = 0;
	public final static int OCTAL = 1;
	public final static int BINARY = 2;
	public final static int DECIMAL = 3;
	public final static int RAW = 4;
	public final static int NATURAL = 5;

	public final static int FLOAT = 10;
	public final static int ADDRESS = 11;
	public final static int INSTRUCTION = 12;
	public final static int CHAR = 13;
	public final static int STRING = 14;
	public final static int UNSIGNED = 15;

	public static BigInteger decodeAdress(String buffer) {
		int radix = 10;
		int cursor = 0;
		int offset = 0;
		int len = buffer.length();

		if ((offset = buffer.indexOf("0x")) != -1 || (offset = buffer.indexOf("0X")) != -1) { //$NON-NLS-1$ //$NON-NLS-2$
			radix = 16;
			cursor = offset + 2;
		}
		while (cursor < len && Character.digit(buffer.charAt(cursor), radix) != -1) {
			cursor++;
		}
		return getBigInteger(buffer.substring(offset, cursor));
	}
	
	public static BigInteger getBigInteger(String address) {
		int index = 0;
		int radix = 10;
		boolean negative = false;

		// Handle zero length
		address = address.trim();
		if (address.length() == 0) {
			return BigInteger.ZERO;
		}

		// Handle minus sign, if present
		if (address.startsWith("-")) { //$NON-NLS-1$
			negative = true;
			index++;
		}
		if (address.startsWith("0x", index) || address.startsWith("0X", index)) { //$NON-NLS-1$ //$NON-NLS-2$
			index += 2;
			radix = 16;
		} else if (address.startsWith("#", index)) { //$NON-NLS-1$
			index ++;
			radix = 16;
		} else if (address.startsWith("0", index) && address.length() > 1 + index) { //$NON-NLS-1$
			index ++;
			radix = 8;
		}

		if (index > 0) {
			address = address.substring(index);
		}
		if (negative) {
			address = "-" + address; //$NON-NLS-1$
		}
		try {
			return new BigInteger(address, radix);
		} catch (NumberFormatException e) {
			// What can we do ???
		}
		return BigInteger.ZERO;
	}	
}
