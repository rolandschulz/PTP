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
package org.eclipse.ptp.internal.debug.core.model;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
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
import org.eclipse.debug.core.sourcelookup.containers.DirectorySourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.FolderSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.ProjectSourceContainer;
import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.debug.core.IPDebugConstants;
import org.eclipse.ptp.debug.core.IPSession;
import org.eclipse.ptp.debug.core.model.IExecFileInfo;
import org.eclipse.ptp.debug.core.model.IPBreakpoint;
import org.eclipse.ptp.debug.core.model.IPDebugElement;
import org.eclipse.ptp.debug.core.model.IPDebugElementStatus;
import org.eclipse.ptp.debug.core.model.IPDebugTarget;
import org.eclipse.ptp.debug.core.model.IPGlobalVariable;
import org.eclipse.ptp.debug.core.model.IPGlobalVariableDescriptor;
import org.eclipse.ptp.debug.core.model.IPLineBreakpoint;
import org.eclipse.ptp.debug.core.model.IPPersistableRegisterGroup;
import org.eclipse.ptp.debug.core.model.IPRegisterDescriptor;
import org.eclipse.ptp.debug.core.model.IPSignal;
import org.eclipse.ptp.debug.core.model.PDebugElementState;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.IPDISessionObject;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.event.IPDIBreakpointInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDIChangedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDICreatedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIDestroyedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIDisconnectedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEndSteppingRangeInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDIErrorInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEventListener;
import org.eclipse.ptp.debug.core.pdi.event.IPDIFunctionFinishedInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDILocationReachedInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDIMemoryBlockInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDIRegisterInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDIResumedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDISharedLibraryInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDISignalInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDISuspendedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIThreadInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDIVariableInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDIWatchpointScopeInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDIWatchpointTriggerInfo;
import org.eclipse.ptp.debug.core.pdi.model.IPDITarget;
import org.eclipse.ptp.debug.core.pdi.model.IPDIThread;
import org.eclipse.ptp.debug.core.pdi.model.IPDIVariableDescriptor;
import org.eclipse.ptp.internal.debug.core.PDebugUtils;
import org.eclipse.ptp.internal.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.internal.debug.core.messages.Messages;
import org.eclipse.ptp.internal.debug.core.sourcelookup.ISourceLookupChangeListener;
import org.eclipse.ptp.internal.debug.core.sourcelookup.PSourceLookupDirector;
import org.eclipse.ptp.internal.debug.core.sourcelookup.PSourceLookupParticipant;
import org.eclipse.ptp.internal.debug.core.sourcelookup.ResourceMappingSourceContainer;

/**
 * @author Clement chu
 * 
 */
