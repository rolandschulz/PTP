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
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeInt;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeRange;
import org.eclipse.ptp.internal.debug.core.pdi.messages.Messages;
import org.eclipse.ptp.internal.debug.core.pdi.model.aif.AIFFactory;

public class AIFTypeRange extends AIFType implements IAIFTypeRange {
	private int fLower;
	private int fSize;
	private IAIFTypeInt fRangeType;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.aif.IAIFType#sizeof()
	 */
	public int sizeof() {
		return fRangeType.sizeof();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeRange#getLower()
	 */
	public int getLower() {
		return fLower;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeRange#getRangeType()
	 */
	public IAIFTypeInt getRangeType() {
		return fRangeType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeRange#getSize()
	 */
	public int getSize() {
		return fSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.debug.core.pdi.aif.TypeDerived#toString()
	 */
	@Override
	public String toString() {
		return String.valueOf(AIFFactory.FDS_RANGE) + getLower() + AIFFactory.FDS_RANGE_SEP + getSize() + getRangeType().toString();
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
		int low_pos = AIFFactory.getFirstNonDigitPos(fmt, 0, true);
		try {
			fLower = Integer.parseInt(fmt.substring(0, low_pos));
		} catch (NumberFormatException e) {
			throw new AIFFormatException(Messages.AIFTypeRange_0);
		}
		int size_pos = AIFFactory.getFirstNonDigitPos(fmt, low_pos + 1, false);
		try {
			fSize = Integer.parseInt(fmt.substring(low_pos + 1, size_pos));
		} catch (NumberFormatException e) {
			throw new AIFFormatException(Messages.AIFTypeRange_1);
		}
		fmt = AIFFactory.parseType(fmt.substring(size_pos));
		IAIFType type = AIFFactory.getType();
		if (!(type instanceof IAIFTypeInt)) {
			throw new AIFFormatException(Messages.AIFTypeRange_2);
		}
		fRangeType = (IAIFTypeInt) type;
		return fmt;
	}
}
