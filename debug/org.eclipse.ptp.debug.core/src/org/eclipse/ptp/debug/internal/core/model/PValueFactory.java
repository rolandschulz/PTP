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
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.aif.IAIF;
import org.eclipse.ptp.debug.core.aif.IAIFValueArray;
import org.eclipse.ptp.debug.core.model.IPStackFrame;
import org.eclipse.ptp.debug.core.model.IPType;
import org.eclipse.ptp.debug.core.model.IPValue;
import org.eclipse.ptp.debug.core.model.PDebugElementState;

/**
 * @author Clement chu
 * 
 */
public class PValueFactory {
	static public final IPValue NULL_VALUE = new IPValue() {
		public String getReferenceTypeName() throws DebugException {
			return "";
		}
		public String getValueString() throws DebugException {
			return "";
		}
		public boolean isAllocated() throws DebugException {
			return true;
		}
		public IVariable[] getVariables() throws DebugException {
			return new IVariable[0];
		}
		public boolean hasVariables() throws DebugException {
			return false;
		}
		public String getModelIdentifier() {
			return PTPDebugCorePlugin.getUniqueIdentifier();
		}
		public IDebugTarget getDebugTarget() {
			return null;
		}
		public ILaunch getLaunch() {
			return null;
		}
		public Object getAdapter(Class adapter) {
			return null;
		}
		public IAIF getAIF() {
			return null;
		}
		public void setAIF(IAIF aif) {}
		public IPType getType() throws DebugException {
			return null;
		}
		public String evaluateAsExpression(IPStackFrame frame) {
			return "";
		}
		public PDebugElementState getState() {
			return PDebugElementState.UNDEFINED;
		}
		public Object getCurrentStateInfo() {
			return null;
		}
	};

	static public IPValue createValue(PVariable parent, IAIF aif) {
		IPValue pValue = new PValue(parent);
		pValue.setAIF(aif);
		return pValue;
	}
	static public PIndexedValue createIndexedValue(AbstractPVariable parent, IAIFValueArray aifArray, int start, int length) {
		return new PIndexedValue(parent, aifArray, start, length);
	}
	static public IPValue createValue(PVariable parent) {
		return new PValue(parent);
	}
	static public IPValue createValueWithError(PVariable parent, String message) {
		return new PValue(parent, message);
	}
}
