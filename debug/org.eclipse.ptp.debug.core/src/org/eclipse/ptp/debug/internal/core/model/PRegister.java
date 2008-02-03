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
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.ptp.debug.core.IPDebugConstants;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.model.IPRegister;
import org.eclipse.ptp.debug.core.model.IPRegisterDescriptor;
import org.eclipse.ptp.debug.core.model.IPStackFrame;
import org.eclipse.ptp.debug.core.model.IPValue;
import org.eclipse.ptp.debug.core.model.PVariableFormat;
import org.eclipse.ptp.debug.core.pdi.IPDISessionObject;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.event.IPDIChangedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIMemoryBlockInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDIResumedEvent;
import org.eclipse.ptp.debug.core.pdi.model.IPDIArgumentDescriptor;
import org.eclipse.ptp.debug.core.pdi.model.IPDIRegister;
import org.eclipse.ptp.debug.core.pdi.model.IPDIRegisterDescriptor;
import org.eclipse.ptp.debug.core.pdi.model.IPDIVariable;
import org.eclipse.ptp.debug.core.pdi.model.IPDIVariableDescriptor;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIF;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeArray;

/**
 * @author clement
 *
 */
public class PRegister extends PVariable implements IPRegister {
	private class InternalVariable implements IInternalVariable {
		private PVariable fVariable;
		private IPDIVariableDescriptor fPDIVariableObject;
		private IPDIRegister fPDIRegister;
		private String fQualifiedName;
		private IPValue fValue = PValueFactory.NULL_VALUE;
		private boolean fChanged = false;

		InternalVariable(PVariable var, IPDIVariableDescriptor varObject) {
			setVariable(var);
			setPDIVariableObject(varObject);
			setPDIRegister((varObject instanceof IPDIRegister) ? (IPDIRegister)varObject : null);
		}

		public IInternalVariable createShadow(int start, int length) throws DebugException {
			IInternalVariable iv = null;
			try {
				iv = new InternalVariable(getVariable(), getPDIVariableObject().getVariableDescriptorAsArray(start, length));
			}
			catch(PDIException e) {
				requestFailed(e.getMessage(), null);
			}
			return iv;
		}

		public IInternalVariable createShadow(String type) throws DebugException {
			IInternalVariable iv = null;
			try {
				iv = new InternalVariable(getVariable(), getPDIVariableObject().getVariableDescriptorAsType(type));
			}
			catch(PDIException e) {
				requestFailed(e.getMessage(), null);
			}
			return iv;
		}
		private synchronized IPDIRegister getPDIRegister() throws DebugException {
			if (fPDIRegister == null) {
				try {
					fPDIRegister = getPDITarget().createRegister((IPDIRegisterDescriptor)getPDIVariableObject());
				}
				catch(PDIException e) {
					requestFailed(e.getMessage(), null);
				}
			}
			return fPDIRegister;
		}

		private void setPDIRegister(IPDIRegister register) {
			fPDIRegister = register;
		}

