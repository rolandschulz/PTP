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
package org.eclipse.ptp.debug.internal.core.breakpoint;

import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.debug.core.messages.Messages;

/**
 * @author Clement chu
 * 
 */
public class PLineBreakpoint extends AbstractLineBreakpoint {
	private static final String P_LINE_BREAKPOINT = "org.eclipse.ptp.debug.core.pLineBreakpointMarker"; //$NON-NLS-1$

	/**
	 * @return
	 */
	public static String getMarkerType() {
		return P_LINE_BREAKPOINT;
	}

	public PLineBreakpoint() {
	}

	public PLineBreakpoint(IResource resource, Map<?, ?> attributes, boolean add) throws CoreException {
		super(resource, getMarkerType(), attributes, add);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.internal.core.breakpoint.PBreakpoint#getMarkerMessage()
	 */
	@Override
	protected String getMarkerMessage() throws CoreException {
		String fileName = ensureMarker().getResource().getName();
		if (fileName != null && fileName.length() > 0) {
			fileName = ' ' + fileName + ' ';
		}
		return getJobSetFormat()
				+ " " //$NON-NLS-1$
				+ NLS.bind(Messages.PLineBreakpoint_0, new Object[] { fileName, new Integer(getLineNumber()),
						getConditionText() });
	}
}
