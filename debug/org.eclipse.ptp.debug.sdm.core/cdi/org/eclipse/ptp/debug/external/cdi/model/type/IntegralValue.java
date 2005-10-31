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
import org.eclipse.ptp.debug.external.ExtFormat;
import org.eclipse.ptp.debug.external.cdi.model.variable.Variable;

public abstract class IntegralValue extends Value implements ICDIIntegralValue {
	public IntegralValue(Variable v) {
		super(v);
	}
	public BigInteger bigIntegerValue() throws CDIException {
		return bigIntegerValue(getValueString());
	}
	public static BigInteger bigIntegerValue(String valueString) {
		// Coming from a reference
		if (valueString.startsWith("@")) {
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
			return ExtFormat.getBigInteger(valueString);
		} catch (NumberFormatException e) {
		}
		return BigInteger.ZERO;
	}
	public long longValue() throws CDIException {
		return bigIntegerValue().longValue();
	}
	public int intValue() throws CDIException {
		return bigIntegerValue().intValue();
	}
	public short shortValue() throws CDIException {
		return bigIntegerValue().shortValue();
	}
	public int byteValue() throws CDIException {
		return bigIntegerValue().byteValue();
	}
}
