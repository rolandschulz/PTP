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

import java.math.BigInteger;
import org.eclipse.ptp.debug.core.aif.AIFException;
import org.eclipse.ptp.debug.core.aif.AIFFactory;
import org.eclipse.ptp.debug.core.aif.IAIFTypePointer;
import org.eclipse.ptp.debug.core.aif.IAIFValue;
import org.eclipse.ptp.debug.core.aif.IAIFValueNamed;
import org.eclipse.ptp.debug.core.aif.IAIFValuePointer;
import org.eclipse.ptp.debug.core.aif.IValueParent;

/**
 * @author Clement chu
 * 
 */
public class AIFValuePointer extends ValueDerived implements IAIFValuePointer {
	int marker = 0;
	IAIFValue value;
	
	public AIFValuePointer(IValueParent parent, IAIFTypePointer type, byte[] data) {
		super(parent, type);
		parse(data);
	}
	public int getChildrenNumber() throws AIFException {
		return value.getChildrenNumber();
	}
	
	public String getValueString() throws AIFException {
		if (result == null) {
			result = value.getValueString();
		}
		return result;
	}
	protected void parse(byte[] data) {
System.err.println("------- total: " + data.length);
		marker = data[0];
		IAIFTypePointer pType = (IAIFTypePointer)type;
System.err.println("--------------- marker: " + marker);
		switch (marker) {
		case 0:
			value = AIFFactory.UNKNOWNVALUE;
			break;
		case 1:
			byte[] newByte = createByteArray(data, 1, data.length-1);
			value = AIFFactory.getAIFValue(this, pType.getBaseType(), newByte);
			break;
		case 2:
			break;
		case 3:
			break;
		default:
			value = AIFFactory.UNKNOWNVALUE;
			break;
		}
		size = value.sizeof();
System.err.println("--------------- pointer value: " + value.toString());
	}
	
	public BigInteger pointerValue() throws AIFException {
		return ValueIntegral.bigIntegerValue(getValueString());
	}
	
	public IAIFValue getValue() {
		if (value instanceof IAIFValueNamed) {
			return ((IAIFValueNamed)value).getValue();
		}
		return value;
	}
}
