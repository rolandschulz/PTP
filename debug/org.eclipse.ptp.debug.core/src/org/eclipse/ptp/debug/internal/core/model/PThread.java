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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.model.IJumpToAddress;
import org.eclipse.cdt.debug.core.model.IJumpToLine;
import org.eclipse.cdt.debug.core.model.IRestart;
import org.eclipse.cdt.debug.core.model.IRunToAddress;
import org.eclipse.cdt.debug.core.model.IRunToLine;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.ptp.debug.core.cdi.IPCDIBreakpointHit;
import org.eclipse.ptp.debug.core.cdi.IPCDIBreakpointManager;
import org.eclipse.ptp.debug.core.cdi.IPCDIEndSteppingRange;
import org.eclipse.ptp.debug.core.cdi.IPCDISessionObject;
import org.eclipse.ptp.debug.core.cdi.IPCDISignalReceived;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIChangedEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIDestroyedEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIDisconnectedEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIEventListener;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIResumedEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDISuspendedEvent;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIBreakpoint;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIObject;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIStackFrame;
import org.eclipse.ptp.debug.core.cdi.model.IPCDITargetConfiguration;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIThread;
import org.eclipse.ptp.debug.core.model.IPDebugElementStatus;
import org.eclipse.ptp.debug.core.model.IPDummyStackFrame;
import org.eclipse.ptp.debug.core.model.IPStackFrame;
import org.eclipse.ptp.debug.core.model.IPThread;
import org.eclipse.ptp.debug.core.model.IResumeWithoutSignal;
import org.eclipse.ptp.debug.core.model.PDebugElementState;

/**
 * @author Clement chu
 *
 */
public class PThread extends PDebugElement implements IPThread, IRestart, IResumeWithoutSignal, IPCDIEventListener {
	private final static int MAX_STACK_DEPTH = 100;
	private IPCDIThread fCDIThread;
	private ArrayList fStackFrames;
	private boolean fRefreshChildren = true;
	private IPCDITargetConfiguration fConfig;
	private boolean fIsCurrent = false;
	private int fLastStackDepth = 0;
	private boolean fDisposed = false;

