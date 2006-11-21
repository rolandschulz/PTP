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
package org.eclipse.ptp.debug.internal.core.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IAddressFactory;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.model.ICModule;
import org.eclipse.cdt.debug.core.model.IDebuggerProcessSupport;
import org.eclipse.cdt.debug.core.model.IDisassembly;
import org.eclipse.cdt.debug.core.model.IPersistableRegisterGroup;
import org.eclipse.cdt.debug.core.model.IRegisterDescriptor;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionListener;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.core.model.IMemoryBlockRetrievalExtension;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;
import org.eclipse.debug.core.sourcelookup.containers.FolderSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.ProjectSourceContainer;
import org.eclipse.ptp.debug.core.IPDebugConstants;
import org.eclipse.ptp.debug.core.PDebugUtils;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.cdi.IPCDIBreakpointHit;
import org.eclipse.ptp.debug.core.cdi.IPCDIEndSteppingRange;
import org.eclipse.ptp.debug.core.cdi.IPCDIErrorInfo;
import org.eclipse.ptp.debug.core.cdi.IPCDISession;
import org.eclipse.ptp.debug.core.cdi.IPCDISessionConfiguration;
import org.eclipse.ptp.debug.core.cdi.IPCDISessionObject;
import org.eclipse.ptp.debug.core.cdi.IPCDISharedLibraryEvent;
import org.eclipse.ptp.debug.core.cdi.IPCDISignalReceived;
import org.eclipse.ptp.debug.core.cdi.IPCDIWatchpointScope;
import org.eclipse.ptp.debug.core.cdi.IPCDIWatchpointTrigger;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIChangedEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDICreatedEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIDestroyedEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIDisconnectedEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIEventListener;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIExitedEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIRestartedEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIResumedEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDISuspendedEvent;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIObject;
import org.eclipse.ptp.debug.core.cdi.model.IPCDISignal;
import org.eclipse.ptp.debug.core.cdi.model.IPCDITarget;
import org.eclipse.ptp.debug.core.cdi.model.IPCDITargetConfiguration;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIThread;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIVariableDescriptor;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.debug.core.model.IExecFileInfo;
import org.eclipse.ptp.debug.core.model.IGlobalVariableDescriptor;
import org.eclipse.ptp.debug.core.model.IPBreakpoint;
import org.eclipse.ptp.debug.core.model.IPDebugElement;
import org.eclipse.ptp.debug.core.model.IPDebugElementStatus;
import org.eclipse.ptp.debug.core.model.IPDebugTarget;
import org.eclipse.ptp.debug.core.model.IPGlobalVariable;
import org.eclipse.ptp.debug.core.model.IPGlobalVariableManager;
import org.eclipse.ptp.debug.core.model.IPLineBreakpoint;
import org.eclipse.ptp.debug.core.model.IPSignal;
import org.eclipse.ptp.debug.core.model.PDebugElementState;
import org.eclipse.ptp.debug.core.sourcelookup.IPSourceLocator;
import org.eclipse.ptp.debug.core.sourcelookup.ISourceLookupChangeListener;
import org.eclipse.ptp.debug.core.sourcelookup.PDirectorySourceContainer;
import org.eclipse.ptp.debug.internal.core.PGlobalVariableManager;
import org.eclipse.ptp.debug.internal.core.PSignalManager;
import org.eclipse.ptp.debug.internal.core.PTPMemoryBlockRetrievalExtension;
import org.eclipse.ptp.debug.internal.core.sourcelookup.PSourceLookupParticipant;
import org.eclipse.ptp.debug.internal.core.sourcelookup.PSourceManager;

/**
 * @author Clement chu
 *
 */
public class PDebugTarget extends PDebugElement implements IPDebugTarget, IPCDIEventListener, ILaunchListener, IExpressionListener, ISourceLookupChangeListener {
	private final String PROCESS_NAME = "Process ";
	private ArrayList fThreads;
	private IProcess fDebuggeeProcess = null;
	private IPCDITarget fCDITarget;
	private IPLaunch fLaunch;
	private IPCDITargetConfiguration fConfig;
	private PGlobalVariableManager fGlobalVariableManager;
	private PSignalManager fSignalManager;
	private IBinaryObject fBinaryFile;
	private Boolean fIsLittleEndian = null;
	private Preferences fPreferences = null;
	private IAddressFactory fAddressFactory;
	/**
	 * Support for the memory retrival on this target.
	 */
	private PTPMemoryBlockRetrievalExtension fMemoryBlockRetrieval;

