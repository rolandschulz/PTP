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

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import org.eclipse.ptp.debug.core.aif.IAIFTypeInt;
import org.eclipse.ptp.debug.core.aif.IAIFValueInt;
import org.eclipse.ptp.debug.core.cdi.PCDIException;

public class AIFValueInt extends AIFValueIntegral implements IAIFValueInt {
	ByteBuffer byteBuffer;
	
	public AIFValueInt(IAIFTypeInt type, byte[] data) {
		super(type);
		parse(data);
	}
	protected void parse(byte[] data) {
		byteBuffer = byteBuffer(data);
	}
	public String getValueString() throws PCDIException {
		if (result == null) {
			result = getString();
		}
		return result;
	}
	private String getString() throws PCDIException {
		if (isShort()) {
			return String.valueOf(shortValue());
		}
		else if (isInt()) {
			return String.valueOf(intValue());
		}
		else if (isLong()) {
			return String.valueOf(longValue());
		}
		else {
			return new String(byteValue());
		}
	}
	public boolean isLong() {
		return (type.sizeof() == 8);
	}
	public boolean isShort() {
		return (type.sizeof() == 2);
	}
	public boolean isInt() {
		return (type.sizeof() == 4);
	}
	public byte[] byteValue() throws PCDIException {
		return byteBuffer.array();
	}
	public long longValue() throws PCDIException {
		try {
			return byteBuffer.getLong();
		} catch (BufferUnderflowException e) {
			return 0;
		} finally {
			byteBuffer.rewind();			
		}
	}
	public short shortValue() throws PCDIException {
		try {
			return byteBuffer.getShort();
		} catch (BufferUnderflowException e) {
			return 0;
		} finally {
			byteBuffer.rewind();			
		}
	}
	public int intValue() throws PCDIException {
		try {
			return byteBuffer.getInt();
		} catch (BufferUnderflowException e) {
			return 0;
		} finally {
			byteBuffer.rewind();			
		}
	}
}
