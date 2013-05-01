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
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.Breakpoint;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.debug.core.model.IPBreakpoint;
import org.eclipse.ptp.internal.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.internal.debug.core.messages.Messages;

/**
 * @author Clement chu
 * 
 */
public abstract class PBreakpoint extends Breakpoint implements IPBreakpoint {
	/*
	 * These can't be stored as marker attributes as they are needed after the
	 * marker may have been deleted.
	 */
	private String fJobId = ""; //$NON-NLS-1$
	private String fSetId = ""; //$NON-NLS-1$

	public PBreakpoint() {
	}

	public PBreakpoint(final IResource resource, final String markerType, final Map<String, ? extends Object> attributes,
			final String jobId, final String setId, final boolean add) throws CoreException {
		this();
		fJobId = jobId;
		fSetId = setId;
		final IWorkspaceRunnable wr = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				// create the marker
				setMarker(resource.createMarker(markerType));
				// set attributes
				ensureMarker().setAttributes(attributes);
				// set the marker message
				setAttribute(IMarker.MESSAGE, getMarkerMessage());
				// add to breakpoint manager if requested
				register(add);
			}
		};
		try {
			ResourcesPlugin.getWorkspace().run(wr, null);
		} catch (final CoreException e) {
			throw new DebugException(e.getStatus());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.model.IPBreakpoint#decrementInstallCount()
	 */
	public synchronized int decrementInstallCount() throws CoreException {
		int count = getInstallCount();
		if (count > 0) {
			setAttribute(INSTALL_COUNT, --count);
		}
		return count;
	}

	public void fireChanged() {
		if (markerExists()) {
			DebugPlugin.getDefault().getBreakpointManager().fireBreakpointChanged(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IPBreakpoint#getCondition()
	 */
	public String getCondition() throws CoreException {
		return ensureMarker().getAttribute(CONDITION, ""); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IPBreakpoint#getCurSetId()
	 */
	public String getCurSetId() throws CoreException {
		return ensureMarker().getAttribute(CUR_SET_ID, ""); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IPBreakpoint#getIgnoreCount()
	 */
	public int getIgnoreCount() throws CoreException {
		return ensureMarker().getAttribute(IGNORE_COUNT, 0);
	}

	/**
	 * @return
	 * @throws CoreException
	 */
	public int getInstallCount() throws CoreException {
		return ensureMarker().getAttribute(INSTALL_COUNT, 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IPBreakpoint#getJobId()
	 */
	public String getJobId() throws CoreException {
		return fJobId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IPBreakpoint#getJobName()
	 */
	public String getJobName() throws CoreException {
		return ensureMarker().getAttribute(JOB_NAME, ""); //$NON-NLS-1$
	}

	/**
	 * @return
	 * @throws CoreException
	 */
	public String getJobSetFormat() throws CoreException {
		return "{" + getJobName() + ":" + getSetId() + "}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IBreakpoint#getModelIdentifier()
	 */
	public String getModelIdentifier() {
		return PTPDebugCorePlugin.getUniqueIdentifier();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IPBreakpoint#getSetId()
	 */
	public String getSetId() throws CoreException {
		return fSetId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IPBreakpoint#getSourceHandle()
	 */
	public String getSourceHandle() throws CoreException {
		return ensureMarker().getAttribute(SOURCE_HANDLE, ""); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.model.IPBreakpoint#incrementInstallCount()
	 */
	public synchronized int incrementInstallCount() throws CoreException {
		int count = getInstallCount();
		setAttribute(INSTALL_COUNT, ++count);
		return count;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IPBreakpoint#isConditional()
	 */
	public boolean isConditional() throws CoreException {
		return ((getCondition() != null && getCondition().trim().length() > 0) || getIgnoreCount() > 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IPBreakpoint#isGlobal()
	 */
	public boolean isGlobal() throws CoreException {
		return (getJobId().equals(IPBreakpoint.GLOBAL));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IPBreakpoint#isInstalled()
	 */
	public boolean isInstalled() throws CoreException {
		return ensureMarker().getAttribute(INSTALL_COUNT, 0) > 0;
	}

	/**
	 * @param register
	 * @throws CoreException
	 */
	public void register(boolean register) throws CoreException {
		if (register) {
			DebugPlugin.getDefault().getBreakpointManager().addBreakpoint(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IPBreakpoint#resetInstallCount()
	 */
	public synchronized void resetInstallCount() throws CoreException {
		setAttribute(INSTALL_COUNT, 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.model.IPBreakpoint#setCondition(java.lang.
	 * String)
	 */
	public void setCondition(String condition) throws CoreException {
		setAttribute(CONDITION, condition);
		setAttribute(IMarker.MESSAGE, getMarkerMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.model.IPBreakpoint#setCurSetId(java.lang.String
	 * )
	 */
	public void setCurSetId(String id) throws CoreException {
		setAttribute(CUR_SET_ID, id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IPBreakpoint#setIgnoreCount(int)
	 */
	public void setIgnoreCount(int ignoreCount) throws CoreException {
		setAttribute(IGNORE_COUNT, ignoreCount);
		setAttribute(IMarker.MESSAGE, getMarkerMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.model.IPBreakpoint#setJobId(java.lang.String)
	 */
	public void setJobId(String id) throws CoreException {
		fJobId = id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.model.IPBreakpoint#setJobName(java.lang.String
	 * )
	 */
	public void setJobName(String name) throws CoreException {
		setAttribute(JOB_NAME, name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.model.IPBreakpoint#setSetId(java.lang.String)
	 */
	public void setSetId(String id) throws CoreException {
		fSetId = id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.model.IPBreakpoint#setSourceHandle(java.lang
	 * .String)
	 */
	public void setSourceHandle(String sourceHandle) throws CoreException {
		setAttribute(SOURCE_HANDLE, sourceHandle);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IPBreakpoint#updateMarkerMessage()
	 */
	public void updateMarkerMessage() throws CoreException {
		setAttribute(IMarker.MESSAGE, getMarkerMessage());
	}

	/**
	 * @return
	 * @throws CoreException
	 */
	protected String getConditionText() throws CoreException {
		final StringBuffer sb = new StringBuffer();
		final int ignoreCount = getIgnoreCount();
		if (ignoreCount > 0) {
			sb.append(NLS.bind(Messages.PBreakpoint_0, new Object[] { new Integer(ignoreCount) }));
		}
		final String condition = getCondition();
		if (condition != null && condition.length() > 0) {
			sb.append(NLS.bind(Messages.PBreakpoint_10, new Object[] { condition }));
		}
		return sb.toString();
	}

	/**
	 * @return
	 * @throws CoreException
	 */
	protected abstract String getMarkerMessage() throws CoreException;
}
