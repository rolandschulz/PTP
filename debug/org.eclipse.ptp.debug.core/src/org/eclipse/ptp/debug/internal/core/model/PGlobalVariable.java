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
import org.eclipse.ptp.debug.core.model.IPGlobalVariable;
import org.eclipse.ptp.debug.core.model.IPGlobalVariableDescriptor;
import org.eclipse.ptp.debug.core.model.IPValue;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIResumedEvent;
import org.eclipse.ptp.debug.core.pdi.model.IPDIArgumentDescriptor;
import org.eclipse.ptp.debug.core.pdi.model.IPDIGlobalVariableDescriptor;
import org.eclipse.ptp.debug.core.pdi.model.IPDIVariable;
import org.eclipse.ptp.debug.core.pdi.model.IPDIVariableDescriptor;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIF;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeArray;

/**
 * @author Clement chu
 * 
 */
public class PGlobalVariable extends PVariable implements IPGlobalVariable {
	private class InternalVariable implements IInternalVariable {
		private PVariable fVariable;
		private IPDIVariableDescriptor fPDIVariableObject;
		private IPDIVariable fPDIVariable;
		private String fQualifiedName;
		private IPValue fValue = PValueFactory.NULL_VALUE;
		private boolean fChanged = false;

		InternalVariable(PVariable var, IPDIVariableDescriptor varObject) {
			setVariable(var);
			setPDIVariableObject(varObject);
			setPDIVariable((varObject instanceof IPDIVariable) ? (IPDIVariable) varObject : null);
		}
		public IInternalVariable createShadow(int start, int length) throws DebugException {
			IInternalVariable iv = null;
			try {
				iv = new InternalVariable(getVariable(), getPDIVariableObject().getVariableDescriptorAsArray(start, length));
			}
			catch (PDIException e) {
				requestFailed(e.getMessage(), null);
			}
			return iv;
		}
		public IInternalVariable createShadow(String type) throws DebugException {
			IInternalVariable iv = null;
			try {
				iv = new InternalVariable(getVariable(), getPDIVariableObject().getVariableDescriptorAsType(type));
			} catch (PDIException e) {
				requestFailed(e.getMessage(), null);
			}
			return iv;
		}
		private synchronized IPDIVariable getPDIVariable() throws DebugException {
			if (fPDIVariable == null) {
				try {
					fPDIVariable = getPDITarget().createGlobalVariable((IPDIGlobalVariableDescriptor) getPDIVariableObject());
				} catch (PDIException e) {
					requestFailed(e.getMessage(), null);
				}
			}
			return fPDIVariable;
		}
		private void setPDIVariable(IPDIVariable variable) {
			fPDIVariable = variable;
		}
		private IPDIVariableDescriptor getPDIVariableObject() {
			if (fPDIVariable != null) {
				return fPDIVariable;
			}
			return fPDIVariableObject;
		}
		private void setPDIVariableObject(IPDIVariableDescriptor variableObject) {
			fPDIVariableObject = variableObject;
		}
		public String getQualifiedName() throws DebugException {
			if (fQualifiedName == null) {
				try {
					fQualifiedName = (fPDIVariableObject != null) ? fPDIVariableObject.getQualifiedName() : null;
				} catch (PDIException e) {
					requestFailed(e.getMessage(), null);
				}
			}
			return fQualifiedName;
		}
		private synchronized void invalidate(boolean destroy) {
			try {
				if (destroy && fPDIVariable != null)
					fPDIVariable.dispose();
			} catch (PDIException e) {
				logError(e.getMessage());
			}
			invalidateValue();
			setPDIVariable(null);
		}
		public void dispose(boolean destroy) {
			invalidate(destroy);
		}
		public boolean isSameVariable(IPDIVariable pdiVar) {
			return (fPDIVariable != null) ? fPDIVariable.equals(pdiVar) : false;
		}
		public int sizeof() {
			if (getPDIVariableObject() != null) {
				try {
					return getPDIVariableObject().sizeof();
				}
				catch (PDIException e) {
				}
			}
			return 0;
		}
		public boolean isArgument() {
			return (getPDIVariableObject() instanceof IPDIArgumentDescriptor);
		}
		public void setValue(String expression) throws DebugException {
			IPDIVariable pdiVariable = null;
			try {
				pdiVariable = getPDIVariable();
				if (pdiVariable != null)
					pdiVariable.setValue(expression);
				else
					requestFailed(CoreModelMessages.getString("PModificationVariable.0"), null);
			}
			catch (PDIException e) {
				targetRequestFailed(e.getMessage(), null);
			}
		}
		public synchronized IPValue getValue() throws DebugException {
			if (fValue.equals(PValueFactory.NULL_VALUE)) {
				IPDIVariable var = getPDIVariable();
				if (var != null) {
					try {
						IAIF aif = var.getAIF();
						if (aif != null && aif.getType() instanceof IAIFTypeArray) {
							int[] dims = ((IAIFTypeArray)aif.getType()).getDimensionDetails();
							if (dims.length > 0 && dims[0] > 0)
								fValue = PValueFactory.createIndexedValue(getVariable(), var, 0, dims[0]);
						}
						else {
							fValue = PValueFactory.createValue(getVariable(), var);
						}
					} catch (PDIException e) {
						requestFailed(e.getMessage(), e);
					}
				}
			}
			return fValue;
		}
		public void invalidateValue() {
			if (fValue instanceof AbstractPValue) {
				((AbstractPValue) fValue).dispose();
				fValue = PValueFactory.NULL_VALUE;
				if (fPDIVariable != null)
					fPDIVariable.resetValue();
			}
		}
		public boolean isChanged() {
			return fChanged;
		}
		public synchronized void setChanged(boolean changed) {
			if (changed) {
				invalidateValue();
			}
			if (fValue instanceof AbstractPValue) {
				((AbstractPValue) fValue).setChanged(changed);
			}
			fChanged = changed;
		}
		public synchronized void preserve() {
			setChanged(false);
			if (fValue instanceof AbstractPValue) {
				((AbstractPValue) fValue).preserve();
			}
		}
		PVariable getVariable() {
			return fVariable;
		}
		private void setVariable(PVariable variable) {
			fVariable = variable;
		}
		public void resetValue() {
			if (fValue instanceof AbstractPValue) {
				((AbstractPValue) fValue).reset();
			}
		}
		public boolean isEditable() throws DebugException {
			IPDIVariable var = getPDIVariable();
			if (var != null) {
				try {
					return var.isEditable();
				}
				catch (PDIException e) {
				}
			}
			return false;
		}
		public boolean equals(Object obj) {
			if (obj instanceof InternalVariable) {
				return getPDIVariableObject().equals(((InternalVariable) obj).getPDIVariableObject());
			}
			return false;
		}
		public boolean isSameDescriptor(IPDIVariableDescriptor desc) {
			return getPDIVariableObject().equals(desc);
		}
	}
	private IPGlobalVariableDescriptor fDescriptor;

