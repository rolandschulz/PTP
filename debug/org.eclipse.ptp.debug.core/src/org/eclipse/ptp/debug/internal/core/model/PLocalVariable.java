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

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.ptp.debug.core.aif.IAIFType;
import org.eclipse.ptp.debug.core.aif.IAIFTypeArray;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIArgumentDescriptor;
import org.eclipse.ptp.debug.core.cdi.model.IPCDILocalVariableDescriptor;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIVariable;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIVariableDescriptor;
import org.eclipse.ptp.debug.core.model.IPType;
import org.eclipse.ptp.debug.core.model.IPValue;

/**
 * @author Clement chu
 *
 */
public class PLocalVariable extends PVariable {
	private class InternalVariable implements IInternalVariable {
		private PVariable fVariable;
		private IPCDIVariableDescriptor fCDIVariableObject;
		private IPCDIVariable fCDIVariable;
		private PType fType;
		private String fQualifiedName;
		private IPValue fValue = PValueFactory.NULL_VALUE;
		private boolean fChanged = false;
		
		InternalVariable(PVariable var, IPCDIVariableDescriptor varObject) {
			setVariable(var);
			setCDIVariableObject(varObject);
			setCDIVariable((varObject instanceof IPCDIVariable) ? (IPCDIVariable) varObject : null);
		}
		public IInternalVariable createShadow(int start, int length) throws DebugException {
			IInternalVariable iv = null;
			try {
				iv = new InternalVariable(getVariable(), getCDIVariableObject().getVariableDescriptorAsArray(start, length));
			} catch (PCDIException e) {
				requestFailed(e.getMessage(), null);
			}
			return iv;
		}
		public IInternalVariable createShadow(String type) throws DebugException {
			IInternalVariable iv = null;
			try {
				iv = new InternalVariable(getVariable(), getCDIVariableObject().getVariableDescriptorAsType(type));
			} catch (PCDIException e) {
				requestFailed(e.getMessage(), null);
			}
			return iv;
		}
		private synchronized IPCDIVariable getCDIVariable() throws DebugException {
			if (fCDIVariable == null) {
				try {
					fCDIVariable = ((PStackFrame)getStackFrame()).getCDIStackFrame().createLocalVariable((IPCDILocalVariableDescriptor)getCDIVariableObject());
				}
				catch(PCDIException e) {
					requestFailed(e.getMessage(), null);
				}
			}
			return fCDIVariable;
		}
		private void setCDIVariable(IPCDIVariable variable) {
			fCDIVariable = variable;
		}
		private IPCDIVariableDescriptor getCDIVariableObject() {
			if (fCDIVariable != null) {
				return fCDIVariable;
			}
			return fCDIVariableObject;
		}
		private void setCDIVariableObject(IPCDIVariableDescriptor variableObject) {
			fCDIVariableObject = variableObject;
		}
		public String getQualifiedName() throws DebugException {
			if (fQualifiedName == null) {
				try {
					fQualifiedName = (fCDIVariableObject != null) ? fCDIVariableObject.getQualifiedName() : null;
				} catch (PCDIException e) {
					requestFailed(e.getMessage(), null);
				}
			}
			return fQualifiedName;
		}
		public PType getType() throws DebugException {
			if (fType == null) {
				IPCDIVariableDescriptor varObject = getCDIVariableObject();
				if (varObject != null) {
					synchronized (this) {
						if (fType == null) {
							try {
								fType = new PType(varObject.getType());
							}
							catch(CDIException e) {
								requestFailed(e.getMessage(), null);
							}
						}
					}
				}
			}
			return fType;
		}
		private synchronized void invalidate(boolean destroy) {
			try {
				if (destroy && fCDIVariable != null)
					fCDIVariable.dispose();
			} catch (PCDIException e) {
				logError(e.getMessage());
			}
			invalidateValue();			
			setCDIVariable(null);
			if (fType != null)
				fType.dispose();
			fType = null;
		}
		public void dispose(boolean destroy) {
			invalidate(destroy);
		}
		public boolean isSameVariable(IPCDIVariable cdiVar) {
			return (fCDIVariable != null) ? fCDIVariable.equals(cdiVar) : false;
		}
		public int sizeof() {
			if (getCDIVariableObject() != null) {
				try {
					return getCDIVariableObject().sizeof();
				} catch (PCDIException e) {
				}
			}
			return 0;
		}
		public boolean isArgument() {
			return (getCDIVariableObject() instanceof IPCDIArgumentDescriptor);
		}
		public void setValue(String expression) throws DebugException {
			IPCDIVariable cdiVariable = null;
			try {
				cdiVariable = getCDIVariable();
				if (cdiVariable != null)
					cdiVariable.setValue(expression);
				else
					requestFailed(CoreModelMessages.getString("PModificationVariable.0"), null);
			} catch (PCDIException e) {
				targetRequestFailed(e.getMessage(), null);
			}
		}
		public synchronized IPValue getValue() throws DebugException {
			if (fValue.equals(PValueFactory.NULL_VALUE)) {
				IPCDIVariable var = getCDIVariable();
				if (var != null) {
					try {
						IAIFType aifType = var.getType();
						if (aifType != null && aifType instanceof IAIFTypeArray) {
							IPType type = new PType(aifType);
							if (type.isArray()) {
								int[] dims = type.getArrayDimensions();
								//int cur_dim = aifValueArray.getCurrentDimensionPosition();
								if (dims.length > 0 && dims[0] > 0)
									fValue = PValueFactory.createIndexedValue(getVariable(), var, 0, dims[0]);
							}
						}
						else {
							fValue = PValueFactory.createValue(getVariable(), var);
						}
						//if (getCDITarget().getConfiguration() instanceof IPCDITargetConfiguration2 && ((IPCDITargetConfiguration2)getCDITarget().getConfiguration()).supportsRuntimeTypeIdentification())
							//fType = null; // When the debugger supports RTTI getting a new value may also mean a new type.
					} catch (PCDIException e) {
						requestFailed(e.getMessage(), null);
					}
				}
			}
			return fValue;
		}
		public void invalidateValue() {
			if (fValue instanceof AbstractPValue) {
				((AbstractPValue) fValue).dispose();
				fValue = PValueFactory.NULL_VALUE;
				if (fCDIVariable != null)
					fCDIVariable.resetValue();
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
			IPCDIVariable var = getCDIVariable();
			if (var != null) {
				try {
					return var.isEditable();
				} catch (PCDIException e) {
				}
			}
			return false;
		}
		public boolean equals(Object obj) {
			if (obj instanceof InternalVariable) {
				return getCDIVariableObject().equals(((InternalVariable) obj).getCDIVariableObject());
			}
			return false;
		}
		public boolean isSameDescriptor(IPCDIVariableDescriptor desc) {
			return getCDIVariableObject().equals(desc);
		}
	}

	public PLocalVariable(PDebugElement parent, IPCDIVariableDescriptor cdiVariableObject, String errorMessage) {
		super(parent, cdiVariableObject, errorMessage);
	}
	public PLocalVariable(PDebugElement parent, IPCDIVariableDescriptor cdiVariableObject) {
		super(parent, cdiVariableObject);
	}
	protected void createOriginal(IPCDIVariableDescriptor vo) {
		if (vo != null) {
			setName(vo.getName());
			setOriginal(new InternalVariable(this, vo));
		}
	}
}
