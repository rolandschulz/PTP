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
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.internal.core.model; 

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIDoubleValue;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIFloatValue;
 
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
