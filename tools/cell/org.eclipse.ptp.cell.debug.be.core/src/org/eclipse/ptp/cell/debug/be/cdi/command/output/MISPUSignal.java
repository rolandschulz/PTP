/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.cell.debug.be.cdi.command.output;

import org.eclipse.cdt.debug.mi.core.output.MIConst;
import org.eclipse.cdt.debug.mi.core.output.MIResult;
import org.eclipse.cdt.debug.mi.core.output.MIValue;

/**
 * 
 * @author Ricardo M. Matinata
 * @since 1.3
 *
 */
public class MISPUSignal extends MISPUTupleElement {

	public static final String SIGNAL1_PENDING = "signal1_pending"; //$NON-NLS-1$
	
	public static final String SIGNAL1 = "signal1"; //$NON-NLS-1$
	
	public static final String SIGNAL1_TYPE = "signal1_type"; //$NON-NLS-1$
	
	public static final String SIGNAL2_PENDING = "signal2_pending"; //$NON-NLS-1$
	
	public static final String SIGNAL2 = "signal2"; //$NON-NLS-1$
	
	public static final String SIGNAL2_TYPE = "signal2_type"; //$NON-NLS-1$

	public MISPUSignal(String name, String value) {
		super(name,value);
	}
	
	public MISPUSignal(MIResult result) {
		super(result);
	}

	protected void parseResult(MIResult result) {
		
		String var;
		MIValue value = null;
		
		var = result.getVariable();
		value = result.getMIValue();
		if (var.equals(SIGNAL1_PENDING)) {
			// event_mask
			setName(SIGNAL1_PENDING);
			if (value != null && value instanceof MIConst) {
				setValue(((MIConst)value).getCString());
			} else {
				setValue(EMPTY);
			}
		} else if (var.equals(SIGNAL1)) {
			// event_status
			setName(SIGNAL1);
			if (value != null && value instanceof MIConst) {
				setValue(((MIConst)value).getCString());
			} else {
				setValue(EMPTY);
			}
		} else if (var.equals(SIGNAL1_TYPE)) {
			// event_status
			setName(SIGNAL1_TYPE);
			if (value != null && value instanceof MIConst) {
				setValue(((MIConst)value).getCString());
			} else {
				setValue(EMPTY);
			}
		} else if (var.equals(SIGNAL2_PENDING)) {
			// event_mask
			setName(SIGNAL2_PENDING);
			if (value != null && value instanceof MIConst) {
				setValue(((MIConst)value).getCString());
			} else {
				setValue(EMPTY);
			}
		} else if (var.equals(SIGNAL2)) {
			// event_status
			setName(SIGNAL2);
			if (value != null && value instanceof MIConst) {
				setValue(((MIConst)value).getCString());
			} else {
				setValue(EMPTY);
			}
		} else if (var.equals(SIGNAL2_TYPE)) {
			// event_status
			setName(SIGNAL2_TYPE);
			if (value != null && value instanceof MIConst) {
				setValue(((MIConst)value).getCString());
			} else {
				setValue(EMPTY);
			}
		}

	}

}
