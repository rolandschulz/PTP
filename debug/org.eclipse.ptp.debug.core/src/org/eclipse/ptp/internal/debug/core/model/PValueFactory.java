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
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.ptp.debug.core.IPSession;
import org.eclipse.ptp.debug.core.model.IPStackFrame;
import org.eclipse.ptp.debug.core.model.IPValue;
import org.eclipse.ptp.debug.core.model.PDebugElementState;
import org.eclipse.ptp.debug.core.pdi.model.IPDIVariable;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIF;
import org.eclipse.ptp.internal.debug.core.PTPDebugCorePlugin;

/**
 * @author Clement chu
 * 
 */
public class PValueFactory {
	public static final IPValue NULL_VALUE = new IPValue() {
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.debug.core.model.IPValue#evaluateAsExpression(org.eclipse.ptp.debug.core.model.IPStackFrame)
		 */
		public String evaluateAsExpression(IPStackFrame frame) {
			return ""; //$NON-NLS-1$
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
		 */
		@SuppressWarnings("rawtypes")
		public Object getAdapter(Class adapter) {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ptp.debug.core.model.IPValue#getAIF()
		 */
		public IAIF getAIF() {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ptp.debug.core.model.IPDebugElement#getCurrentStateInfo()
		 */
		public Object getCurrentStateInfo() {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.core.model.IDebugElement#getDebugTarget()
		 */
		public IDebugTarget getDebugTarget() {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ptp.debug.core.model.IPDebugElement#getID()
		 */
		public int getID() {
			return -1;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.core.model.IDebugElement#getLaunch()
		 */
		public ILaunch getLaunch() {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.core.model.IDebugElement#getModelIdentifier()
		 */
		public String getModelIdentifier() {
			return PTPDebugCorePlugin.getUniqueIdentifier();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.core.model.IValue#getReferenceTypeName()
		 */
		public String getReferenceTypeName() throws DebugException {
			return ""; //$NON-NLS-1$
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ptp.debug.core.model.IPDebugElement#getSession()
		 */
		public IPSession getSession() {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ptp.debug.core.model.IPDebugElement#getState()
		 */
		public PDebugElementState getState() {
			return PDebugElementState.UNDEFINED;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.core.model.IValue#getValueString()
		 */
		public String getValueString() throws DebugException {
			return ""; //$NON-NLS-1$
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.core.model.IValue#getVariables()
		 */
		public IVariable[] getVariables() throws DebugException {
			return new IVariable[0];
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.core.model.IValue#hasVariables()
		 */
		public boolean hasVariables() throws DebugException {
			return false;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.core.model.IValue#isAllocated()
		 */
		public boolean isAllocated() throws DebugException {
			return true;
		}
	};

	/**
	 * @param parent
	 * @param variable
	 * @return
	 */
	static public PValue createGlobalValue(PVariable parent, IPDIVariable variable) {
		return new PGlobalValue(parent, variable);
	}

	/**
	 * @param parent
	 * @param variable
	 * @param start
	 * @param length
	 * @return
	 */
	static public PIndexedValue createIndexedValue(AbstractPVariable parent, IPDIVariable variable, int start, int length) {
		return new PIndexedValue(parent, variable, start, length);
	}

	/**
	 * @param parent
	 * @param variable
	 * @return
	 */
	static public PValue createValue(PVariable parent, IPDIVariable variable) {
		return new PValue(parent, variable);
	}

	/**
	 * @param parent
	 * @param message
	 * @return
	 */
	static public PValue createValueWithError(PVariable parent, String message) {
		return new PValue(parent, message);
	}
}
