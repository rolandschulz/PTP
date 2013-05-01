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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.debug.core.model.IJumpToAddress;
import org.eclipse.ptp.debug.core.model.IJumpToLine;
import org.eclipse.ptp.debug.core.model.IPGlobalVariable;
import org.eclipse.ptp.debug.core.model.IPStackFrame;
import org.eclipse.ptp.debug.core.model.IPVariable;
import org.eclipse.ptp.debug.core.model.IRestart;
import org.eclipse.ptp.debug.core.model.IResumeWithoutSignal;
import org.eclipse.ptp.debug.core.model.IRunToAddress;
import org.eclipse.ptp.debug.core.model.IRunToLine;
import org.eclipse.ptp.debug.core.pdi.IPDILocation;
import org.eclipse.ptp.debug.core.pdi.IPDILocator;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEventListener;
import org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrame;
import org.eclipse.ptp.debug.core.pdi.model.IPDITargetExpression;
import org.eclipse.ptp.debug.core.pdi.model.IPDIThread;
import org.eclipse.ptp.debug.core.pdi.model.IPDIVariableDescriptor;
import org.eclipse.ptp.internal.debug.core.messages.Messages;
import org.eclipse.ptp.internal.debug.core.sourcelookup.IPSourceLocator;

import com.ibm.icu.text.NumberFormat;

/**
 * @author Clement chu
 * 
 */
public class PStackFrame extends PDebugElement implements IPStackFrame, IRestart, IResumeWithoutSignal, IPDIEventListener {
	/**
	 * @param frameOne
	 * @param frameTwo
	 * @return
	 */
	protected static boolean equalFrame(IPDIStackFrame frameOne, IPDIStackFrame frameTwo) {
		if (frameOne == null || frameTwo == null)
			return false;
		IPDILocator loc1 = frameOne.getLocator();
		IPDILocator loc2 = frameTwo.getLocator();
		if (loc1 == null || loc2 == null)
			return false;
		if (loc1.getFile() != null && loc1.getFile().length() > 0 && loc2.getFile() != null && loc2.getFile().length() > 0
				&& loc1.getFile().equals(loc2.getFile())) {
			if (loc1.getFunction() != null && loc1.getFunction().length() > 0 && loc2.getFunction() != null
					&& loc2.getFunction().length() > 0 && loc1.getFunction().equals(loc2.getFunction()))
				return true;
		}
		if ((loc1.getFile() == null || loc1.getFile().length() < 1) && (loc2.getFile() == null || loc2.getFile().length() < 1)) {
			if (loc1.getFunction() != null && loc1.getFunction().length() > 0 && loc2.getFunction() != null
					&& loc2.getFunction().length() > 0 && loc1.getFunction().equals(loc2.getFunction()))
				return true;
		}
		if ((loc1.getFile() == null || loc1.getFile().length() < 1) && (loc2.getFile() == null || loc2.getFile().length() < 1)
				&& (loc1.getFunction() == null || loc1.getFunction().length() < 1)
				&& (loc2.getFunction() == null || loc2.getFunction().length() < 1)) {
			if (loc1.getAddress() == loc2.getAddress())
				return true;
		}
		return false;
	}

	private IPDIStackFrame pdiStackFrame;
	private IPDIStackFrame lastPDIStackFrame;
	private PThread fThread;
	private List<IPVariable> fVariables;
	private boolean fRefreshVariables = true;
	private List<PExpression> fExpressions;

	private boolean fIsDisposed = false;

