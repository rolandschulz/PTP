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
package org.eclipse.ptp.internal.debug.core;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManagerListener;
import org.eclipse.debug.core.IBreakpointsListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;
import org.eclipse.ptp.core.PreferenceConstants;
import org.eclipse.ptp.debug.core.IPBreakpointManager;
import org.eclipse.ptp.debug.core.IPSession;
import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.event.IPDebugEvent;
import org.eclipse.ptp.debug.core.event.IPDebugInfo;
import org.eclipse.ptp.debug.core.model.IPAddressBreakpoint;
import org.eclipse.ptp.debug.core.model.IPBreakpoint;
import org.eclipse.ptp.debug.core.model.IPFunctionBreakpoint;
import org.eclipse.ptp.debug.core.model.IPLineBreakpoint;
import org.eclipse.ptp.debug.core.model.IPWatchpoint;
import org.eclipse.ptp.debug.core.pdi.IPDIAddressLocation;
import org.eclipse.ptp.debug.core.pdi.IPDICondition;
import org.eclipse.ptp.debug.core.pdi.IPDIFunctionLocation;
import org.eclipse.ptp.debug.core.pdi.IPDILineLocation;
import org.eclipse.ptp.debug.core.pdi.IPDILocator;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.IPDISessionObject;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.event.IPDIBreakpointInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDIChangedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDICreatedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIDestroyedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEventListener;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIBreakpointManager;
import org.eclipse.ptp.debug.core.pdi.model.IPDIAddressBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDIFunctionBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDILineBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDILocationBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDIWatchpoint;
import org.eclipse.ptp.internal.debug.core.event.PDebugBreakpointInfo;
import org.eclipse.ptp.internal.debug.core.messages.Messages;
import org.eclipse.ptp.internal.debug.core.sourcelookup.IPSourceLocator;
import org.eclipse.ptp.internal.debug.core.sourcelookup.PSourceLookupDirector;