		private IPDIVariableDescriptor getPDIVariableObject() {
			if (fPDIRegister != null) {
				return fPDIRegister;
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
				}
				catch(PDIException e) {
					requestFailed(e.getMessage(), null);
				}
			}
			return fQualifiedName;
		}
		private synchronized void invalidate(boolean destroy) {
			try {
				if (destroy && fPDIRegister != null)
					fPDIRegister.dispose();
			}
			catch(PDIException e) {
				logError(e.getMessage());
			}
			invalidateValue();
			setPDIRegister(null);
		}
		public void dispose(boolean destroy) {
			invalidate(destroy);
		}
		public boolean isSameVariable(IPDIVariable pdiVar) {
			return (fPDIRegister != null) ? fPDIRegister.equals(pdiVar) : false;
		}
		public int sizeof() {
			if (getPDIVariableObject() != null) {
				try {
					return getPDIVariableObject().sizeof();
				}
				catch(PDIException e) {
				}
			}
			return 0;
		}
		public boolean isArgument() {
			return (getPDIVariableObject() instanceof IPDIArgumentDescriptor);
		}
		public void setValue(String expression) throws DebugException {
			IPDIRegister pdiRegister = null;
			try {
				pdiRegister = getPDIRegister();
				if (pdiRegister != null)
					pdiRegister.setValue(expression);
				else
					requestFailed(CoreModelMessages.getString("CModificationVariable.0"), null);
			}
			catch(PDIException e) {
				targetRequestFailed(e.getMessage(), null);
			}
		}
		public synchronized IPValue getValue() throws DebugException {
			if (fValue.equals(PValueFactory.NULL_VALUE)) {
				IPDIRegister reg = getPDIRegister();
				if (reg != null) {
					try {
						IAIF aif = reg.getAIF(getCurrentStackFrame().getPDIStackFrame());
						if (aif != null && aif.getType() instanceof IAIFTypeArray) {
							int[] dims = ((IAIFTypeArray)aif.getType()).getDimensionDetails();
							if (dims.length > 0 && dims[0] > 0)
								fValue = PValueFactory.createIndexedValue(getVariable(), reg, 0, dims[0]);
						}
						else {
							fValue = PValueFactory.createValue(getVariable(), reg);
						}
					}
					catch(PDIException e) {
						requestFailed(e.getMessage(), e);
					}
				}
			}
			return fValue;
		}
		public void invalidateValue() {
			if (fValue instanceof AbstractPValue) {
				((AbstractPValue)fValue).dispose();
				fValue = PValueFactory.NULL_VALUE;
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
				((AbstractPValue)fValue).setChanged(changed);
			}
			fChanged = changed;
		}
		public synchronized void preserve() {
			setChanged(false);
			if (fValue instanceof AbstractPValue) {
				((AbstractPValue)fValue).preserve();
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
				((AbstractPValue)fValue).reset();
			}
		}
		public boolean isEditable() throws DebugException {
			IPDIRegister reg = getPDIRegister();
			if (reg != null) {
				try {
					return reg.isEditable();
				}
				catch(PDIException e) {
				}
			}
			return false;
		}
		public boolean equals(Object obj) {
			if (obj instanceof InternalVariable) {
				return getPDIVariableObject().equals(((InternalVariable)obj).getPDIVariableObject());
			}
			return false;
		}
		public boolean isSameDescriptor(IPDIVariableDescriptor desc) {
			return getPDIVariableObject().equals(desc);
		}
	}
	protected PRegister(PRegisterGroup parent, IPRegisterDescriptor descriptor) {
		super(parent, ((PRegisterDescriptor)descriptor).getPDIDescriptor());
		setFormat(PVariableFormat.getFormat(PTPDebugCorePlugin.getDefault().getPluginPreferences().getInt(IPDebugConstants.PREF_DEFAULT_REGISTER_FORMAT)));
	}
	protected PRegister(PRegisterGroup parent, IPRegisterDescriptor descriptor, String message) {
		super(parent, ((PRegisterDescriptor)descriptor).getPDIDescriptor(), message);
		setFormat(PVariableFormat.getFormat(PTPDebugCorePlugin.getDefault().getPluginPreferences().getInt(IPDebugConstants.PREF_DEFAULT_REGISTER_FORMAT)));
	}
	public IRegisterGroup getRegisterGroup() throws DebugException {
		return (IRegisterGroup)getParent();
	}
	protected boolean isBookkeepingEnabled() {
		boolean result = false;
		return result;
	}
	public boolean canEnableDisable() {
		return true;
	}
	public void handleDebugEvents(IPDIEvent[] events) {
		for(int i = 0; i < events.length; i++) {
			IPDIEvent event = events[i];
			if (!event.contains(getTasks()))
				continue;

			if (event instanceof IPDIChangedEvent) {
				IPDISessionObject reason = ((IPDIChangedEvent)event).getReason();
				if (reason instanceof IPDIMemoryBlockInfo) {
					resetValue();
					return;	// avoid similar but logic inappropriate for us in PVariable
				}
			}
			else if (event instanceof IPDIResumedEvent) {
				setChanged(false);
			}
		}
		super.handleDebugEvents(events);
	}
	public void dispose() {
		internalDispose(true);
		setDisposed(true);
	}
	protected IPStackFrame getStackFrame() {
		IPStackFrame frame = super.getStackFrame();
		if (frame == null)
			frame = getCurrentStackFrame();
		return frame;
	}
	protected PStackFrame getCurrentStackFrame() {
		return fSession.getRegisterManager().getCurrentFrame(getTasks());
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