	public PThread(PDebugTarget target, IPCDIThread cdiThread) {
		super(target);
		setState(cdiThread.isSuspended() ? PDebugElementState.SUSPENDED : PDebugElementState.RESUMED);
		setCDIThread(cdiThread);
		fConfig = (IPCDITargetConfiguration)getCDITarget().getConfiguration();
		initialize();
		getCDISession().getEventManager().addEventListener(this);
	}
	protected void initialize() {
		fStackFrames = new ArrayList();
	}
	public IStackFrame[] getStackFrames() throws DebugException {
		List list = Collections.EMPTY_LIST;
		try {
			list = computeStackFrames();
		} catch (DebugException e) {
			setStatus(IPDebugElementStatus.ERROR, e.getStatus().getMessage());
			throw e;
		}
		return (IStackFrame[]) list.toArray(new IStackFrame[list.size()]);
	}
	public boolean hasStackFrames() throws DebugException {
		// Always return true to postpone the stack frames request
		return true;
	}
	protected synchronized List computeStackFrames(boolean refreshChildren) throws DebugException {
		if (isSuspended()) {
			if (isTerminated()) {
				fStackFrames = new ArrayList();
			} else if (refreshChildren) {
				if (fStackFrames.size() > 0) {
					Object frame = fStackFrames.get(fStackFrames.size() - 1);
					if (frame instanceof IPDummyStackFrame) {
						fStackFrames.remove(frame);
					}
				}
				int depth = getStackDepth();
				IPCDIStackFrame[] frames = (depth != 0) ? getCDIStackFrames(0, (depth > getMaxStackDepth()) ? getMaxStackDepth() : depth) : new IPCDIStackFrame[0];
				if (fStackFrames.isEmpty()) {
					if (frames.length > 0) {
						addStackFrames(frames, 0, frames.length);
					}
				} else if (depth < getLastStackDepth()) {
					disposeStackFrames(0, getLastStackDepth() - depth);
					if (frames.length > 0) {
						updateStackFrames(frames, 0, fStackFrames, fStackFrames.size());
						if (fStackFrames.size() < frames.length) {
							addStackFrames(frames, fStackFrames.size(), frames.length - fStackFrames.size());
						}
					}
				} else if (depth > getLastStackDepth()) {
					disposeStackFrames(frames.length - depth + getLastStackDepth(), depth - getLastStackDepth());
					addStackFrames(frames, 0, depth - getLastStackDepth());
					updateStackFrames(frames, depth - getLastStackDepth(), fStackFrames, frames.length - depth + getLastStackDepth());
				} else { // depth == getLastStackDepth()
					if (depth != 0) {
						// same number of frames - if top frames are in different
						// function, replace all frames
						IPCDIStackFrame newTopFrame = (frames.length > 0) ? frames[0] : null;
						IPCDIStackFrame oldTopFrame = (fStackFrames.size() > 0) ? ((PStackFrame) fStackFrames.get(0)).getLastCDIStackFrame() : null;
						if (!PStackFrame.equalFrame(newTopFrame, oldTopFrame)) {
							disposeStackFrames(0, fStackFrames.size());
							addStackFrames(frames, 0, frames.length);
						} else // we are in the same frame
						{
							updateStackFrames(frames, 0, fStackFrames, frames.length);
						}
					}
				}
				if (depth > getMaxStackDepth()) {
					fStackFrames.add(new PDummyStackFrame(this));
				}
				setLastStackDepth(depth);
				setRefreshChildren(false);
			}
		}
		return fStackFrames;
	}
	protected IPCDIStackFrame[] getCDIStackFrames() throws DebugException {
		return new IPCDIStackFrame[0];
	}
	protected IPCDIStackFrame[] getCDIStackFrames(int lowFrame, int highFrame) throws DebugException {
		try {
			return getCDIThread().getStackFrames(lowFrame, highFrame);
		} catch (PCDIException e) {
			setStatus(IPDebugElementStatus.WARNING, MessageFormat.format(CoreModelMessages.getString("PThread.0"), new String[] { e.getMessage() }));
			targetRequestFailed(e.getMessage(), null);
		}
		return new IPCDIStackFrame[0];
	}
	protected void updateStackFrames(IPCDIStackFrame[] newFrames, int offset, List oldFrames, int length) throws DebugException {
		for (int i = 0; i < length; i++) {
			PStackFrame frame = (PStackFrame) oldFrames.get(offset);
			frame.setCDIStackFrame(newFrames[offset]);
			offset++;
		}
	}
	protected void addStackFrames(IPCDIStackFrame[] newFrames, int startIndex, int length) {
		if (newFrames.length >= startIndex + length) {
			for (int i = 0; i < length; ++i) {
				fStackFrames.add(i, new PStackFrame(this, newFrames[startIndex + i]));
			}
		}
	}
	public List computeStackFrames() throws DebugException {
		return computeStackFrames(refreshChildren());
	}
	public List computeNewStackFrames() throws DebugException {
		return computeStackFrames(true);
	}
	protected List createAllStackFrames(int depth, IPCDIStackFrame[] frames) throws DebugException {
		List list = new ArrayList(frames.length);
		for (int i = 0; i < frames.length; ++i) {
			list.add(new PStackFrame(this, frames[i]));
		}
		if (depth > frames.length) {
			list.add(new PDummyStackFrame(this));
		}
		return list;
	}
	public int getPriority() throws DebugException {
		return 0;
	}
	public IStackFrame getTopStackFrame() throws DebugException {
		List c = computeStackFrames();
		return (c.isEmpty()) ? null : (IStackFrame) c.get(0);
	}
	public String getName() throws DebugException {
		return getCDIThread().toString();
	}
	public IBreakpoint[] getBreakpoints() {
		List list = new ArrayList(1);
		if (isSuspended()) {
			IBreakpoint bkpt = null;
			IPCDIBreakpointManager bManager = getCDISession().getBreakpointManager();
			 if (getCurrentStateInfo() instanceof IPCDIBreakpointHit) {
				 bkpt = bManager.findBreakpoint(((IPCDIBreakpointHit)getCurrentStateInfo()).getBreakpoint()); 				 
			 }
			 //FIXME
			 //else if (getCurrentStateInfo() instanceof IPCDIWatchpointTrigger) {
			 //bkpt = bManager.getBreakpoint(((ICDIWatchpointTrigger)getCurrentStateInfo()).getWatchpoint());
			 //}
			 if (bkpt != null)
				list.add(bkpt);
		}
		return (IBreakpoint[]) list.toArray(new IBreakpoint[list.size()]);
	}
	public void handleDebugEvents(IPCDIEvent[] events) {
		if (isDisposed())
			return;
		for (int i = 0; i < events.length; i++) {
			IPCDIEvent event = events[i];
			if (!event.containTask(getCDITarget().getTargetID()))
				return;
			IPCDIObject source = event.getSource(getCDITarget().getTargetID());
			if (source instanceof IPCDIThread && source.equals(getCDIThread())) {
				if (event instanceof IPCDISuspendedEvent) {
					handleSuspendedEvent((IPCDISuspendedEvent) event);
				} else if (event instanceof IPCDIResumedEvent) {
					handleResumedEvent((IPCDIResumedEvent) event);
				} else if (event instanceof IPCDIDestroyedEvent) {
					handleTerminatedEvent((IPCDIDestroyedEvent) event);
				} else if (event instanceof IPCDIDisconnectedEvent) {
					handleDisconnectedEvent((IPCDIDisconnectedEvent) event);
				} else if (event instanceof IPCDIChangedEvent) {
					handleChangedEvent((IPCDIChangedEvent) event);
				}
			}
		}
	}
	public boolean canResume() {
		return (fConfig.supportsResume() && isSuspended());
	}
	public boolean canSuspend() {
		PDebugElementState state = getState();
		return (fConfig.supportsSuspend() && (state.equals(PDebugElementState.RESUMED) || state.equals(PDebugElementState.STEPPED)));
	}
	public boolean isSuspended() {
		return getState().equals(PDebugElementState.SUSPENDED);
	}
	public void resume() throws DebugException {
		if (!canResume())
			return;
		PDebugElementState oldState = getState();
		setState(PDebugElementState.RESUMING);
		try {
			getCDIThread().resume(false);
		} catch (PCDIException e) {
			setState(oldState);
			targetRequestFailed(e.getMessage(), null);
		}
	}
	public void suspend() throws DebugException {
		if (!canSuspend())
			return;
		PDebugElementState oldState = getState();
		setState(PDebugElementState.SUSPENDING);
		try {
			getCDIThread().suspend();
		} catch (CDIException e) {
			setState(oldState);
			targetRequestFailed(e.getMessage(), null);
		}
	}
	public boolean canStepInto() {
		return canStep();
	}
	public boolean canStepOver() {
		return canStep();
	}
	public boolean canStepReturn() {
		if (!fConfig.supportsStepping() || !canResume()) {
			return false;
		}
		return (fStackFrames.size() > 1);
	}
	protected boolean canStep() {
		if (!fConfig.supportsStepping() || !isSuspended()) {
			return false;
		}
		return !fStackFrames.isEmpty();
	}
	public boolean isStepping() {
		return (getState().equals(PDebugElementState.STEPPING)) || (getState().equals(PDebugElementState.STEPPED));
	}
	public void stepInto() throws DebugException {
		if (!canStepInto())
			return;
		PDebugElementState oldState = getState();
		setState(PDebugElementState.STEPPING);
		try {
			if (!isInstructionsteppingEnabled()) {
				getCDIThread().stepInto(1);
			} else {
				getCDIThread().stepIntoInstruction(1);
			}
		} catch (CDIException e) {
			setState(oldState);
			targetRequestFailed(e.getMessage(), null);
		}
	}
	public void stepOver() throws DebugException {
		if (!canStepOver())
			return;
		PDebugElementState oldState = getState();
		setState(PDebugElementState.STEPPING);
		try {
			if (!isInstructionsteppingEnabled()) {
				getCDIThread().stepOver(1);
			} else {
				getCDIThread().stepOverInstruction(1);
			}
		} catch (CDIException e) {
			setState(oldState);
			targetRequestFailed(e.getMessage(), null);
		}
	}
	public void stepReturn() throws DebugException {
		if (!canStepReturn())
			return;
		IStackFrame[] frames = getStackFrames();
		if (frames.length == 0)
			return;
		PStackFrame f = (PStackFrame) frames[0];
		PDebugElementState oldState = getState();
		setState(PDebugElementState.STEPPING);
		try {
			f.doStepReturn();
		} catch (DebugException e) {
			setState(oldState);
			throw e;
		}
	}
	public boolean canTerminate() {
		return getDebugTarget().canTerminate();
	}
	public boolean isTerminated() {
		return getDebugTarget().isTerminated();
	}
	public void terminate() throws DebugException {
		getDebugTarget().terminate();
	}
	protected void setCDIThread(IPCDIThread cdiThread) {
		fCDIThread = cdiThread;
	}
	protected IPCDIThread getCDIThread() {
		return fCDIThread;
	}
	protected synchronized void preserveStackFrames() {
		Iterator it = fStackFrames.iterator();
		while (it.hasNext()) {
			PStackFrame frame = (PStackFrame) (((IAdaptable) it.next()).getAdapter(PStackFrame.class));
			if (frame != null) {
				frame.preserve();
			}
		}
		setRefreshChildren(true);
	}
	protected synchronized void disposeStackFrames() {
		Iterator it = fStackFrames.iterator();
		while (it.hasNext()) {
			Object obj = it.next();
			if (obj instanceof PStackFrame) {
				((PStackFrame) obj).dispose();
			}
		}
		fStackFrames.clear();
		setLastStackDepth(0);
		resetStatus();
		setRefreshChildren(true);
	}
	protected void disposeStackFrames(int index, int length) {
		List removeList = new ArrayList(length);
		Iterator it = fStackFrames.iterator();
		int counter = 0;
		while (it.hasNext()) {
			PStackFrame frame = (PStackFrame) (((IAdaptable) it.next()).getAdapter(PStackFrame.class));
			if (frame != null && counter >= index && counter < index + length) {
				frame.dispose();
				removeList.add(frame);
			}
			++counter;
		}
		fStackFrames.removeAll(removeList);
	}
	protected void terminated() {
		setState(PDebugElementState.TERMINATED);
		dispose();
	}
	private void handleSuspendedEvent(IPCDISuspendedEvent event) {
		if (!(getState().equals(PDebugElementState.RESUMED) || getState().equals(PDebugElementState.STEPPED) || getState().equals(PDebugElementState.SUSPENDING)))
			return;
		setState(PDebugElementState.SUSPENDED);
		IPCDISessionObject reason = event.getReason();
		setCurrentStateInfo(reason);
		if (reason instanceof IPCDIEndSteppingRange) {
			handleEndSteppingRange((IPCDIEndSteppingRange) reason);
		} else if (reason instanceof IPCDIBreakpoint) {
			handleBreakpointHit((IPCDIBreakpoint) reason);
		} else if (reason instanceof IPCDISignalReceived) {
			handleSuspendedBySignal((IPCDISignalReceived) reason);
		} else {
			// fireSuspendEvent( DebugEvent.CLIENT_REQUEST );
			// Temporary fix for bug 56520
			fireSuspendEvent(DebugEvent.BREAKPOINT);
		}
	}
	private void handleResumedEvent(IPCDIResumedEvent event) {
		PDebugElementState state = PDebugElementState.RESUMED;
		int detail = DebugEvent.RESUME;
		if (isCurrent() && event.getType() != IPCDIResumedEvent.CONTINUE) {
			preserveStackFrames();
			switch (event.getType()) {
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
			state = PDebugElementState.STEPPING;
		} else {
			disposeStackFrames();
			fireChangeEvent(DebugEvent.CONTENT);
		}
		setCurrent(false);
		setState(state);
		setCurrentStateInfo(null);
		fireResumeEvent(detail);
	}
	private void handleEndSteppingRange(IPCDIEndSteppingRange endSteppingRange) {
		fireSuspendEvent(DebugEvent.STEP_END);
	}
	private void handleBreakpointHit(IPCDIBreakpoint breakpoint) {
		fireSuspendEvent(DebugEvent.BREAKPOINT);
	}
	private void handleSuspendedBySignal(IPCDISignalReceived signal) {
		fireSuspendEvent(DebugEvent.UNSPECIFIED);
	}
	private void handleTerminatedEvent(IPCDIDestroyedEvent event) {
		setState(PDebugElementState.TERMINATED);
		setCurrentStateInfo(null);
		terminated();
	}
	private void handleDisconnectedEvent(IPCDIDisconnectedEvent event) {
		setState(PDebugElementState.TERMINATED);
		setCurrentStateInfo(null);
		terminated();
	}
	private void handleChangedEvent(IPCDIChangedEvent event) {}
	protected void cleanup() {
		getCDISession().getEventManager().removeEventListener(this);
		disposeStackFrames();
	}
	private void setRefreshChildren(boolean refresh) {
		fRefreshChildren = refresh;
	}
	private boolean refreshChildren() {
		return fRefreshChildren;
	}
	public boolean canRestart() {
		return getDebugTarget() instanceof IRestart && ((IRestart) getDebugTarget()).canRestart();
	}
	public void restart() throws DebugException {
		if (canRestart()) {
			((IRestart) getDebugTarget()).restart();
		}
	}
	protected boolean isCurrent() {
		return fIsCurrent;
	}
	protected void setCurrent(boolean current) {
		fIsCurrent = current;
	}
	protected int getStackDepth() throws DebugException {
		int depth = 0;
		try {
			depth = getCDIThread().getStackFrameCount();
		} catch (PCDIException e) {
			setStatus(IPDebugElementStatus.WARNING, MessageFormat.format(CoreModelMessages.getString("PThread.1"), new String[] { e.getMessage() }));
		}
		return depth;
	}
	protected int getMaxStackDepth() {
		return MAX_STACK_DEPTH;
	}
	private void setLastStackDepth(int depth) {
		fLastStackDepth = depth;
	}
	protected int getLastStackDepth() {
		return fLastStackDepth;
	}
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IRunToLine.class) || adapter.equals(IRunToAddress.class) || adapter.equals(IJumpToLine.class) || adapter.equals(IJumpToAddress.class)) {
			try {
				return (IPStackFrame) getTopStackFrame();
			} catch (DebugException e) {
				// do nothing
			}
		}
		if (adapter.equals(PDebugElementState.class))
			return this;
		if (adapter == IPStackFrame.class) {
			try {
				return (IPStackFrame) getTopStackFrame();
			} catch (DebugException e) {
				// do nothing
			}
		}
		if (adapter == IMemoryBlockRetrieval.class) {
			return getDebugTarget().getAdapter(adapter);
		}
		return super.getAdapter(adapter);
	}
	protected void dispose() {
		fDisposed = true;
		cleanup();
	}
	protected boolean isDisposed() {
		return fDisposed;
	}
	public boolean canResumeWithoutSignal() {
		return (getDebugTarget() instanceof IResumeWithoutSignal && ((IResumeWithoutSignal) getDebugTarget()).canResumeWithoutSignal());
	}
	public void resumeWithoutSignal() throws DebugException {
		if (canResumeWithoutSignal()) {
			((IResumeWithoutSignal) getDebugTarget()).resumeWithoutSignal();
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
	protected void resumedByTarget(int detail, List events) {
		if (isCurrent() && detail != DebugEvent.CLIENT_REQUEST && detail != DebugEvent.UNSPECIFIED) {
			setState(PDebugElementState.STEPPED);
			preserveStackFrames();
			events.add(createResumeEvent(detail));
		} else {
			setState(PDebugElementState.RESUMED);
			disposeStackFrames();
			events.add(createChangeEvent(DebugEvent.CONTENT));
		}
		setCurrent(false);
		setCurrentStateInfo(null);
	}
	protected boolean isInstructionsteppingEnabled() {
		return ((PDebugTarget) getDebugTarget()).isInstructionSteppingEnabled();
	}
	protected void suspendByTarget(IPCDISessionObject reason, IPCDIThread suspensionThread) {
		setState(PDebugElementState.SUSPENDED);
		setCurrentStateInfo(null);
		if (getCDIThread().equals(suspensionThread)) {
			setCurrent(true);
			setCurrentStateInfo(reason);
			if (reason instanceof IPCDIEndSteppingRange) {
				handleEndSteppingRange((IPCDIEndSteppingRange) reason);
			} else if (reason instanceof IPCDIBreakpoint) {
				handleBreakpointHit((IPCDIBreakpoint) reason);
			} else if (reason instanceof IPCDISignalReceived) {
				handleSuspendedBySignal((IPCDISignalReceived) reason);
			} else {
				// fireSuspendEvent( DebugEvent.CLIENT_REQUEST );
				// Temporary fix for bug 56520
				fireSuspendEvent(DebugEvent.BREAKPOINT);
			}
		}
	}
}
