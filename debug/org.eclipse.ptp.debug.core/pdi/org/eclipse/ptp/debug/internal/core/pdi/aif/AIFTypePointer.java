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

import org.eclipse.ptp.debug.core.pdi.model.aif.AIFFactory;
import org.eclipse.ptp.debug.core.pdi.model.aif.AIFFormatException;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFType;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeAddress;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypePointer;

public class AIFTypePointer extends TypeDerived implements IAIFTypePointer {
	public static void main(String[] args) {
		// IAIFType testType =
		// AIFFactory.getAIFType("^%1/{s1 *|a=is4,b=^>1/,c=^>1/;;;}");
		IAIFType testType;
		try {
			testType = AIFFactory.getAIFType("^a4^a4"); //$NON-NLS-1$
			System.out.println("----: " + ((IAIFTypePointer) testType).getBaseType()); //$NON-NLS-1$
			System.out.println("----: " + testType); //$NON-NLS-1$
			System.out.println("----: " + testType.sizeof()); //$NON-NLS-1$
		} catch (AIFFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private IAIFType fAddrType;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypePointer#getAddressType()
	 */
	public IAIFTypeAddress getAddressType() {
		return (IAIFTypeAddress) fAddrType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.internal.core.pdi.aif.TypeDerived#sizeof()
	 */
	@Override
	public int sizeof() {
		return super.sizeof() + getAddressType().sizeof() + 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.internal.core.pdi.aif.TypeDerived#toString()
	 */
	@Override
	public String toString() {
		return String.valueOf(AIFFactory.FDS_POINTER) + getAddressType().toString() + super.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.internal.core.pdi.aif.TypeDerived#parse(java.lang
	 * .String)
	 */
	@Override
	public String parse(String fmt) throws AIFFormatException {
		fmt = AIFFactory.parseType(fmt);
		fAddrType = AIFFactory.getType();
		return super.parse(fmt);
	}
}
