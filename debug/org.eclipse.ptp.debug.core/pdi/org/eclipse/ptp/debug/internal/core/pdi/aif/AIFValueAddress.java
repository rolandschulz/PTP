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
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeAddress;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValueAddress;
import org.eclipse.ptp.debug.core.pdi.model.aif.AIFFactory.SimpleByteBuffer;

/**
 * @author Clement chu
 * 
 */
public class AIFValueAddress extends AIFValue implements IAIFValueAddress {
	String addr = ""; //$NON-NLS-1$

	public AIFValueAddress(IAIFTypeAddress type, SimpleByteBuffer buffer) {
		super(type);
		parse(buffer);
	}
	protected void parse(SimpleByteBuffer buffer) {
		size = type.sizeof();
		for (int i=0; i<size; i++) {
			addr += Integer.toHexString(0x0100 + (buffer.get() & 0x00FF)).substring(1);
		}
	}
	public String getValueString() throws AIFException {
		if (result == null) {
			result = "0x" + addr; //$NON-NLS-1$
		}
		return result;
	}
	public BigInteger getAddress() throws AIFException {
		return ValueIntegral.bigIntegerValue(getValueString());
	}
}
