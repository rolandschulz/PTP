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
package org.eclipse.ptp.debug.internal.core.breakpoints.old;

import java.util.Map;

import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Base class for different types of location breakponts.
 */
public abstract class AbstractLineBreakpoint extends CBreakpoint implements ICLineBreakpoint {

	/**
	 * Constructor for AbstractLineBreakpoint.
	 */
	public AbstractLineBreakpoint() {
		super();
	}

	/**
	 * Constructor for AbstractLineBreakpoint.
	 *
	 * @param resource
	 * @param markerType
	 * @param attributes
	 * @param add
	 * @throws CoreException
	 */
	public AbstractLineBreakpoint( IResource resource, String markerType, Map attributes, boolean add ) throws CoreException {
		super( resource, markerType, attributes, add );
	}

	/*(non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILineBreakpoint#getLineNumber()
	 */
	public int getLineNumber() throws CoreException {
		return ensureMarker().getAttribute( IMarker.LINE_NUMBER, -1 );
	}

	/*(non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILineBreakpoint#getCharStart()
	 */
	public int getCharStart() throws CoreException {
		return ensureMarker().getAttribute( IMarker.CHAR_START, -1 );
	}

	/*(non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILineBreakpoint#getCharEnd()
	 */
	public int getCharEnd() throws CoreException {
		return ensureMarker().getAttribute( IMarker.CHAR_END, -1 );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICLineBreakpoint#getAddress()
	 */
	public String getAddress() throws CoreException {
		return ensureMarker().getAttribute( ICLineBreakpoint.ADDRESS, "" ); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICLineBreakpoint#getFileName()
	 */
	public String getFileName() throws CoreException {
		String fileName = ensureMarker().getAttribute( ICBreakpoint.SOURCE_HANDLE, "" ); //$NON-NLS-1$
		IPath path = new Path( fileName );
		return ( path.isValidPath( fileName ) ) ? path.lastSegment() : null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICLineBreakpoint#getFunction()
	 */
	public String getFunction() throws CoreException {
		return ensureMarker().getAttribute( ICLineBreakpoint.FUNCTION, "" ); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICLineBreakpoint#setAddress(java.lang.String)
	 */
	public void setAddress( String address ) throws CoreException {
		setAttribute( ICLineBreakpoint.ADDRESS, address );
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICLineBreakpoint#setFunction(java.lang.String)
	 */
	public void setFunction( String function ) throws CoreException {
		setAttribute( ICLineBreakpoint.FUNCTION, function );
	}
}
