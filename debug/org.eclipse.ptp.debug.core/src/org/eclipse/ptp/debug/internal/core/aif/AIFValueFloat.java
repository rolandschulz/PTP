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
import org.eclipse.ptp.debug.core.aif.AIFException;
import org.eclipse.ptp.debug.core.aif.IAIFTypeFloat;
import org.eclipse.ptp.debug.core.aif.IAIFValueFloat;
import org.eclipse.ptp.debug.core.aif.AIFFactory.SimpleByteBuffer;

/**
 * @author Clement chu
 * 
 */
public class AIFValueFloat extends ValueParent implements IAIFValueFloat {
	ByteBuffer byteBuffer;
	
	public AIFValueFloat(IAIFTypeFloat type, SimpleByteBuffer buffer) {
		super(type);
		parse(buffer);
	}
	protected void parse(SimpleByteBuffer buffer) {
		byte[] dst = new byte[type.sizeof()]; 
		for (int i=0; i<dst.length; i++) {
			dst[i] = buffer.get();
		}
		byteBuffer = ByteBuffer.wrap(dst, 0, dst.length);
		size = type.sizeof();
	}
	public String getValueString() throws AIFException {
		if (result == null) {
			result = getString();
		}
		return result;
	}
	private String getString() throws AIFException {
		if (isFloat()) {
			return String.valueOf(floatValue());
		} else if (isDouble()) {
			return String.valueOf(doubleValue());
		} else {
			return new String(byteBuffer.array());
		}
	}
	public float floatValue() throws AIFException {
		try {
			return byteBuffer.getFloat();
		} catch (BufferUnderflowException e) {
			return 0;
		} finally {
			byteBuffer.rewind();			
		}
	}
	public double doubleValue() throws AIFException {
		try {
			return byteBuffer.getDouble();
		} catch (BufferUnderflowException e) {
			return 0;
		} finally {
			byteBuffer.rewind();			
		}
	}
	
	public boolean isDouble() {
		return (type.sizeof() == 8);
	}
	public boolean isFloat() {
		return (type.sizeof() == 4);
	}
	/*
	public double doubleValue() throws PCDIException {
		double result = 0;
		String valueString = getValueString();
		if (isNaN(valueString))
			result = Double.NaN;
		else if (isNegativeInfinity(valueString))
			result = Double.NEGATIVE_INFINITY;
		else if (isPositiveInfinity(valueString))
			result = Double.POSITIVE_INFINITY;
		else {		
			try {
				result = Double.parseDouble(valueString);
			} catch (NumberFormatException e) {
			}
		}
		return result;
	}
	public float floatValue() throws PCDIException {
		float result = 0;
		String valueString = getValueString();
		if (isNaN(valueString))
			result = Float.NaN;
		else if (isNegativeInfinity(valueString))
			result = Float.NEGATIVE_INFINITY;
		else if (isPositiveInfinity(valueString))
			result = Float.POSITIVE_INFINITY;
		else {		
			try {
				result = Float.parseFloat(valueString);
			} catch (NumberFormatException e) {
			}
		}
		return result;
	}
	private boolean isPositiveInfinity(String valueString) {
		return (valueString != null) ? valueString.indexOf("inf") != -1 : false;
	}
	private boolean isNegativeInfinity(String valueString) {
		return (valueString != null) ? valueString.indexOf("-inf") != -1 : false;
	}
	private boolean isNaN(String valueString) {
		return (valueString != null) ? valueString.indexOf("nan") != -1 : false;
	}
	*/
}
