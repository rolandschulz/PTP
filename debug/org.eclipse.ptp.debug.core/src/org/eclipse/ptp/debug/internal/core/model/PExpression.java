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
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.ptp.debug.core.IPDebugConstants;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.aif.IAIFValue;
import org.eclipse.ptp.debug.core.aif.IAIFValueArray;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIResumedEvent;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIExpression;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIObject;
import org.eclipse.ptp.debug.core.cdi.model.IPCDITarget;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIVariable;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIVariableDescriptor;
import org.eclipse.ptp.debug.core.model.IPStackFrame;
import org.eclipse.ptp.debug.core.model.IPType;
import org.eclipse.ptp.debug.core.model.PVariableFormat;

/**
 * @author Clement chu
 * 
 */
public class PExpression extends PLocalVariable implements IExpression {
	private String fText;
	private IPCDIExpression fCDIExpression;
	private PStackFrame fStackFrame;
	private IValue fValue = PValueFactory.NULL_VALUE;
	private IPType fType;

	/** Constructor
	 * @param frame
	 * @param cdiExpression
	 * @param varObject
	 */
	public PExpression(PStackFrame frame, IPCDIExpression cdiExpression, IPCDIVariableDescriptor varObject) {
		super(frame, varObject);
		setFormat(PVariableFormat.getFormat(PTPDebugCorePlugin.getDefault().getPluginPreferences().getInt(IPDebugConstants.PREF_DEFAULT_EXPRESSION_FORMAT)));
		fText = cdiExpression.getExpressionText();
		fCDIExpression = cdiExpression;
		fStackFrame = frame;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IExpression#getExpressionText()
	 */
	public String getExpressionText() {
		return fText;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.cdi.event.IPCDIEventListener#handleDebugEvents(org.eclipse.ptp.debug.core.cdi.event.IPCDIEvent[])
	 */
	public void handleDebugEvents(IPCDIEvent[] events) {
		for (int i = 0; i < events.length; i++) {
			IPCDIEvent event = events[i];
			if (event instanceof IPCDIResumedEvent) {
				IPCDIObject source = event.getSource();
				if (source != null) {
					IPCDITarget cdiTarget = source.getTarget();
					if (getCDITarget().equals(cdiTarget)) {
						setChanged(false);
						resetValue();
					}
				}
			}
		}
		super.handleDebugEvents(events);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IEnableDisableTarget#isEnabled()
	 */
	public boolean isEnabled() {
		return true;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IEnableDisableTarget#canEnableDisable()
	 */
	public boolean canEnableDisable() {
		return true;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.internal.core.model.PVariable#isBookkeepingEnabled()
	 */
	protected boolean isBookkeepingEnabled() {
		return false;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IVariable#getValue()
	 */
	public IValue getValue() {
		PStackFrame frame = (PStackFrame) getStackFrame();
		try {
			return getValue(frame);
		} catch (DebugException e) {
		}
		return null;
	}
	/** Get value
	 * @param frame
	 * @return
	 * @throws DebugException
	 */
	protected synchronized IValue getValue(PStackFrame frame) throws DebugException {
		if (fValue.equals(PValueFactory.NULL_VALUE)) {
			if (frame.isSuspended()) {
				try {
					IPCDIVariable variable = fCDIExpression.getCDIVariable(frame.getCDIStackFrame());
					if (variable != null) {
						IAIFValue aifValue = variable.getValue();
						if (aifValue != null) {
							if (aifValue instanceof IAIFValueArray) {
								IPType type = new PType(aifValue.getType());
								if (type != null && type.isArray()) {
									int[] dims = type.getArrayDimensions();
									if (dims.length > 0 && dims[0] > 0)
										fValue = PValueFactory.createIndexedValue(this, variable, 0, dims[0]);
								}
							}
						}
						else {
							fValue = PValueFactory.createValue(this, variable);
						}
					}
				} catch (PCDIException e) {
					targetRequestFailed(e.getMessage(), null);
				}
			}
		}
		return fValue;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.internal.core.model.AbstractPVariable#getStackFrame()
	 */
	protected IPStackFrame getStackFrame() {
		return fStackFrame;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.internal.core.model.AbstractPVariable#resetValue()
	 */
	protected void resetValue() {
		if (fValue instanceof AbstractPValue) {
			((AbstractPValue) fValue).reset();
		} 
		fValue = PValueFactory.NULL_VALUE;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.internal.core.model.AbstractPVariable#getExpressionString()
	 */
	public String getExpressionString() throws DebugException {
		return getExpressionText();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.internal.core.model.AbstractPVariable#dispose()
	 */
	public void dispose() {
		if (fCDIExpression != null) {
			try {
				fCDIExpression.dispose();
				fCDIExpression = null;
			} catch (PCDIException e) {
			}
		}
		if (fValue instanceof AbstractPValue) {
			((AbstractPValue) fValue).dispose();
			fValue = PValueFactory.NULL_VALUE;
		}
		internalDispose(true);
		setDisposed(true);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.model.IPVariable#getType()
	 */
	public IPType getType() throws DebugException {
		if (isDisposed())
			return null;
		if (fType == null) {
			synchronized (this) {
				if (fType == null) {
					fType = ((AbstractPValue)fValue).getType();
				}
			}
		}
		return fType;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IVariable#getReferenceTypeName()
	 */
	public String getReferenceTypeName() throws DebugException {
		IPType type = getType();
		return (type != null) ? type.getName() : "";
	}
}
