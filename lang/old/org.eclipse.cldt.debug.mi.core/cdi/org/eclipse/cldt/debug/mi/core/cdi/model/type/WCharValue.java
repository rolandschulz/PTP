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

package org.eclipse.cldt.debug.mi.core.cdi.model.type;

import org.eclipse.cldt.debug.core.cdi.CDIException;
import org.eclipse.cldt.debug.core.cdi.model.type.ICDIWCharValue;
import org.eclipse.cldt.debug.mi.core.cdi.model.Variable;

/**
 */
public class WCharValue extends IntegralValue implements ICDIWCharValue {

	/**
	 * @param v
	 */
	public WCharValue(Variable v) {
		super(v);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDICharValue#getValue()
	 */
	public char getValue() throws CDIException {
		// TODO Auto-generated method stub
		return 0;
	}

}
