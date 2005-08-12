/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.debug.external.cdi.model.type;

import java.math.BigInteger;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIIntegralValue;
import org.eclipse.ptp.debug.external.cdi.model.Value;
import org.eclipse.ptp.debug.external.cdi.model.Variable;

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
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIIntegralValue#biIntegerValue()
	 */
	public BigInteger bigIntegerValue() throws CDIException {
		return bigIntegerValue(getValueString());
	}

	public static BigInteger bigIntegerValue(String valueString) {
		// Coming from a reference
		if (valueString.startsWith("@")) { //$NON-NLS-1$
			valueString = valueString.substring(1);
			int colon = valueString.indexOf(':');
			if (colon != -1) {
				valueString = valueString.substring(colon + 1); 
			}
		} else {
			int space = valueString.indexOf(' ');
			if (space != -1) {
				valueString = valueString.substring(0, space).trim();
			}			
		}

		try {
			int index = 0;
			int radix = 10;
			boolean negative = false;

			// Handle zero length
			valueString = valueString.trim();
			if (valueString.length() == 0) {
				return BigInteger.ZERO;
			}

			// Handle minus sign, if present
			if (valueString.startsWith("-")) { //$NON-NLS-1$
				negative = true;
				index++;
			}
			if (valueString.startsWith("0x", index) || valueString.startsWith("0X", index)) { //$NON-NLS-1$ //$NON-NLS-2$
				index += 2;
				radix = 16;
			} else if (valueString.startsWith("#", index)) { //$NON-NLS-1$
				index ++;
				radix = 16;
			} else if (valueString.startsWith("0", index) && valueString.length() > 1 + index) { //$NON-NLS-1$
				index ++;
				radix = 8;
			}

			if (index > 0) {
				valueString = valueString.substring(index);
			}
			if (negative) {
				valueString = "-" + valueString; //$NON-NLS-1$
			}
			try {
				return new BigInteger(valueString, radix);
			} catch (NumberFormatException e) {
				// ...
				// What can we do ???
			}
			return BigInteger.ZERO;
		} catch (NumberFormatException e) {
		}
		return BigInteger.ZERO;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIIntegralValue#longValue()
	 */
	public long longValue() throws CDIException {
		return bigIntegerValue().longValue();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIIntegralValue#longValue()
	 */
	public int intValue() throws CDIException {
		return bigIntegerValue().intValue();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIIntegralValue#shortValue()
	 */
	public short shortValue() throws CDIException {
		return bigIntegerValue().shortValue();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIIntegralValue#byteValue()
	 */
	public int byteValue() throws CDIException {
		return bigIntegerValue().byteValue();
	}

}
