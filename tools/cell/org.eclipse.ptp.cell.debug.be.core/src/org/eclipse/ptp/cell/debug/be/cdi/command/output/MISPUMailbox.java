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
 * @author Ricardom M. Matinata
 * @since 1.3
 *
 */
public class MISPUMailbox extends MISPUTableTupleElement {
	
	public static final String MBOX = "mbox"; //$NON-NLS-1$
	
	public static final String IBOX = "ibox"; //$NON-NLS-1$
	
	public static final String WBOX = "wbox"; //$NON-NLS-1$

	public MISPUMailbox(String name, String value, String index) {
		super(name,value,index);
	}
	
	public MISPUMailbox (MIResult result, String index) {
		super(result);
		setIndex(index);
	}
	
	protected void parseResult(MIResult result) {
		String var;
		MIValue value = null;
		
		var = result.getVariable();
		value = result.getMIValue();
		if (var.equals(MBOX)) {
			// mbox
			setName(MBOX);
			if (value != null && value instanceof MIConst) {
				setValue(((MIConst)value).getCString());
			} else {
				setValue(EMPTY);
			}
		} else if (var.equals(IBOX)) {
			// ibox
			setName(IBOX);
			if (value != null && value instanceof MIConst) {
				setValue(((MIConst)value).getCString());
			} else {
				setValue(EMPTY);
			}
		} else if (var.equals(WBOX)) {
			// wbox
			setName(WBOX);
			if (value != null && value instanceof MIConst) {
				setValue(((MIConst)value).getCString());
			} else {
				setValue(EMPTY);
			}
		}

	}

}
