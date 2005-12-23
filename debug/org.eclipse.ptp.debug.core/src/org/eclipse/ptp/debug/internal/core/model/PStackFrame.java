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

import java.math.BigInteger;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IAddressFactory;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.model.IJumpToAddress;
import org.eclipse.cdt.debug.core.model.IJumpToLine;
import org.eclipse.cdt.debug.core.model.IRestart;
import org.eclipse.cdt.debug.core.model.IResumeWithoutSignal;
import org.eclipse.cdt.debug.core.model.IRunToAddress;
import org.eclipse.cdt.debug.core.model.IRunToLine;
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
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIEventListener;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIExpression;
import org.eclipse.ptp.debug.core.cdi.model.IPCDILocation;
import org.eclipse.ptp.debug.core.cdi.model.IPCDILocator;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIStackFrame;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIThread;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIVariableDescriptor;
import org.eclipse.ptp.debug.core.model.IPGlobalVariable;
import org.eclipse.ptp.debug.core.model.IPStackFrame;
import org.eclipse.ptp.debug.core.sourcelookup.IPSourceLocator;
import org.eclipse.ptp.debug.internal.core.PGlobalVariableManager;

/**
 * @author Clement chu
 * 
 */
public class PStackFrame extends PDebugElement implements IPStackFrame, IRestart, IResumeWithoutSignal, IPCDIEventListener {
	private IPCDIStackFrame fCDIStackFrame;
	private IPCDIStackFrame fLastCDIStackFrame;
	private PThread fThread;
	private List fVariables;
	private boolean fRefreshVariables = true;
	private List fExpressions;
	private boolean fIsDisposed = false;

