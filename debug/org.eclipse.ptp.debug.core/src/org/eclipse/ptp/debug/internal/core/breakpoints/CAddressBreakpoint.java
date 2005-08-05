/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/
package org.eclipse.ptp.debug.internal.core.breakpoints;

import java.text.MessageFormat;
import java.util.Map;

import org.eclipse.cdt.debug.core.model.ICAddressBreakpoint;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * A breakpoint that suspend the execution when a particular address is reached.
 */
public class CAddressBreakpoint extends AbstractLineBreakpoint implements ICAddressBreakpoint {

	private static final String C_ADDRESS_BREAKPOINT = "org.eclipse.ptp.debug.core.cAddressBreakpointMarker"; //$NON-NLS-1$

	/**
	 * Constructor for CAddressBreakpoint.
	 */
	public CAddressBreakpoint() {
	}

	/**
	 * Constructor for CAddressBreakpoint.
	 */
	public CAddressBreakpoint( IResource resource, Map attributes, boolean add ) throws CoreException {
		super( resource, getMarkerType(), attributes, add );
	}

	/**
	 * Returns the type of marker associated with this type of breakpoints
	 */
	public static String getMarkerType() {
		return C_ADDRESS_BREAKPOINT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.internal.core.breakpoints.CBreakpoint#getMarkerMessage()
	 */
	protected String getMarkerMessage() throws CoreException {
		String fileName = ensureMarker().getResource().getName();
		if ( fileName != null && fileName.length() > 0 ) {
			fileName = ' ' + fileName + ' ';
		}
		return MessageFormat.format( BreakpointMessages.getString( "CAddressBreakpoint.0" ), new String[] { fileName, getAddress(), getConditionText() } ); //$NON-NLS-1$
	}
}