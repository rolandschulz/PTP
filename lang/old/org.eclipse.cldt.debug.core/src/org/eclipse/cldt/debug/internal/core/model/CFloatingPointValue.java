/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/ 
package org.eclipse.cldt.debug.internal.core.model; 

import org.eclipse.cldt.debug.core.cdi.CDIException;
import org.eclipse.cldt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cldt.debug.core.cdi.model.type.ICDIDoubleValue;
import org.eclipse.cldt.debug.core.cdi.model.type.ICDIFloatValue;
 
/**
 * Represents a value of a float or double variable type.
 */
public class CFloatingPointValue extends CValue {

	private Number fFloatingPointValue;
	
	/** 
	 * Constructor for CFloatingPointValue. 
	 */
	public CFloatingPointValue( CVariable parent, ICDIValue cdiValue ) {
		super( parent, cdiValue );
	}

	public Number getFloatingPointValue() throws CDIException {
		if ( fFloatingPointValue == null ) {
			ICDIValue cdiValue = getUnderlyingValue();
			if ( cdiValue instanceof ICDIDoubleValue ) {
				fFloatingPointValue = new Double( ((ICDIDoubleValue)cdiValue).doubleValue() );
			}
			else if ( cdiValue instanceof ICDIFloatValue ) {
				fFloatingPointValue = new Float( ((ICDIFloatValue)cdiValue).floatValue() );
			}
		}
		return fFloatingPointValue;
	}
}
