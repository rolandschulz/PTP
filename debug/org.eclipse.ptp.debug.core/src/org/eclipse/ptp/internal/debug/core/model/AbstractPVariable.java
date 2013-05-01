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

import org.eclipse.ptp.debug.core.model.IEnableDisableTarget;
import org.eclipse.ptp.debug.core.model.IPStackFrame;
import org.eclipse.ptp.debug.core.model.IPVariable;

/**
 * @author Clement chu
 * 
 */
public abstract class AbstractPVariable extends PDebugElement implements IPVariable {
	private PDebugElement fParent;

	public AbstractPVariable(PDebugElement parent) {
		super(parent.getSession(), parent.getTasks());
		setParent(parent);
	}

	/**
	 * 
	 */
	public abstract void dispose();

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.debug.core.model.PDebugElement#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IEnableDisableTarget.class))
			return this;
		return super.getAdapter(adapter);
	}

	/**
	 * @param parent
	 */
	private void setParent(PDebugElement parent) {
		fParent = parent;
	}

	/**
	 * @return
	 */
	protected PDebugElement getParent() {
		return fParent;
	}

	/**
	 * @return
	 */
	protected IPStackFrame getStackFrame() {
		PDebugElement parent = getParent();
		if (parent instanceof AbstractPValue) {
			AbstractPVariable pv = ((AbstractPValue) parent).getParentVariable();
			if (pv != null)
				return pv.getStackFrame();
		}
		if (parent instanceof PStackFrame)
			return (PStackFrame) parent;
		return null;
	}

	/**
	 * 
	 */
	protected abstract void preserve();

	/**
	 * 
	 */
	protected abstract void resetValue();

	/**
	 * @param changed
	 */
	protected abstract void setChanged(boolean changed);
}
