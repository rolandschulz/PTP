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
import org.eclipse.fdt.debug.core.cdi.model.type.ICDIIntegralValue;
import org.eclipse.fdt.debug.mi.core.MIFormat;
import org.eclipse.fdt.debug.mi.core.cdi.model.Value;
import org.eclipse.fdt.debug.mi.core.cdi.model.Variable;

/**
 */
public abstract class IntegralValue extends Value implements ICDIIntegralValue {

	/**
	 * @param v
	 */
	public IntegralValue(Variable v) {
		super(v);
	}



	/* (non-Javadoc)
	 * @see org.eclipse.fdt.debug.core.cdi.model.ICDIIntegralValue#biIntegerValue()
	 */
	public BigInteger bigIntegerValue() throws CDIException {
		String valueString = getValueString();
		int space = valueString.indexOf(' ');
		if (space != -1) {
			valueString = valueString.substring(0, space).trim();
		}
		try {
			return MIFormat.getBigInteger(valueString);
		} catch (NumberFormatException e) {
		}
		return BigInteger.ZERO;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.debug.core.cdi.model.ICDIIntegralValue#longValue()
	 */
	public long longValue() throws CDIException {
		long value = 0;
		String valueString = getValueString();
		int space = valueString.indexOf(' ');
		if (space != -1) {
			valueString = valueString.substring(0, space).trim();
		}
		try {
			value = MIFormat.getBigInteger(valueString).longValue();
		} catch (NumberFormatException e) {
		}
		return value;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.debug.core.cdi.model.ICDIIntegralValue#longValue()
	 */
	public int intValue() throws CDIException {
		int value = 0;
		String valueString = getValueString();
		int space = valueString.indexOf(' ');
		if (space != -1) {
			valueString = valueString.substring(0, space).trim();
		}
		try {
			value = MIFormat.getBigInteger(valueString).intValue();
		} catch (NumberFormatException e) {
		}
		return value;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.debug.core.cdi.model.ICDIIntegralValue#shortValue()
	 */
	public short shortValue() throws CDIException {
		short value = 0;
		String valueString = getValueString();
		int space = valueString.indexOf(' ');
		if (space != -1) {
			valueString = valueString.substring(0, space).trim();
		}
		try {
			value = MIFormat.getBigInteger(valueString).shortValue();
		} catch (NumberFormatException e) {
		}
		return value;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.debug.core.cdi.model.ICDIIntegralValue#byteValue()
	 */
	public int byteValue() throws CDIException {
		byte value = 0;
		String valueString = getValueString();
		int space = valueString.indexOf(' ');
		if (space != -1) {
			valueString = valueString.substring(0, space).trim();
		}
		try {
			value = MIFormat.getBigInteger(valueString).byteValue();
		} catch (NumberFormatException e) {
		}
		return value;
	}

}