	protected PGlobalVariable(PDebugElement parent, IPGlobalVariableDescriptor descriptor, IPDIVariableDescriptor pdiVariableObject) {
		super(parent, pdiVariableObject);
		fDescriptor = descriptor;
	}
	protected PGlobalVariable(PDebugElement parent, IPGlobalVariableDescriptor descriptor, IPDIVariableDescriptor pdiVariableObject, String message) {
		super(parent, pdiVariableObject, message);
		fDescriptor = descriptor;
	}
	public boolean canEnableDisable() {
		return true;
	}
	public void handleDebugEvents(IPDIEvent[] events) {
		for (int i = 0; i < events.length; i++) {
			IPDIEvent event = events[i];
			if (!event.contains(getTasks()))
				continue;

			if (event instanceof IPDIResumedEvent) {
				setChanged(false);
			}
		}
		super.handleDebugEvents(events);
	}
	public IPGlobalVariableDescriptor getDescriptor() {
		return fDescriptor;
	}
	public void dispose() {
		internalDispose(true);
		setDisposed(true);
	}
	protected void createOriginal(IPDIVariableDescriptor vo) {
		if (vo != null) {
			setName(vo.getName());
			setOriginal(new InternalVariable(this, vo));
		}
	}
	public IAIF getAIF() throws DebugException {
		return getValue().getAIF();
	}
}
