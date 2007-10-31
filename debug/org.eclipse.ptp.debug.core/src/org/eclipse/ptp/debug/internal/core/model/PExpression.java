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
import org.eclipse.ptp.debug.core.IPDebugConstants;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.model.IPStackFrame;
import org.eclipse.ptp.debug.core.model.IPValue;
import org.eclipse.ptp.debug.core.model.PVariableFormat;
import org.eclipse.ptp.debug.core.pdi.IPDISessionObject;
import org.eclipse.ptp.debug.core.pdi.IPDIVariableInfo;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.event.IPDIChangedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIResumedEvent;
import org.eclipse.ptp.debug.core.pdi.model.IPDITargetExpression;
import org.eclipse.ptp.debug.core.pdi.model.IPDIVariable;
import org.eclipse.ptp.debug.core.pdi.model.IPDIVariableDescriptor;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIF;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeArray;

/**
 * @author Clement chu
 * 
 */
public class PExpression extends PLocalVariable implements IExpression {
	private String fText;
	private IPDITargetExpression fPDIExpression;
	private PStackFrame fStackFrame;
	private IPValue fValue = PValueFactory.NULL_VALUE;

	/** Constructor
	 * @param frame
	 * @param cdiExpression
	 * @param varObject
	 */
	public PExpression(PStackFrame frame, IPDITargetExpression pdiExpression, IPDIVariableDescriptor varObject) {
		super(frame, varObject);
		setFormat(PVariableFormat.getFormat(PTPDebugCorePlugin.getDefault().getPluginPreferences().getInt(IPDebugConstants.PREF_DEFAULT_EXPRESSION_FORMAT)));
		fText = pdiExpression.getExpressionText();
		fPDIExpression = pdiExpression;
		fStackFrame = frame;
	}
	public String getExpressionText() {
		return fText;
	}
	public void handleDebugEvents(IPDIEvent[] events) {
		for (int i = 0; i < events.length; i++) {
			IPDIEvent event = events[i];
			if (!event.contains(getTasks()))
				continue;
			
			if (event instanceof IPDIResumedEvent) {
				setChanged(false);
				resetValue();
			}
			else if (event instanceof IPDIChangedEvent) {
				IPDISessionObject reason = ((IPDIChangedEvent)event).getReason();
				if (reason instanceof IPDIVariableInfo) {
					setChanged(false);
					resetValue();
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
	public IPValue getValue() {
		PStackFrame frame = (PStackFrame) getStackFrame();
		try {
			return getValue(frame);
		} catch (DebugException e) {
		}
		return null;
	}
	protected synchronized IPValue getValue(PStackFrame frame) throws DebugException {
		if (fValue.equals(PValueFactory.NULL_VALUE)) {
			if (frame.isSuspended()) {
				try {
					IPDIVariable variable = fPDIExpression.getVariable(frame.getPDIStackFrame());
					if (variable != null) {
						IAIF aif = variable.getAIF();
						if (aif != null && aif.getType() instanceof IAIFTypeArray) {
							int[] dims = ((IAIFTypeArray)aif.getType()).getDimensionDetails();
							if (dims.length > 0 && dims[0] > 0)
								fValue = PValueFactory.createIndexedValue(this, variable, 0, dims[0]);
						}
						else {
							fValue = PValueFactory.createValue(this, variable);
						}
					}
				} catch (PDIException e) {
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
		if (fPDIExpression != null) {
			try {
				fPDIExpression.dispose();
				fPDIExpression = null;
			} catch (PDIException e) {
			}
		}
		if (fValue instanceof AbstractPValue) {
			((AbstractPValue) fValue).dispose();
			fValue = PValueFactory.NULL_VALUE;
		}
		internalDispose(true);
		setDisposed(true);
	}
}
