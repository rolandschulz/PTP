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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.ptp.debug.core.model.IJumpToAddress;
import org.eclipse.ptp.debug.core.model.IJumpToLine;
import org.eclipse.ptp.debug.core.model.IPDebugElementStatus;
import org.eclipse.ptp.debug.core.model.IPDummyStackFrame;
import org.eclipse.ptp.debug.core.model.IPStackFrame;
import org.eclipse.ptp.debug.core.model.IPThread;
import org.eclipse.ptp.debug.core.model.IRestart;
import org.eclipse.ptp.debug.core.model.IResumeWithoutSignal;
import org.eclipse.ptp.debug.core.model.IRunToAddress;
import org.eclipse.ptp.debug.core.model.IRunToLine;
import org.eclipse.ptp.debug.core.model.PDebugElementState;
import org.eclipse.ptp.debug.core.pdi.IPDIBreakpointInfo;
import org.eclipse.ptp.debug.core.pdi.IPDIEndSteppingRangeInfo;
import org.eclipse.ptp.debug.core.pdi.IPDIFunctionFinishedInfo;
import org.eclipse.ptp.debug.core.pdi.IPDILocationReachedInfo;
import org.eclipse.ptp.debug.core.pdi.IPDISessionObject;
import org.eclipse.ptp.debug.core.pdi.IPDISharedLibraryInfo;
import org.eclipse.ptp.debug.core.pdi.IPDISignalInfo;
import org.eclipse.ptp.debug.core.pdi.IPDIWatchpointScopeInfo;
import org.eclipse.ptp.debug.core.pdi.IPDIWatchpointTriggerInfo;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.event.IPDIChangedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIDestroyedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIDisconnectedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEventListener;
import org.eclipse.ptp.debug.core.pdi.event.IPDIResumedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDISuspendedEvent;
import org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrame;
import org.eclipse.ptp.debug.core.pdi.model.IPDIThread;
import org.eclipse.ptp.debug.internal.core.PBreakpointManager;
import org.eclipse.ptp.debug.internal.core.PSession;

/**
 * @author Clement chu
 *
 */
public class PThread extends PDebugElement implements IPThread, IRestart, IResumeWithoutSignal, IPDIEventListener {
	private final static int MAX_STACK_DEPTH = 100;
	private IPDIThread pdiThread;
	private ArrayList<IStackFrame> fStackFrames;
	private boolean fRefreshChildren = true;
	private boolean fIsCurrent = false;
	private int fLastStackDepth = 0;
	private boolean fDisposed = false;
	private PDebugTarget fDebugTarget = null;

