/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.core.data.impl;

import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.messages.Messages;

public class AclImpl implements IJAXBNonNLSConstants {
	private char acl = 0;

	public AclImpl(char acl) {
		this.acl = acl;
	}

	public AclImpl(String expression) {
		if (expression == null) {
			acl = 0;
			return;
		}
		int len = expression.length();
		if (len == 3 || len == 4) {
			String decimal = convert(expression, 8, 10);
			acl = (char) Integer.parseInt(decimal);
		} else if (len == 9) {
			StringBuffer buffer = new StringBuffer(expression);
			acl9ToBinary(buffer);
			String decimal = convert(buffer.toString(), 2, 10);
			acl = (char) Integer.parseInt(decimal);
		} else {
			throw new IllegalArgumentException(Messages.ACLParseError + expression);
		}
	}

	public String getACL3() {
		return convert(ZEROSTR + (int) acl, 10, 8);
	}

	public String getACL9() {
		StringBuffer binary = new StringBuffer(convert(ZEROSTR + (int) acl, 10, 2));
		binaryToACL9(binary);
		return binary.toString();
	}

	public boolean isAllCanExecute() {
		return isSet(acl, 8);
	}

	public boolean isAllCanRead() {
		return isSet(acl, 6);
	}

	public boolean isAllCanWrite() {
		return isSet(acl, 7);
	}

	public char isChar() {
		return acl;
	}

	public boolean isGroupCanExecute() {
		return isSet(acl, 5);
	}

	public boolean isGroupCanRead() {
		return isSet(acl, 3);
	}

	public boolean isGroupCanWrite() {
		return isSet(acl, 4);
	}

	public boolean isUserCanExecute() {
		return isSet(acl, 2);
	}

	public boolean isUserCanRead() {
		return isSet(acl, 0);
	}

	public boolean isUserCanWrite() {
		return isSet(acl, 1);
	}

	@Override
	public String toString() {
		return getACL3();
	}

	/*
	 * It seems that some ACLs include other characters like 't', 'T', 's' in
	 * the 'x' position. We need a default.
	 */
	private static void acl9ToBinary(StringBuffer acl) {
		for (int i = 0; i < acl.length(); i++) {
			char c = acl.charAt(i);
			switch (c) {
			case '-':
				acl.setCharAt(i, '0');
				break;
			case 'r':
			case 'w':
			case 'x':
			default:
				acl.setCharAt(i, '1');
				break;
			}
		}
	}

	private static void binaryToACL9(StringBuffer binary) {
		for (int i = 0; i < binary.length(); i++) {
			char c = binary.charAt(i);
			switch (c) {
			case '0':
				binary.setCharAt(i, '-');
				break;
			case '1':
				switch (i % 3) {
				case 0:
					binary.setCharAt(i, 'r');
					break;
				case 1:
					binary.setCharAt(i, 'w');
					break;
				case 2:
					binary.setCharAt(i, 'x');
					break;
				}
				break;
			}
		}
	}

	private static int charToDecimal(char c) {
		if (c >= '0' && c <= '9') {
			return c - '0';
		} else if (c >= 'a' && c <= 'z') {
			return 10 + (c - 'a');
		}
		throw new IllegalArgumentException(Messages.CannotConvertToDecimal + c);
	}

	private static String convert(String orig, int from, int to) {
		int place = 1;
		int decimal = 0;
		boolean minus = false;

		if (orig.startsWith(HYPH)) {
			minus = true;
			orig = orig.substring(1);
		}

		int last = orig.length() - 1;

		for (int i = last; i >= 0; i--) {
			char c = orig.charAt(i);
			decimal += (place * charToDecimal(c));
			place *= from;
		}

		StringBuffer converted = new StringBuffer();

		if (decimal == 0) {
			converted.insert(0, Z3);
		} else {
			while (decimal > 0) {
				int digit = decimal % to;
				decimal /= to;
				converted.insert(0, decimalToChar(digit));
			}
		}

		if (minus) {
			converted.insert(0, HYPH);
		}

		return converted.toString();
	}

	private static char decimalToChar(int i) {
		if (i >= 0 && i < 10) {
			return (char) ('0' + i);
		} else if (i >= 10 && i < 35) {
			return (char) ('a' + i - 10);
		}
		throw new IllegalArgumentException(Messages.CannotConvertToChar + i);
	}

	private static boolean isSet(char acl, int bit) {
		String binary = convert(ZEROSTR + (int) acl, 10, 2);
		return '1' == binary.charAt(bit);
	}
}
