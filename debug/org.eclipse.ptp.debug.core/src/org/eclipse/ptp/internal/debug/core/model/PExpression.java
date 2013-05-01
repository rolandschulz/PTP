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
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.debug.core.IPDebugConstants;
import org.eclipse.ptp.debug.core.model.IPStackFrame;
import org.eclipse.ptp.debug.core.model.IPValue;
import org.eclipse.ptp.debug.core.model.PVariableFormat;
import org.eclipse.ptp.debug.core.pdi.IPDISessionObject;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.event.IPDIChangedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIResumedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIVariableInfo;
import org.eclipse.ptp.debug.core.pdi.model.IPDITargetExpression;
import org.eclipse.ptp.debug.core.pdi.model.IPDIVariable;
import org.eclipse.ptp.debug.core.pdi.model.IPDIVariableDescriptor;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIF;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeArray;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeRange;
import org.eclipse.ptp.internal.debug.core.PTPDebugCorePlugin;

/**
 * @author Clement chu
 * 
 */
public class PExpression extends PLocalVariable implements IExpression {
	private IPDITargetExpression fPDIExpression;
	private final PStackFrame fStackFrame;
	private final String fText;
	private IPValue fValue = PValueFactory.NULL_VALUE;

	/**
	 * Constructor
	 * 
	 * @param frame
	 * @param cdiExpression
	 * @param varObject
	 */
	public PExpression(PStackFrame frame, IPDITargetExpression pdiExpression, IPDIVariableDescriptor varObject) {
		super(frame, varObject);
		setFormat(PVariableFormat.getFormat(Preferences.getInt(PTPDebugCorePlugin.getUniqueIdentifier(),
				IPDebugConstants.PREF_DEFAULT_EXPRESSION_FORMAT)));
		fText = pdiExpression.getExpressionText();
		fPDIExpression = pdiExpression;
		fStackFrame = frame;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.model.PVariable#canEnableDisable()
	 */
	@Override
	public boolean canEnableDisable() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.debug.core.model.PVariable#dispose()
	 */
	@Override
	public void dispose() {
		if (fPDIExpression != null) {
			try {
				fPDIExpression.dispose();
				fPDIExpression = null;
			} catch (final PDIException e) {
			}
		}
		if (fValue instanceof AbstractPValue) {
			((AbstractPValue) fValue).dispose();
			fValue = PValueFactory.NULL_VALUE;
		}
		internalDispose(true);
		setDisposed(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.model.PVariable#getExpressionString()
	 */
	@Override
	public String getExpressionString() throws DebugException {
		return getExpressionText();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IExpression#getExpressionText()
	 */
	public String getExpressionText() {
		return fText;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.debug.core.model.PVariable#getValue()
	 */
	@Override
	public IPValue getValue() {
		final PStackFrame frame = (PStackFrame) getStackFrame();
		try {
			return getValue(frame);
		} catch (final DebugException e) {
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.model.PVariable#handleDebugEvents
	 * (org.eclipse.ptp.debug.core.pdi.event.IPDIEvent[])
	 */
	@Override
	public void handleDebugEvents(IPDIEvent[] events) {
		for (final IPDIEvent event2 : events) {
			final IPDIEvent event = event2;
			if (!event.contains(getTasks())) {
				continue;
			}

			if (event instanceof IPDIResumedEvent) {
				setChanged(false);
				resetValue();
			} else if (event instanceof IPDIChangedEvent) {
				final IPDISessionObject reason = ((IPDIChangedEvent) event).getReason();
				if (reason instanceof IPDIVariableInfo) {
					setChanged(false);
					resetValue();
				}
			}
		}
		super.handleDebugEvents(events);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.debug.core.model.PVariable#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.model.AbstractPVariable#getStackFrame
	 * ()
	 */
	@Override
	protected IPStackFrame getStackFrame() {
		return fStackFrame;
	}

	/**
	 * @param frame
	 * @return
	 * @throws DebugException
	 */
	protected synchronized IPValue getValue(PStackFrame frame) throws DebugException {
		if (fValue.equals(PValueFactory.NULL_VALUE)) {
			if (frame.isSuspended()) {
				try {
					final IPDIVariable variable = fPDIExpression.getVariable(frame.getPDIStackFrame());
					if (variable != null) {
						final IAIF aif = variable.getAIF();
						if (aif != null && aif.getType() instanceof IAIFTypeArray) {
							final IAIFTypeRange range = ((IAIFTypeArray) aif.getType()).getRange();
							fValue = PValueFactory.createIndexedValue(this, variable, 0, range.getSize());
						} else {
							fValue = PValueFactory.createValue(this, variable);
						}
					}
				} catch (final PDIException e) {
					targetRequestFailed(e.getMessage(), null);
				}
			}
		}
		return fValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.model.PVariable#isBookkeepingEnabled
	 * ()
	 */
	@Override
	protected boolean isBookkeepingEnabled() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.debug.core.model.PVariable#resetValue()
	 */
	@Override
	protected void resetValue() {
		if (fValue instanceof AbstractPValue) {
			((AbstractPValue) fValue).reset();
		}
		fValue = PValueFactory.NULL_VALUE;
	}
}