	public PThread(PDebugTarget target, IPDIThread pdiThread) {
		super((PSession)target.getSession(), target.getTasks());
		this.fDebugTarget = target;
		this.pdiThread = pdiThread;
		if (target.getPDISession().isSuspended(target.getTasks())) {
			setState(PDebugElementState.SUSPENDED);
			setCurrent(true);
		}
		else {
			setState(PDebugElementState.RESUMED);
		}
		initialize();
		getPDISession().getEventManager().addEventListener(this);
	}
	public PDebugTarget getDebugTarget() {
		return fDebugTarget;
	}
	protected void initialize() {
		fStackFrames = new ArrayList<IStackFrame>();
	}
	public IStackFrame[] getStackFrames() throws DebugException {
		List<IStackFrame> list = Collections.EMPTY_LIST;
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
		if (getState().equals(PDebugElementState.RESUMED))
			return false;		
		return true;
	}
	protected synchronized List<IStackFrame> computeStackFrames(boolean refreshChildren) throws DebugException {
		if (isSuspended()) {
			if (isTerminated()) {
				fStackFrames = new ArrayList<IStackFrame>();
			} else if (refreshChildren) {
				if (fStackFrames.size() > 0) {
					Object frame = fStackFrames.get(fStackFrames.size() - 1);
					if (frame instanceof IPDummyStackFrame) {
						fStackFrames.remove(frame);
					}
				}
				int depth = getStackDepth();
				if (depth >= getMaxStackDepth())
					depth = getMaxStackDepth() - 1;
				
				IPDIStackFrame[] frames = (depth != 0) ? getPDIStackFrames(0, depth - 1) : new IPDIStackFrame[0];
				depth = frames.length;
				
				if (fStackFrames.isEmpty()) {
					if (frames.length > 0) {
						addStackFrames(frames, 0, frames.length, false);
					}
				} else {
					int diff = depth - getLastStackDepth();
					int offset = (diff > 0) ? frames.length - diff : 0;
					int length = (diff > 0) ? diff : -diff;
					if (!compareStackFrames(frames, fStackFrames, offset, length)) {
						disposeStackFrames(0, fStackFrames.size());
						addStackFrames(frames, 0, frames.length, false);						
					}
					if (diff < 0) {
						// stepping out of the last frame
						disposeStackFrames(0, getLastStackDepth() - depth);
						if (frames.length > 0) {
							updateStackFrames(frames, 0, fStackFrames, fStackFrames.size());
							if (fStackFrames.size() < frames.length) {
								addStackFrames(frames, fStackFrames.size(), frames.length - fStackFrames.size(), true);
							}
						}
					}
					else if (diff > 0) {
						// stepping into a new frame
						disposeStackFrames(frames.length - depth + getLastStackDepth(), depth - getLastStackDepth());
						addStackFrames(frames, 0, depth - getLastStackDepth(), false );
						updateStackFrames(frames, depth - getLastStackDepth(), fStackFrames, frames.length - depth + getLastStackDepth());
					}
					else { // diff == 0
						if (depth != 0) {
							// we are in the same frame
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
	private boolean compareStackFrames(IPDIStackFrame[] newFrames, List<IStackFrame> oldFrames, int offset, int length ) {
		int index = offset;
		Iterator<IStackFrame> it = oldFrames.iterator();
		while (it.hasNext() && index < newFrames.length) {
			PStackFrame frame = (PStackFrame)it.next();
			if (!frame.getPDIStackFrame().equals(newFrames[index++]))
				return false;
		}
		return true;
	}	
	protected IPDIStackFrame[] getPDIStackFrames() throws DebugException {
		return new IPDIStackFrame[0];
	}
	protected IPDIStackFrame[] getPDIStackFrames(int lowFrame, int highFrame) throws DebugException {
		try {
			return getPDIThread().getStackFrames(lowFrame, highFrame);
		} catch (PDIException e) {
			setStatus(IPDebugElementStatus.WARNING, MessageFormat.format(CoreModelMessages.getString("PThread.0"), new Object[] { e.getMessage() }));
			targetRequestFailed(e.getMessage(), null);
		}
		return new IPDIStackFrame[0];
	}
	protected void updateStackFrames(IPDIStackFrame[] newFrames, int offset, List<IStackFrame> oldFrames, int length) throws DebugException {
		for (int i = 0; i < length; i++) {
			PStackFrame frame = (PStackFrame) oldFrames.get(offset);
			frame.setPDIStackFrame(newFrames[offset]);
			offset++;
		}
	}
	protected void addStackFrames(IPDIStackFrame[] newFrames, int startIndex, int length, boolean append) {
		if (newFrames.length >= startIndex + length) {
			for (int i = 0; i < length; ++i) {
				if (append)
					fStackFrames.add(new PStackFrame(this, newFrames[startIndex + i]));
				else
					fStackFrames.add(i, new PStackFrame(this, newFrames[startIndex + i]));
			}
		}
	}
	public List<IStackFrame> computeStackFrames() throws DebugException {
		return computeStackFrames(refreshChildren());
	}
	public List<IStackFrame> computeNewStackFrames() throws DebugException {
		return computeStackFrames(true);
	}
	protected List<IStackFrame> createAllStackFrames(int depth, IPDIStackFrame[] frames) throws DebugException {
		List<IStackFrame> list = new ArrayList<IStackFrame>(frames.length);
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
		List<IStackFrame> c = computeStackFrames();
		return (c.isEmpty()) ? null : (IStackFrame) c.get(0);
	}
	public String getName() throws DebugException {
		return getPDIThread().toString();
	}
	public IBreakpoint[] getBreakpoints() {
		List<IBreakpoint> list = new ArrayList<IBreakpoint>(1);
		if (isSuspended()) {
			IBreakpoint bkpt = null;
			PBreakpointManager bMgr = fSession.getBreakpointManager();
			 if (getCurrentStateInfo() instanceof IPDIBreakpointInfo) {
				 bkpt = bMgr.getBreakpoint(((IPDIBreakpointInfo)getCurrentStateInfo()).getBreakpoint()); 				 
			 }
			 else if (getCurrentStateInfo() instanceof IPDIWatchpointTriggerInfo) {
				 bkpt = bMgr.getBreakpoint(((IPDIWatchpointTriggerInfo)getCurrentStateInfo()).getWatchpoint());
			 }
			 if (bkpt != null)
				list.add(bkpt);
		}
		return (IBreakpoint[]) list.toArray(new IBreakpoint[list.size()]);
	}
	public boolean canResume() {
		return isSuspended();
	}
	public boolean canSuspend() {
		PDebugElementState state = getState();
		return (state.equals(PDebugElementState.RESUMED) || state.equals(PDebugElementState.STEPPED));
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
			getPDISession().resume(getTasks(), false);
		} catch (PDIException e) {
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
			getPDISession().suspend(getTasks());
		} catch (PDIException e) {
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
		if (!canResume()) {
			return false;
		}
		return (fStackFrames.size() > 1);
	}
	protected boolean canStep() {
		if (!isSuspended()) {
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
				getPDISession().stepInto(getTasks(), 1);
			} else {
				getPDISession().stepIntoInstruction(getTasks(), 1);
			}
		} catch (PDIException e) {
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
				getPDISession().stepOver(getTasks(), 1);
			} else {
				getPDISession().stepOverInstruction(getTasks(), 1);
			}
		} catch (PDIException e) {
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
	protected IPDIThread getPDIThread() {
		return pdiThread;
	}
	protected synchronized void preserveStackFrames() {
		Iterator<IStackFrame> it = fStackFrames.iterator();
		while (it.hasNext()) {
			IPStackFrame frame = (IPStackFrame) (((IAdaptable) it.next()).getAdapter(IPStackFrame.class));
			if (frame instanceof PStackFrame) {
				((PStackFrame)frame).preserve();
			}
		}
		setRefreshChildren(true);
	}
	protected synchronized void disposeStackFrames() {
		Iterator<IStackFrame> it = fStackFrames.iterator();
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
		List<IStackFrame> removeList = new ArrayList<IStackFrame>(length);
		Iterator<IStackFrame> it = fStackFrames.iterator();
		int counter = 0;
		while (it.hasNext()) {
			IPStackFrame frame = (IPStackFrame) (((IAdaptable) it.next()).getAdapter(IPStackFrame.class));
			if (frame instanceof PStackFrame && counter >= index && counter < index + length) {
				((PStackFrame)frame).dispose();
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
	/******************************************************
	 * EVENT
	 ******************************************************/
	public synchronized void handleDebugEvents(IPDIEvent[] events) {
		/*
		 * FIXME Not support thread, always fire event by target
		if (isDisposed())
			return;
		for (int i = 0; i < events.length; i++) {
			IPDIEvent event = events[i];
			if (!event.contains(getPDITarget().getTasks()))
				continue;
			
			if (event instanceof IPDISuspendedEvent) {
				handleSuspendedEvent((IPDISuspendedEvent)event);
			}
			else if (event instanceof IPDIResumedEvent) {
				handleResumedEvent((IPDIResumedEvent)event);
			}
			else if (event instanceof IPDIDestroyedEvent) {
				handleTerminatedEvent((IPDIDestroyedEvent)event);
			}
			else if (event instanceof IPDIDisconnectedEvent) {
				handleDisconnectedEvent((IPDIDisconnectedEvent)event);
			}
			else if (event instanceof IPDIChangedEvent) {
				handleChangedEvent((IPDIChangedEvent)event);
			}
		}
		*/
	}
	private void handleSuspendedEvent(IPDISuspendedEvent event) {
		if (!(getState().equals(PDebugElementState.RESUMED) || getState().equals(PDebugElementState.STEPPED) || getState().equals(PDebugElementState.SUSPENDING)))
			return;
		setState(PDebugElementState.SUSPENDED);
		IPDISessionObject reason = event.getReason();
		setCurrentStateInfo(reason);
		if (reason instanceof IPDIBreakpointInfo) {
			handleBreakpointHit((IPDIBreakpointInfo)reason);
		}
		else if (reason instanceof IPDIEndSteppingRangeInfo) {
			handleEndSteppingRange((IPDIEndSteppingRangeInfo)reason);
		}
		else if (reason instanceof IPDIFunctionFinishedInfo) {
			
		}
		else if (reason instanceof IPDILocationReachedInfo) {
			
		}
		else if (reason instanceof IPDISignalInfo) {
			handleSuspendedBySignal((IPDISignalInfo)reason);
		}
		else if (reason instanceof IPDISharedLibraryInfo) {
			
		}
		else if (reason instanceof IPDIWatchpointScopeInfo) {
			
		}
		else if (reason instanceof IPDIWatchpointTriggerInfo) {
			
		}
		else {
			// fireSuspendEvent( DebugEvent.CLIENT_REQUEST );
			// Temporary fix for bug 56520
			fireSuspendEvent(DebugEvent.BREAKPOINT);
		}
	}
	private void handleResumedEvent(IPDIResumedEvent event) {
		PDebugElementState state = PDebugElementState.RESUMED;
		int detail = DebugEvent.RESUME;
		syncWithBackend();
		if (isCurrent() && event.getType() != IPDIResumedEvent.CONTINUE) {
			preserveStackFrames();
			switch (event.getType()) {
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
	private void handleTerminatedEvent(IPDIDestroyedEvent event) {
		setState(PDebugElementState.TERMINATED);
		setCurrentStateInfo(null);
		terminated();
	}
	private void handleDisconnectedEvent(IPDIDisconnectedEvent event) {
		setState(PDebugElementState.TERMINATED);
		setCurrentStateInfo(null);
		terminated();
	}
	private void handleChangedEvent(IPDIChangedEvent event) {}
	private void handleEndSteppingRange(IPDIEndSteppingRangeInfo info) {
		fireSuspendEvent(DebugEvent.STEP_END);
	}
	private void handleBreakpointHit(IPDIBreakpointInfo info) {
		fireSuspendEvent(DebugEvent.BREAKPOINT);
	}
	private void handleSuspendedBySignal(IPDISignalInfo info) {
		fireSuspendEvent(DebugEvent.UNSPECIFIED);
	}
	private void syncWithBackend() {
		IPDIThread pdiThread = getPDIThread();
		IPDIThread currentThread = null;
		try {
			currentThread = pdiThread.getTarget().getCurrentThread();
		}
		catch(PDIException e) {
			// ignore
		}
		setCurrent(pdiThread.equals(currentThread));
	}
	protected void suspendByTarget(IPDISessionObject reason, IPDIThread suspensionThread) {
		setState(PDebugElementState.SUSPENDED);
		setCurrentStateInfo(null);
		if (getPDIThread().equals(suspensionThread)) {
			setCurrent(true);
			setCurrentStateInfo(reason);
			if (reason instanceof IPDIBreakpointInfo) {
				handleBreakpointHit((IPDIBreakpointInfo)reason);
			}
			else if (reason instanceof IPDIEndSteppingRangeInfo) {
				handleEndSteppingRange((IPDIEndSteppingRangeInfo)reason);
			}
			else if (reason instanceof IPDIFunctionFinishedInfo) {
				
			}
			else if (reason instanceof IPDILocationReachedInfo) {
				
			}
			else if (reason instanceof IPDISignalInfo) {
				handleSuspendedBySignal((IPDISignalInfo) reason);
			}
			else if (reason instanceof IPDISharedLibraryInfo) {
				
			}
			else if (reason instanceof IPDIWatchpointScopeInfo) {
				
			}
			else if (reason instanceof IPDIWatchpointTriggerInfo) {
				
			}
			else {
				// fireSuspendEvent( DebugEvent.CLIENT_REQUEST );
				// Temporary fix for bug 56520
				fireSuspendEvent(DebugEvent.BREAKPOINT);
			}
		}
	}	
	protected void cleanup() {
		getPDISession().getEventManager().removeEventListener(this);
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
			depth = getPDIThread().getStackFrameCount();
		} catch (PDIException e) {
			setStatus(IPDebugElementStatus.WARNING, MessageFormat.format(CoreModelMessages.getString("PThread.1"), new Object[] { e.getMessage() }));
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
	protected void resumedByTarget(int detail, List<DebugEvent> events) {
		syncWithBackend();
		if (isCurrent() && detail != DebugEvent.CLIENT_REQUEST && detail != DebugEvent.UNSPECIFIED) {
			setState(PDebugElementState.STEPPED);
			preserveStackFrames();
			events.add(createResumeEvent(detail));
		} else {
			setState(PDebugElementState.RESUMED);
			disposeStackFrames();
			events.add(createChangeEvent(DebugEvent.CLIENT_REQUEST));
		}
		setCurrent(false);
		setCurrentStateInfo(null);
	}
	protected boolean isInstructionsteppingEnabled() {
		return ((PDebugTarget) getDebugTarget()).isInstructionSteppingEnabled();
	}
}