	public PStackFrame(PThread thread, IPDIStackFrame pdiFrame) {
		super(thread.getSession(), thread.getTasks());
		setPDIStackFrame(pdiFrame);
		setThread(thread);
		getPDISession().getEventManager().addEventListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IPStackFrame#canEvaluate()
	 */
	public boolean canEvaluate() {
		return getDebugTarget().isSuspended();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.model.IJumpToAddress#canJumpToAddress(java
	 * .math.BigInteger)
	 */
	public boolean canJumpToAddress(BigInteger address) {
		return getThread().canResume();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.model.IJumpToLine#canJumpToLine(org.eclipse
	 * .core.resources.IFile, int)
	 */
	public boolean canJumpToLine(IFile file, int lineNumber) {
		return getThread().canResume();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.model.IJumpToLine#canJumpToLine(java.lang.
	 * String, int)
	 */
	public boolean canJumpToLine(String fileName, int lineNumber) {
		return getThread().canResume();
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
		return getThread().canResume();
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
	 * @see
	 * org.eclipse.ptp.debug.core.model.IRunToAddress#canRunToAddress(java.math
	 * .BigInteger)
	 */
	public boolean canRunToAddress(BigInteger address) {
		return getThread().canResume();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.model.IRunToLine#canRunToLine(org.eclipse.
	 * core.resources.IFile, int)
	 */
	public boolean canRunToLine(IFile file, int lineNumber) {
		return getThread().canResume();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.model.IRunToLine#canRunToLine(java.lang.String
	 * , int)
	 */
	public boolean canRunToLine(String fileName, int lineNumber) {
		return getThread().canResume();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IStep#canStepInto()
	 */
	public boolean canStepInto() {
		try {
			return exists() /* && isTopStackFrame() */&& getThread().canStepInto();
		} catch (DebugException e) {
			logError(e);
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IStep#canStepOver()
	 */
	public boolean canStepOver() {
		try {
			return exists() && getThread().canStepOver();
		} catch (DebugException e) {
			logError(e);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IStep#canStepReturn()
	 */
	public boolean canStepReturn() {
		try {
			if (!exists()) {
				return false;
			}
			List<IStackFrame> frames = ((PThread) getThread()).computeStackFrames();
			if (frames != null && !frames.isEmpty()) {
				boolean bottomFrame = this.equals(frames.get(frames.size() - 1));
				return !bottomFrame && getThread().canStepReturn();
			}
		} catch (DebugException e) {
			logError(e);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.ISuspendResume#canSuspend()
	 */
	public boolean canSuspend() {
		return getThread().canSuspend();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.ITerminate#canTerminate()
	 */
	public boolean canTerminate() {
		boolean exists = false;
		try {
			exists = exists();
		} catch (DebugException e) {
			logError(e);
		}
		return exists && getThread().canTerminate() || getDebugTarget().canTerminate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.model.IPStackFrame#evaluateExpression(java
	 * .lang.String)
	 */
	public IValue evaluateExpression(String expressionText) throws DebugException {
		if (!isDisposed()) {
			PExpression expression = getExpression(expressionText);
			if (expression != null) {
				return expression.getValue(this);
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.model.IPStackFrame#evaluateExpressionToString
	 * (java.lang.String)
	 */
	public String evaluateExpressionToString(String expression) throws DebugException {
		try {
			return getPDITarget().evaluateExpressionToString(getPDIStackFrame(), expression);
		} catch (PDIException e) {
			targetRequestFailed(e.getMessage(), null);
		}
		return null;
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
		if (adapter == IRunToLine.class) {
			return this;
		}
		if (adapter == IRunToAddress.class) {
			return this;
		}
		if (adapter == IJumpToLine.class) {
			return this;
		}
		if (adapter == IJumpToAddress.class) {
			return this;
		}
		if (adapter == IPStackFrame.class) {
			return this;
		}
		if (adapter == IStackFrame.class) {
			return this;
		}
		if (adapter == IPDIStackFrame.class) {
			return getPDIStackFrame();
		}
		if (adapter == IMemoryBlockRetrieval.class) {
			return getDebugTarget().getAdapter(adapter);
		}
		return super.getAdapter(adapter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IPStackFrame#getAddress()
	 */
	public BigInteger getAddress() {
		return getPDIStackFrame().getLocator().getAddress();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IStackFrame#getCharEnd()
	 */
	public int getCharEnd() throws DebugException {
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IStackFrame#getCharStart()
	 */
	public int getCharStart() throws DebugException {
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.model.PDebugElement#getDebugTarget()
	 */
	@Override
	public PDebugTarget getDebugTarget() {
		return fThread.getDebugTarget();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IPStackFrame#getFile()
	 */
	public String getFile() {
		return getPDIStackFrame().getLocator().getFile();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IPStackFrame#getFrameLineNumber()
	 */
	public int getFrameLineNumber() {
		return getPDIStackFrame().getLocator().getLineNumber();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IPStackFrame#getFunction()
	 */
	public String getFunction() {
		return getPDIStackFrame().getLocator().getFunction();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IPStackFrame#getLevel()
	 */
	public int getLevel() {
		return getPDIStackFrame().getLevel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IStackFrame#getLineNumber()
	 */
	public int getLineNumber() throws DebugException {
		if (isSuspended()) {
			ISourceLocator locator = getLaunch().getSourceLocator();
			if (locator != null && locator instanceof IAdaptable
					&& ((IAdaptable) locator).getAdapter(IPSourceLocator.class) != null)
				return ((IPSourceLocator) ((IAdaptable) locator).getAdapter(IPSourceLocator.class)).getLineNumber(this);
			if (getPDIStackFrame() != null && getPDIStackFrame().getLocator() != null)
				return getPDIStackFrame().getLocator().getLineNumber();
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IStackFrame#getName()
	 */
	public String getName() throws DebugException {
		IPDILocator locator = getPDIStackFrame().getLocator();
		String func = ""; //$NON-NLS-1$
		String file = ""; //$NON-NLS-1$
		String line = ""; //$NON-NLS-1$
		if (locator.getFunction() != null && locator.getFunction().trim().length() > 0)
			func += locator.getFunction() + "() "; //$NON-NLS-1$
		if (locator.getFile() != null && locator.getFile().trim().length() > 0) {
			file = locator.getFile();
			if (locator.getLineNumber() != 0) {
				line = NumberFormat.getInstance().format(new Integer(locator.getLineNumber()));
			}
		} else {
			return func;
		}
		return NLS.bind(Messages.PStackFrame_0, new Object[] { func, file, line });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IPStackFrame#getPDIStackFrame()
	 */
	public IPDIStackFrame getPDIStackFrame() {
		return pdiStackFrame;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IPStackFrame#getPDIThread()
	 */
	public IPDIThread getPDIThread() {
		return ((PThread) getThread()).getPDIThread();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IStackFrame#getRegisterGroups()
	 */
	public IRegisterGroup[] getRegisterGroups() throws DebugException {
		return (isDisposed()) ? new IRegisterGroup[0] : fSession.getRegisterManager().getRegisterGroups(getTasks(), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IStackFrame#getThread()
	 */
	public IThread getThread() {
		return fThread;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IStackFrame#getVariables()
	 */
	public IVariable[] getVariables() throws DebugException {
		if (isDisposed()) {
			return new IVariable[0];
		}
		IPGlobalVariable[] globals = getGlobals();
		List<IPVariable> vars = getVariables0();
		List<IPVariable> all = new ArrayList<IPVariable>(globals.length + vars.size());
		all.addAll(Arrays.asList(globals));
		all.addAll(vars);
		return all.toArray(new IVariable[all.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.event.IPDIEventListener#handleDebugEvents
	 * (org.eclipse.ptp.debug.core.pdi.event.IPDIEvent[])
	 */
	public void handleDebugEvents(IPDIEvent[] events) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IStackFrame#hasRegisterGroups()
	 */
	public boolean hasRegisterGroups() throws DebugException {
		return (isDisposed()) ? false
				: getDebugTarget().fSession.getRegisterManager().getRegisterGroups(getTasks(), this).length > 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IStackFrame#hasVariables()
	 */
	public boolean hasVariables() throws DebugException {
		return getVariables0().size() > 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IStep#isStepping()
	 */
	public boolean isStepping() {
		return getThread().isStepping();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.ISuspendResume#isSuspended()
	 */
	public boolean isSuspended() {
		return getThread().isSuspended();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.ITerminate#isTerminated()
	 */
	public boolean isTerminated() {
		return getThread().isTerminated();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.model.IJumpToAddress#jumpToAddress(java.math
	 * .BigInteger)
	 */
	public void jumpToAddress(BigInteger address) throws DebugException {
		if (!canJumpToAddress(address))
			return;

		IPDILocation location = getPDISession().getBreakpointManager().createAddressLocation(address);
		try {
			getPDISession().resume(getTasks(), location);
		} catch (PDIException e) {
			targetRequestFailed(e.getMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.model.IJumpToLine#jumpToLine(org.eclipse.core
	 * .resources.IFile, int)
	 */
	public void jumpToLine(IFile file, int lineNumber) throws DebugException {
		if (!canJumpToLine(file, lineNumber))
			return;
		jumpToLine(file.getFullPath().lastSegment(), lineNumber);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.model.IJumpToLine#jumpToLine(java.lang.String,
	 * int)
	 */
	public void jumpToLine(String fileName, int lineNumber) throws DebugException {
		if (!canJumpToLine(fileName, lineNumber))
			return;
		IPDILocation location = getPDISession().getBreakpointManager().createLineLocation(fileName, lineNumber);
		try {
			getPDISession().resume(getTasks(), location);
		} catch (PDIException e) {
			targetRequestFailed(e.getMessage(), e);
		}
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
		getThread().resume();
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
	 * @see
	 * org.eclipse.ptp.debug.core.model.IRunToAddress#runToAddress(java.math
	 * .BigInteger, boolean)
	 */
	public void runToAddress(BigInteger address, boolean skipBreakpoints) throws DebugException {
		if (!canRunToAddress(address))
			return;
		if (skipBreakpoints) {
			fSession.getBreakpointManager().skipBreakpoints(true);
		}
		IPDILocation location = getPDISession().getBreakpointManager().createAddressLocation(address);
		try {
			getPDISession().stepUntil(getTasks(), location);
		} catch (PDIException e) {
			if (skipBreakpoints) {
				fSession.getBreakpointManager().skipBreakpoints(false);
			}
			targetRequestFailed(e.getMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.model.IRunToLine#runToLine(org.eclipse.core
	 * .resources.IFile, int, boolean)
	 */
	public void runToLine(IFile file, int lineNumber, boolean skipBreakpoints) throws DebugException {
		if (!canRunToLine(file, lineNumber))
			return;
		runToLine(file.getLocation().lastSegment(), lineNumber, skipBreakpoints);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.model.IRunToLine#runToLine(java.lang.String,
	 * int, boolean)
	 */
	public void runToLine(String fileName, int lineNumber, boolean skipBreakpoints) throws DebugException {
		if (!canRunToLine(fileName, lineNumber))
			return;
		if (skipBreakpoints) {
			fSession.getBreakpointManager().skipBreakpoints(true);
		}
		IPDILocation location = getPDISession().getBreakpointManager().createLineLocation(fileName, lineNumber);
		try {
			getPDISession().stepUntil(getTasks(), location);
		} catch (PDIException e) {
			if (skipBreakpoints) {
				fSession.getBreakpointManager().skipBreakpoints(false);
			}
			targetRequestFailed(e.getMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IStep#stepInto()
	 */
	public void stepInto() throws DebugException {
		if (canStepInto()) {
			getThread().stepInto();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IStep#stepOver()
	 */
	public void stepOver() throws DebugException {
		if (canStepOver()) {
			getThread().stepOver();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IStep#stepReturn()
	 */
	public void stepReturn() throws DebugException {
		if (canStepReturn()) {
			getThread().stepReturn();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.ISuspendResume#suspend()
	 */
	public void suspend() throws DebugException {
		getThread().suspend();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.ITerminate#terminate()
	 */
	public void terminate() throws DebugException {
		if (getThread().canTerminate()) {
			getThread().terminate();
		} else {
			getDebugTarget().terminate();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		try {
			return getName();
		} catch (DebugException e) {
			return e.getLocalizedMessage();
		}
	}

	/**
	 * @param expressionText
	 * @return
	 * @throws DebugException
	 */
	private synchronized PExpression getExpression(String expressionText) throws DebugException {
		if (isDisposed()) {
			return null;
		}
		if (fExpressions == null) {
			fExpressions = new ArrayList<PExpression>(5);
		}
		PExpression expression = null;
		Iterator<PExpression> it = fExpressions.iterator();
		while (it.hasNext()) {
			expression = it.next();
			if (expression.getExpressionText().compareTo(expressionText) == 0) {
				return expression;
			}
		}
		try {
			IPDITargetExpression pdiExpression = getPDISession().getExpressionManager()
					.createExpression(getTasks(), expressionText);
			expression = new PExpression(this, pdiExpression, null);
			fExpressions.add(expression);
		} catch (PDIException e) {
			targetRequestFailed(e.getMessage(), null);
		}
		return expression;
	}

	/**
	 * @return
	 */
	private IPGlobalVariable[] getGlobals() {
		// TODO Not implement PStackFrame - getGlobals() yet
		return new IPGlobalVariable[0];
	}

	/**
	 * 
	 */
	private void preserveExpressions() {
		if (fExpressions == null)
			return;
		Iterator<PExpression> it = fExpressions.iterator();
		while (it.hasNext()) {
			PExpression exp = it.next();
			exp.preserve();
		}
	}

	/**
	 * 
	 */
	private void preserveVariables() {
		if (fVariables == null)
			return;
		Iterator<IPVariable> it = fVariables.iterator();
		while (it.hasNext()) {
			AbstractPVariable av = (AbstractPVariable) it.next();
			av.preserve();
		}
	}

	/**
	 * @return
	 */
	private boolean refreshVariables() {
		return fRefreshVariables;
	}

	// FIXME - commented synchronized to prevent hanging here
	/**
	 * @param isDisposed
	 */
	private/* synchronized */void setDisposed(boolean isDisposed) {
		fIsDisposed = isDisposed;
	}

	/**
	 * @param refresh
	 */
	private void setRefreshVariables(boolean refresh) {
		fRefreshVariables = refresh;
	}

	/**
	 * 
	 */
	protected void dispose() {
		setDisposed(true);
		getPDISession().getEventManager().removeEventListener(this);
		disposeAllVariables();
		disposeExpressions();
	}

	/**
	 * 
	 */
	protected void disposeAllVariables() {
		if (fVariables == null)
			return;
		Iterator<IPVariable> it = fVariables.iterator();
		while (it.hasNext()) {
			((PVariable) it.next()).dispose();
		}
		fVariables.clear();
		fVariables = null;
	}

	/**
	 * 
	 */
	protected void disposeExpressions() {
		if (fExpressions != null) {
			Iterator<PExpression> it = fExpressions.iterator();
			while (it.hasNext()) {
				(it.next()).dispose();
			}
			fExpressions.clear();
		}
		fExpressions = null;
	}

	/**
	 * @throws DebugException
	 */
	protected void doStepReturn() throws DebugException {
		try {
			getPDISession().stepReturn(getTasks(), 0);
		} catch (PDIException e) {
			targetRequestFailed(e.getMessage(), null);
		}
	}

	/**
	 * @return
	 * @throws DebugException
	 */
	protected boolean exists() throws DebugException {
		return ((PThread) getThread()).computeStackFrames().indexOf(this) != -1;
	}

	/**
	 * @param list
	 * @param var
	 * @return
	 */
	protected IPDIVariableDescriptor findVariable(List<IPDIVariableDescriptor> list, PVariable var) {
		Iterator<IPDIVariableDescriptor> it = list.iterator();
		while (it.hasNext()) {
			IPDIVariableDescriptor newVarObject = it.next();
			if (var.sameVariable(newVarObject))
				return newVarObject;
		}
		return null;
	}

	/**
	 * @return
	 * @throws DebugException
	 */
	protected List<IPDIVariableDescriptor> getAllPDIVariableObjects() throws DebugException {
		List<IPDIVariableDescriptor> list = new ArrayList<IPDIVariableDescriptor>();
		list.addAll(getPDIArgumentObjects());
		list.addAll(getPDILocalVariableObjects());
		return list;
	}

	/**
	 * @return
	 */
	protected IPDIStackFrame getLastPDIStackFrame() {
		return lastPDIStackFrame;
	}

	/**
	 * @return
	 * @throws DebugException
	 */
	protected List<IPDIVariableDescriptor> getPDIArgumentObjects() throws DebugException {
		List<IPDIVariableDescriptor> list = new ArrayList<IPDIVariableDescriptor>();
		try {
			list.addAll(Arrays.asList(getPDIStackFrame().getArgumentDescriptors()));
		} catch (PDIException e) {
			targetRequestFailed(e.getMessage(), null);
		}
		return list;
	}

	/**
	 * @return
	 * @throws DebugException
	 */
	protected List<IPDIVariableDescriptor> getPDILocalVariableObjects() throws DebugException {
		List<IPDIVariableDescriptor> list = new ArrayList<IPDIVariableDescriptor>();
		try {
			list.addAll(Arrays.asList(getPDIStackFrame().getLocalVariableDescriptors()));
		} catch (PDIException e) {
			targetRequestFailed(e.getMessage(), null);
		}
		return list;
	}

	/**
	 * @return
	 * @throws DebugException
	 */
	protected synchronized List<IPVariable> getVariables0() throws DebugException {
		if (!isDisposed()) {
			PThread thread = (PThread) getThread();
			if (thread.isSuspended()) {
				if (fVariables == null) {
					List<IPDIVariableDescriptor> vars = getAllPDIVariableObjects();
					fVariables = new ArrayList<IPVariable>(vars.size());
					Iterator<IPDIVariableDescriptor> it = vars.iterator();
					while (it.hasNext()) {
						fVariables.add(PVariableFactory.createLocalVariable(this, it.next()));
					}
				} else {
					if (refreshVariables()) {
						updateVariables();
					}
				}
				setRefreshVariables(false);
			}
			if (fVariables != null) {
				return fVariables;
			}
		}
		return Collections.emptyList();
	}

	/**
	 * @return
	 */
	protected boolean isDisposed() {
		return fIsDisposed;
	}

	/**
	 * @return
	 * @throws DebugException
	 */
	protected boolean isTopStackFrame() throws DebugException {
		IStackFrame tos = getThread().getTopStackFrame();
		return tos != null && tos.equals(this);
	}

	/**
	 * 
	 */
	protected synchronized void preserve() {
		preserveVariables();
		preserveExpressions();
	}

	/**
	 * @param frame
	 */
	protected void setPDIStackFrame(IPDIStackFrame frame) {
		if (frame != null) {
			lastPDIStackFrame = frame;
		} else {
			lastPDIStackFrame = pdiStackFrame;
		}
		pdiStackFrame = frame;
		setRefreshVariables(true);
	}

	/**
	 * @param thread
	 */
	protected void setThread(PThread thread) {
		fThread = thread;
	}

	/**
	 * @throws DebugException
	 */
	protected void updateVariables() throws DebugException {
		List<IPDIVariableDescriptor> locals = getAllPDIVariableObjects();
		int index = 0;
		while (index < fVariables.size()) {
			IPDIVariableDescriptor varObject = findVariable(locals, (PVariable) fVariables.get(index));
			if (varObject != null) {
				locals.remove(varObject);
				index++;
			} else {
				// remove variable
				fVariables.remove(index);
			}
		}
		// add any new locals
		Iterator<IPDIVariableDescriptor> newOnes = locals.iterator();
		while (newOnes.hasNext()) {
			fVariables.add(PVariableFactory.createLocalVariable(this, newOnes.next()));
		}
	}
}
