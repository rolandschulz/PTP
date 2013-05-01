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
import org.eclipse.ptp.debug.core.pdi.model.aif.ITypeIntegral;
import org.eclipse.ptp.internal.debug.core.pdi.messages.Messages;
import org.eclipse.ptp.internal.debug.core.pdi.model.aif.AIFFactory;

/**
 * @author Clement chu
 * 
 */
public abstract class TypeIntegral extends AIFType implements ITypeIntegral {
	private boolean fSigned;
	private int fSize;

	public TypeIntegral() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.aif.ITypeIntegral#isSigned()
	 */
	public boolean isSigned() {
		return fSigned;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return (isSigned() ? String.valueOf(AIFFactory.FDS_INTEGER_SIGNED) : String.valueOf(AIFFactory.FDS_INTEGER_UNSIGNED))
				+ sizeof();
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
		fSigned = (fmt.charAt(0) == AIFFactory.FDS_INTEGER_SIGNED);
		int pos = AIFFactory.getFirstNonDigitPos(fmt, 1, false);
		if (pos == -1) {
			throw new AIFFormatException(Messages.TypeIntegral_0);
		}
		fSize = Integer.parseInt(fmt.substring(1, pos));
		return fmt.substring(pos);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.aif.IAIFType#sizeof()
	 */
	public int sizeof() {
		return fSize;
	}
}
