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
package org.eclipse.cldt.debug.internal.core.breakpoints;

import java.text.MessageFormat;
import java.util.Map;

import org.eclipse.cldt.debug.core.model.ICFunctionBreakpoint;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * A breakpoint that suspends the execution when a function is entered.
 */
public class CFunctionBreakpoint extends AbstractLineBreakpoint implements ICFunctionBreakpoint {

	private static final String C_FUNCTION_BREAKPOINT = "org.eclipse.cldt.debug.core.cFunctionBreakpointMarker"; //$NON-NLS-1$

	/**
	 * Constructor for CFunctionBreakpoint.
	 */
	public CFunctionBreakpoint() {
	}

	/**
	 * Constructor for CFunctionBreakpoint.
	 */
	public CFunctionBreakpoint( IResource resource, Map attributes, boolean add ) throws CoreException {
		super( resource, getMarkerType(), attributes, add );
	}

	/**
	 * Returns the type of marker associated with this type of breakpoints
	 */
	public static String getMarkerType() {
		return C_FUNCTION_BREAKPOINT;
	}

	/*(non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.breakpoints.CBreakpoint#getMarkerMessage()
	 */
	protected String getMarkerMessage() throws CoreException {
		StringBuffer sb = new StringBuffer( BreakpointMessages.getString( "CFunctionBreakpoint.2" ) ); //$NON-NLS-1$
		String name = ensureMarker().getResource().getName();
		if ( name != null && name.length() > 0 ) {
			sb.append( ' ' );
			sb.append( name );
		}
		String function = getFunction();
		if ( function != null && function.trim().length() > 0 ) {
			sb.append( MessageFormat.format( BreakpointMessages.getString( "CFunctionBreakpoint.3" ), new String[] { function.trim() } ) ); //$NON-NLS-1$
		}
		sb.append( getConditionText() );
		return sb.toString();
	}
}
