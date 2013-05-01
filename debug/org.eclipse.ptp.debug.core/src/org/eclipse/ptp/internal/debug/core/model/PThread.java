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
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.debug.core.IPBreakpointManager;
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
import org.eclipse.ptp.debug.core.pdi.IPDISessionObject;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.event.IPDIBreakpointInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEndSteppingRangeInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEventListener;
import org.eclipse.ptp.debug.core.pdi.event.IPDIFunctionFinishedInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDILocationReachedInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDISharedLibraryInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDISignalInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDIWatchpointScopeInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDIWatchpointTriggerInfo;
import org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrame;
import org.eclipse.ptp.debug.core.pdi.model.IPDIThread;
import org.eclipse.ptp.internal.debug.core.messages.Messages;

/**
 * @author Clement chu
 * 
 */
public class PThread extends PDebugElement implements IPThread, IRestart, IResumeWithoutSignal, IPDIEventListener {
	private final static int MAX_STACK_DEPTH = 100;
	private final IPDIThread pdiThread;
	private ArrayList<IStackFrame> fStackFrames;
	private boolean fRefreshChildren = true;
	private boolean fIsCurrent = false;
	private int fLastStackDepth = 0;
	private boolean fDisposed = false;
	private PDebugTarget fDebugTarget = null;

