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
import org.eclipse.ptp.debug.core.model.IPValue;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.model.IPDIArgumentDescriptor;
import org.eclipse.ptp.debug.core.pdi.model.IPDILocalVariableDescriptor;
import org.eclipse.ptp.debug.core.pdi.model.IPDIVariable;
import org.eclipse.ptp.debug.core.pdi.model.IPDIVariableDescriptor;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIF;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeArray;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeRange;
import org.eclipse.ptp.internal.debug.core.messages.Messages;

/**
 * @author Clement chu
 * 
 */
public class PLocalVariable extends PVariable {
	private class InternalVariable implements IInternalVariable {
		private boolean fChanged = false;
		private IPDIVariable fPDIVariable;
		private IPDIVariableDescriptor fPDIVariableObject;
		private String fQualifiedName;
		private IPValue fValue = PValueFactory.NULL_VALUE;
		private PVariable fVariable;

		InternalVariable(PVariable var, IPDIVariableDescriptor varObject) {
			setVariable(var);
			setPDIVariableObject(varObject);
			setPDIVariable((varObject instanceof IPDIVariable) ? (IPDIVariable) varObject : null);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.internal.debug.core.model.PVariable.IInternalVariable
		 * #createShadow(int, int)
		 */
		public IInternalVariable createShadow(int start, int length) throws DebugException {
			IInternalVariable iv = null;
			try {
				iv = new InternalVariable(getVariable(), getPDIVariableObject().getVariableDescriptorAsArray(start, length));
			} catch (final PDIException e) {
				requestFailed(e.getMessage(), null);
			}
			return iv;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.internal.debug.core.model.PVariable.IInternalVariable
		 * #createShadow(java.lang.String)
		 */
		public IInternalVariable createShadow(String type) throws DebugException {
			IInternalVariable iv = null;
			try {
				iv = new InternalVariable(getVariable(), getPDIVariableObject().getVariableDescriptorAsType(type));
			} catch (final PDIException e) {
				requestFailed(e.getMessage(), null);
			}
			return iv;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.internal.debug.core.model.PVariable.IInternalVariable
		 * #dispose(boolean)
		 */
		public void dispose(boolean destroy) {
			invalidate(destroy);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof InternalVariable) {
				return getPDIVariableObject().equals(((InternalVariable) obj).getPDIVariableObject());
			}
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.internal.debug.core.model.PVariable.IInternalVariable
		 * #getQualifiedName()
		 */
		public String getQualifiedName() throws DebugException {
			if (fQualifiedName == null) {
				try {
					fQualifiedName = (fPDIVariableObject != null) ? fPDIVariableObject.getQualifiedName() : null;
				} catch (final PDIException e) {
					requestFailed(e.getMessage(), null);
				}
			}
			return fQualifiedName;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.internal.debug.core.model.PVariable.IInternalVariable
		 * #getValue()
		 */
		public synchronized IPValue getValue() throws DebugException {
			if (fValue.equals(PValueFactory.NULL_VALUE)) {
				final IPDIVariable var = getPDIVariable();
				if (var != null) {
					try {
						final IAIF aif = var.getAIF();
						if (aif != null && aif.getType() instanceof IAIFTypeArray) {
							final IAIFTypeRange range = ((IAIFTypeArray) aif.getType()).getRange();
							fValue = PValueFactory.createIndexedValue(getVariable(), var, 0, range.getSize());
						} else {
							fValue = PValueFactory.createValue(getVariable(), var);
						}
					} catch (final PDIException e) {
						requestFailed(e.getMessage(), null);
					}
				}
			}
			return fValue;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.internal.debug.core.model.PVariable.IInternalVariable
		 * #invalidateValue()
		 */
		public void invalidateValue() {
			if (fValue instanceof AbstractPValue) {
				((AbstractPValue) fValue).dispose();
				fValue = PValueFactory.NULL_VALUE;
				if (fPDIVariable != null) {
					fPDIVariable.resetValue();
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.internal.debug.core.model.PVariable.IInternalVariable
		 * #isArgument()
		 */
		public boolean isArgument() {
			return (getPDIVariableObject() instanceof IPDIArgumentDescriptor);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.internal.debug.core.model.PVariable.IInternalVariable
		 * #isChanged()
		 */
		public boolean isChanged() {
			return fChanged;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.internal.debug.core.model.PVariable.IInternalVariable
		 * #isEditable()
		 */
		public boolean isEditable() throws DebugException {
			final IPDIVariable var = getPDIVariable();
			if (var != null) {
				try {
					return var.isEditable();
				} catch (final PDIException e) {
				}
			}
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.internal.debug.core.model.PVariable.IInternalVariable
		 * #isSameDescriptor(org.eclipse.ptp.debug.core.pdi.model.
		 * IPDIVariableDescriptor)
		 */
		public boolean isSameDescriptor(IPDIVariableDescriptor desc) {
			return getPDIVariableObject().equals(desc);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.internal.debug.core.model.PVariable.IInternalVariable
		 * #isSameVariable(org.eclipse.ptp.debug.core.pdi.model.IPDIVariable)
		 */
		public boolean isSameVariable(IPDIVariable pdiVar) {
			return (fPDIVariable != null) ? fPDIVariable.equals(pdiVar) : false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.internal.debug.core.model.PVariable.IInternalVariable
		 * #preserve()
		 */
		public synchronized void preserve() {
			setChanged(false);
			if (fValue instanceof AbstractPValue) {
				((AbstractPValue) fValue).preserve();
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.internal.debug.core.model.PVariable.IInternalVariable
		 * #resetValue()
		 */
		public void resetValue() {
			if (fValue instanceof AbstractPValue) {
				((AbstractPValue) fValue).reset();
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.internal.debug.core.model.PVariable.IInternalVariable
		 * #setChanged(boolean)
		 */
		public synchronized void setChanged(boolean changed) {
			if (changed) {
				invalidateValue();
			}
			if (fValue instanceof AbstractPValue) {
				((AbstractPValue) fValue).setChanged(changed);
			}
			fChanged = changed;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.internal.debug.core.model.PVariable.IInternalVariable
		 * #setValue(java.lang.String)
		 */
		public void setValue(String expression) throws DebugException {
			IPDIVariable pdiVariable = null;
			try {
				pdiVariable = getPDIVariable();
				if (pdiVariable != null) {
					pdiVariable.setValue(expression);
				} else {
					requestFailed(Messages.PLocalVariable_0, null);
				}
			} catch (final PDIException e) {
				targetRequestFailed(e.getMessage(), null);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.internal.debug.core.model.PVariable.IInternalVariable
		 * #sizeof()
		 */
		public int sizeof() {
			if (getPDIVariableObject() != null) {
				try {
					return getPDIVariableObject().sizeof();
				} catch (final PDIException e) {
				}
			}
			return 0;
		}

		/**
		 * @return
		 * @throws DebugException
		 */
		private synchronized IPDIVariable getPDIVariable() throws DebugException {
			if (fPDIVariable == null) {
				try {
					fPDIVariable = ((PStackFrame) getStackFrame()).getPDIStackFrame().createLocalVariable(
							(IPDILocalVariableDescriptor) getPDIVariableObject());
				} catch (final PDIException e) {
					requestFailed(e.getMessage(), null);
				}
			}
			return fPDIVariable;
		}

		/**
		 * @return
		 */
		private IPDIVariableDescriptor getPDIVariableObject() {
			if (fPDIVariable != null) {
				return fPDIVariable;
			}
			return fPDIVariableObject;
		}

		/**
		 * @param destroy
		 */
		private synchronized void invalidate(boolean destroy) {
			try {
				if (destroy && fPDIVariable != null) {
					fPDIVariable.dispose();
				}
			} catch (final PDIException e) {
				logError(e.getMessage());
			}
			invalidateValue();
			setPDIVariable(null);
		}

		/**
		 * @param variable
		 */
		private void setPDIVariable(IPDIVariable variable) {
			fPDIVariable = variable;
		}

		/**
		 * @param variableObject
		 */
		private void setPDIVariableObject(IPDIVariableDescriptor variableObject) {
			fPDIVariableObject = variableObject;
		}

		/**
		 * @param variable
		 */
		private void setVariable(PVariable variable) {
			fVariable = variable;
		}

		private PVariable getVariable() {
			return fVariable;
		}
	}

	public PLocalVariable(PDebugElement parent, IPDIVariableDescriptor pdiVariableObject) {
		super(parent, pdiVariableObject);
	}

	public PLocalVariable(PDebugElement parent, IPDIVariableDescriptor pdiVariableObject, String errorMessage) {
		super(parent, pdiVariableObject, errorMessage);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IPVariable#getAIF()
	 */
	public IAIF getAIF() throws DebugException {
		return getValue().getAIF();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.model.PVariable#createOriginal(org
	 * .eclipse.ptp.debug.core.pdi.model.IPDIVariableDescriptor)
	 */
	@Override
	protected void createOriginal(IPDIVariableDescriptor vo) {
		if (vo != null) {
			setName(vo.getName());
			setOriginal(new InternalVariable(this, vo));
		}
	}
}