public class PBreakpointManager implements IBreakpointsListener, IBreakpointManagerListener, IPDIEventListener, IAdaptable,
		IPBreakpointManager {
	private class BreakpointMap {
		private final Map<String, IPBreakpoint> fPBreakpoints;
		private final Map<String, IPDIBreakpoint> fPDIBreakpoints;

		protected BreakpointMap() {
			fPBreakpoints = new HashMap<String, IPBreakpoint>(10);
			fPDIBreakpoints = new HashMap<String, IPDIBreakpoint>(10);
		}

		/**
		 * 
		 */
		public void dispose() {
			fPBreakpoints.clear();
			fPDIBreakpoints.clear();
		}

		/**
		 * @return
		 */
		public IPBreakpoint[] getAllPBreakpoints() {
			Collection<IPBreakpoint> collection = fPBreakpoints.values();
			return collection.toArray(new IPBreakpoint[collection.size()]);
		}

		/**
		 * @param pdiBpt
		 * @return
		 */
		public IPBreakpoint getPBreakpoint(IPDIBreakpoint pdiBpt) {
			IPBreakpoint breakpoint = fPBreakpoints.get(String.valueOf(pdiBpt.getInternalID()));
			if (breakpoint == null) {
				IPBreakpoint[] bpts = getAllPBreakpoints();
				for (IPBreakpoint bpt : bpts) {
					if (isSameBreakpoint(bpt, pdiBpt)) {
						fPBreakpoints.put(String.valueOf(pdiBpt.getInternalID()), bpt);
						breakpoint = bpt;
						break;
					}
				}
			}
			return breakpoint;
		}

		/**
		 * @param pBpt
		 * @return
		 */
		public IPDIBreakpoint getPDIBreakpoint(IPBreakpoint pBpt) {
			return fPDIBreakpoints.get(String.valueOf(pBpt.getMarker().getId()));
		}

		/**
		 * @param pBpt
		 * @param pdiBpt
		 */
		public void put(IPBreakpoint pBpt, IPDIBreakpoint pdiBpt) {
			fPBreakpoints.put(String.valueOf(pdiBpt.getInternalID()), pBpt);
			fPDIBreakpoints.put(String.valueOf(pBpt.getMarker().getId()), pdiBpt);
		}

		/**
		 * @param pdiBpt
		 */
		public void removePDIBreakpoint(IPDIBreakpoint pdiBpt) {
			if (pdiBpt != null) {
				IPBreakpoint pBpt = fPBreakpoints.remove(String.valueOf(pdiBpt.getInternalID()));
				if (pBpt != null)
					fPDIBreakpoints.remove(String.valueOf(pBpt.getMarker().getId()));
			}
		}

		/**
		 * @param breakpoint
		 * @param pdiBreakpoint
		 * @return
		 */
		private boolean isSameBreakpoint(IPBreakpoint breakpoint, IPDIBreakpoint pdiBreakpoint) {
			try {
				if (breakpoint instanceof IPFunctionBreakpoint && pdiBreakpoint instanceof IPDIFunctionBreakpoint) {
					return (((IPFunctionBreakpoint) breakpoint).getFunction().compareTo(
							((IPDIFunctionBreakpoint) pdiBreakpoint).getLocator().getFunction()) == 0);
				}
				if (breakpoint instanceof IPAddressBreakpoint && pdiBreakpoint instanceof IPDIAddressBreakpoint) {
					return (((IPAddressBreakpoint) breakpoint).getAddress()).equals((((IPDIAddressBreakpoint) pdiBreakpoint)
							.getLocator().getAddress()));
				}
				if (breakpoint instanceof IPLineBreakpoint && pdiBreakpoint instanceof IPDILineBreakpoint) {
					IPDILocator location = ((IPDILineBreakpoint) pdiBreakpoint).getLocator();
					String file = location.getFile();
					String sourceHandle = file;
					if (!isEmpty(file)) {
						Object sourceElement = getSourceElement(file);
						if (sourceElement instanceof IFile) {
							sourceHandle = ((IFile) sourceElement).getLocationURI().getPath();
						} else if (sourceElement instanceof IStorage) {
							sourceHandle = ((IStorage) sourceElement).getFullPath().toOSString();
						}
						String bpSourceHandle = ((IPLineBreakpoint) breakpoint).getSourceHandle();
						if (sourceElement instanceof LocalFileStorage) {
							try {
								bpSourceHandle = new File(bpSourceHandle).getCanonicalPath();
							} catch (IOException e) {
								return false;
							}
						}
						return sourceHandle.equals(bpSourceHandle)
								&& location.getLineNumber() == ((IPLineBreakpoint) breakpoint).getLineNumber();
					}
				}
				if (breakpoint instanceof IPWatchpoint && pdiBreakpoint instanceof IPDIWatchpoint) {
					try {
						IPWatchpoint watchpoint = (IPWatchpoint) breakpoint;
						IPDIWatchpoint pdiWatchpoint = (IPDIWatchpoint) pdiBreakpoint;
						return (watchpoint.getExpression().compareTo(pdiWatchpoint.getWatchExpression()) == 0
								&& watchpoint.isReadType() == pdiWatchpoint.isReadType() && watchpoint.isWriteType() == pdiWatchpoint
								.isWriteType());
					} catch (PDIException e) {
						return false;
					}
				}
			} catch (CoreException e) {
				return false;
			}
			return false;
		}
	}

	private final IPSession session;
	private final BreakpointMap fBreakpointMap = new BreakpointMap();
	private boolean fSkipBreakpoint = false;

	public PBreakpointManager(IPSession session) {
		this.session = session;
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointManagerListener(this);
		getPDISession().getEventManager().addEventListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.IPBreakpointManager#addSetBreakpoints
	 * (org.eclipse.ptp.core.util.TaskSet,
	 * org.eclipse.ptp.debug.core.model.IPBreakpoint[])
	 */
	public void addSetBreakpoints(TaskSet tasks, IPBreakpoint[] breakpoints) {
		if (breakpoints.length == 0) {
			return;
		}
		for (IPBreakpoint bpt : breakpoints) {
			IPDIBreakpoint pdiBreakpoint = null;
			synchronized (fBreakpointMap) {
				pdiBreakpoint = fBreakpointMap.getPDIBreakpoint(bpt);
			}
			if (pdiBreakpoint != null) {
				try {
					getPDISession().getBreakpointManager().addSetBreakpoint(tasks, pdiBreakpoint);
				} catch (PDIException ex) {
					PTPDebugCorePlugin.log(ex);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.IBreakpointManagerListener#
	 * breakpointManagerEnablementChanged(boolean)
	 */
	public void breakpointManagerEnablementChanged(boolean enabled) {
		doSkipBreakpoints(!enabled);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.core.IBreakpointsListener#breakpointsAdded(org.eclipse
	 * .debug.core.model.IBreakpoint[])
	 */
	public void breakpointsAdded(IBreakpoint[] breakpoints) {
		ArrayList<IPBreakpoint> list = new ArrayList<IPBreakpoint>(breakpoints.length);
		for (int i = 0; i < breakpoints.length; ++i) {
			if (isTargetBreakpoint(breakpoints[i])) {
				list.add((IPBreakpoint) breakpoints[i]);
			}
		}
		if (list.isEmpty())
			return;
		final IPBreakpoint[] pBreakpoints = list.toArray(new IPBreakpoint[list.size()]);
		DebugPlugin.getDefault().asyncExec(new Runnable() {
			public void run() {
				setBreakpointsOnSession0(pBreakpoints);
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.core.IBreakpointsListener#breakpointsChanged(org.eclipse
	 * .debug.core.model.IBreakpoint[],
	 * org.eclipse.core.resources.IMarkerDelta[])
	 */
	public void breakpointsChanged(IBreakpoint[] breakpoints, IMarkerDelta[] deltas) {
		for (int i = 0; i < breakpoints.length; ++i) {
			if (isTargetBreakpoint(breakpoints[i])) {
				changeBreakpointProperties((IPBreakpoint) breakpoints[i], deltas[i]);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.core.IBreakpointsListener#breakpointsRemoved(org.eclipse
	 * .debug.core.model.IBreakpoint[],
	 * org.eclipse.core.resources.IMarkerDelta[])
	 */
	public void breakpointsRemoved(IBreakpoint[] breakpoints, IMarkerDelta[] deltas) {
		final List<IPBreakpoint> pBreakpoints = new ArrayList<IPBreakpoint>();
		for (IBreakpoint breakpoint : breakpoints) {
			if (isTargetBreakpoint(breakpoint)) {
				pBreakpoints.add((IPBreakpoint) breakpoint);
			}
		}
		final IPBreakpoint[] bpToRemove = pBreakpoints.toArray(new IPBreakpoint[0]);
		DebugPlugin.getDefault().asyncExec(new Runnable() {
			public void run() {
				deleteBreakpointsOnSession0(bpToRemove);
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.IPBreakpointManager#deleteBreakpoint(org.eclipse
	 * .ptp.debug.core.model.IPBreakpoint)
	 */
	public void deleteBreakpoint(IPBreakpoint breakpoint) {
		if (isTargetBreakpoint(breakpoint)) {
			final IPBreakpoint[] pBreakpoints = new IPBreakpoint[] { breakpoint };
			DebugPlugin.getDefault().asyncExec(new Runnable() {
				public void run() {
					try {
						/*
						 * Tell the debug plugin that the breakpoint should be
						 * removed. This will result in a call to
						 * breakpointsRemoved().
						 */
						DebugPlugin.getDefault().getBreakpointManager().removeBreakpoints(pBreakpoints, true);
					} catch (CoreException e) {
						PTPDebugCorePlugin.log(e);
					}
				}
			});
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.IPBreakpointManager#deleteSetBreakpoints
	 * (org.eclipse.ptp.core.util.TaskSet,
	 * org.eclipse.ptp.debug.core.model.IPBreakpoint[])
	 */
	public void deleteSetBreakpoints(TaskSet tasks, IPBreakpoint[] breakpoints) {
		if (breakpoints.length == 0)
			return;
		for (IPBreakpoint bpt : breakpoints) {
			IPDIBreakpoint pdiBreakpoint = null;
			synchronized (fBreakpointMap) {
				pdiBreakpoint = fBreakpointMap.getPDIBreakpoint(bpt);
			}
			if (pdiBreakpoint != null) {
				try {
					getPDISession().getBreakpointManager().deleteSetBreakpoint(tasks, pdiBreakpoint);
				} catch (PDIException ex) {
					PTPDebugCorePlugin.log(ex);
				}
			}
		}
	}

	/**
	 * @param monitor
	 */
	public void dispose(IProgressMonitor monitor) {
		getPDISession().getEventManager().removeEventListener(this);
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointManagerListener(this);
		fBreakpointMap.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		if (adapter.equals(PBreakpointManager.class))
			return this;
		if (adapter.equals(PSession.class))
			return getSession();
		if (adapter.equals(IPSession.class))
			return getSession();
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.IPBreakpointManager#getBreakpoint
	 * (org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint)
	 */
	public IBreakpoint getBreakpoint(IPDIBreakpoint pdiBpt) {
		Object b = null;
		synchronized (fBreakpointMap) {
			b = fBreakpointMap.getPBreakpoint(pdiBpt);
		}
		return (b instanceof IBreakpoint) ? (IBreakpoint) b : null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.IPBreakpointManager#getBreakpointAddress
	 * (org.eclipse.ptp.debug.core.model.IPLineBreakpoint)
	 */
	public BigInteger getBreakpointAddress(IPLineBreakpoint breakpoint) {
		BigInteger address = null;
		synchronized (fBreakpointMap) {
			IPDIBreakpoint pdiBreakpoint = fBreakpointMap.getPDIBreakpoint(breakpoint);
			if (pdiBreakpoint instanceof IPDILocationBreakpoint) {
				IPDILocator locator = ((IPDILocationBreakpoint) pdiBreakpoint).getLocator();
				if (locator != null) {
					address = locator.getAddress();
				}
			}
		}
		return address;
	}

	/**
	 * @return
	 */
	public IPSession getSession() {
		return session;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.event.IPDIEventListener#handleDebugEvents
	 * (org.eclipse.ptp.debug.core.pdi.event.IPDIEvent[])
	 */
	public void handleDebugEvents(IPDIEvent[] events) {
		for (int i = 0; i < events.length; i++) {
			IPDIEvent event = events[i];
			if (event instanceof IPDICreatedEvent) {
				handleCreatedEvent((IPDICreatedEvent) event);
			} else if (event instanceof IPDIDestroyedEvent) {
				handleDestroyedEvent((IPDIDestroyedEvent) event);
			} else if (event instanceof IPDIChangedEvent) {
				handleChangedEvent((IPDIChangedEvent) event);
			}
		}
	}

	/**
	 * 
	 */
	public void setInitialBreakpoints() {
		IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager()
				.getBreakpoints(PDebugModel.getPluginIdentifier());
		ArrayList<IPBreakpoint> list = new ArrayList<IPBreakpoint>(breakpoints.length);
		for (int i = 0; i < breakpoints.length; ++i) {
			if (isTargetBreakpoint(breakpoints[i])) {
				list.add((IPBreakpoint) breakpoints[i]);
			}
		}
		if (!list.isEmpty()) {
			IPBreakpoint[] pBreakpoints = list.toArray(new IPBreakpoint[list.size()]);
			setBreakpointsOnSession0(pBreakpoints);
		}
	}

	/**
	 * @param tasks
	 * @throws PDIException
	 */
	public void setStopInMain(TaskSet tasks) throws PDIException {
		IPDIBreakpointManager pdiBptMgr = getPDISession().getBreakpointManager();
		IPDIFunctionLocation location = pdiBptMgr.createFunctionLocation("", "main"); //$NON-NLS-1$ //$NON-NLS-2$
		pdiBptMgr.setFunctionBreakpoint(tasks, IPDIBreakpoint.TEMPORARY, location, null, true, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.IPBreakpointManager#skipBreakpoints
	 * (boolean)
	 */
	public void skipBreakpoints(boolean enabled) {
		if (fSkipBreakpoint != enabled && (DebugPlugin.getDefault().getBreakpointManager().isEnabled() || !enabled)) {
			fSkipBreakpoint = enabled;
			doSkipBreakpoints(enabled);
		}
	}

	/**
	 * @param breakpoint
	 * @return
	 */
	public boolean supportsAddressBreakpoint(IPAddressBreakpoint breakpoint) {
		// TODO NOT implemented address breakpoint supported
		return false;
		/*
		 * String module = null; try { module = breakpoint.getModule(); }
		 * catch(CoreException e) { } if (module != null) return
		 * getExecFilePath().toOSString().equals(module); try { return
		 * getExecFilePath().toOSString().equals(breakpoint.getSourceHandle());
		 * } catch(CoreException e) { } return false;
		 */
	}

	/**
	 * Update any pending breakpoints. This should be called when the debugger
	 * enters suspended state to update any breakpoints that could not be set or
	 * deleted because tasks were running.
	 * 
	 * @throws PDIException
	 */
	public void updatePendingBreakpoints() throws PDIException {
		getPDISession().getBreakpointManager().updatePendingBreakpoints();
	}

	/**
	 * @param tasks
	 * @param pdiWatchpoint
	 */
	public void watchpointOutOfScope(TaskSet tasks, IPDIWatchpoint pdiWatchpoint) {
		doHandleDestroyedEvent(tasks, pdiWatchpoint);
	}

	/**
	 * @param breakpoint
	 * @param delta
	 */
	private void changeBreakpointProperties(IPBreakpoint breakpoint, IMarkerDelta delta) {
		IPDIBreakpoint pdiBreakpoint = null;
		synchronized (fBreakpointMap) {
			pdiBreakpoint = fBreakpointMap.getPDIBreakpoint(breakpoint);
		}
		if (pdiBreakpoint == null)
			return;
		try {
			boolean enabled = breakpoint.isEnabled();
			boolean oldEnabled = (delta != null) ? delta.getAttribute(IBreakpoint.ENABLED, true) : enabled;
			int ignoreCount = breakpoint.getIgnoreCount();
			int oldIgnoreCount = (delta != null) ? delta.getAttribute(IPBreakpoint.IGNORE_COUNT, 0) : ignoreCount;
			String condition = breakpoint.getCondition();
			String oldCondition = (delta != null) ? delta.getAttribute(IPBreakpoint.CONDITION, "") : condition; //$NON-NLS-1$
			// TODO not support thread breakpoint
			Boolean enabled0 = null;
			IPDICondition condition0 = null;
			if (enabled != oldEnabled && enabled != pdiBreakpoint.isEnabled()) {
				enabled0 = Boolean.valueOf(enabled);
			}
			if (ignoreCount != oldIgnoreCount || condition.compareTo(oldCondition) != 0) {
				IPDICondition pdiCondition = getPDISession().getBreakpointManager().createCondition(ignoreCount, condition, null);
				if (!pdiCondition.equals(pdiBreakpoint.getCondition())) {
					condition0 = pdiCondition;
				}
			}
			if (enabled0 != null || condition0 != null) {
				changeBreakpointPropertiesOnSession(getBreakpointTasks(breakpoint), pdiBreakpoint, enabled0, condition0);
			}
		} catch (CoreException e) {
			PTPDebugCorePlugin.log(e);
		} catch (PDIException e) {
			PTPDebugCorePlugin.log(e);
		}
	}

	/**
	 * @param breakpoint
	 * @param pdiBreakpoint
	 * @throws CoreException
	 */
	private void changeBreakpointProperties(IPBreakpoint breakpoint, IPDIBreakpoint pdiBreakpoint) throws CoreException {
		try {
			Boolean enabled = null;
			if (pdiBreakpoint.isEnabled() != breakpoint.isEnabled())
				enabled = Boolean.valueOf(breakpoint.isEnabled());

			IPDICondition condition = null;
			IPDICondition c = createCondition(breakpoint);
			if (!pdiBreakpoint.getCondition().equals(c))
				condition = c;
			if (enabled != null || condition != null)
				changeBreakpointPropertiesOnSession(getBreakpointTasks(breakpoint), pdiBreakpoint, enabled, condition);
		} catch (PDIException pe) {
			PTPDebugCorePlugin.log(pe);
		}
	}

	/**
	 * @param tasks
	 * @param breakpoint
	 * @param enabled
	 * @param condition
	 */
	private void changeBreakpointPropertiesOnSession(final TaskSet tasks, final IPDIBreakpoint breakpoint, final Boolean enabled,
			final IPDICondition condition) {
		DebugPlugin.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (enabled != null) {
					try {
						if (enabled.booleanValue())
							getPDISession().getBreakpointManager().enableBreakpoint(tasks, breakpoint);
						else
							getPDISession().getBreakpointManager().disableBreakpoint(tasks, breakpoint);
					} catch (PDIException e) {
						PTPDebugCorePlugin.log(e);
					}
				}
				if (condition != null) {
					try {
						getPDISession().getBreakpointManager().setCondition(tasks, breakpoint, condition);
					} catch (PDIException e) {
						PTPDebugCorePlugin.log(e);
					}
				}
			}
		});
	}

	/**
	 * @param sourceHandle
	 * @return
	 */
	private IPath convertPath(String sourceHandle) {
		IPath path = null;
		if (Path.EMPTY.isValidPath(sourceHandle)) {
			ISourceLocator sl = getSourceLocator();
			if (sl instanceof PSourceLookupDirector) {
				path = ((PSourceLookupDirector) sl).getCompilationPath(sourceHandle);
			}
			if (path == null) {
				path = new Path(sourceHandle);
			}
		}
		return path;
	}

	/**
	 * @param pBpt
	 * @return
	 * @throws CoreException
	 * @throws PDIException
	 */
	private IPDICondition createCondition(IPBreakpoint pBpt) throws CoreException, PDIException {
		return getPDISession().getBreakpointManager().createCondition(pBpt.getIgnoreCount(), pBpt.getCondition(), null);
	}

	/**
	 * @param tasks
	 * @param pdiBreakpoint
	 */
	private void doHandleChangedEvent(TaskSet tasks, IPDIBreakpoint pdiBreakpoint) {
		IPBreakpoint breakpoint = fBreakpointMap.getPBreakpoint(pdiBreakpoint);
		if (breakpoint != null) {
			Map<String, Object> map = new HashMap<String, Object>(3);
			try {
				if (!fSkipBreakpoint && DebugPlugin.getDefault().getBreakpointManager().isEnabled()) {
					map.put(IBreakpoint.ENABLED, Boolean.valueOf(pdiBreakpoint.isEnabled()));
				} else {
					map.put(IBreakpoint.ENABLED, Boolean.valueOf(breakpoint.isEnabled()));
				}
			} catch (CoreException e) {
				PTPDebugCorePlugin.log(e);
			} catch (PDIException pe) {
				PTPDebugCorePlugin.log(pe);
			}
			try {
				map.put(IPBreakpoint.IGNORE_COUNT, new Integer(pdiBreakpoint.getCondition().getIgnoreCount()));
			} catch (PDIException e) {
				PTPDebugCorePlugin.log(e);
			}
			try {
				map.put(IPBreakpoint.CONDITION, pdiBreakpoint.getCondition().getExpression());
			} catch (PDIException e) {
				PTPDebugCorePlugin.log(e);
			}
			IPDebugInfo info = new PDebugBreakpointInfo(getSession().getDebugInfo(tasks), pdiBreakpoint.getBreakpointID(), map);
			getSession().fireDebugEvent(IPDebugEvent.CHANGE, IPDebugEvent.BREAKPOINT, info);
		}
	}

	/**
	 * @param tasks
	 * @param pdiBreakpoint
	 */
	private void doHandleDestroyedEvent(TaskSet tasks, IPDIBreakpoint pdiBreakpoint) {
		IPBreakpoint breakpoint = null;
		synchronized (fBreakpointMap) {
			breakpoint = fBreakpointMap.getPBreakpoint(pdiBreakpoint);
			fBreakpointMap.removePDIBreakpoint(pdiBreakpoint);
		}
		if (breakpoint != null) {
			IPDebugInfo info = new PDebugBreakpointInfo(getSession().getDebugInfo(tasks), pdiBreakpoint.getBreakpointID());
			getSession().fireDebugEvent(IPDebugEvent.TERMINATE, IPDebugEvent.BREAKPOINT, info);
		}
	}

	/**
	 * @param tasks
	 * @param pdiBreakpoint
	 */
	private void doHandleLocationBreakpointCreatedEvent(TaskSet tasks, IPDILocationBreakpoint pdiBreakpoint) {
		if (pdiBreakpoint.isTemporary())
			return;
		IPBreakpoint breakpoint = null;
		synchronized (fBreakpointMap) {
			breakpoint = fBreakpointMap.getPBreakpoint(pdiBreakpoint);
			if (breakpoint != null)
				fBreakpointMap.put(breakpoint, pdiBreakpoint);
		}
		if (breakpoint != null) {
			try {
				IPDebugInfo info = new PDebugBreakpointInfo(getSession().getDebugInfo(tasks), pdiBreakpoint.getBreakpointID());
				getSession().fireDebugEvent(IPDebugEvent.CREATE, IPDebugEvent.BREAKPOINT, info);
				changeBreakpointProperties(breakpoint, pdiBreakpoint);
			} catch (CoreException e) {
				PTPDebugCorePlugin.log(e);
			}
		}
	}

	/**
	 * @param tasks
	 * @param pdiWatchpoint
	 */
	private void doHandleWatchpointCreatedEvent(TaskSet tasks, IPDIWatchpoint pdiWatchpoint) {
		IPBreakpoint breakpoint = null;
		synchronized (fBreakpointMap) {
			breakpoint = fBreakpointMap.getPBreakpoint(pdiWatchpoint);
			if (breakpoint != null)
				fBreakpointMap.put(breakpoint, pdiWatchpoint);
		}
		if (breakpoint != null) {
			try {
				IPDebugInfo info = new PDebugBreakpointInfo(getSession().getDebugInfo(tasks), pdiWatchpoint.getBreakpointID());
				getSession().fireDebugEvent(IPDebugEvent.CREATE, IPDebugEvent.BREAKPOINT, info);
				changeBreakpointProperties(breakpoint, pdiWatchpoint);
			} catch (CoreException e) {
				PTPDebugCorePlugin.log(e);
			}
		}
	}

	/**
	 * @param enabled
	 */
	private void doSkipBreakpoints(boolean enabled) {
		IPBreakpoint[] pBreakpoints = fBreakpointMap.getAllPBreakpoints();
		for (IPBreakpoint bpt : pBreakpoints) {
			try {
				if (bpt.isEnabled()) {
					IPDIBreakpoint pdiBreakpoint = fBreakpointMap.getPDIBreakpoint(bpt);
					if (pdiBreakpoint != null) {
						pdiBreakpoint.setEnabled(!enabled);
					}
				}
			} catch (CoreException e) {
				PTPDebugCorePlugin.log(e);
			} catch (PDIException e) {
				PTPDebugCorePlugin.log(e);
			}
		}
	}

	/**
	 * Find the task set that this breakpoint applies to. Assumes that the
	 * breakpoint marker exists.
	 * 
	 * @param pBpt
	 *            breakpoint
	 * @return set of tasks
	 * @throws CoreException
	 */
	private TaskSet getBreakpointTasks(IPBreakpoint pBpt) throws CoreException {
		String sid = pBpt.getSetId();
		if (sid.equals(PreferenceConstants.SET_ROOT_ID)) {
			return session.getTasks();
		}
		TaskSet setTasks = session.getSetManager().getTasks(sid);
		if (setTasks != null) {
			return setTasks.copy();
		}

		IPDIBreakpoint pdiBpt = fBreakpointMap.getPDIBreakpoint(pBpt);
		if (pdiBpt == null) {
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), IStatus.ERROR,
					Messages.PBreakpointManager_0, null));
		}
		return pdiBpt.getTasks().copy();
	}

	/**
	 * @return
	 */
	private ISourceLocator getSourceLocator() {
		return getSession().getLaunch().getSourceLocator();
	}

	/**
	 * @param event
	 */
	private void handleChangedEvent(IPDIChangedEvent event) {
		IPDISessionObject reason = event.getReason();
		if (reason instanceof IPDIBreakpointInfo) {
			doHandleChangedEvent(event.getTasks(), ((IPDIBreakpointInfo) reason).getBreakpoint());
		}
	}

	/***************************************************************************
	 * Events
	 **************************************************************************/
	/**
	 * @param event
	 */
	private void handleCreatedEvent(IPDICreatedEvent event) {
		IPDISessionObject reason = event.getReason();
		if (reason instanceof IPDIBreakpointInfo) {
			IPDIBreakpoint pdiBreakpoint = ((IPDIBreakpointInfo) reason).getBreakpoint();
			if (pdiBreakpoint instanceof IPDIWatchpoint) {
				doHandleWatchpointCreatedEvent(event.getTasks(), (IPDIWatchpoint) pdiBreakpoint);
			} else if (pdiBreakpoint instanceof IPDILocationBreakpoint) {
				doHandleLocationBreakpointCreatedEvent(event.getTasks(), (IPDILocationBreakpoint) pdiBreakpoint);
			}

			if (!pdiBreakpoint.isTemporary() && !DebugPlugin.getDefault().getBreakpointManager().isEnabled()) {
				changeBreakpointPropertiesOnSession(event.getTasks(), pdiBreakpoint, new Boolean(false), null);
			}
		}
	}

	/**
	 * @param event
	 */
	private void handleDestroyedEvent(IPDIDestroyedEvent event) {
		IPDISessionObject reason = event.getReason();
		if (reason instanceof IPDIBreakpointInfo) {
			doHandleDestroyedEvent(event.getTasks(), ((IPDIBreakpointInfo) reason).getBreakpoint());
		}
	}

	/**
	 * Determine if the breakpoint is a breakpoint on our debug target.
	 * 
	 * @param breakpoint
	 *            breakpoint to test
	 * @return true if this is a target breakpoint
	 */
	private boolean isTargetBreakpoint(IBreakpoint breakpoint) {
		if (breakpoint instanceof IPBreakpoint) {
			try {
				String bp_job_id = ((IPBreakpoint) breakpoint).getJobId();
				if (bp_job_id.equals(getPDISession().getJobID()) || bp_job_id.equals(IPBreakpoint.GLOBAL)) {
					if (breakpoint instanceof IPAddressBreakpoint) {
						return supportsAddressBreakpoint((IPAddressBreakpoint) breakpoint);
					}
					if (breakpoint instanceof IPLineBreakpoint) {
						try {
							String handle = ((IPBreakpoint) breakpoint).getSourceHandle();
							ISourceLocator sl = getSourceLocator();
							if (sl instanceof IPSourceLocator) {
								return (((IPSourceLocator) sl).findSourceElement(handle) != null);
							}
							if (sl instanceof PSourceLookupDirector) {
								return (((PSourceLookupDirector) sl).contains(((IPBreakpoint) breakpoint)));
							}
						} catch (CoreException e) {
							return false;
						}
					}
					return true;
				}
			} catch (CoreException e) {
				return false;
			}
		}
		return false;
	}

	/**
	 * @param breakpoints
	 */
	protected void deleteBreakpointsOnSession0(IPBreakpoint[] breakpoints) {
		IPDIBreakpointManager pdiBptMgr = getPDISession().getBreakpointManager();
		for (IPBreakpoint breakpoint : breakpoints) {
			IPDIBreakpoint pdiBreakpoint = null;
			synchronized (fBreakpointMap) {
				pdiBreakpoint = fBreakpointMap.getPDIBreakpoint(breakpoint);
			}
			if (pdiBreakpoint == null) {
				continue;
			}

			try {
				/*
				 * Add the tasks this breakpoint applies to, to the set of
				 * pending tasks.
				 */
				pdiBreakpoint.getPendingTasks().or(getBreakpointTasks(breakpoint));

				/*
				 * Now delete the breakpoint
				 */
				pdiBptMgr.deleteBreakpoint(pdiBreakpoint.getPendingTasks(), pdiBreakpoint);
			} catch (PDIException e) {
				PTPDebugCorePlugin.log(e);
			} catch (CoreException e) {
				PTPDebugCorePlugin.log(e);
			}
		}
	}

	/**
	 * Get the PDI session
	 * 
	 * @return PDI session
	 */
	protected IPDISession getPDISession() {
		return getSession().getPDISession();
	}

	/**
	 * @param file
	 * @return
	 */
	protected Object getSourceElement(String file) {
		Object sourceElement = null;
		ISourceLocator locator = getSourceLocator();
		if (locator instanceof IPSourceLocator || locator instanceof PSourceLookupDirector) {
			if (locator instanceof IPSourceLocator)
				sourceElement = ((IPSourceLocator) locator).findSourceElement(file);
			else
				sourceElement = ((PSourceLookupDirector) locator).getSourceElement(file);
		}
		return sourceElement;
	}

	/**
	 * @param str
	 * @return
	 */
	protected boolean isEmpty(String str) {
		return !(str != null && str.trim().length() > 0);
	}

	/**
	 * @param breakpoints
	 */
	protected void setBreakpointsOnSession0(IPBreakpoint[] breakpoints) {
		IPDIBreakpointManager pdiBptMgr = getPDISession().getBreakpointManager();
		for (IPBreakpoint bpt : breakpoints) {
			try {
				IPDIBreakpoint b = null;
				if (bpt instanceof IPFunctionBreakpoint) {
					IPFunctionBreakpoint breakpoint = (IPFunctionBreakpoint) bpt;
					String function = breakpoint.getFunction();
					String fileName = breakpoint.getFileName();
					IPDIFunctionLocation location = pdiBptMgr.createFunctionLocation(fileName, function);
					IPDICondition condition = createCondition(breakpoint);
					b = pdiBptMgr.setFunctionBreakpoint(getBreakpointTasks(bpt), IPDIBreakpoint.REGULAR, location, condition, true,
							bpt.isEnabled());
				} else if (bpt instanceof IPAddressBreakpoint) {
					IPAddressBreakpoint breakpoint = (IPAddressBreakpoint) bpt;
					String address = breakpoint.getAddress();
					IPDIAddressLocation location = pdiBptMgr.createAddressLocation(new BigInteger(
							(address.startsWith("0x")) ? address.substring(2) : address, 16)); //$NON-NLS-1$
					IPDICondition condition = createCondition(breakpoint);
					b = pdiBptMgr.setAddressBreakpoint(getBreakpointTasks(bpt), IPDIBreakpoint.REGULAR, location, condition, true,
							bpt.isEnabled());
				} else if (bpt instanceof IPLineBreakpoint) {
					IPLineBreakpoint breakpoint = (IPLineBreakpoint) bpt;
					String handle = breakpoint.getSourceHandle();
					IPath path = convertPath(handle);
					IPDILineLocation location = pdiBptMgr.createLineLocation(path.toPortableString(), breakpoint.getLineNumber());
					IPDICondition condition = createCondition(breakpoint);
					b = pdiBptMgr.setLineBreakpoint(getBreakpointTasks(bpt), IPDIBreakpoint.REGULAR, location, condition, true,
							bpt.isEnabled());
				} else if (bpt instanceof IPWatchpoint) {
					IPWatchpoint watchpoint = (IPWatchpoint) bpt;
					int accessType = 0;
					accessType |= (watchpoint.isWriteType()) ? IPDIWatchpoint.WRITE : 0;
					accessType |= (watchpoint.isReadType()) ? IPDIWatchpoint.READ : 0;
					String expression = watchpoint.getExpression();
					IPDICondition condition = createCondition(watchpoint);
					b = pdiBptMgr.setWatchpoint(getBreakpointTasks(bpt), IPDIBreakpoint.REGULAR, accessType, expression, condition,
							bpt.isEnabled());
				}
				if (b != null) {
					fBreakpointMap.put(bpt, b);
				}
			} catch (CoreException e) {
				PTPDebugCorePlugin.log(e);
			} catch (PDIException e) {
				PTPDebugCorePlugin.log(e);
			}
		}
	}
}
