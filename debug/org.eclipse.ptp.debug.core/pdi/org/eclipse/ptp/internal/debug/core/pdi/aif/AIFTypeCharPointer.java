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

import org.eclipse.ptp.debug.core.pdi.model.aif.AIFFormatException;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFType;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeAddress;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeCharPointer;
import org.eclipse.ptp.internal.debug.core.pdi.messages.Messages;
import org.eclipse.ptp.internal.debug.core.pdi.model.aif.AIFFactory;

/**
 * @author Clement chu
 * 
 */
public class AIFTypeCharPointer extends AIFTypeString implements IAIFTypeCharPointer {
	private IAIFType fAddrType;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeCharPointer#getAddressType
	 * ()
	 */
	public IAIFTypeAddress getAddressType() {
		return (IAIFTypeAddress) fAddrType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.pdi.aif.AIFType#parse(java.lang.String
	 * )
	 */
	@Override
	public String parse(String fmt) throws AIFFormatException {
		fmt = AIFFactory.parseType(fmt);
		IAIFType type = AIFFactory.getType();
		if (!(type instanceof IAIFTypeAddress)) {
			throw new AIFFormatException(Messages.AIFTypeCharPointer_0);
		}
		fAddrType = type;
		return fmt;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.debug.core.pdi.aif.AIFTypeString#sizeof()
	 */
	@Override
	public int sizeof() {
		return getAddressType().sizeof() + super.sizeof();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.debug.core.pdi.aif.AIFTypeString#toString()
	 */
	@Override
	public String toString() {
		return AIFFactory.FDS_CHAR_POINTER + getAddressType().toString();
	}
}