	public PDebugTarget(IPLaunch launch, IPCDITarget cdiTarget, IProcess debuggeeProcess, IBinaryObject file, boolean allowsTerminate, boolean allowsDisconnect) {
		super(null);
		fLaunch = launch;
		fCDITarget = cdiTarget;
		setDebugTarget(this);
		setProcess(debuggeeProcess);
		setExecFile(file);
		initializePreferences();
		setConfiguration((IPCDITargetConfiguration)cdiTarget.getConfiguration());
		setThreadList(new ArrayList(5));
		if (fCDITarget.isTerminated()) {
			setState(PDebugElementState.TERMINATED);
		} else {
			if (fCDITarget.isSuspended())
				setState(PDebugElementState.SUSPENDED);
			else
				setState(PDebugElementState.UNDEFINED);

			setGlobalVariableManager(new PGlobalVariableManager(this));
			setMemoryBlockRetrieval(new PTPMemoryBlockRetrievalExtension(this));
			setSignalManager(new PSignalManager(this));
			initialize();
			DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
			DebugPlugin.getDefault().getExpressionManager().addExpressionListener(this);
			getCDISession().getEventManager().addEventListener(this);
		}
	}
	public int getTargetID() {
		return fCDITarget.getTargetID();
	}
	private PTPMemoryBlockRetrievalExtension getMemoryBlockRetrieval() {
		return fMemoryBlockRetrieval;
	}

