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
import org.eclipse.ptp.debug.core.aif.AIFFactory;
import org.eclipse.ptp.debug.core.aif.IAIFTypePointer;
import org.eclipse.ptp.debug.core.aif.IAIFValue;
import org.eclipse.ptp.debug.core.aif.IAIFValuePointer;
import org.eclipse.ptp.debug.core.cdi.PCDIException;

/**
 * @author Clement chu
 * 
 */
public class AIFValuePointer extends AIFValueDerived implements IAIFValuePointer {
	int marker = 0;
	IAIFValue value;
	
	public AIFValuePointer(IAIFTypePointer type, byte[] data) {
		super(type);
		parse(data);
	}
	public String getValueString() throws PCDIException {
		if (result == null) {
			result = value.getValueString();
		}
		return result;
	}
	protected void parse(byte[] data) {
		marker = data[0];
		String bb = String.valueOf(data[0]);
		System.out.println("--------------- len: " + data.length);
		System.out.println("--------------- type: " + type.sizeof());
		System.out.println("--------------- marker1: " + bb);
		System.out.println("--------------- marker2: " + marker);
		switch (marker) {
		case 0:
			value = AIFFactory.UNKNOWNVALUE;
			break;
		case 1:
			byte[] newByte = new byte[data.length-1];
			System.arraycopy(data, 1, newByte, 0, newByte.length);
			String a = new String(newByte);
			System.out.println("=============== 1: " + a);
			break;
		case 2:
			break;
		case 3:
			break;
		default:
			value = AIFFactory.UNKNOWNVALUE;
			break;
		}
		size = data.length;		
	}
	
	public BigInteger pointerValue() throws PCDIException {
		return AIFValueIntegral.bigIntegerValue(getValueString());
	}
}
