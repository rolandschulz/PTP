/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.fdt.debug.mi.core.cdi.model.type;

import java.math.BigInteger;

import org.eclipse.fdt.debug.core.cdi.CDIException;
import org.eclipse.fdt.debug.core.cdi.model.type.ICDIPointerValue;
import org.eclipse.fdt.debug.mi.core.MIFormat;
import org.eclipse.fdt.debug.mi.core.cdi.model.Variable;

/**
 * Enter type comment.
 * 
 * @since Jun 3, 2003
 */
public class PointerValue extends DerivedValue implements ICDIPointerValue {

	public PointerValue(Variable v) {
		super(v);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.debug.core.cdi.model.type.ICDIPointerValue#pointerValue()
	 */
	public BigInteger pointerValue() throws CDIException {
		String valueString = getValueString().trim();
		int space = valueString.indexOf(' ');
		if (space != -1) {
			valueString = valueString.substring(0, space).trim();
		}
		try {
			return MIFormat.getBigInteger(valueString);
		} catch(Exception e) {
			return null;
		}
	}
}
