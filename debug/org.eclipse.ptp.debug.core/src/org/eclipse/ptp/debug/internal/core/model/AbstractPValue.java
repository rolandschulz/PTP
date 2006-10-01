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

import org.eclipse.debug.core.DebugException;
import org.eclipse.ptp.debug.core.aif.AIFException;
import org.eclipse.ptp.debug.core.aif.IAIF;
import org.eclipse.ptp.debug.core.aif.IAIFValue;
import org.eclipse.ptp.debug.core.model.IPStackFrame;
import org.eclipse.ptp.debug.core.model.IPValue;

/**
 * @author Clement chu
 * 
 */
public abstract class AbstractPValue extends PDebugElement implements IPValue {
	private AbstractPVariable fParent = null;
	private IAIF aif = null;

	public AbstractPValue(AbstractPVariable parent) {
		super((PDebugTarget) parent.getDebugTarget());
		fParent = parent;
	}
	public IAIF getAIF() throws DebugException {
		if (aif == null) {
			aif = fParent.getAIF();
		}
		return aif;
	}
	public void setAIF(IAIF aif) {
		this.aif = aif;
	}
	public AbstractPVariable getParentVariable() {
		return fParent;
	}
	public String evaluateAsExpression(IPStackFrame frame) {
		String valueString = "";
		AbstractPVariable parent = getParentVariable();
		if (parent != null) {
			if (frame != null && frame.canEvaluate()) {
				if (aif != null) {
					try {
						return processUnderlyingValue(aif.getValue());
					} catch (AIFException e) {
						valueString = e.getMessage();
					}
				}
				try {
					valueString = frame.evaluateExpressionToString(parent.getExpressionString());
				} catch (DebugException e) {
					valueString = e.getMessage();
				}
			}
		}
		return valueString;
	}
	abstract protected void setChanged(boolean changed);
	abstract public void dispose();
	abstract protected void reset();
	abstract protected void preserve();
	
	protected abstract String processUnderlyingValue(IAIFValue aifValue) throws AIFException;
}
