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

import java.text.MessageFormat;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.debug.core.model.IPWatchpoint;

/**
 * @author clement
 *
 */
public class PWatchpoint extends PBreakpoint implements IPWatchpoint {
	private static final String P_WATCHPOINT = "org.eclipse.ptp.debug.core.pWatchpointMarker";

	public PWatchpoint() {
	}
	public PWatchpoint(IResource resource, Map<?,?> attributes, boolean add) throws CoreException {
		super(resource, getMarkerType(), attributes, add);
	}
	public boolean isWriteType() throws CoreException {
		return ensureMarker().getAttribute(WRITE, true);
	}
	public boolean isReadType() throws CoreException {
		return ensureMarker().getAttribute(READ, false);
	}
	public String getExpression() throws CoreException {
		return ensureMarker().getAttribute(EXPRESSION, "");
	}
	public static String getMarkerType() {
		return P_WATCHPOINT;
	}
	protected String getMarkerMessage() throws CoreException {
		String format = BreakpointMessages.getString("PWatchpoint.3");
		if (isWriteType() && !isReadType())
			format = BreakpointMessages.getString("PWatchpoint.0");
		else if (!isWriteType() && isReadType())
			format = BreakpointMessages.getString("PWatchpoint.1");
		else if (isWriteType() && isReadType())
			format = BreakpointMessages.getString("PWatchpoint.2");
		return getJobSetFormat() + " " + MessageFormat.format(BreakpointMessages.getString("PFunctinBreakpoint"), new Object[] { format });
	}
	public int getLineNumber() throws CoreException {
		return ensureMarker().getAttribute(IMarker.LINE_NUMBER, -1);
	}
	public int getCharStart() throws CoreException {
		return ensureMarker().getAttribute(IMarker.CHAR_START, -1);
	}
	public int getCharEnd() throws CoreException {
		return ensureMarker().getAttribute(IMarker.CHAR_END, -1);
	}
}
