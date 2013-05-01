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
package org.eclipse.ptp.internal.debug.core.breakpoint;

import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.debug.core.model.IPWatchpoint;
import org.eclipse.ptp.internal.debug.core.messages.Messages;

/**
 * @author clement
 * 
 */
public class PWatchpoint extends PBreakpoint implements IPWatchpoint {
	private static final String P_WATCHPOINT = "org.eclipse.ptp.debug.core.pWatchpointMarker"; //$NON-NLS-1$

	/**
	 * @return
	 */
	public static String getMarkerType() {
		return P_WATCHPOINT;
	}

	public PWatchpoint() {
	}

	public PWatchpoint(IResource resource, Map<String, ? extends Object> attributes, final String jobId, final String setId,
			boolean add) throws CoreException {
		super(resource, getMarkerType(), attributes, jobId, setId, add);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.ILineBreakpoint#getCharEnd()
	 */
	public int getCharEnd() throws CoreException {
		return ensureMarker().getAttribute(IMarker.CHAR_END, -1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.ILineBreakpoint#getCharStart()
	 */
	public int getCharStart() throws CoreException {
		return ensureMarker().getAttribute(IMarker.CHAR_START, -1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IPWatchpoint#getExpression()
	 */
	public String getExpression() throws CoreException {
		return ensureMarker().getAttribute(EXPRESSION, ""); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.ILineBreakpoint#getLineNumber()
	 */
	public int getLineNumber() throws CoreException {
		return ensureMarker().getAttribute(IMarker.LINE_NUMBER, -1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IPWatchpoint#isReadType()
	 */
	public boolean isReadType() throws CoreException {
		return ensureMarker().getAttribute(READ, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IPWatchpoint#isWriteType()
	 */
	public boolean isWriteType() throws CoreException {
		return ensureMarker().getAttribute(WRITE, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.breakpoint.PBreakpoint#getMarkerMessage
	 * ()
	 */
	@Override
	protected String getMarkerMessage() throws CoreException {
		String format = Messages.PWatchpoint_0;
		if (isWriteType() && !isReadType()) {
			format = Messages.PWatchpoint_1;
		} else if (!isWriteType() && isReadType()) {
			format = Messages.PWatchpoint_2;
		} else if (isWriteType() && isReadType()) {
			format = Messages.PWatchpoint_3;
		}
		return getJobSetFormat() + " " + NLS.bind(Messages.PWatchpoint_4, new Object[] { format }); //$NON-NLS-1$
	}
}
