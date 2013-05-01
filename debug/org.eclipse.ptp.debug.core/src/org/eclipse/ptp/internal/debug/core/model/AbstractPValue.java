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
import org.eclipse.ptp.debug.core.model.IPStackFrame;
import org.eclipse.ptp.debug.core.model.IPValue;
import org.eclipse.ptp.debug.core.pdi.model.aif.AIFException;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValue;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValueArray;

/**
 * @author Clement chu
 * 
 */
public abstract class AbstractPValue extends PDebugElement implements IPValue {
	private AbstractPVariable fParent = null;

	public AbstractPValue(AbstractPVariable parent) {
		super(parent.getSession(), parent.getTasks());
		fParent = parent;
	}

	/**
	 * 
	 */
	public abstract void dispose();

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.model.IPValue#evaluateAsExpression(org.eclipse.ptp.debug.core.model.IPStackFrame)
	 */
	public String evaluateAsExpression(IPStackFrame frame) {
		String valueString = ""; //$NON-NLS-1$
		AbstractPVariable parent = getParentVariable();
		if (parent != null) {
			if (frame != null && frame.canEvaluate()) {
				try {
					IAIFValue value = parent.getAIF().getValue();
					if (value instanceof IAIFValueArray) {
						//TODO if value is array, show nothing.  Prevent no value for partial aif
						valueString = ""; //$NON-NLS-1$
					} else {
						valueString = value.getValueString();
						if (valueString == null || valueString.length() == 0)
							valueString = frame.evaluateExpressionToString(parent.getExpressionString());
					}
				} catch (AIFException e) {
					valueString = e.getMessage();
				} catch (DebugException e) {
					valueString = e.getMessage();
				}
			}
		}
		return valueString;
	}

	/**
	 * @return
	 */
	public AbstractPVariable getParentVariable() {
		return fParent;
	}

	/**
	 * 
	 */
	protected abstract void preserve();

	/**
	 * 
	 */
	protected abstract void reset();

	/**
	 * @param changed
	 */
	protected abstract void setChanged(boolean changed);
}
