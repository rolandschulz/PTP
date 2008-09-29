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
public class MISPUEvent extends MISPUTupleElement {

	final public static String EVENT_MASK = "event_mask"; //$NON-NLS-1$
	
	final public static String EVENT_STATUS = "event_status"; //$NON-NLS-1$

	public MISPUEvent(String name, String value) {
		super(name,value);
	}
	
	public MISPUEvent(MIResult result) {
		super(result);
	}

	protected void parseResult(MIResult result) {
		
		String var;
		MIValue value = null;
		
		// event_mask
		var = result.getVariable();
		value = result.getMIValue();
		if (var.equals(EVENT_MASK)) {
			// event_mask
			setName(EVENT_MASK);
			if (value != null && value instanceof MIConst) {
				setValue(((MIConst)value).getCString());
			} else {
				setValue(EMPTY);
			}
		} else if (var.equals(EVENT_STATUS)) {
			// event_status
			setName(EVENT_STATUS);
			if (value != null && value instanceof MIConst) {
				setValue(((MIConst)value).getCString());
			} else {
				setValue(EMPTY);
			}
		}

	}

}