	public PStackFrame(PThread thread, IPCDIStackFrame cdiFrame) {
		super((PDebugTarget) thread.getDebugTarget());
		setCDIStackFrame(cdiFrame);
		setThread(thread);
		getCDISession().getEventManager().addEventListener(this);
	}
	public IThread getThread() {
		return fThread;
	}
	public IVariable[] getVariables() throws DebugException {
		IPGlobalVariable[] globals = getGlobals();
		List vars = getVariables0();
		List all = new ArrayList(globals.length + vars.size());
		all.addAll(Arrays.asList(globals));
		all.addAll(vars);
		return (IVariable[]) all.toArray(new IVariable[all.size()]);
	}
	protected synchronized List getVariables0() throws DebugException {
		PThread thread = (PThread) getThread();
		if (thread.isSuspended()) {
			if (fVariables == null) {
				List vars = getAllCDIVariableObjects();
				fVariables = new ArrayList(vars.size());
				Iterator it = vars.iterator();
				while (it.hasNext()) {
					fVariables.add(PVariableFactory.createLocalVariable(this, (IPCDIVariableDescriptor) it.next()));
				}
			} else if (refreshVariables()) {
				updateVariables();
			}
			setRefreshVariables(false);
		}
		return (fVariables != null) ? fVariables : Collections.EMPTY_LIST;
	}
	protected void updateVariables() throws DebugException {
		List locals = getAllCDIVariableObjects();
		int index = 0;
		while (index < fVariables.size()) {
			IPCDIVariableDescriptor varObject = findVariable(locals, (PVariable) fVariables.get(index));
			if (varObject != null) {
				locals.remove(varObject);
				index++;
			} else {
				// remove variable
				fVariables.remove(index);
			}
		}
		// add any new locals
		Iterator newOnes = locals.iterator();
		while (newOnes.hasNext()) {
			fVariables.add(PVariableFactory.createLocalVariable(this, (IPCDIVariableDescriptor) newOnes.next()));
		}
	}
	protected IPCDIVariableDescriptor findVariable(List list, PVariable var) {
		Iterator it = list.iterator();
		while (it.hasNext()) {
			IPCDIVariableDescriptor newVarObject = (IPCDIVariableDescriptor) it.next();
			if (var.sameVariable(newVarObject))
				return newVarObject;
		}
		return null;
	}
	protected void setThread(PThread thread) {
		fThread = thread;
	}
	public boolean hasVariables() throws DebugException {
		return getVariables0().size() > 0;
	}
	public int getLineNumber() throws DebugException {
		PTPDebugCorePlugin.getDefault().getLogger().finer("");
		if (isSuspended()) {
			ISourceLocator locator = ((PDebugTarget) getDebugTarget()).getSourceLocator();
			if (locator != null && locator instanceof IAdaptable && ((IAdaptable) locator).getAdapter(IPSourceLocator.class) != null)
				return ((IPSourceLocator) ((IAdaptable) locator).getAdapter(IPSourceLocator.class)).getLineNumber(this);
			if (getCDIStackFrame() != null && getCDIStackFrame().getLocator() != null)
				return getCDIStackFrame().getLocator().getLineNumber();
		}
		return -1;
	}
	public int getCharStart() throws DebugException {
		return -1;
	}
	public int getCharEnd() throws DebugException {
		return -1;
	}
	public String getName() throws DebugException {
		IPCDILocator locator = (IPCDILocator)getCDIStackFrame().getLocator();
		String func = "";
		String file = "";
		String line = "";
		if (locator.getFunction() != null && locator.getFunction().trim().length() > 0)
			func += locator.getFunction() + "() ";
		if (locator.getFile() != null && locator.getFile().trim().length() > 0) {
			file = locator.getFile();
			if (locator.getLineNumber() != 0) {
				line = NumberFormat.getInstance().format(new Integer(locator.getLineNumber()));
			}
		} else {
			return func;
		}
		return MessageFormat.format(CoreModelMessages.getString("CStackFrame.0"), new String[] { func, file, line });
	}
	public boolean canStepInto() {
		try {
			return exists() /* && isTopStackFrame() */&& getThread().canStepInto();
		} catch (DebugException e) {
			logError(e);
			return false;
		}
	}
	public boolean canStepOver() {
		try {
			return exists() && getThread().canStepOver();
		} catch (DebugException e) {
			logError(e);
		}
		return false;
	}
	public boolean canStepReturn() {
		try {
			if (!exists()) {
				return false;
			}
			List frames = ((PThread) getThread()).computeStackFrames();
			if (frames != null && !frames.isEmpty()) {
				boolean bottomFrame = this.equals(frames.get(frames.size() - 1));
				return !bottomFrame && getThread().canStepReturn();
			}
		} catch (DebugException e) {
			logError(e);
		}
		return false;
	}
	public boolean isStepping() {
		return getThread().isStepping();
	}
	public void stepInto() throws DebugException {
		if (canStepInto()) {
			getThread().stepInto();
		}
	}
	public void stepOver() throws DebugException {
		if (canStepOver()) {
			getThread().stepOver();
		}
	}
	public void stepReturn() throws DebugException {
		if (canStepReturn()) {
			getThread().stepReturn();
		}
	}
	public boolean canResume() {
		return getThread().canResume();
	}
	public boolean canSuspend() {
		return getThread().canSuspend();
	}
	public boolean isSuspended() {
		return getThread().isSuspended();
	}
	public void resume() throws DebugException {
		getThread().resume();
	}
	public void suspend() throws DebugException {
		getThread().suspend();
	}
	public boolean canTerminate() {
		boolean exists = false;
		try {
			exists = exists();
		} catch (DebugException e) {
			logError(e);
		}
		return exists && getThread().canTerminate() || getDebugTarget().canTerminate();
	}
	public boolean isTerminated() {
		return getThread().isTerminated();
	}
	public void terminate() throws DebugException {
		if (getThread().canTerminate()) {
			getThread().terminate();
		} else {
			getDebugTarget().terminate();
		}
	}
	protected IPCDIStackFrame getCDIStackFrame() {
		return fCDIStackFrame;
	}
	protected void setCDIStackFrame(IPCDIStackFrame frame) {
		if (frame != null) {
			fLastCDIStackFrame = frame;
		} else {
			fLastCDIStackFrame = fCDIStackFrame;
		}
		fCDIStackFrame = frame;
		setRefreshVariables(true);
	}
	protected IPCDIStackFrame getLastCDIStackFrame() {
		return fLastCDIStackFrame;
	}
	protected static boolean equalFrame(IPCDIStackFrame frameOne, IPCDIStackFrame frameTwo) {
		if (frameOne == null || frameTwo == null)
			return false;
		IPCDILocator loc1 = (IPCDILocator)frameOne.getLocator();
		IPCDILocator loc2 = (IPCDILocator)frameTwo.getLocator();
		if (loc1 == null || loc2 == null)
			return false;
		if (loc1.getFile() != null && loc1.getFile().length() > 0 && loc2.getFile() != null && loc2.getFile().length() > 0 && loc1.getFile().equals(loc2.getFile())) {
			if (loc1.getFunction() != null && loc1.getFunction().length() > 0 && loc2.getFunction() != null && loc2.getFunction().length() > 0 && loc1.getFunction().equals(loc2.getFunction()))
				return true;
		}
		if ((loc1.getFile() == null || loc1.getFile().length() < 1) && (loc2.getFile() == null || loc2.getFile().length() < 1)) {
			if (loc1.getFunction() != null && loc1.getFunction().length() > 0 && loc2.getFunction() != null && loc2.getFunction().length() > 0 && loc1.getFunction().equals(loc2.getFunction()))
				return true;
		}
		if ((loc1.getFile() == null || loc1.getFile().length() < 1) && (loc2.getFile() == null || loc2.getFile().length() < 1) && (loc1.getFunction() == null || loc1.getFunction().length() < 1) && (loc2.getFunction() == null || loc2.getFunction().length() < 1)) {
			if (loc1.getAddress() == loc2.getAddress())
				return true;
		}
		return false;
	}
	protected boolean exists() throws DebugException {
		return ((PThread) getThread()).computeStackFrames().indexOf(this) != -1;
	}
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
		if (adapter == PStackFrame.class) {
			return this;
		}
		if (adapter == IPStackFrame.class) {
			return this;
		}
		if (adapter == IStackFrame.class) {
			return this;
		}
		if (adapter == IPCDIStackFrame.class) {
			return getCDIStackFrame();
		}
		if (adapter == IMemoryBlockRetrieval.class) {
			return getDebugTarget().getAdapter(adapter);
		}
		return super.getAdapter(adapter);
	}
	protected void dispose() {
		setDisposed(true);
		getCDISession().getEventManager().removeEventListener(this);
		disposeAllVariables();
		disposeExpressions();
	}
	protected void disposeAllVariables() {
		if (fVariables == null)
			return;
		Iterator it = fVariables.iterator();
		while (it.hasNext()) {
			((PVariable) it.next()).dispose();
		}
		fVariables.clear();
		fVariables = null;
	}
	protected void disposeExpressions() {
		if (fExpressions != null) {
			Iterator it = fExpressions.iterator();
			while (it.hasNext()) {
				((PExpression) it.next()).dispose();
			}
			fExpressions.clear();
		}
		fExpressions = null;
	}
	protected List getCDILocalVariableObjects() throws DebugException {
		List list = new ArrayList();
		try {
			list.addAll(Arrays.asList(getCDIStackFrame().getLocalVariableDescriptors()));
		} catch (PCDIException e) {
			targetRequestFailed(e.getMessage(), null);
		}
		return list;
	}
	protected List getCDIArgumentObjects() throws DebugException {
		List list = new ArrayList();
		try {
			list.addAll(Arrays.asList(getCDIStackFrame().getArgumentDescriptors()));
		} catch (PCDIException e) {
			targetRequestFailed(e.getMessage(), null);
		}
		return list;
	}
	protected List getAllCDIVariableObjects() throws DebugException {
		List list = new ArrayList();
		list.addAll(getCDIArgumentObjects());
		list.addAll(getCDILocalVariableObjects());
		return list;
	}
	protected boolean isTopStackFrame() throws DebugException {
		IStackFrame tos = getThread().getTopStackFrame();
		return tos != null && tos.equals(this);
	}
	public IAddress getAddress() {
		IAddressFactory factory = ((PDebugTarget) getDebugTarget()).getAddressFactory();
		return factory.createAddress(getCDIStackFrame().getLocator().getAddress());
	}
	public String getFile() {
		return getCDIStackFrame().getLocator().getFile();
	}
	public String getFunction() {
		return getCDIStackFrame().getLocator().getFunction();
	}
	public int getLevel() {
		return getCDIStackFrame().getLevel();
	}
	public int getFrameLineNumber() {
		return getCDIStackFrame().getLocator().getLineNumber();
	}
	protected synchronized void preserve() {
		preserveVariables();
		preserveExpressions();
	}
	private void preserveVariables() {
		if (fVariables == null)
			return;
		Iterator it = fVariables.iterator();
		while (it.hasNext()) {
			AbstractPVariable av = (AbstractPVariable) it.next();
			av.preserve();
		}
	}
	private void preserveExpressions() {
		if (fExpressions == null)
			return;
		Iterator it = fExpressions.iterator();
		while (it.hasNext()) {
			PExpression exp = (PExpression) it.next();
			exp.preserve();
		}
	}
	public boolean canRestart() {
		return getDebugTarget() instanceof IRestart && ((IRestart) getDebugTarget()).canRestart();
	}
	public void restart() throws DebugException {
		if (canRestart()) {
			((IRestart) getDebugTarget()).restart();
		}
	}
	private void setRefreshVariables(boolean refresh) {
		fRefreshVariables = refresh;
	}
	private boolean refreshVariables() {
		return fRefreshVariables;
	}
	public boolean canResumeWithoutSignal() {
		return (getDebugTarget() instanceof IResumeWithoutSignal && ((IResumeWithoutSignal) getDebugTarget()).canResumeWithoutSignal());
	}
	public void resumeWithoutSignal() throws DebugException {
		if (canResumeWithoutSignal()) {
			((IResumeWithoutSignal) getDebugTarget()).resumeWithoutSignal();
		}
	}
	private IPGlobalVariable[] getGlobals() {
		PGlobalVariableManager gvm = ((PDebugTarget) getDebugTarget()).getGlobalVariableManager();
		if (gvm != null) {
			return gvm.getGlobals();
		}
		return new IPGlobalVariable[0];
	}
	public String toString() {
		try {
			return getName();
		} catch (DebugException e) {
			return e.getLocalizedMessage();
		}
	}
	public String evaluateExpressionToString(String expression) throws DebugException {
		try {
			return getCDITarget().evaluateExpressionToString(getCDIStackFrame(), expression);
		} catch (PCDIException e) {
			targetRequestFailed(e.getMessage(), null);
		}
		return null;
	}
	public boolean canEvaluate() {
		PDebugTarget target = ((PDebugTarget) getDebugTarget());
		return target.supportsExpressionEvaluation() && target.isSuspended();
	}
	protected void doStepReturn() throws DebugException {
		try {
			getCDIStackFrame().stepReturn();
		} catch (PCDIException e) {
			targetRequestFailed(e.getMessage(), null);
		}
	}
	private synchronized PExpression getExpression(String expressionText) throws DebugException {
		if (isDisposed()) {
			return null;
		}
		if (fExpressions == null) {
			fExpressions = new ArrayList(5);
		}
		PExpression expression = null;
		Iterator it = fExpressions.iterator();
		while (it.hasNext()) {
			expression = (PExpression) it.next();
			if (expression.getExpressionText().compareTo(expressionText) == 0) {
				return expression;
			}
		}
		try {
			IPCDIExpression cdiExpression = (IPCDIExpression)((PDebugTarget) getDebugTarget()).getCDITarget().createExpression(expressionText);
			expression = new PExpression(this, cdiExpression, null);
			fExpressions.add(expression);
		} catch (PCDIException e) {
			targetRequestFailed(e.getMessage(), null);
		}
		return expression;
	}
	protected boolean isDisposed() {
		return fIsDisposed;
	}
	private synchronized void setDisposed(boolean isDisposed) {
		fIsDisposed = isDisposed;
	}
	public boolean canRunToLine(IFile file, int lineNumber) {
		return getThread().canResume();
	}
	public void runToLine(IFile file, int lineNumber, boolean skipBreakpoints) throws DebugException {
		if (!canRunToLine(file, lineNumber))
			return;
		runToLine(file.getLocation().lastSegment(), lineNumber, skipBreakpoints);
	}
	public boolean canRunToLine(String fileName, int lineNumber) {
		return getThread().canResume();
	}
	public void runToLine(String fileName, int lineNumber, boolean skipBreakpoints) throws DebugException {
		if (!canRunToLine(fileName, lineNumber))
			return;
		if (skipBreakpoints) {
			((PDebugTarget) getDebugTarget()).skipBreakpoints(true);
		}
		IPCDILocation location = (IPCDILocation)getCDITarget().createLineLocation(fileName, lineNumber);
		try {
			getCDIThread().stepUntil(location);
		} catch (CDIException e) {
			if (skipBreakpoints) {
				((PDebugTarget) getDebugTarget()).skipBreakpoints(false);
			}
			targetRequestFailed(e.getMessage(), e);
		}
	}
	public boolean canRunToAddress(IAddress address) {
		return getThread().canResume();
	}
	public void runToAddress(IAddress address, boolean skipBreakpoints) throws DebugException {
		if (!canRunToAddress(address))
			return;
		if (skipBreakpoints) {
			((PDebugTarget) getDebugTarget()).skipBreakpoints(true);
		}
		IPCDILocation location = (IPCDILocation)getCDITarget().createAddressLocation(new BigInteger(address.toString()));
		try {
			getCDIThread().stepUntil(location);
		} catch (CDIException e) {
			if (skipBreakpoints) {
				((PDebugTarget) getDebugTarget()).skipBreakpoints(false);
			}
			targetRequestFailed(e.getMessage(), e);
		}
	}
	public boolean canJumpToLine(IFile file, int lineNumber) {
		return getThread().canResume();
	}
	public void jumpToLine(IFile file, int lineNumber) throws DebugException {
		if (!canJumpToLine(file, lineNumber))
			return;
		jumpToLine(file.getLocation().lastSegment(), lineNumber);
	}
	public boolean canJumpToLine(String fileName, int lineNumber) {
		return getThread().canResume();
	}
	public void jumpToLine(String fileName, int lineNumber) throws DebugException {
		if (!canJumpToLine(fileName, lineNumber))
			return;
		IPCDILocation location = (IPCDILocation)getCDITarget().createLineLocation(fileName, lineNumber);
		try {
			getCDIThread().resume(location);
		} catch (PCDIException e) {
			targetRequestFailed(e.getMessage(), e);
		}
	}
	public boolean canJumpToAddress(IAddress address) {
		return getThread().canResume();
	}
	public void jumpToAddress(IAddress address) throws DebugException {
		if (!canJumpToAddress(address))
			return;
		IPCDILocation location = (IPCDILocation)getCDITarget().createAddressLocation(new BigInteger(address.toString()));
		try {
			getCDIThread().resume(location);
		} catch (PCDIException e) {
			targetRequestFailed(e.getMessage(), e);
		}
	}
	private IPCDIThread getCDIThread() {
		return (IPCDIThread)((PThread) getThread()).getCDIThread();
	}
	public IRegisterGroup[] getRegisterGroups() throws DebugException {
		PTPDebugCorePlugin.getDefault().getLogger().finer("");
		return null;
	}
	public boolean hasRegisterGroups() throws DebugException {
		PTPDebugCorePlugin.getDefault().getLogger().finer("");
		return false;
	}
	public IValue evaluateExpression(String expressionText) throws DebugException {
		PTPDebugCorePlugin.getDefault().getLogger().finer("");
		if (!isDisposed()) {
			PExpression expression = getExpression(expressionText);
			if (expression != null) {
				return expression.getValue(this);
			}
		}
		return null;
	}

	public void handleDebugEvents(IPCDIEvent[] events) {}
}
