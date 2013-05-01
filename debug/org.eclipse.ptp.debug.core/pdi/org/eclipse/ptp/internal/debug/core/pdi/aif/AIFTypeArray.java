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
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeArray;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeRange;
import org.eclipse.ptp.internal.debug.core.pdi.messages.Messages;
import org.eclipse.ptp.internal.debug.core.pdi.model.aif.AIFFactory;

public class AIFTypeArray extends TypeDerived implements IAIFTypeArray {
	private IAIFTypeRange fRange;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeArray#getRange()
	 */
	public IAIFTypeRange getRange() {
		return fRange;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.debug.core.pdi.aif.TypeDerived#sizeof()
	 */
	@Override
	public int sizeof() {
		return getRange().getSize() * super.sizeof();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.debug.core.pdi.aif.TypeDerived#toString()
	 */
	@Override
	public String toString() {
		return String.valueOf(AIFFactory.FDS_ARRAY) + getRange().toString() + AIFFactory.FDS_ARRAY_END + getBaseType().toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.pdi.aif.TypeDerived#parse(java.lang
	 * .String)
	 */
	@Override
	public String parse(String fmt) throws AIFFormatException {
		fmt = AIFFactory.parseType(fmt);
		if (fmt.charAt(0) != AIFFactory.FDS_ARRAY_END) {
			throw new AIFFormatException(Messages.AIFTypeArray_1);
		}
		IAIFType type = AIFFactory.getType();
		if (!(type instanceof IAIFTypeRange)) {
			throw new AIFFormatException(Messages.AIFTypeArray_0);
		}
		fRange = (IAIFTypeRange) type;
		return super.parse(fmt.substring(1));
	}
}