public class PDebugTarget extends PDebugElement implements IPDebugTarget, IPDIEventListener, ILaunchListener, IExpressionListener,
		ISourceLookupChangeListener {
	private final String PROCESS_NAME = Messages.PDebugTarget_0;
	private ArrayList<IThread> fThreads;
	private final IPDITarget pdiTarget;
	private final IProcess fProcess;
	private Boolean fIsLittleEndian = null;

	public PDebugTarget(IPSession session, IProcess process, IPDITarget pdiTarget, boolean allowTerminate, boolean allowDisconnect) {
		super(session, pdiTarget.getTasks());
		fProcess = process;
		this.pdiTarget = pdiTarget;
		initializePreferences();
		setThreadList(new ArrayList<IThread>(5));
		if (session.getPDISession().isTerminated(pdiTarget.getTasks())) {
			setState(PDebugElementState.TERMINATED);
		} else {
			if (session.getPDISession().isSuspended(pdiTarget.getTasks())) {
				setState(PDebugElementState.SUSPENDED);
			} else {
				setState(PDebugElementState.UNDEFINED);
			}

			initialize();
			DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
			DebugPlugin.getDefault().getExpressionManager().addExpressionListener(this);
			getPDISession().getEventManager().addEventListener(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.ITargetProperties#
	 * addPreferenceChangeListener
	 * (org.eclipse.core.runtime.preferences.IEclipsePreferences
	 * .IPreferenceChangeListener)
	 */
	public void addPreferenceChangeListener(IPreferenceChangeListener listener) {
		Preferences.addPreferenceChangeListener(PTPDebugCorePlugin.getUniqueIdentifier(), listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.model.IPDebugTarget#addRegisterGroup(java.
	 * lang.String, org.eclipse.ptp.debug.core.model.IPRegisterDescriptor[])
	 */
	public void addRegisterGroup(String name, IPRegisterDescriptor[] descriptors) {
		fSession.getRegisterManager().addRegisterGroup(getTasks(), name, descriptors);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.core.IBreakpointListener#breakpointAdded(org.eclipse
	 * .debug.core.model.IBreakpoint)
	 */
	public void breakpointAdded(IBreakpoint breakpoint) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.core.IBreakpointListener#breakpointChanged(org.eclipse
	 * .debug.core.model.IBreakpoint, org.eclipse.core.resources.IMarkerDelta)
	 */
	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.core.IBreakpointListener#breakpointRemoved(org.eclipse
	 * .debug.core.model.IBreakpoint, org.eclipse.core.resources.IMarkerDelta)
	 */
	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IDisconnect#canDisconnect()
	 */
	public boolean canDisconnect() {
		// FIXME No disconnect
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IRestart#canRestart()
	 */
	public boolean canRestart() {
		return isSuspended();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.ISuspendResume#canResume()
	 */
	public boolean canResume() {
		return isSuspended();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.model.IResumeWithoutSignal#canResumeWithoutSignal
	 * ()
	 */
	public boolean canResumeWithoutSignal() {
		// Check if the configuration supports this!!!
		return (canResume() && getCurrentStateInfo() instanceof IPDISignalInfo);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.ISuspendResume#canSuspend()
	 */
	public boolean canSuspend() {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.ITerminate#canTerminate()
	 */
	public boolean canTerminate() {
		return isAvailable();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.model.IPDebugTarget#createGlobalVariable(org
	 * .eclipse.ptp.debug.core.model.IPGlobalVariableDescriptor)
	 */
	public IPGlobalVariable createGlobalVariable(IPGlobalVariableDescriptor info) throws DebugException {
		IPDIVariableDescriptor vo = null;
		try {
			vo = getPDITarget().getGlobalVariableDescriptors(info.getPath().lastSegment(), null, info.getName());
		} catch (PDIException e) {
			throw new DebugException(new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(),
					DebugException.TARGET_REQUEST_FAILED, (vo != null) ? vo.getName() + ": " + e.getMessage() : e.getMessage(), //$NON-NLS-1$
					null));
		}
		return PVariableFactory.createGlobalVariable(this, info, vo);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IDisconnect#disconnect()
	 */
	public void disconnect() throws DebugException {
		targetRequestFailed(Messages.PDebugTarget_1, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IPDebugTarget#dispose()
	 */
	public void dispose() {
		cleanup();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.ISteppingModeTarget#
	 * enableInstructionStepping(boolean)
	 */
	public void enableInstructionStepping(boolean enabled) {
		Preferences.setBoolean(PTPDebugCorePlugin.getUniqueIdentifier(), PREF_INSTRUCTION_STEPPING_MODE, enabled);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.core.IExpressionListener#expressionAdded(org.eclipse
	 * .debug.core.model.IExpression)
	 */
	public void expressionAdded(IExpression expression) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.core.IExpressionListener#expressionChanged(org.eclipse
	 * .debug.core.model.IExpression)
	 */
	public void expressionChanged(IExpression expression) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.core.IExpressionListener#expressionRemoved(org.eclipse
	 * .debug.core.model.IExpression)
	 */
	public void expressionRemoved(IExpression expression) {
		if (expression instanceof PExpression && expression.getDebugTarget().equals(this)) {
			((PExpression) expression).dispose();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.model.PDebugElement#getAdapter(java
	 * .lang.Class)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IPDebugElement.class))
			return this;
		if (adapter.equals(IDebugTarget.class))
			return this;
		if (adapter.equals(IPDebugTarget.class))
			return this;
		if (adapter.equals(IPDITarget.class))
			return pdiTarget;
		if (adapter.equals(IExecFileInfo.class))
			return this;
		if (adapter.equals(IPDISession.class))
			return getPDISession();
		if (adapter.equals(IMemoryBlockRetrievalExtension.class))
			return fSession.getMemoryManager().getMemoryRetrieval(getTasks());
		if (adapter.equals(IMemoryBlockRetrieval.class))
			return fSession.getMemoryManager().getMemoryRetrieval(getTasks());
		return super.getAdapter(adapter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.model.IPDebugTarget#getBreakpointAddress(org
	 * .eclipse.ptp.debug.core.model.IPLineBreakpoint)
	 */
	public BigInteger getBreakpointAddress(IPLineBreakpoint breakpoint) throws DebugException {
		return (fSession.getBreakpointManager() != null) ? fSession.getBreakpointManager().getBreakpointAddress(breakpoint)
				: new BigInteger("0"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.model.PDebugElement#getDebugTarget()
	 */
	@Override
	public PDebugTarget getDebugTarget() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IExecFileInfo#getGlobals()
	 */
	public IPGlobalVariableDescriptor[] getGlobals() throws DebugException {
		targetRequestFailed(Messages.PDebugTarget_2, null);
		return new IPGlobalVariableDescriptor[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.core.model.IMemoryBlockRetrieval#getMemoryBlock(long,
	 * long)
	 */
	public IMemoryBlock getMemoryBlock(long startAddress, long length) throws DebugException {
		return fSession.getMemoryManager().getMemoryBlock(getTasks(), startAddress, length);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IDebugTarget#getName()
	 */
	public String getName() throws DebugException {
		return PROCESS_NAME + getID();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.model.PDebugElement#getPDITarget()
	 */
	@Override
	public IPDITarget getPDITarget() {
		return pdiTarget;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IDebugTarget#getProcess()
	 */
	public IProcess getProcess() {
		return fProcess;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.model.IPDebugTarget#getRegisterDescriptors()
	 */
	public IPRegisterDescriptor[] getRegisterDescriptors() throws DebugException {
		return fSession.getRegisterManager().getAllRegisterDescriptors(getTasks());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IPDebugTarget#getSignals()
	 */
	public IPSignal[] getSignals() throws DebugException {
		return fSession.getSignalManager().getSignals(getTasks());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IDebugTarget#getThreads()
	 */
	public IThread[] getThreads() {
		List<IThread> threads = getThreadList();
		return threads.toArray(new IThread[threads.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.event.IPDIEventListener#handleDebugEvents
	 * (org.eclipse.ptp.debug.core.pdi.event.IPDIEvent[])
	 */
	public void handleDebugEvents(IPDIEvent[] events) {
		for (IPDIEvent event : events) {
			if (!event.contains(getTasks()))
				continue;

			if (event instanceof IPDIDestroyedEvent) {
				handleTerminatedEvent((IPDIDestroyedEvent) event);
			} else if (event instanceof IPDICreatedEvent) {
				handleCreatedEvent((IPDICreatedEvent) event);
			} else if (event instanceof IPDISuspendedEvent) {
				handleSuspendedEvent((IPDISuspendedEvent) event);
			} else if (event instanceof IPDIResumedEvent) {
				handleResumedEvent((IPDIResumedEvent) event);
			} else if (event instanceof IPDIDisconnectedEvent) {
				handleDisconnectedEvent((IPDIDisconnectedEvent) event);
			} else if (event instanceof IPDIChangedEvent) {
				handleChangedEvent((IPDIChangedEvent) event);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IPDebugTarget#hasSignals()
	 */
	public boolean hasSignals() throws DebugException {
		return (fSession.getSignalManager().getSignals(getTasks()).length > 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IDebugTarget#hasThreads()
	 */
	public boolean hasThreads() throws DebugException {
		return getThreadList().size() > 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IDisconnect#isDisconnected()
	 */
	public boolean isDisconnected() {
		return (getState().equals(PDebugElementState.DISCONNECTED));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.ISteppingModeTarget#
	 * isInstructionSteppingEnabled()
	 */
	public boolean isInstructionSteppingEnabled() {
		return Preferences.getBoolean(PTPDebugCorePlugin.getUniqueIdentifier(), PREF_INSTRUCTION_STEPPING_MODE)
				|| Preferences.getBoolean(PTPDebugCorePlugin.getUniqueIdentifier(), IPDebugConstants.PREF_INSTRUCTION_STEP_MODE_ON);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IPDebugTarget#isLittleEndian()
	 */
	public boolean isLittleEndian() {
		// TODO always true
		if (fIsLittleEndian == null) {
			fIsLittleEndian = Boolean.TRUE;
		}
		return fIsLittleEndian.booleanValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IPDebugTarget#isPostMortem()
	 */
	public boolean isPostMortem() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.ISuspendResume#isSuspended()
	 */
	public boolean isSuspended() {
		return (getState().equals(PDebugElementState.SUSPENDED));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.ITerminate#isTerminated()
	 */
	public boolean isTerminated() {
		return (getState().equals(PDebugElementState.TERMINATED));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.core.ILaunchListener#launchAdded(org.eclipse.debug.
	 * core.ILaunch)
	 */
	public void launchAdded(ILaunch launch) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.core.ILaunchListener#launchChanged(org.eclipse.debug
	 * .core.ILaunch)
	 */
	public void launchChanged(ILaunch launch) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.core.ILaunchListener#launchRemoved(org.eclipse.debug
	 * .core.ILaunch)
	 */
	public void launchRemoved(ILaunch launch) {
		if (!isAvailable()) {
			return;
		}
		if (launch.equals(getLaunch())) {
			disconnected();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.model.IPDebugTarget#modifyRegisterGroup(org
	 * .eclipse.ptp.debug.core.model.IPPersistableRegisterGroup,
	 * org.eclipse.ptp.debug.core.model.IPRegisterDescriptor[])
	 */
	public void modifyRegisterGroup(IPPersistableRegisterGroup group, IPRegisterDescriptor[] descriptors) {
		fSession.getRegisterManager().modifyRegisterGroup(getTasks(), group, descriptors);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.ITargetProperties#
	 * removePreferenceChangeListener
	 * (org.eclipse.core.runtime.preferences.IEclipsePreferences
	 * .IPreferenceChangeListener)
	 */
	public void removePreferenceChangeListener(IPreferenceChangeListener listener) {
		Preferences.removePreferenceChangeListener(PTPDebugCorePlugin.getUniqueIdentifier(), listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.model.IPDebugTarget#removeRegisterGroups(org
	 * .eclipse.debug.core.model.IRegisterGroup[])
	 */
	public void removeRegisterGroups(IRegisterGroup[] groups) {
		fSession.getRegisterManager().removeRegisterGroups(getTasks(), groups);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IRestart#restart()
	 */
	public void restart() throws DebugException {
		targetRequestFailed(Messages.PDebugTarget_3, null);
		if (!canRestart()) {
			return;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.model.IPDebugTarget#restoreDefaultRegisterGroups
	 * ()
	 */
	public void restoreDefaultRegisterGroups() {
		fSession.getRegisterManager().restoreDefaults(getTasks());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.ISuspendResume#resume()
	 */
	public void resume() throws DebugException {
		if (!canResume()) {
			return;
		}
		changeState(PDebugElementState.RESUMING);
		try {
			getPDISession().resume(getTasks(), false);
		} catch (PDIException e) {
			restoreOldState();
			targetRequestFailed(e.getMessage(), null);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.model.IResumeWithoutSignal#resumeWithoutSignal
	 * ()
	 */
	public void resumeWithoutSignal() throws DebugException {
		if (!canResume())
			return;
		changeState(PDebugElementState.RESUMING);
		try {
			getPDISession().resume(getTasks(), false);
		} catch (PDIException e) {
			restoreOldState();
			targetRequestFailed(e.getMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.sourcelookup.ISourceLookupChangeListener#
	 * sourceContainersChanged
	 * (org.eclipse.debug.core.sourcelookup.ISourceLookupDirector)
	 */
	public void sourceContainersChanged(ISourceLookupDirector director) {
		setSourceLookupPath(director.getSourceContainers());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.core.model.IDebugTarget#supportsBreakpoint(org.eclipse
	 * .debug.core.model.IBreakpoint)
	 */
	public boolean supportsBreakpoint(IBreakpoint breakpoint) {
		return (breakpoint instanceof IPBreakpoint);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.ISteppingModeTarget#
	 * supportsInstructionStepping()
	 */
	public boolean supportsInstructionStepping() {
		// TODO Not implement yet
		// return getConfiguration().supportsInstructionStepping();
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.core.model.IMemoryBlockRetrieval#supportsStorageRetrieval
	 * ()
	 */
	public boolean supportsStorageRetrieval() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.ISuspendResume#suspend()
	 */
	public void suspend() throws DebugException {
		if (!canSuspend()) {
			return;
		}
		changeState(PDebugElementState.SUSPENDING);
		try {
			getPDISession().suspend(getTasks());
		} catch (PDIException e) {
			restoreOldState();
			targetRequestFailed(e.getMessage(), null);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.ITerminate#terminate()
	 */
	public void terminate() throws DebugException {
		if (!canTerminate()) {
			return;
		}
		changeState(PDebugElementState.TERMINATING);
		try {
			getPDISession().terminate(getTasks());
		} catch (PDIException e) {
			restoreOldState();
			targetRequestFailed(e.getMessage(), null);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String result = ""; //$NON-NLS-1$
		try {
			result = getName();
		} catch (DebugException e) {
		}
		return result;
	}

	/**
	 * @param state
	 */
	private void changeState(PDebugElementState state) {
		setState(state);
		Iterator<IThread> it = getThreadList().iterator();
		while (it.hasNext()) {
			((PThread) it.next()).setState(state);
		}
	}

	/**
	 * 
	 */
	private void cleanup() {
		resetStatus();
		removeAllThreads();
		getPDISession().getEventManager().removeEventListener(this);
		DebugPlugin.getDefault().getExpressionManager().removeExpressionListener(this);
		DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
		disposeSignalManager();
		disposeRegisterManager();
		disposeMemoryManager();
		// disposeSourceManager();
		disposeSourceLookupPath();
		removeAllExpressions();
	}

	/**
	 * @param pdiThread
	 * @return
	 */
	private PThread findThread(IPDIThread pdiThread) {
		List<IThread> threads = getThreadList();
		for (int i = 0; i < threads.size(); i++) {
			PThread t = (PThread) threads.get(i);
			if (t.getPDIThread().equals(pdiThread))
				return t;
		}
		return null;
	}

	/**
	 * @param threads
	 * @param pdiThread
	 * @return
	 */
	private PThread findThread(List<IThread> threads, IPDIThread pdiThread) {
		for (int i = 0; i < threads.size(); i++) {
			PThread t = (PThread) threads.get(i);
			if (t.getPDIThread().equals(pdiThread))
				return t;
		}
		return null;
	}

	/**
	 * @param list
	 * @param containers
	 */
	private void getSourceLookupPath(List<String> list, ISourceContainer[] containers) {
		for (int i = 0; i < containers.length; ++i) {
			if (containers[i] instanceof ProjectSourceContainer) {
				IProject project = ((ProjectSourceContainer) containers[i]).getProject();
				if (project != null && project.exists())
					list.add(project.getLocationURI().getPath());
			}
			if (containers[i] instanceof FolderSourceContainer) {
				IContainer container = ((FolderSourceContainer) containers[i]).getContainer();
				if (container != null && container.exists())
					list.add(container.getLocationURI().getPath());
			}
			if (containers[i] instanceof DirectorySourceContainer) {
				File dir = ((DirectorySourceContainer) containers[i]).getDirectory();
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

	/**
	 * @param event
	 */
	private void handleChangedEvent(IPDIChangedEvent event) {
		IPDISessionObject reason = event.getReason();
		if (reason instanceof IPDISignalInfo) {
			fSession.getSignalManager().signalChanged(getTasks(), ((IPDISignalInfo) reason).getSignal());
		} else if (reason instanceof IPDIBreakpointInfo) {
		} else if (reason instanceof IPDIMemoryBlockInfo) {
		} else if (reason instanceof IPDIVariableInfo) {
		}
	}

	/**
	 * @param event
	 */
	private void handleCreatedEvent(IPDICreatedEvent event) {
		IPDISessionObject reason = event.getReason();
		if (reason instanceof IPDIBreakpointInfo) {

		} else if (reason instanceof IPDIThreadInfo) {
			IPDIThread pdiThread = ((IPDIThreadInfo) reason).getThread();
			PThread thread = findThread(pdiThread);
			if (thread == null) {
				thread = createThread(pdiThread);
				thread.fireCreationEvent();
			}
		} else if (reason instanceof IPDISharedLibraryInfo) {
			// fSession.getModuleManager().sharedLibraryLoaded(((IPDISharedLibraryInfo)reason).getSharedLibrary());
		} else if (reason instanceof IPDIMemoryBlockInfo) {

		} else if (reason instanceof IPDIRegisterInfo) {

		} else if (reason instanceof IPDIVariableInfo) {

		}
	}

	/**
	 * @param event
	 */
	private void handleDisconnectedEvent(IPDIDisconnectedEvent event) {
		disconnected();
	}

	/**
	 * @param event
	 */
	private void handleResumedEvent(IPDIResumedEvent event) {
		setState(PDebugElementState.RESUMED);
		setCurrentStateInfo(null);
		resetStatus();
		ArrayList<DebugEvent> debugEvents = new ArrayList<DebugEvent>(10);
		int detail = DebugEvent.UNSPECIFIED;
		switch (event.getType()) {
		case IPDIResumedEvent.CONTINUE:
			detail = DebugEvent.CLIENT_REQUEST;
			break;
		case IPDIResumedEvent.STEP_INTO:
		case IPDIResumedEvent.STEP_INTO_INSTRUCTION:
			detail = DebugEvent.STEP_INTO;
			break;
		case IPDIResumedEvent.STEP_OVER:
		case IPDIResumedEvent.STEP_OVER_INSTRUCTION:
			detail = DebugEvent.STEP_OVER;
			break;
		case IPDIResumedEvent.STEP_RETURN:
			detail = DebugEvent.STEP_RETURN;
			break;
		}
		debugEvents.add(createResumeEvent(detail));
		resumeThreads(debugEvents, detail);
		fireEventSet(debugEvents.toArray(new DebugEvent[debugEvents.size()]));
	}

	/**
	 * @param event
	 */
	private void handleSuspendedEvent(IPDISuspendedEvent event) {
		setState(PDebugElementState.SUSPENDED);
		IPDISessionObject reason = event.getReason();
		setCurrentStateInfo(reason);
		fSession.getRegisterManager().targetSuspended(getTasks());
		fSession.getBreakpointManager().skipBreakpoints(false);
		refreshThreads();
		suspendThreads(event);
		if (reason instanceof IPDIEndSteppingRangeInfo) {
			fireSuspendEvent(DebugEvent.UNSPECIFIED);
		} else if (reason instanceof IPDIThreadInfo) {
			IPDIThread pdiThread = ((IPDIThreadInfo) reason).getThread();
			PThread thread = findThread(pdiThread);
			if (thread != null) {
				getThreadList().remove(thread);
				thread.handleDebugEvents(new IPDIEvent[] { event });
			}
		} else if (reason instanceof IPDIBreakpointInfo) {
			fireSuspendEvent(DebugEvent.BREAKPOINT);
		} else if (reason instanceof IPDISignalInfo) {
			fireSuspendEvent(DebugEvent.CLIENT_REQUEST);
		} else if (reason instanceof IPDIWatchpointTriggerInfo) {
			fireSuspendEvent(DebugEvent.BREAKPOINT);
		} else if (reason instanceof IPDIWatchpointScopeInfo) {
			fSession.getBreakpointManager().watchpointOutOfScope(getTasks(), ((IPDIWatchpointScopeInfo) reason).getWatchpoint());
			fireSuspendEvent(DebugEvent.BREAKPOINT);
		} else if (reason instanceof IPDISharedLibraryInfo) {
			fireSuspendEvent(DebugEvent.UNSPECIFIED);
		} else if (reason instanceof IPDIFunctionFinishedInfo) {

		} else if (reason instanceof IPDILocationReachedInfo) {

		} else {
			fireSuspendEvent(DebugEvent.UNSPECIFIED);
		}
	}

	/**
	 * @param event
	 */
	private void handleTerminatedEvent(IPDIDestroyedEvent event) {
		IPDISessionObject reason = event.getReason();
		if (reason instanceof IPDIBreakpointInfo) {

		} else if (reason instanceof IPDIThreadInfo) {
			IPDIThread pdiThread = ((IPDIThreadInfo) reason).getThread();
			PThread thread = findThread(pdiThread);
			if (thread != null) {
				getThreadList().remove(thread);
				thread.terminated();
				thread.fireTerminateEvent();
			}
		} else if (reason instanceof IPDIErrorInfo) {
			IPDIErrorInfo info = (IPDIErrorInfo) reason;
			setStatus(IPDebugElementStatus.ERROR, info.getMessage());
			MultiStatus status = new MultiStatus(PTPDebugCorePlugin.getUniqueIdentifier(), IPDebugConstants.STATUS_CODE_ERROR,
					Messages.PDebugTarget_4, null);
			StringTokenizer st = new StringTokenizer(info.getDetailMessage(), "\n\r"); //$NON-NLS-1$
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				if (token.length() > 200) {
					token = token.substring(0, 200);
				}
				status.add(new Status(IStatus.ERROR, status.getPlugin(), IPDebugConstants.STATUS_CODE_ERROR, token, null));
			}
			PDebugUtils.error(status, this);
			fireTerminateEvent();
		} else if (reason instanceof IPDISharedLibraryInfo) {
			// fSession.getModuleManager().sharedLibraryUnloaded(((IPDISharedLibraryInfo)reason).getSharedLibrary());
		} else if (reason instanceof IPDISignalInfo) {
			removeAllThreads();
			setState(PDebugElementState.EXITED);
			setCurrentStateInfo(reason);
			fireChangeEvent(DebugEvent.CONTENT);
			terminated();
		} else if (reason instanceof IPDIVariableInfo) {

		}
	}

	/**
	 * 
	 */
	private void initializePreferences() {
		Preferences.setDefaultBoolean(PTPDebugCorePlugin.getUniqueIdentifier(), PREF_INSTRUCTION_STEPPING_MODE,
				IPDebugConstants.DEFAULT_INSTRUCTION_STEP_MODE);
	}

	/**
	 * @return
	 */
	private boolean isAvailable() {
		return !(isTerminated() || isTerminating() || isDisconnected() || isDisconnecting());
	}

	/**
	 * @param containers
	 */
	private void setSourceLookupPath(ISourceContainer[] containers) {
		ArrayList<String> list = new ArrayList<String>(containers.length);
		getSourceLookupPath(list, containers);
		try {
			getPDITarget().setSourcePaths(list.toArray(new String[list.size()]));
		} catch (PDIException e) {
			PTPDebugCorePlugin.log(e);
		}
	}

	/**
	 * @param threads
	 */
	private void setThreadList(ArrayList<IThread> threads) {
		fThreads = threads;
	}

	/**
	 * 
	 */
	private void terminated() {
		if (!isTerminated()) {
			if (!isDisconnected()) {
				setState(PDebugElementState.TERMINATED);
			}
			cleanup();
			fireTerminateEvent();
		}
	}

	/**
	 * @param pdiThread
	 * @return
	 */
	protected PThread createThread(IPDIThread pdiThread) {
		PThread thread = new PThread(this, pdiThread);
		getThreadList().add(thread);
		return thread;
	}

	/**
	 * 
	 */
	protected void disconnected() {
		if (!isDisconnected()) {
			setState(PDebugElementState.DISCONNECTED);
			cleanup();
			fireTerminateEvent();
		}
	}

	/**
	 * 
	 */
	protected void disposeMemoryManager() {
		fSession.getMemoryManager().dispose(getTasks());
	}

	/**
	 * 
	 */
	protected void disposeRegisterManager() {
		fSession.getMemoryManager().dispose(getTasks());
	}

	/**
	 * 
	 */
	protected void disposeSignalManager() {
		fSession.getSignalManager().dispose(getTasks());
	}

	/**
	 * 
	 */
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

	/**
	 * @return
	 * @throws DebugException
	 */
	protected IThread getCurrentThread() throws DebugException {
		IThread[] threads = getThreads();
		for (int i = 0; i < threads.length; ++i) {
			if (((PThread) threads[i]).isCurrent())
				return threads[i];
		}
		return null;
	}

	/**
	 * @return
	 */
	protected ArrayList<IThread> getThreadList() {
		return fThreads;
	}

	/**
	 * 
	 */
	protected void initialize() {
		initializeSourceLookupPath();
		ArrayList<DebugEvent> debugEvents = new ArrayList<DebugEvent>(1);
		debugEvents.add(createCreateEvent());
		initializeThreads(debugEvents);
		initializeRegisters();
		initializeMemoryBlocks();
		fireEventSet(debugEvents.toArray(new DebugEvent[debugEvents.size()]));
	}

	/**
	 * 
	 */
	protected void initializeMemoryBlocks() {
		fSession.getMemoryManager().initialize(getTasks(), this);
	}

	/**
	 * 
	 */
	protected void initializeRegisters() {
		fSession.getRegisterManager().initialize(getTasks(), this);
	}

	/**
	 * 
	 */
	protected void initializeSourceLookupPath() {
		ISourceLocator locator = getLaunch().getSourceLocator();
		if (locator instanceof PSourceLookupDirector) {
			PSourceLookupDirector director = (PSourceLookupDirector) locator;
			ISourceContainer[] sc = director.getSourceContainers();
			List<ISourceContainer> list = new ArrayList<ISourceContainer>(Arrays.asList(sc));
			IPath backend = new Path(getSession().getProject().getLocationURI().getPath());
			ResourceMappingSourceContainer container = new ResourceMappingSourceContainer();
			container.setPath(backend);
			container.setContainer(getSession().getProject());
			list.add(container);
			director.setSourceContainers(list.toArray(new ISourceContainer[0]));
		}
	}

	/**
	 * @param debugEvents
	 */
	protected void initializeThreads(List<DebugEvent> debugEvents) {
		IPDIThread[] pdiThreads = new IPDIThread[0];
		try {
			if (isSuspended())
				pdiThreads = getPDITarget().getThreads();
		} catch (PDIException e) {
			// ignore
		}
		DebugEvent suspendEvent = null;
		for (int i = 0; i < pdiThreads.length; ++i) {
			PThread thread = createThread(pdiThreads[i]);
			debugEvents.add(thread.createCreateEvent());
			try {
				if (pdiThreads[i].equals(getPDITarget().getCurrentThread()) && thread.isSuspended()) {
					// Use BREAKPOINT as a detail to force perspective switch
					suspendEvent = thread.createSuspendEvent(DebugEvent.BREAKPOINT);
				}
			} catch (PDIException e) {
				// ignore
			}
		}
		if (suspendEvent != null) {
			debugEvents.add(suspendEvent);
		}
	}

	/**
	 * @return
	 */
	protected boolean isDisconnecting() {
		return (getState().equals(PDebugElementState.DISCONNECTING));
	}

	/**
	 * @return
	 */
	protected boolean isSuspending() {
		return (getState().equals(PDebugElementState.SUSPENDING));
	}

	/**
	 * @return
	 */
	protected boolean isTerminating() {
		return (getState().equals(PDebugElementState.TERMINATING));
	}

	/**
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected synchronized List<IThread> refreshThreads() {
		ArrayList<IThread> newThreads = new ArrayList<IThread>(5);
		ArrayList<IThread> list = new ArrayList<IThread>(5);
		ArrayList<DebugEvent> debugEvents = new ArrayList<DebugEvent>(5);
		List<IThread> oldList = (List<IThread>) getThreadList().clone();
		IPDIThread[] pdiThreads = new IPDIThread[0];
		IPDIThread currentPDIThread = null;
		try {
			pdiThreads = getPDITarget().getThreads();
			currentPDIThread = getPDITarget().getCurrentThread();
		} catch (PDIException e) {
		}
		for (int i = 0; i < pdiThreads.length; ++i) {
			PThread thread = findThread(oldList, pdiThreads[i]);
			if (thread == null) {
				thread = new PThread(this, pdiThreads[i]);
				newThreads.add(thread);
			} else {
				oldList.remove(thread);
			}
			thread.setCurrent(pdiThreads[i].equals(currentPDIThread));
			list.add(thread);
		}
		Iterator<IThread> it = oldList.iterator();
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
			fireEventSet(debugEvents.toArray(new DebugEvent[debugEvents.size()]));
		return newThreads;
	}

	/**
	 * 
	 */
	protected void removeAllExpressions() {
		IExpressionManager em = DebugPlugin.getDefault().getExpressionManager();
		IExpression[] expressions = em.getExpressions();
		for (int i = 0; i < expressions.length; ++i) {
			if (expressions[i] instanceof PExpression && expressions[i].getDebugTarget().equals(this)) {
				em.removeExpression(expressions[i]);
			}
		}
	}

	/**
	 * 
	 */
	protected void removeAllThreads() {
		List<IThread> threads = getThreadList();
		setThreadList(new ArrayList<IThread>(0));
		ArrayList<DebugEvent> debugEvents = new ArrayList<DebugEvent>(threads.size());
		Iterator<IThread> it = threads.iterator();
		while (it.hasNext()) {
			PThread thread = (PThread) it.next();
			thread.terminated();
			debugEvents.add(thread.createTerminateEvent());
		}
		fireEventSet(debugEvents.toArray(new DebugEvent[debugEvents.size()]));
	}

	/**
	 * 
	 */
	protected void restoreOldState() {
		restoreState();
		Iterator<IThread> it = getThreadList().iterator();
		while (it.hasNext()) {
			((PThread) it.next()).restoreState();
		}
	}

	/**
	 * @param debugEvents
	 * @param detail
	 */
	protected synchronized void resumeThreads(List<DebugEvent> debugEvents, int detail) {
		IThread[] threads = getThreadList().toArray(new IThread[0]);
		for (IThread thread : threads) {
			((PThread) thread).resumedByTarget(detail, debugEvents);
		}
	}

	/**
	 * 
	 */
	protected void saveMemoryBlocks() {
		fSession.getMemoryManager().save(getTasks());
	}

	/**
	 * @param event
	 */
	protected void suspendThreads(IPDISuspendedEvent event) {
		Iterator<IThread> it = getThreadList().iterator();
		while (it.hasNext()) {
			PThread thread = (PThread) it.next();
			IPDIThread suspensionThread = null;
			try {
				suspensionThread = getPDITarget().getCurrentThread();
			} catch (PDIException e) {
				// ignore
			}
			thread.suspendByTarget(event.getReason(), suspensionThread);
		}
	}
}