	private void setMemoryBlockRetrieval(PTPMemoryBlockRetrievalExtension memoryBlockRetrieval) {
		fMemoryBlockRetrieval = memoryBlockRetrieval;
	}	
	protected void initialize() {
		initializeSourceLookupPath();
		ArrayList debugEvents = new ArrayList(1);
		debugEvents.add(createCreateEvent());
		initializeThreads(debugEvents);
		initializeSourceManager();
		initializeMemoryBlocks();
		fireEventSet((DebugEvent[]) debugEvents.toArray(new DebugEvent[debugEvents.size()]));
	}
	protected void initializeThreads(List debugEvents) {
		IPCDIThread[] cdiThreads = new IPCDIThread[0];
		try {
			if (isSuspended())
				cdiThreads = getCDITarget().getThreads();
		} catch (PCDIException e) {
			// ignore
		}
		DebugEvent suspendEvent = null;
		for (int i = 0; i < cdiThreads.length; ++i) {
			PThread thread = createThread(cdiThreads[i]);
			debugEvents.add(thread.createCreateEvent());
			try {
				if (cdiThreads[i].equals(getCDITarget().getCurrentThread()) && thread.isSuspended()) {
					// Use BREAKPOINT as a detail to force perspective switch
					suspendEvent = thread.createSuspendEvent(DebugEvent.BREAKPOINT);
				}
			} catch (PCDIException e) {
				// ignore
			}
		}
		if (suspendEvent != null) {
			debugEvents.add(suspendEvent);
		}
	}
	protected void initializeMemoryBlocks() {
		getMemoryBlockRetrieval().initialize();
	}	
	protected void initializeSourceManager() {
		ISourceLocator locator = getLaunch().getSourceLocator();
		if (locator instanceof IAdaptable) {
			IPSourceLocator clocator = (IPSourceLocator) ((IAdaptable) locator).getAdapter(IPSourceLocator.class);
			if (clocator instanceof IAdaptable) {
				PSourceManager sm = (PSourceManager) ((IAdaptable) clocator).getAdapter(PSourceManager.class);
				if (sm != null)
					sm.setDebugTarget(this);
			}
			IResourceChangeListener listener = (IResourceChangeListener) ((IAdaptable) locator).getAdapter(IResourceChangeListener.class);
			if (listener != null)
				ResourcesPlugin.getWorkspace().addResourceChangeListener(listener);
		}
	}
	protected void initializeSourceLookupPath() {
		ISourceLocator locator = getLaunch().getSourceLocator();
		if (locator instanceof ISourceLookupDirector) {
			ISourceLookupParticipant[] participants = ((ISourceLookupDirector) locator).getParticipants();
			for (int i = 0; i < participants.length; ++i) {
				if (participants[i] instanceof PSourceLookupParticipant) {
					((PSourceLookupParticipant) participants[i]).addSourceLookupChangeListener(this);
				}
			}
			setSourceLookupPath(((ISourceLookupDirector) locator).getSourceContainers());
		}
	}
	public IProcess getProcess() {
		return fDebuggeeProcess;
	}
	protected void setProcess(IProcess debuggeeProcess) {
		fDebuggeeProcess = debuggeeProcess;
	}
	public IThread[] getThreads() {
		List threads = getThreadList();
		return (IThread[]) threads.toArray(new IThread[threads.size()]);
	}
	public boolean hasThreads() throws DebugException {
		return getThreadList().size() > 0;
	}
	public String getName() throws DebugException {
		return PROCESS_NAME + getTargetID();
	}
	public boolean supportsBreakpoint(IBreakpoint breakpoint) {
		if (!getConfiguration().supportsBreakpoints())
			return false;
		// return (breakpoint instanceof IPBreakpoint && getBreakpointManager().supportsBreakpoint( (IPBreakpoint)breakpoint ));
		return (breakpoint instanceof IPBreakpoint);
	}
	public void launchRemoved(ILaunch launch) {
		if (!isAvailable()) {
			return;
		}
		if (launch.equals(getLaunch())) {
			disconnected();
		}
	}
	public void launchAdded(ILaunch launch) {}
	public void launchChanged(ILaunch launch) {}
	public boolean canTerminate() {
		return supportsTerminate() && isAvailable();
	}
	public boolean isTerminated() {
		return (getState().equals(PDebugElementState.TERMINATED));
	}
	public void terminate() throws DebugException {
		if (!canTerminate()) {
			return;
		}
		changeState(PDebugElementState.TERMINATING);
		try {
			getCDITarget().terminate();
		} catch (PCDIException e) {
			restoreOldState();
			targetRequestFailed(e.getMessage(), null);
		}
	}
	public boolean canResume() {
		return getConfiguration().supportsResume() && isSuspended();
	}
	public boolean canSuspend() {
		if (!getConfiguration().supportsSuspend())
			return false;
		if (getState().equals(PDebugElementState.RESUMED)) {
			// only allow suspend if no threads are currently suspended
			IThread[] threads = getThreads();
			for (int i = 0; i < threads.length; i++) {
				if (threads[i].isSuspended()) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	public boolean isSuspended() {
		return (getState().equals(PDebugElementState.SUSPENDED));
	}
	public void resume() throws DebugException {
		if (!canResume())
			return;
		changeState(PDebugElementState.RESUMING);
		try {
			getCDITarget().resume(false);
		} catch (PCDIException e) {
			restoreOldState();
			targetRequestFailed(e.getMessage(), null);
		}
	}
	public void suspend() throws DebugException {
		if (!canSuspend())
			return;
		changeState(PDebugElementState.SUSPENDING);
		try {
			getCDITarget().suspend();
		} catch (CDIException e) {
			restoreOldState();
			targetRequestFailed(e.getMessage(), null);
		}
	}
	protected boolean isSuspending() {
		return (getState().equals(PDebugElementState.SUSPENDING));
	}
	protected void suspendThreads(IPCDISuspendedEvent event) {
		Iterator it = getThreadList().iterator();
		while (it.hasNext()) {
			PThread thread = (PThread) it.next();
			IPCDIThread suspensionThread = null;
			try {
				suspensionThread = (IPCDIThread)getCDITarget().getCurrentThread();
			} catch (PCDIException e) {
				// ignore
			}
			thread.suspendByTarget(event.getReason(), suspensionThread);
		}
	}
	protected synchronized List refreshThreads() {
		ArrayList newThreads = new ArrayList(5);
		ArrayList list = new ArrayList(5);
		ArrayList debugEvents = new ArrayList(5);
		List oldList = (List) getThreadList().clone();
		IPCDIThread[] cdiThreads = new IPCDIThread[0];
		IPCDIThread currentCDIThread = null;
		try {
			cdiThreads = (IPCDIThread[])getCDITarget().getThreads();
			currentCDIThread = (IPCDIThread)getCDITarget().getCurrentThread();
		} catch (PCDIException e) {
		}
		for (int i = 0; i < cdiThreads.length; ++i) {
			PThread thread = findThread(oldList, cdiThreads[i]);
			if (thread == null) {
				thread = new PThread(this, cdiThreads[i]);
				newThreads.add(thread);
			} else {
				oldList.remove(thread);
			}
			thread.setCurrent(cdiThreads[i].equals(currentCDIThread));
			list.add(thread);
		}
		Iterator it = oldList.iterator();
		while (it.hasNext()) {
			PThread thread = (PThread) it.next();
			thread.terminated();
			debugEvents.add(thread.createTerminateEvent());
		}
		setThreadList(list);
		it = newThreads.iterator();
		while (it.hasNext()) {
			debugEvents.add(((PThread) it.next()).createCreateEvent());
		}
		if (debugEvents.size() > 0)
			fireEventSet((DebugEvent[]) debugEvents.toArray(new DebugEvent[debugEvents.size()]));
		return newThreads;
	}
	protected synchronized void resumeThreads(List debugEvents, int detail) {
		Iterator it = getThreadList().iterator();
		while (it.hasNext()) {
			((PThread) it.next()).resumedByTarget(detail, debugEvents);
		}
	}
	public void breakpointAdded(IBreakpoint breakpoint) {}
	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {}
	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {}
	protected boolean supportsDisconnect() {
		// No Discount
		// return getConfiguration().supportsDisconnect();
		return false;
	}
	protected boolean supportsTerminate() {
		return getConfiguration().supportsTerminate();
	}
	public boolean canDisconnect() {
		// No disconnet
		// return supportsDisconnect() && isAvailable();
		return false;
	}
	public void disconnect() throws DebugException {
		if (isDisconnecting()) {
			return;
		}
		changeState(PDebugElementState.DISCONNECTING);
		try {
			getCDITarget().disconnect();
		} catch (PCDIException e) {
			restoreOldState();
			targetRequestFailed(e.getMessage(), null);
		}
	}
	public boolean isDisconnected() {
		return (getState().equals(PDebugElementState.DISCONNECTED));
	}
	public boolean supportsStorageRetrieval() {
		return false;
	}
	public IMemoryBlock getMemoryBlock(long startAddress, long length) throws DebugException {
		return null;
	}
	public ILaunch getLaunch() {
		return fLaunch;
	}
	protected ArrayList getThreadList() {
		return fThreads;
	}
	private void setThreadList(ArrayList threads) {
		fThreads = threads;
	}
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IPDebugElement.class))
			return this;
		if (adapter.equals(PDebugElement.class))
			return this;
		if (adapter.equals(IDebugTarget.class))
			return this;
		if (adapter.equals(IPDebugTarget.class))
			return this;
		if (adapter.equals(PDebugTarget.class))
			return this;
		if (adapter.equals(IPCDITarget.class))
			return fCDITarget;
		if (adapter.equals(IDebuggerProcessSupport.class))
			return this;
		if (adapter.equals(IExecFileInfo.class))
			return this;
		if (adapter.equals(IPGlobalVariableManager.class))
			return getGlobalVariableManager();
		if (adapter.equals(IPCDISession.class))
			return getCDISession();
		if (adapter.equals(IMemoryBlockRetrievalExtension.class))
			return getMemoryBlockRetrieval();		
		if ( adapter.equals( IMemoryBlockRetrieval.class ) )
			return getMemoryBlockRetrieval();
		return super.getAdapter(adapter);
	}
	public void handleDebugEvents(IPCDIEvent[] events) {
		for (int i = 0; i < events.length; i++) {
			IPCDIEvent event = events[i];
			if (!event.containTask(getTargetID()))
				return;
			IPCDIObject source = event.getSource(getTargetID());
			if (source == null && event instanceof IPCDIDestroyedEvent) {
				handleTerminatedEvent((IPCDIDestroyedEvent) event, source);
			} else if (source != null && source.getTarget().equals(getCDITarget())) {
				if (event instanceof IPCDICreatedEvent) {
					if (source instanceof IPCDIThread) {
						handleThreadCreatedEvent((IPCDICreatedEvent) event, source);
					}
				} else if (event instanceof IPCDISuspendedEvent) {
					if (source instanceof IPCDITarget) {
						handleSuspendedEvent((IPCDISuspendedEvent) event, source);
					}
				} else if (event instanceof IPCDIResumedEvent) {
					if (source instanceof IPCDITarget) {
						handleResumedEvent((IPCDIResumedEvent) event, source);
					}
				} else if (event instanceof IPCDIExitedEvent) {
					if (source instanceof IPCDITarget) {
						handleExitedEvent((IPCDIExitedEvent) event, source);
					}
				} else if (event instanceof IPCDIDestroyedEvent) {
					if (source instanceof IPCDIThread) {
						handleThreadTerminatedEvent((IPCDIDestroyedEvent) event, source);
					}
				} else if (event instanceof IPCDIDisconnectedEvent) {
					if (source instanceof IPCDITarget) {
						handleDisconnectedEvent((IPCDIDisconnectedEvent) event, source);
					}
				} else if (event instanceof IPCDIChangedEvent) {
					if (source instanceof IPCDITarget) {
						handleChangedEvent((IPCDIChangedEvent) event, source);
					}
					if (source instanceof IPCDISignal) {
						getSignalManager().signalChanged((IPCDISignal)source);
					}
				} else if (event instanceof IPCDIRestartedEvent) {
					if (source instanceof IPCDITarget) {
						handleRestartedEvent((IPCDIRestartedEvent) event, source);
					}
				}
			}
		}
	}
	public boolean canRestart() {
		// return getConfiguration().supportsRestart() && isSuspended();
		// Cannot restart
		return false;
	}
	public void restart() throws DebugException {
		if (!canRestart()) {
			return;
		}
		changeState(PDebugElementState.RESTARTING);
		//IPCDILocation location = (IPCDILocation)getCDITarget().createFunctionLocation("", "main");
		//setInternalTemporaryBreakpoint(location);
		try {
			getCDITarget().restart();
		} catch (PCDIException e) {
			restoreOldState();
			targetRequestFailed(e.getMessage(), e);
		}
	}
	public boolean isAvailable() {
		return !(isTerminated() || isTerminating() || isDisconnected() || isDisconnecting());
	}
	protected boolean isTerminating() {
		return (getState().equals(PDebugElementState.TERMINATING));
	}
	public void terminated() {
		if (!isTerminated()) {
			if (!isDisconnected()) {
				setState(PDebugElementState.TERMINATED);
			}
			cleanup();
			fireTerminateEvent();
		}
	}
	protected boolean isDisconnecting() {
		return (getState().equals(PDebugElementState.DISCONNECTING));
	}
	protected void disconnected() {
		if (!isDisconnected()) {
			setState(PDebugElementState.DISCONNECTED);
			cleanup();
			fireTerminateEvent();
		}
	}
	public void cleanup() {
		resetStatus();
		removeAllThreads();
		getCDISession().getEventManager().removeEventListener(this);
		DebugPlugin.getDefault().getExpressionManager().removeExpressionListener(this);
		DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
		saveGlobalVariables();
		//saveMemoryBlocks(); //Don't save to launch configuration
		disposeMemoryBlockRetrieval();
		disposeGlobalVariableManager();
		disposeSourceManager();
		disposeSourceLookupPath();
		removeAllExpressions();
		disposePreferences();
	}
	protected void saveMemoryBlocks() {
		getMemoryBlockRetrieval().save();
	}	
	protected void disposeMemoryBlockRetrieval() {
		getMemoryBlockRetrieval().dispose();
	}
	protected void removeAllThreads() {
		List threads = getThreadList();
		setThreadList(new ArrayList(0));
		ArrayList debugEvents = new ArrayList(threads.size());
		Iterator it = threads.iterator();
		while (it.hasNext()) {
			PThread thread = (PThread) it.next();
			thread.terminated();
			debugEvents.add(thread.createTerminateEvent());
		}
		fireEventSet((DebugEvent[]) debugEvents.toArray(new DebugEvent[debugEvents.size()]));
	}
	protected void removeAllExpressions() {
		IExpressionManager em = DebugPlugin.getDefault().getExpressionManager();
		IExpression[] expressions = em.getExpressions();
		for (int i = 0; i < expressions.length; ++i) {
			if (expressions[i] instanceof PExpression && expressions[i].getDebugTarget().equals(this)) {
				em.removeExpression(expressions[i]);
			}
		}
	}
	protected PThread createThread(IPCDIThread cdiThread) {
		PThread thread = new PThread(this, cdiThread);
		getThreadList().add(thread);
		return thread;
	}
	private void handleSuspendedEvent(IPCDISuspendedEvent event, IPCDIObject source) {
		setState(PDebugElementState.SUSPENDED);
		IPCDISessionObject reason = event.getReason();
		setCurrentStateInfo(reason);
		skipBreakpoints(false);
		List newThreads = refreshThreads();
		if (source instanceof IPCDITarget) {
			//if (!(this.getConfiguration() instanceof ICDITargetConfiguration2) || !((ICDITargetConfiguration2)this.getConfiguration()).supportsThreadControl())
			suspendThreads(event);
		}
		// We need this for debuggers that don't have notifications for newly created threads.
		else if (source instanceof IPCDIThread) {
			PThread thread = findThread((IPCDIThread) source);
			if (thread != null && newThreads.contains(thread)) {
				IPCDIEvent[] evts = new IPCDIEvent[] { event };
				thread.handleDebugEvents(evts);
			}
		}
		if (reason instanceof IPCDIEndSteppingRange) {
			handleEndSteppingRange((IPCDIEndSteppingRange) reason);
		} else if (reason instanceof IPCDIBreakpointHit) {
			handleBreakpointHit((IPCDIBreakpointHit) reason);
		} else if (reason instanceof IPCDISignalReceived) {
			handleSuspendedBySignal((IPCDISignalReceived) reason);
		} else if (reason instanceof IPCDIWatchpointTrigger) {
			handleWatchpointTrigger((IPCDIWatchpointTrigger) reason);
		} else if (reason instanceof IPCDIWatchpointScope) {
			handleWatchpointScope((IPCDIWatchpointScope) reason);
		} else if (reason instanceof IPCDIErrorInfo) {
			handleErrorInfo((IPCDIErrorInfo) reason);
		} else if (reason instanceof IPCDISharedLibraryEvent) {
			handleSuspendedBySolibEvent((IPCDISharedLibraryEvent) reason);
		} else { // reason is not specified
			fireSuspendEvent(DebugEvent.UNSPECIFIED);
		}
	}
	private void handleResumedEvent(IPCDIResumedEvent event, IPCDIObject source) {
		setState(PDebugElementState.RESUMED);
		setCurrentStateInfo(null);
		resetStatus();
		ArrayList debugEvents = new ArrayList(10);
		int detail = DebugEvent.UNSPECIFIED;
		switch (event.getType()) {
		case IPCDIResumedEvent.CONTINUE:
			detail = DebugEvent.CLIENT_REQUEST;
			break;
		case IPCDIResumedEvent.STEP_INTO:
		case IPCDIResumedEvent.STEP_INTO_INSTRUCTION:
			detail = DebugEvent.STEP_INTO;
			break;
		case IPCDIResumedEvent.STEP_OVER:
		case IPCDIResumedEvent.STEP_OVER_INSTRUCTION:
			detail = DebugEvent.STEP_OVER;
			break;
		case IPCDIResumedEvent.STEP_RETURN:
			detail = DebugEvent.STEP_RETURN;
			break;
		}
		debugEvents.add(createResumeEvent(detail));
		//if (!(this.getConfiguration() instanceof ICDITargetConfiguration2) || !((ICDITargetConfiguration2)this.getConfiguration()).supportsThreadControl())
		resumeThreads(debugEvents, detail);
		fireEventSet((DebugEvent[]) debugEvents.toArray(new DebugEvent[debugEvents.size()]));
	}
	private void handleEndSteppingRange(IPCDIEndSteppingRange endSteppingRange) {
		fireSuspendEvent(DebugEvent.UNSPECIFIED);
	}
	private void handleBreakpointHit(IPCDIBreakpointHit breakpointHit) {
		fireSuspendEvent(DebugEvent.BREAKPOINT);
	}
	private void handleWatchpointTrigger(IPCDIWatchpointTrigger wt) {
		fireSuspendEvent(DebugEvent.BREAKPOINT);
	}
	private void handleWatchpointScope(IPCDIWatchpointScope ws) {
	// DONNY
	// getBreakpointManager().watchpointOutOfScope( ws.getWatchpoint() ); fireSuspendEvent( DebugEvent.BREAKPOINT );
	}
	private void handleSuspendedBySignal(IPCDISignalReceived signal) {
		fireSuspendEvent(DebugEvent.UNSPECIFIED);
	}
	private void handleErrorInfo(IPCDIErrorInfo info) {
		setStatus(IPDebugElementStatus.ERROR, (info != null) ? info.getMessage() : null);
		if (info != null) {
			MultiStatus status = new MultiStatus(PTPDebugCorePlugin.getUniqueIdentifier(), IPDebugConstants.STATUS_CODE_ERROR, CoreModelMessages.getString("PDebugTarget.1"), null);
			StringTokenizer st = new StringTokenizer(info.getDetailMessage(), "\n\r");
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				if (token.length() > 200) {
					token = token.substring(0, 200);
				}
				status.add(new Status(IStatus.ERROR, status.getPlugin(), IPDebugConstants.STATUS_CODE_ERROR, token, null));
			}
			PDebugUtils.error(status, this);
		}
		fireSuspendEvent(DebugEvent.UNSPECIFIED);
	}
	private void handleSuspendedBySolibEvent(IPCDISharedLibraryEvent solibEvent) {
		fireSuspendEvent(DebugEvent.UNSPECIFIED);
	}
	private void handleExitedEvent(IPCDIExitedEvent event, IPCDIObject source) {
		removeAllThreads();
		setState(PDebugElementState.EXITED);
		setCurrentStateInfo(event.getReason());
		fireChangeEvent(DebugEvent.CONTENT);
		IPCDISessionConfiguration sessionConfig = getCDISession().getConfiguration();
		if (sessionConfig != null && sessionConfig.terminateSessionOnExit())
			terminated();
	}
	private void handleTerminatedEvent(IPCDIDestroyedEvent event, IPCDIObject source) {
		terminated();
	}
	private void handleDisconnectedEvent(IPCDIDisconnectedEvent event, IPCDIObject source) {
		disconnected();
	}
	private void handleChangedEvent(IPCDIChangedEvent event, IPCDIObject source) {}
	private void handleRestartedEvent(IPCDIRestartedEvent event, IPCDIObject source) {}
	private void handleThreadCreatedEvent(IPCDICreatedEvent event, IPCDIObject source) {
		IPCDIThread cdiThread = (IPCDIThread) source;
		PThread thread = findThread(cdiThread);
		if (thread == null) {
			thread = createThread(cdiThread);
			thread.fireCreationEvent();
		}
	}
	private void handleThreadTerminatedEvent(IPCDIDestroyedEvent event, IPCDIObject source) {
		IPCDIThread cdiThread = (IPCDIThread) source;
		PThread thread = findThread(cdiThread);
		if (thread != null) {
			getThreadList().remove(thread);
			thread.terminated();
			thread.fireTerminateEvent();
		}
	}
	public PThread findThread(IPCDIThread cdiThread) {
		List threads = getThreadList();
		for (int i = 0; i < threads.size(); i++) {
			PThread t = (PThread) threads.get(i);
			if (t.getCDIThread().equals(cdiThread))
				return t;
		}
		return null;
	}
	public PThread findThread(List threads, IPCDIThread cdiThread) {
		for (int i = 0; i < threads.size(); i++) {
			PThread t = (PThread) threads.get(i);
			if (t.getCDIThread().equals(cdiThread))
				return t;
		}
		return null;
	}
	protected IPCDITargetConfiguration getConfiguration() {
		return fConfig;
	}
	private void setConfiguration(IPCDITargetConfiguration config) {
		fConfig = config;
	}
	protected boolean supportsExpressionEvaluation() {
		return getConfiguration().supportsExpressionEvaluation();
	}
	public void expressionAdded(IExpression expression) {}
	public void expressionChanged(IExpression expression) {}
	public void expressionRemoved(IExpression expression) {
		if (expression instanceof PExpression && expression.getDebugTarget().equals(this)) {
			((PExpression) expression).dispose();
		}
	}
	/*
	public void setInternalTemporaryBreakpoint(IPCDILocation location) throws DebugException {
		try {
			if (location instanceof ICDIFunctionLocation) {
				getCDITarget().setFunctionBreakpoint(IPCDIBreakpoint.TEMPORARY, (IPCDIFunctionLocation) location, null, false);
			} else if (location instanceof ICDILineLocation) {
				getCDITarget().setLineBreakpoint(IPCDIBreakpoint.TEMPORARY, (IPCDILineLocation) location, null, false);
			} else if (location instanceof ICDIAddressLocation) {
				getCDITarget().setAddressBreakpoint(IPCDIBreakpoint.TEMPORARY, (IPCDIAddressLocation) location, null, false);
			} else {
				// ???
				targetRequestFailed("not_a_location", null);
			}
		} catch (PCDIException e) {
			targetRequestFailed(e.getMessage(), null);
		}
	}
	*/
	protected IThread getCurrentThread() throws DebugException {
		IThread[] threads = getThreads();
		for (int i = 0; i < threads.length; ++i) {
			if (((PThread) threads[i]).isCurrent())
				return threads[i];
		}
		return null;
	}
	protected ISourceLocator getSourceLocator() {
		return getLaunch().getSourceLocator();
	}
	public boolean isLittleEndian() {
		if (fIsLittleEndian == null) {
			fIsLittleEndian = Boolean.TRUE;
			IBinaryObject file = getBinaryFile();
			if (file != null) {
				fIsLittleEndian = new Boolean(file.isLittleEndian());
			}
		}
		return fIsLittleEndian.booleanValue();
	}
	public IBinaryObject getExecFile() {
		return getBinaryFile();
	}
	public IBinaryObject getBinaryFile() {
		return fBinaryFile;
	}
	private void setExecFile(IBinaryObject file) {
		fBinaryFile = file;
	}
	protected void saveGlobalVariables() {
		fGlobalVariableManager.save();
	}
	protected void disposeGlobalVariableManager() {
		fGlobalVariableManager.dispose();
	}
	public boolean canResumeWithoutSignal() {
		// Check if the configuration supports this!!!
		return (canResume() && getCurrentStateInfo() instanceof IPCDISignalReceived);
	}
	public void resumeWithoutSignal() throws DebugException {
		if (!canResume())
			return;
		changeState(PDebugElementState.RESUMING);
		try {
			getCDITarget().resume(false);
		} catch (PCDIException e) {
			restoreOldState();
			targetRequestFailed(e.getMessage(), e);
		}
	}
	protected void disposeSourceManager() {
		ISourceLocator locator = getSourceLocator();
		if (locator instanceof IAdaptable) {
			IResourceChangeListener listener = (IResourceChangeListener) ((IAdaptable) locator).getAdapter(IResourceChangeListener.class);
			if (listener != null)
				CCorePlugin.getWorkspace().removeResourceChangeListener(listener);
		}
	}
	protected void disposeSourceLookupPath() {
		ISourceLocator locator = getLaunch().getSourceLocator();
		if (locator instanceof ISourceLookupDirector) {
			ISourceLookupParticipant[] participants = ((ISourceLookupDirector) locator).getParticipants();
			for (int i = 0; i < participants.length; ++i) {
				if (participants[i] instanceof PSourceLookupParticipant) {
					((PSourceLookupParticipant) participants[i]).removeSourceLookupChangeListener(this);
				}
			}
		}
	}
	public String toString() {
		String result = "";
		try {
			result = getName();
		} catch (DebugException e) {
		}
		return result;
	}
	public IPSignal[] getSignals() throws DebugException {
		PSignalManager sm = getSignalManager();
		if (sm != null) {
			return sm.getSignals();
		}
		return new IPSignal[0];
	}
	public boolean hasSignals() throws DebugException {
		PSignalManager sm = getSignalManager();
		if (sm != null) {
			return (sm.getSignals().length > 0);
		}
		return false;
	}
	public IAddress getBreakpointAddress(IPLineBreakpoint breakpoint) throws DebugException {
		// TODO Not implement yet
		return null;
	}
	public void enableInstructionStepping(boolean enabled) {
		fPreferences.setValue(PREF_INSTRUCTION_STEPPING_MODE, enabled);
	}
	public boolean supportsInstructionStepping() {
		return getConfiguration().supportsInstructionStepping();
	}
	public boolean isInstructionSteppingEnabled() {
		// TODO Not implement yet
		// return fPreferences.getBoolean( PREF_INSTRUCTION_STEPPING_MODE ) || CDebugCorePlugin.getDefault().getPluginPreferences().getBoolean( ICDebugConstants.PREF_INSTRUCTION_STEP_MODE_ON );
		return false;
	}
	private void initializePreferences() {
		fPreferences = new Preferences();
		fPreferences.setDefault(PREF_INSTRUCTION_STEPPING_MODE, false);
	}
	private void disposePreferences() {
		fPreferences = null;
	}
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		if (fPreferences != null)
			fPreferences.addPropertyChangeListener(listener);
	}
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		if (fPreferences != null)
			fPreferences.removePropertyChangeListener(listener);
	}
	protected PGlobalVariableManager getGlobalVariableManager() {
		return fGlobalVariableManager;
	}
	private void setGlobalVariableManager(PGlobalVariableManager globalVariableManager) {
		fGlobalVariableManager = globalVariableManager;
	}
	protected void setSignalManager(PSignalManager sm) {
		fSignalManager = sm;
	}
	protected PSignalManager getSignalManager() {
		return fSignalManager;
	}
	protected void disposeSignalManager() {
		fSignalManager.dispose();
	}
	
	public boolean isPostMortem() {
		return false;
	}
	public IAddressFactory getAddressFactory() {
		if (fAddressFactory == null) {
			if (getExecFile() != null) {
				IBinaryObject file = getBinaryFile();
				if (file != null) {
					fAddressFactory = file.getAddressFactory();
				}
			}
		}
		return fAddressFactory;
	}
	private void changeState(PDebugElementState state) {
		setState(state);
		Iterator it = getThreadList().iterator();
		while (it.hasNext()) {
			((PThread) it.next()).setState(state);
		}
	}
	protected void restoreOldState() {
		restoreState();
		Iterator it = getThreadList().iterator();
		while (it.hasNext()) {
			((PThread) it.next()).restoreState();
		}
	}
	protected void skipBreakpoints(boolean enabled) {
	// FIXME Donny
	// We do not have skipBreakpoints support for each individual target
	// getBreakpointManager().skipBreakpoints( enabled );
	}
	public IDisassembly getDisassembly() throws DebugException {
		// TODO Not implement yet
		return null;
	}
	public IPGlobalVariable createGlobalVariable(IGlobalVariableDescriptor info) throws DebugException {
		IPCDIVariableDescriptor vo = null;
		try {
			vo = (IPCDIVariableDescriptor)getCDITarget().getGlobalVariableDescriptors(info.getPath().lastSegment(), null, info.getName());
		} catch (PCDIException e) {
			throw new DebugException(new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), DebugException.TARGET_REQUEST_FAILED, (vo != null) ? vo.getName() + ": " + e.getMessage() : e.getMessage(), null)); //$NON-NLS-1$
		}
		return PVariableFactory.createGlobalVariable(this, info, vo);
	}
	public boolean hasModules() throws DebugException {
		// TODO Not implement yet
		return false;
	}
	public ICModule[] getModules() throws DebugException {
		// TODO Not implement yet
		return null;
	}
	public void loadSymbolsForAllModules() throws DebugException {
		// TODO Not implement yet
	}
	public IRegisterDescriptor[] getRegisterDescriptors() throws DebugException {
		// TODO Not implement yet
		return null;
	}
	public void addRegisterGroup(String name, IRegisterDescriptor[] descriptors) {
		// TODO Not implement yet
	}
	public void removeRegisterGroups(IRegisterGroup[] groups) {
		// TODO Not implement yet
	}
	public void modifyRegisterGroup(IPersistableRegisterGroup group, IRegisterDescriptor[] descriptors) {
		// TODO Not implement yet
	}
	public void restoreDefaultRegisterGroups() {
		// TODO Not implement yet
	}
	public IGlobalVariableDescriptor[] getGlobals() throws DebugException {
		ArrayList list = new ArrayList();
		IBinaryObject file = getBinaryFile();
		if (file != null) {
			list.addAll(getCFileGlobals(file));
		}
		return (IGlobalVariableDescriptor[]) list.toArray(new IGlobalVariableDescriptor[list.size()]);
	}
	private List getCFileGlobals(IBinaryObject file) throws DebugException {
		ArrayList list = new ArrayList();
		ISymbol[] symbols = file.getSymbols();
		for (int i = 0; i < symbols.length; ++i) {
			if (symbols[i].getType() == ISymbol.VARIABLE) {
				list.add(PVariableFactory.createGlobalVariableDescriptor(symbols[i]));
			}
		}
		return list;
	}
	public void sourceContainersChanged(ISourceLookupDirector director) {
		setSourceLookupPath(director.getSourceContainers());
	}
	private void setSourceLookupPath(ISourceContainer[] containers) {
		ArrayList list = new ArrayList(containers.length);
		getSourceLookupPath(list, containers);
		try {
			getCDITarget().setSourcePaths((String[]) list.toArray(new String[list.size()]));
		} catch (CDIException e) {
			PTPDebugCorePlugin.log(e);
		}
	}
	private void getSourceLookupPath(List list, ISourceContainer[] containers) {
		for (int i = 0; i < containers.length; ++i) {
			if (containers[i] instanceof ProjectSourceContainer) {
				IProject project = ((ProjectSourceContainer) containers[i]).getProject();
				if (project != null && project.exists())
					list.add(project.getLocation().toPortableString());
			}
			if (containers[i] instanceof FolderSourceContainer) {
				IContainer container = ((FolderSourceContainer) containers[i]).getContainer();
				if (container != null && container.exists())
					list.add(container.getLocation().toPortableString());
			}
			if (containers[i] instanceof PDirectorySourceContainer) {
				File dir = ((PDirectorySourceContainer) containers[i]).getDirectory();
				if (dir != null && dir.exists()) {
					IPath path = new Path(dir.getAbsolutePath());
					list.add(path.toPortableString());
				}
			}
			if (containers[i].isComposite()) {
				try {
					getSourceLookupPath(list, containers[i].getSourceContainers());
				} catch (CoreException e) {
					PTPDebugCorePlugin.log(e.getStatus());
				}
			}
		}
	}
}
