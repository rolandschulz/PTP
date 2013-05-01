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

import org.eclipse.ptp.debug.core.pdi.model.aif.IAIF;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFType;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValue;
import org.eclipse.ptp.internal.debug.core.pdi.messages.Messages;

public class AIF implements IAIF {
	private IAIFType fType;
	private IAIFValue fValue;
	private String fDesc = ""; //$NON-NLS-1$

	public AIF(IAIFType aifType, IAIFValue aifValue, String desc) {
		fType = aifType;
		fValue = aifValue;
		fDesc = desc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.aif.IAIF#getDescription()
	 */
	public String getDescription() {
		return fDesc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.aif.IAIF#getType()
	 */
	public IAIFType getType() {
		return fType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.aif.IAIF#getValue()
	 */
	public IAIFValue getValue() {
		return fValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		try {
			return "<" + getType().toString() + "," + getValue().toString() + ">"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} catch (Exception e) {
			return Messages.AIF_4 + e.getMessage();
		}
	}

	/**
	 * Set the AIF type
	 * 
	 * @param t
	 */
	protected void setType(IAIFType t) {
		fType = t;
	}

	/**
	 * Set the AIF value
	 * 
	 * @param v
	 */
	protected void setValue(IAIFValue v) {
		fValue = v;
	}
}
