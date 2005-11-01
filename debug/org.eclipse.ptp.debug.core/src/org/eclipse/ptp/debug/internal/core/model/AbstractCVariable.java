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

import org.eclipse.cdt.debug.core.model.ICStackFrame;
import org.eclipse.cdt.debug.core.model.ICVariable;
import org.eclipse.cdt.debug.core.model.IEnableDisableTarget;
import org.eclipse.debug.core.DebugException;

public abstract class AbstractCVariable extends PDebugElement implements ICVariable {
	private PDebugElement fParent;

	public AbstractCVariable(PDebugElement parent) {
		super((PDebugTarget) parent.getDebugTarget());
		setParent(parent);
	}
	protected PDebugElement getParent() {
		return fParent;
	}
	private void setParent(PDebugElement parent) {
		fParent = parent;
	}
	protected ICStackFrame getStackFrame() {
		PDebugElement parent = getParent();
		if (parent instanceof AbstractCValue) {
			AbstractCVariable pv = ((AbstractCValue) parent).getParentVariable();
			if (pv != null)
				return pv.getStackFrame();
		}
		if (parent instanceof CStackFrame)
			return (CStackFrame) parent;
		return null;
	}
	public Object getAdapter(Class adapter) {
		if (IEnableDisableTarget.class.equals(adapter))
			return this;
		return super.getAdapter(adapter);
	}
	public abstract String getExpressionString() throws DebugException;
	public abstract void dispose();
	protected abstract void resetValue();
	protected abstract void setChanged(boolean changed);
	protected abstract void preserve();
}
