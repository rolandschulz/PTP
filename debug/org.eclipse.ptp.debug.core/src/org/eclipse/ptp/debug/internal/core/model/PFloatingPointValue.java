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
package org.eclipse.ptp.debug.internal.core.model;

import org.eclipse.ptp.debug.core.aif.AIFException;
import org.eclipse.ptp.debug.core.aif.IAIFValue;
import org.eclipse.ptp.debug.core.aif.IAIFValueFloat;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIVariable;

/**
 * @author clement CHU
 * 
 */
public class PFloatingPointValue extends PValue {
	private Number fFloatingPointValue;

	public PFloatingPointValue(PVariable parent, IPCDIVariable variable) {
		super(parent, variable);
	}
	public Number getFloatingPointValue() throws AIFException {
		if (fFloatingPointValue == null) {
			IAIFValue aifValue = getUnderlyingValue();
			if (aifValue instanceof IAIFValueFloat) {
				IAIFValueFloat floatValue = (IAIFValueFloat)aifValue;
				if (floatValue.isDouble()) {
					fFloatingPointValue = new Double(floatValue.doubleValue());
				}
				else if (floatValue.isFloat()) {
					fFloatingPointValue = new Float(floatValue.floatValue());
				}
			}
		}
		return fFloatingPointValue;
	}
}
