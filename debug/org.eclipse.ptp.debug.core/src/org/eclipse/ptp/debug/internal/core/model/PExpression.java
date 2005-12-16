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
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.ptp.debug.core.IPDebugConstants;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.aif.IAIF;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIResumedEvent;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIExpression;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIObject;
import org.eclipse.ptp.debug.core.cdi.model.IPCDITarget;
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

	public PExpression(PStackFrame frame, IPCDIExpression cdiExpression, IPCDIVariableDescriptor varObject) {
		super(frame, varObject);
		setFormat(PVariableFormat.getFormat(PTPDebugCorePlugin.getDefault().getPluginPreferences().getInt(IPDebugConstants.PREF_DEFAULT_EXPRESSION_FORMAT)));
		fText = cdiExpression.getExpressionText();
		fCDIExpression = cdiExpression;
		fStackFrame = frame;
	}
	public String getExpressionText() {
		return fText;
	}
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
	public boolean isEnabled() {
		return true;
	}
	public boolean canEnableDisable() {
		return true;
	}
	protected boolean isBookkeepingEnabled() {
		return false;
	}
	public IValue getValue() {
		PStackFrame frame = (PStackFrame) getStackFrame();
		try {
			return getValue(frame);
		} catch (DebugException e) {
		}
		return null;
	}
	protected synchronized IValue getValue(PStackFrame frame) throws DebugException {
		if (fValue.equals(PValueFactory.NULL_VALUE)) {
			if (frame.isSuspended()) {
				try {
					IAIF aif = fCDIExpression.getAIF(frame.getCDIStackFrame());
					if (aif != null) {
						fValue = PValueFactory.createValue(this, aif);
					}
				} catch (PCDIException e) {
					targetRequestFailed(e.getMessage(), null);
				}
			}
		}
		return fValue;
	}
	protected IPStackFrame getStackFrame() {
		return fStackFrame;
	}
	protected void resetValue() {
		if (fValue instanceof AbstractPValue) {
			((AbstractPValue) fValue).reset();
		}
		fValue = PValueFactory.NULL_VALUE;
	}
	public String getExpressionString() throws DebugException {
		return getExpressionText();
	}
	public void dispose() {
		if (fCDIExpression != null) {
			try {
				fCDIExpression.dispose();
				fCDIExpression = null;
			} catch (CDIException e) {
			}
		}
		if (fValue instanceof AbstractPValue) {
			((AbstractPValue) fValue).dispose();
			fValue = PValueFactory.NULL_VALUE;
		}
		internalDispose(true);
		setDisposed(true);
	}
	public IPType getType() throws DebugException {
		if (isDisposed())
			return null;
		if (fType == null) {
			synchronized (this) {
				if (fType == null) {
					fType = new PType(((AbstractPValue) fValue).getAIF().getType());
				}
			}
		}
		return fType;
	}
	public String getReferenceTypeName() throws DebugException {
		IPType type = getType();
		return (type != null) ? type.getName() : "";
	}
}
