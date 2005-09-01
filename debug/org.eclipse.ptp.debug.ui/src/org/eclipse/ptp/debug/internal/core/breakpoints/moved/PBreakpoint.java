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
package org.eclipse.ptp.debug.internal.core.breakpoints.moved;

import java.text.MessageFormat;
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
import org.eclipse.ptp.debug.core.model.moved.IPBreakpoint;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;

/**
 * @author Clement chu
 *
 */
public abstract class PBreakpoint extends Breakpoint implements IPBreakpoint {
	//private Map filteredThreadsByTarget;

	public PBreakpoint() {
		//filteredThreadsByTarget = new HashMap(10);
	}
	
	public PBreakpoint(final IResource resource, final String markerType, final Map attributes, final boolean add) throws CoreException {
		this();
		IWorkspaceRunnable wr = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				// create the marker
				setMarker(resource.createMarker(markerType));
				// set attributes
				ensureMarker().setAttributes(attributes);
				//set the marker message
				setAttribute(IMarker.MESSAGE, getMarkerMessage());
				// add to breakpoint manager if requested
				register(add);
			}
		};
		run(wr);
	}

	public void createMarker(final IResource resource, final String markerType, final Map attributes, final boolean add) throws DebugException {
		IWorkspaceRunnable wr = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				// create the marker
				setMarker(resource.createMarker(markerType));
				// set attributes
				ensureMarker().setAttributes( attributes );
				//set the marker message
				setAttribute(IMarker.MESSAGE, getMarkerMessage());
				// add to breakpoint manager if requested
				register(add);
			}
		};
		run(wr);
	}
	
	public String getModelIdentifier() {
		return PTPDebugUIPlugin.getUniqueIdentifier();
	}
	
	public boolean isInstalled() throws CoreException {
		return ensureMarker().getAttribute(INSTALL_COUNT, 0) > 0;
	}
	public String getCondition() throws CoreException {
		return ensureMarker().getAttribute(CONDITION, "");
	}
	public void setCondition( String condition ) throws CoreException {
		setAttribute(CONDITION, condition);
		setAttribute(IMarker.MESSAGE, getMarkerMessage());
	}
	public int getIgnoreCount() throws CoreException {
		return ensureMarker().getAttribute(IGNORE_COUNT, 0);
	}
	public void setIgnoreCount(int ignoreCount) throws CoreException {
		setAttribute(IGNORE_COUNT, ignoreCount);
		setAttribute(IMarker.MESSAGE, getMarkerMessage());
	}
	public String getThreadId() throws CoreException {
		return ensureMarker().getAttribute(THREAD_ID, null);
	}
	public void setThreadId(String threadId) throws CoreException {
		setAttribute(THREAD_ID, threadId);
	}
	public String getSourceHandle() throws CoreException {
		return ensureMarker().getAttribute(SOURCE_HANDLE, "");
	}
	public void setSourceHandle(String sourceHandle) throws CoreException {
		setAttribute(SOURCE_HANDLE, sourceHandle);
	}
	public String getModule() throws CoreException {
		return ensureMarker().getAttribute(MODULE, null);
	}
	public void setModule(String module) throws CoreException {
		setAttribute(MODULE, module);
	}
	public String getSetId() throws CoreException {
		return ensureMarker().getAttribute(SET_ID, "");
	}
	public void setSetId(String id) throws CoreException {
		setAttribute(SET_ID, id);
	}
	public String getJobId() throws CoreException {
		return ensureMarker().getAttribute(JOB_ID, "");
	}
	public void setJobId(String id) throws CoreException {
		setAttribute(JOB_ID, id);
	}
	public String getCurSetId() throws CoreException {
		return ensureMarker().getAttribute(CUR_SET_ID, "");
	}
	public void setCurSetId(String id) throws CoreException {
		setAttribute(CUR_SET_ID, id);
	}

	protected void run(IWorkspaceRunnable wr) throws DebugException {
		try {
			ResourcesPlugin.getWorkspace().run(wr, null);
		}
		catch( CoreException e ) {
			throw new DebugException(e.getStatus());
		}
	}
	public void register(boolean register) throws CoreException {
		if (register) {
			DebugPlugin.getDefault().getBreakpointManager().addBreakpoint(this);
		}
	}
	
	protected abstract String getMarkerMessage() throws CoreException;
	
	public synchronized void resetInstallCount() throws CoreException {
		setAttribute(INSTALL_COUNT, 0);
	}
	public synchronized int incrementInstallCount() throws CoreException {
		int count = getInstallCount();
		setAttribute(INSTALL_COUNT, ++count);
		return count;
	}
	public int getInstallCount() throws CoreException {
		return ensureMarker().getAttribute(INSTALL_COUNT, 0);
	}
	public synchronized int decrementInstallCount() throws CoreException {
		int count = getInstallCount();
		if (count > 0) {
			setAttribute(INSTALL_COUNT, --count);
		}
		return count;
	}
	public boolean isConditional() throws CoreException {
		return ((getCondition() != null && getCondition().trim().length() > 0) || getIgnoreCount() > 0);
	}


	/*
	public IPDebugTarget[] getTargetFilters() throws CoreException {
		Set set = filteredThreadsByTarget.keySet();
		return (IPDebugTarget[])set.toArray(new IPDebugTarget[set.size()]);
	}
	public IPThread[] getThreadFilters(IPDebugTarget target) throws CoreException {
		Set set = (Set)filteredThreadsByTarget.get( target );
		return (set != null) ? (IPThread[])set.toArray new IPThread[set.size()]) : null;
	}
	public void removeTargetFilter(IPDebugTarget target) throws CoreException {
		if (filteredThreadsByTarget.containsKey(target)) {
			filteredThreadsByTarget.remove(target);
		}
	}
	public void removeThreadFilters(IPThread[] threads) throws CoreException {
		if (threads != null && threads.length > 0) {
			IDebugTarget target = threads[0].getDebugTarget();
			if (filteredThreadsByTarget.containsKey(target)) {
				Set set = (Set)filteredThreadsByTarget.get(target);
				if (set != null) {
					set.removeAll(Arrays.asList(threads));
					if (set.isEmpty()) {
						filteredThreadsByTarget.remove(target);
					}
				}
			}
		}
	}
	public void setTargetFilter(IPDebugTarget target) throws CoreException {
		filteredThreadsByTarget.put(target, null);
	}
	public void setThreadFilters(IPThread[] threads) throws CoreException {
		if (threads != null && threads.length > 0) {
			filteredThreadsByTarget.put(threads[0].getDebugTarget(), new HashSet(Arrays.asList(threads)));
		}
	}
	*/
	
	public void fireChanged() {
		if (markerExists()) {
			DebugPlugin.getDefault().getBreakpointManager().fireBreakpointChanged(this);
		}
	}
	
	protected String getConditionText() throws CoreException {
		StringBuffer sb = new StringBuffer();
		int ignoreCount = getIgnoreCount();
		if ( ignoreCount > 0 ) {
			sb.append(MessageFormat.format(BreakpointMessages.getString("PBreakpoint.1"), new Integer[] { new Integer(ignoreCount) }));
		}
		String condition = getCondition();
		if ( condition != null && condition.length() > 0 ) {
			sb.append( MessageFormat.format(BreakpointMessages.getString("PBreakpoint.2"), new String[] { condition }));
		}
		return sb.toString();
	}
}
