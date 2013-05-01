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
package org.eclipse.ptp.internal.debug.core.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.ptp.debug.core.pdi.model.IPDIVariable;
import org.eclipse.ptp.debug.core.pdi.model.aif.AIFException;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIF;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeFloat;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValueFloat;
import org.eclipse.ptp.internal.debug.core.messages.Messages;

/**
 * @author clement CHU
 * 
 */
public class PFloatingPointValue extends PValue {
	private Number fFloatingPointValue;

	public PFloatingPointValue(PVariable parent, IPDIVariable variable) {
		super(parent, variable);
	}

	/**
	 * @return
	 * @throws DebugException
	 * @throws AIFException
	 */
	public Number getFloatingPointValue() throws DebugException, AIFException {
		if (fFloatingPointValue == null) {
			final IAIF aif = getAIF();
			if (aif != null) {
				if (aif.getType() instanceof IAIFTypeFloat) {
					final IAIFValueFloat floatValue = (IAIFValueFloat) aif.getValue();
					if (floatValue.isDouble()) {
						fFloatingPointValue = new Double(floatValue.doubleValue());
					} else if (floatValue.isFloat()) {
						fFloatingPointValue = new Float(floatValue.floatValue());
					} else {
						targetRequestFailed(Messages.PFloatingPointValue_0, null);
					}
				}
			}
		}
		return fFloatingPointValue;
	}
}
