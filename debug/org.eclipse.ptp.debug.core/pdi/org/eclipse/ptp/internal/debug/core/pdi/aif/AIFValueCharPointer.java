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
package org.eclipse.ptp.internal.debug.core.pdi.aif;

import java.math.BigInteger;

import org.eclipse.ptp.debug.core.pdi.model.aif.AIFException;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeCharPointer;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValue;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValueCharPointer;
import org.eclipse.ptp.internal.debug.core.pdi.model.aif.AIFFactory;
import org.eclipse.ptp.internal.debug.core.pdi.model.aif.AIFFactory.SimpleByteBuffer;

/**
 * @author Clement chu
 * 
 */
public class AIFValueCharPointer extends AIFValueString implements IAIFValueCharPointer {
	IAIFValue addrValue;

	public AIFValueCharPointer(IAIFTypeCharPointer type, SimpleByteBuffer buffer) {
		super(type, buffer);
		((AIFTypeCharPointer) type).setSizeof(sizeof());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.pdi.aif.AIFValueString#parse(org.
	 * eclipse.ptp.debug.core.pdi.model.aif.AIFFactory.SimpleByteBuffer)
	 */
	@Override
	protected void parse(SimpleByteBuffer buffer) {
		addrValue = AIFFactory.getAIFValue(null, ((IAIFTypeCharPointer) getType()).getAddressType(), buffer);
		super.parse(buffer);
		setSize(sizeof() + addrValue.sizeof());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValueCharPointer#pointerValue
	 * ()
	 */
	public BigInteger pointerValue() throws AIFException {
		return ValueIntegral.bigIntegerValue(addrValue.getValueString());
	}
}
