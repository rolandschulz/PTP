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

package org.eclipse.ptp.debug.internal.core.aif;

import org.eclipse.ptp.debug.core.aif.IAIFTypeChar;
import org.eclipse.ptp.debug.core.aif.IAIFValueChar;
import org.eclipse.ptp.debug.core.cdi.PCDIException;

public class AIFValueChar extends AIFValueIntegral implements IAIFValueChar {
	public AIFValueChar(IAIFTypeChar type, byte[] data) {
		super(type, data);
	}
	public String getValueString() throws PCDIException {
		if (result == null) {
			result = String.valueOf(charValue());
		}
		return result;
	}
	public char charValue() throws PCDIException {
		return (char)byteValue();
	}
	public byte byteValue() throws PCDIException {
		return data[0];
	}
	public String toString() {
		try {
			char charValue = charValue();
			return ((Character.isISOControl(charValue) && charValue != '\b' && charValue != '\t' && charValue != '\n' && charValue != '\f' && charValue != '\r') || charValue < 0) ? "" : String.valueOf(charValue);		
		} catch (PCDIException e) {
			return "err: " + e.getMessage();
		}
	}
	public int getBufferLength() {
		return 1;
	}	

	/*
	public String toString() {
		String ch = ""+(char)val+"";
		
		switch (this.val) {
		case 7:
			ch = "\\a";
			break;
		case 8:
			ch = "\\b";
			break;
		case 9:
			ch = "\\t";
			break;
		case 10:
			ch = "\n";
			break;
		case 11:
			ch = "\\v";
			break;
		case 13:
			ch = "\\r";
			break;
		case 27:
			ch = "\\e";
			break;
		case 39:
			ch = "\\'";
			break;
		case 92:
			ch = "\\\\";
			break;
		default:
			if (this.val < 32 || this.val >= 127) {
				ch = "\\" + Integer.toOctalString(val & 0xff);
			}
		}
		return Byte.toString(val) + " '" + ch + "'";
	}
	*/
}
