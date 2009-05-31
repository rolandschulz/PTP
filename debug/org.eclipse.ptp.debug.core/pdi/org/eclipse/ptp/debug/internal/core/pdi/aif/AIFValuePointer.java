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
package org.eclipse.ptp.debug.internal.core.pdi.aif;

import java.math.BigInteger;

import org.eclipse.ptp.debug.core.pdi.model.aif.AIFException;
import org.eclipse.ptp.debug.core.pdi.model.aif.AIFFactory;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeAddress;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypePointer;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValue;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValueNamed;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValuePointer;
import org.eclipse.ptp.debug.core.pdi.model.aif.IValueParent;
import org.eclipse.ptp.debug.core.pdi.model.aif.AIFFactory.SimpleByteBuffer;

/**
 * @author Clement chu
 * 
 */
public class AIFValuePointer extends ValueDerived implements IAIFValuePointer {
	IAIFValue value = null;
	IAIFValue addrValue = null;
	
	public AIFValuePointer(IValueParent parent, IAIFTypePointer type, SimpleByteBuffer buffer) {
		super(parent, type);
		parse(buffer);
	}
	protected void parse(SimpleByteBuffer buffer) {
		int marker = buffer.get();
		IAIFTypePointer pType = (IAIFTypePointer)type;

		switch (marker) {
		case 0:
	        IAIFTypeAddress aType = pType.getAddressType();
	        addrValue = AIFFactory.getAIFValue(null, aType, new byte[aType.sizeof()]);
	        value = AIFFactory.UNKNOWNVALUE;
			break;
		case 1:
			addrValue = AIFFactory.getAIFValue(null, pType.getAddressType(), buffer);
			value = AIFFactory.getAIFValue(getParent(), pType.getBaseType(), buffer);
			size = addrValue.sizeof() + value.sizeof();
			break;
		case 2:
			break;
		case 3:
			break;
		default:
			value = AIFFactory.UNKNOWNVALUE;
			break;
		}
	}
	/**
	 * Get the children number of pointer.  Return 1 if the base type is primitive 
	 * 
	 */
	public int getChildrenNumber() throws AIFException {
		int children = value.getChildrenNumber();
		if (children == 0) {
			return 1;
		}
		return children;
	}
	public String getValueString() throws AIFException {
		if (result == null) {
			if (addrValue != null) {
				result = addrValue.getValueString();
			} else {
				result = ""; //$NON-NLS-1$
			}
		}
		return result;
	}
	public BigInteger pointerValue() throws AIFException {
		if (addrValue == null) {
			return BigInteger.ZERO;
		}
		return ValueIntegral.bigIntegerValue(addrValue.getValueString());
	}
	public IAIFValue getValue() {
		if (value instanceof IAIFValueNamed) {
			return ((IAIFValueNamed)value).getValue();
		}
		return value;
	}
}
