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
package org.eclipse.fdt.debug.internal.core.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.fdt.debug.core.cdi.model.ICDIValue;

/**
 *
 * Enter type comment.
 * 
 * @since: Oct 2, 2002
 */
public class CGlobalValue extends CValue
{
	private Boolean fHasChildren = null;


	/**
	 * Constructor for CGlobalValue.
	 * @param parent
	 * @param cdiValue
	 */
	public CGlobalValue( CVariable parent, ICDIValue cdiValue )
	{
		super( parent, cdiValue );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#hasVariables()
	 */
	public boolean hasVariables() throws DebugException
	{
		if ( fHasChildren == null )
		{
			fHasChildren = new Boolean( super.hasVariables() );
		}
		return fHasChildren.booleanValue();
	}
}
