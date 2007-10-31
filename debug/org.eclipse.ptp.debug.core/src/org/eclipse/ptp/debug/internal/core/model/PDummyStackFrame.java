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

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.ptp.debug.core.model.IPDummyStackFrame;
import org.eclipse.ptp.debug.core.model.IPThread;
import org.eclipse.ptp.debug.internal.core.PSession;

/**
 * Implementation of the dummy stack frame.
 */
public class PDummyStackFrame extends PDebugElement implements IStackFrame, IPDummyStackFrame {
	private PThread fThread;

	public PDummyStackFrame(PThread thread) {
		super((PSession)thread.getSession(), thread.getTasks());
		setThread(thread);
	}
	public PDebugTarget getDebugTarget() {
		return fThread.getDebugTarget();
	}
	public IPThread getThread() {
		return fThread;
	}
	public IVariable[] getVariables() throws DebugException {
		return new IVariable[0];
	}
	public boolean hasVariables() throws DebugException {
		return false;
	}
	public int getLineNumber() throws DebugException {
		return 0;
	}
	public int getCharStart() throws DebugException {
		return 0;
	}
	public int getCharEnd() throws DebugException {
		return 0;
	}
	public String getName() throws DebugException {
		return "...";
	}
	public IRegisterGroup[] getRegisterGroups() throws DebugException {
		return new IRegisterGroup[0];
	}
	public boolean hasRegisterGroups() throws DebugException {
		return false;
	}
	public boolean canStepInto() {
		return false;
	}
	public boolean canStepOver() {
		return false;
	}
	public boolean canStepReturn() {
		return false;
	}
	public boolean isStepping() {
		return false;
	}
	public void stepInto() throws DebugException {}
	public void stepOver() throws DebugException {}
	public void stepReturn() throws DebugException {}
	public boolean canResume() {
		return false;
	}
	public boolean canSuspend() {
		return false;
	}
	public boolean isSuspended() {
		return false;
	}
	public void resume() throws DebugException {}
	public void suspend() throws DebugException {}
	public boolean canTerminate() {
		return false;
	}
	public boolean isTerminated() {
		return false;
	}
	public void terminate() throws DebugException {}
	protected void setThread(PThread thread) {
		fThread = thread;
	}
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IPDummyStackFrame.class))
			return this;
		if (adapter.equals(IStackFrame.class))
			return this;
		return super.getAdapter(adapter);
	}
}
