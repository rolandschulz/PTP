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
import java.nio.ByteBuffer;
import org.eclipse.ptp.debug.core.aif.IAIFTypePointer;
import org.eclipse.ptp.debug.core.aif.IAIFValuePointer;
import org.eclipse.ptp.debug.core.cdi.PCDIException;

/**
 * @author Clement chu
 * 
 */
public class AIFValuePointer extends AIFValueDerived implements IAIFValuePointer {
	private int marker = 0;
	
	public AIFValuePointer(IAIFTypePointer type, byte[] data) {
		super(type, data);
		parse();
	}
	public String getValueString() throws PCDIException {
		if (result == null) {
			result = String.valueOf(data);
		}
		return result;
	}
	protected void parse() {
		ByteBuffer buffer = byteBuffer();
		marker = (int)buffer.get();
	}
	
	public BigInteger pointerValue() throws PCDIException {
		return AIFValueIntegral.bigIntegerValue(getValueString());
	}
	public int getBufferLength() {
		return 1;
	}	
}