	public PThread(PDebugTarget target, IPDIThread pdiThread) {
		super(target.getSession(), target.getTasks());
		this.fDebugTarget = target;
		this.pdiThread = pdiThread;
		if (target.getPDISession().isSuspended(target.getTasks())) {
			setState(PDebugElementState.SUSPENDED);
			setCurrent(true);
		} else {
			setState(PDebugElementState.RESUMED);
		}
		initialize();
		getPDISession().getEventManager().addEventListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IRestart#canRestart()
	 */
	public boolean canRestart() {
		return getDebugTarget() instanceof IRestart && ((IRestart) getDebugTarget()).canRestart();
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
		return (getDebugTarget() instanceof IResumeWithoutSignal && ((IResumeWithoutSignal) getDebugTarget())
				.canResumeWithoutSignal());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IStep#canStepInto()
	 */
	public boolean canStepInto() {
		return canStep();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IStep#canStepOver()
	 */
	public boolean canStepOver() {
		return canStep();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IStep#canStepReturn()
	 */
	public boolean canStepReturn() {
		if (!canResume()) {
			return false;
		}
		return (fStackFrames.size() > 1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.ISuspendResume#canSuspend()
	 */
	public boolean canSuspend() {
		PDebugElementState state = getState();
		return (state.equals(PDebugElementState.RESUMED) || state.equals(PDebugElementState.STEPPED));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.ITerminate#canTerminate()
	 */
	public boolean canTerminate() {
		return getDebugTarget().canTerminate();
	}

	/**
	 * @return
	 * @throws DebugException
	 */
	public List<IStackFrame> computeNewStackFrames() throws DebugException {
		return computeStackFrames(true);
	}

	/**
	 * @return
	 * @throws DebugException
	 */
	public List<IStackFrame> computeStackFrames() throws DebugException {
		return computeStackFrames(refreshChildren());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.model.PDebugElement#getAdapter(java
	 * .lang.Class)
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IRunToLine.class) || adapter.equals(IRunToAddress.class) || adapter.equals(IJumpToLine.class)
				|| adapter.equals(IJumpToAddress.class)) {
			try {
				return getTopStackFrame();
			} catch (DebugException e) {
				// do nothing
			}
		}
		if (adapter.equals(PDebugElementState.class))
			return this;
		if (adapter == IPStackFrame.class) {
			try {
				return getTopStackFrame();
			} catch (DebugException e) {
				// do nothing
			}
		}
		if (adapter == IMemoryBlockRetrieval.class) {
			return getDebugTarget().getAdapter(adapter);
		}
		return super.getAdapter(adapter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IThread#getBreakpoints()
	 */
	public IBreakpoint[] getBreakpoints() {
		List<IBreakpoint> list = new ArrayList<IBreakpoint>(1);
		if (isSuspended()) {
			IBreakpoint bkpt = null;
			IPBreakpointManager bMgr = fSession.getBreakpointManager();
			if (getCurrentStateInfo() instanceof IPDIBreakpointInfo) {
				bkpt = bMgr.getBreakpoint(((IPDIBreakpointInfo) getCurrentStateInfo()).getBreakpoint());
			} else if (getCurrentStateInfo() instanceof IPDIWatchpointTriggerInfo) {
				bkpt = bMgr.getBreakpoint(((IPDIWatchpointTriggerInfo) getCurrentStateInfo()).getWatchpoint());
			}
			if (bkpt != null)
				list.add(bkpt);
		}
		return list.toArray(new IBreakpoint[list.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.model.PDebugElement#getDebugTarget()
	 */
	@Override
	public PDebugTarget getDebugTarget() {
		return fDebugTarget;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IThread#getName()
	 */
	public String getName() throws DebugException {
		return getPDIThread().toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IThread#getPriority()
	 */
	public int getPriority() throws DebugException {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IThread#getStackFrames()
	 */
	public IStackFrame[] getStackFrames() throws DebugException {
		List<IStackFrame> list = Collections.emptyList();
		try {
			list = computeStackFrames();
		} catch (DebugException e) {
			setStatus(IPDebugElementStatus.ERROR, e.getStatus().getMessage());
			throw e;
		}
		return list.toArray(new IStackFrame[list.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IThread#getTopStackFrame()
	 */
	public IStackFrame getTopStackFrame() throws DebugException {
		List<IStackFrame> c = computeStackFrames();
		return (c.isEmpty()) ? null : (IStackFrame) c.get(0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.event.IPDIEventListener#handleDebugEvents
	 * (org.eclipse.ptp.debug.core.pdi.event.IPDIEvent[])
	 */
	public synchronized void handleDebugEvents(IPDIEvent[] events) {
		/*
		 * FIXME Not support thread, always fire event by target if
		 * (isDisposed()) return; for (int i = 0; i < events.length; i++) {
		 * IPDIEvent event = events[i]; if
		 * (!event.contains(getPDITarget().getTasks())) continue;
		 * 
		 * if (event instanceof IPDISuspendedEvent) {
		 * handleSuspendedEvent((IPDISuspendedEvent)event); } else if (event
		 * instanceof IPDIResumedEvent) {
		 * handleResumedEvent((IPDIResumedEvent)event); } else if (event
		 * instanceof IPDIDestroyedEvent) {
		 * handleTerminatedEvent((IPDIDestroyedEvent)event); } else if (event
		 * instanceof IPDIDisconnectedEvent) {
		 * handleDisconnectedEvent((IPDIDisconnectedEvent)event); } else if
		 * (event instanceof IPDIChangedEvent) {
		 * handleChangedEvent((IPDIChangedEvent)event); } }
		 */
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IThread#hasStackFrames()
	 */
	public boolean hasStackFrames() throws DebugException {
		// Always return true to postpone the stack frames request
		if (getState().equals(PDebugElementState.RESUMED))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IStep#isStepping()
	 */
	public boolean isStepping() {
		return (getState().equals(PDebugElementState.STEPPING)) || (getState().equals(PDebugElementState.STEPPED));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.ISuspendResume#isSuspended()
	 */
	public boolean isSuspended() {
		return getState().equals(PDebugElementState.SUSPENDED);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.ITerminate#isTerminated()
	 */
	public boolean isTerminated() {
		return getDebugTarget().isTerminated();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IRestart#restart()
	 */
	public void restart() throws DebugException {
		if (canRestart()) {
			((IRestart) getDebugTarget()).restart();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.ISuspendResume#resume()
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.model.IResumeWithoutSignal#resumeWithoutSignal
	 * ()
	 */
	public void resumeWithoutSignal() throws DebugException {
		if (canResumeWithoutSignal()) {
			((IResumeWithoutSignal) getDebugTarget()).resumeWithoutSignal();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IStep#stepInto()
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IStep#stepOver()
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IStep#stepReturn()
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.ISuspendResume#suspend()
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.ITerminate#terminate()
	 */
	public void terminate() throws DebugException {
		getDebugTarget().terminate();
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
	 * @param newFrames
	 * @param oldFrames
	 * @param offset
	 * @param length
	 * @return
	 */
	private boolean compareStackFrames(IPDIStackFrame[] newFrames, List<IStackFrame> oldFrames, int offset, int length) {
		int index = offset;
		Iterator<IStackFrame> it = oldFrames.iterator();
		while (it.hasNext() && index < newFrames.length) {
			PStackFrame frame = (PStackFrame) it.next();
			if (!frame.getPDIStackFrame().equals(newFrames[index++]))
				return false;
		}
		return true;
	}

	/**
	 * @param info
	 */
	private void handleBreakpointHit(IPDIBreakpointInfo info) {
		fireSuspendEvent(DebugEvent.BREAKPOINT);
	}

	/**
	 * @param info
	 */
	private void handleEndSteppingRange(IPDIEndSteppingRangeInfo info) {
		fireSuspendEvent(DebugEvent.STEP_END);
	}

	/**
	 * @param info
	 */
	private void handleSuspendedBySignal(IPDISignalInfo info) {
		fireSuspendEvent(DebugEvent.UNSPECIFIED);
	}

	/**
	 * @return
	 */
	private boolean refreshChildren() {
		return fRefreshChildren;
	}

	/**
	 * @param depth
	 */
	private void setLastStackDepth(int depth) {
		fLastStackDepth = depth;
	}

	/**
	 * @param refresh
	 */
	private void setRefreshChildren(boolean refresh) {
		fRefreshChildren = refresh;
	}

	/**
	 * 
	 */
	private void syncWithBackend() {
		IPDIThread pdiThread = getPDIThread();
		IPDIThread currentThread = null;
		try {
			currentThread = pdiThread.getTarget().getCurrentThread();
		} catch (PDIException e) {
			// ignore
		}
		setCurrent(pdiThread.equals(currentThread));
	}

	/**
	 * @param newFrames
	 * @param startIndex
	 * @param length
	 * @param append
	 */
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

	/**
	 * @return
	 */
	protected boolean canStep() {
		if (!isSuspended()) {
			return false;
		}
		return !fStackFrames.isEmpty();
	}

	/**
	 * 
	 */
	protected void cleanup() {
		getPDISession().getEventManager().removeEventListener(this);
		disposeStackFrames();
	}

	/**
	 * @param refreshChildren
	 * @return
	 * @throws DebugException
	 */
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
					} else if (diff > 0) {
						// stepping into a new frame
						disposeStackFrames(frames.length - depth + getLastStackDepth(), depth - getLastStackDepth());
						addStackFrames(frames, 0, depth - getLastStackDepth(), false);
						updateStackFrames(frames, depth - getLastStackDepth(), fStackFrames, frames.length - depth
								+ getLastStackDepth());
					} else { // diff == 0
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

	/**
	 * @param depth
	 * @param frames
	 * @return
	 * @throws DebugException
	 */
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

	/**
	 * 
	 */
	protected void dispose() {
		fDisposed = true;
		cleanup();
	}

	/**
	 * 
	 */
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

	/**
	 * @param index
	 * @param length
	 */
	protected void disposeStackFrames(int index, int length) {
		List<IStackFrame> removeList = new ArrayList<IStackFrame>(length);
		Iterator<IStackFrame> it = fStackFrames.iterator();
		int counter = 0;
		while (it.hasNext()) {
			IPStackFrame frame = (IPStackFrame) (((IAdaptable) it.next()).getAdapter(IPStackFrame.class));
			if (frame instanceof PStackFrame && counter >= index && counter < index + length) {
				((PStackFrame) frame).dispose();
				removeList.add(frame);
			}
			++counter;
		}
		fStackFrames.removeAll(removeList);
	}

	/**
	 * @return
	 */
	protected int getLastStackDepth() {
		return fLastStackDepth;
	}

	/**
	 * @return
	 */
	protected int getMaxStackDepth() {
		return MAX_STACK_DEPTH;
	}

	/**
	 * @return
	 * @throws DebugException
	 */
	protected IPDIStackFrame[] getPDIStackFrames() throws DebugException {
		return new IPDIStackFrame[0];
	}

	/**
	 * @param lowFrame
	 * @param highFrame
	 * @return
	 * @throws DebugException
	 */
	protected IPDIStackFrame[] getPDIStackFrames(int lowFrame, int highFrame) throws DebugException {
		try {
			return getPDIThread().getStackFrames(lowFrame, highFrame);
		} catch (PDIException e) {
			setStatus(IPDebugElementStatus.WARNING, NLS.bind(Messages.PThread_0, new Object[] { e.getMessage() }));
			targetRequestFailed(e.getMessage(), null);
		}
		return new IPDIStackFrame[0];
	}

	/**
	 * @return
	 */
	protected IPDIThread getPDIThread() {
		return pdiThread;
	}

	/**
	 * @return
	 * @throws DebugException
	 */
	protected int getStackDepth() throws DebugException {
		int depth = 0;
		try {
			depth = getPDIThread().getStackFrameCount();
		} catch (PDIException e) {
			setStatus(IPDebugElementStatus.WARNING, NLS.bind(Messages.PThread_0, new Object[] { e.getMessage() }));
		}
		return depth;
	}

	/**
	 * 
	 */
	protected void initialize() {
		fStackFrames = new ArrayList<IStackFrame>();
	}

	/**
	 * @return
	 */
	protected boolean isCurrent() {
		return fIsCurrent;
	}

	/**
	 * @return
	 */
	protected boolean isDisposed() {
		return fDisposed;
	}

	/**
	 * @return
	 */
	protected boolean isInstructionsteppingEnabled() {
		return (getDebugTarget()).isInstructionSteppingEnabled();
	}

	/**
	 * 
	 */
	protected synchronized void preserveStackFrames() {
		Iterator<IStackFrame> it = fStackFrames.iterator();
		while (it.hasNext()) {
			IPStackFrame frame = (IPStackFrame) (((IAdaptable) it.next()).getAdapter(IPStackFrame.class));
			if (frame instanceof PStackFrame) {
				((PStackFrame) frame).preserve();
			}
		}
		setRefreshChildren(true);
	}

	/**
	 * @param detail
	 * @param events
	 */
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

	/**
	 * @param current
	 */
	protected void setCurrent(boolean current) {
		fIsCurrent = current;
	}

	/**
	 * @param reason
	 * @param suspensionThread
	 */
	protected void suspendByTarget(IPDISessionObject reason, IPDIThread suspensionThread) {
		setState(PDebugElementState.SUSPENDED);
		setCurrentStateInfo(null);
		if (getPDIThread().equals(suspensionThread)) {
			setCurrent(true);
			setCurrentStateInfo(reason);
			if (reason instanceof IPDIBreakpointInfo) {
				handleBreakpointHit((IPDIBreakpointInfo) reason);
			} else if (reason instanceof IPDIEndSteppingRangeInfo) {
				handleEndSteppingRange((IPDIEndSteppingRangeInfo) reason);
			} else if (reason instanceof IPDIFunctionFinishedInfo) {

			} else if (reason instanceof IPDILocationReachedInfo) {

			} else if (reason instanceof IPDISignalInfo) {
				handleSuspendedBySignal((IPDISignalInfo) reason);
			} else if (reason instanceof IPDISharedLibraryInfo) {

			} else if (reason instanceof IPDIWatchpointScopeInfo) {

			} else if (reason instanceof IPDIWatchpointTriggerInfo) {

			} else {
				// fireSuspendEvent( DebugEvent.CLIENT_REQUEST );
				// Temporary fix for bug 56520
				fireSuspendEvent(DebugEvent.BREAKPOINT);
			}
		}
	}

	/**
	 * 
	 */
	protected void terminated() {
		setState(PDebugElementState.TERMINATED);
		dispose();
	}

	/**
	 * @param newFrames
	 * @param offset
	 * @param oldFrames
	 * @param length
	 * @throws DebugException
	 */
	protected void updateStackFrames(IPDIStackFrame[] newFrames, int offset, List<IStackFrame> oldFrames, int length)
			throws DebugException {
		for (int i = 0; i < length; i++) {
			PStackFrame frame = (PStackFrame) oldFrames.get(offset);
			frame.setPDIStackFrame(newFrames[offset]);
			offset++;
		}
	}
}
