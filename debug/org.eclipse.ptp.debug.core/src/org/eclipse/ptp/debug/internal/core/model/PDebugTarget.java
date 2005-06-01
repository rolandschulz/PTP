package org.eclipse.ptp.debug.internal.core.model;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;
import org.eclipse.cdt.debug.core.model.CDebugElementState;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.model.ICModule;
import org.eclipse.cdt.debug.core.model.ICSignal;
import org.eclipse.cdt.debug.core.model.IDisassembly;
import org.eclipse.cdt.debug.core.model.IGlobalVariableDescriptor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.IExpressionListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.ptp.debug.core.cdi.model.IPCDITarget;
import org.eclipse.ptp.debug.core.model.IPDebugTarget;
import org.eclipse.ptp.debug.core.model.IPProcess;

public class PDebugTarget extends PDebugElement implements IPDebugTarget, ICDIEventListener, ILaunchListener, IExpressionListener {
	IPCDITarget target;
	
	public PDebugTarget(ILaunch launch, IPCDITarget t) {
		target = t;
		launch.addDebugTarget(this);
	}
	
	public boolean hasProcesses() throws DebugException {
		System.out.println("PDebugTarget.hasProcesses()");
		return false;
	}

	public IPProcess[] getProcesses() {
		System.out.println("PDebugTarget.getProcesses()");
		return null;
	}

	public IThread[] getProcessThreads(IPProcess process) throws DebugException {
		System.out.println("PDebugTarget.getProcessThreads()");
		return null;
	}

	public boolean isLittleEndian() {
		System.out.println("PDebugTarget.isLittleEndian()");
		return false;
	}

	public boolean hasSignals() throws DebugException {
		System.out.println("PDebugTarget.hasSignals()");
		return false;
	}

	public ICSignal[] getSignals() throws DebugException {
		System.out.println("PDebugTarget.getSignals()");
		return null;
	}

	public IDisassembly getDisassembly() throws DebugException {
		System.out.println("PDebugTarget.getDisassembly()");
		return null;
	}

	public boolean isPostMortem() {
		System.out.println("PDebugTarget.isPostMortem()");
		return false;
	}

	public boolean hasModules() throws DebugException {
		System.out.println("PDebugTarget.hasModules()");
		return false;
	}

	public ICModule[] getModules() throws DebugException {
		System.out.println("PDebugTarget.getModules()");
		return null;
	}

	public void loadSymbolsForAllModules() throws DebugException {
		System.out.println("PDebugTarget.loadSymbolsForAllModules()");
		
	}

	public IProcess getProcess() {
		System.out.println("PDebugTarget.getProcess()");
		return null;
	}

	public IThread[] getThreads() throws DebugException {
		System.out.println("PDebugTarget.getThreads()");
		return null;
	}

	public boolean hasThreads() throws DebugException {
		System.out.println("PDebugTarget.hasThreads()");
		return false;
	}

	public String getName() throws DebugException {
		System.out.println("PDebugTarget.getName()");
		return null;
	}

	public boolean supportsBreakpoint(IBreakpoint breakpoint) {
		System.out.println("PDebugTarget.supportsBreakpoint()");
		return false;
	}

	public String getModelIdentifier() {
		System.out.println("PDebugTarget.getModelIdentifier()");
		return null;
	}

	public IDebugTarget getDebugTarget() {
		System.out.println("PDebugTarget.getDebugTarget()");
		return null;
	}

	public ILaunch getLaunch() {
		System.out.println("PDebugTarget.getLaunch()");
		return null;
	}

	public boolean canTerminate() {
		System.out.println("PDebugTarget.canTerminate()");
		return false;
	}

	public boolean isTerminated() {
		System.out.println("PDebugTarget.isTerminated()");
		return false;
	}

	public void terminate() throws DebugException {
		System.out.println("PDebugTarget.terminate()");
		
	}

	public boolean canResume() {
		System.out.println("PDebugTarget.canResume()");
		return false;
	}

	public boolean canSuspend() {
		System.out.println("PDebugTarget.canSuspend()");
		return false;
	}

	public boolean isSuspended() {
		System.out.println("PDebugTarget.isSuspended()");
		return false;
	}

	public void resume() throws DebugException {
		System.out.println("PDebugTarget.resume()");
		
	}

	public void suspend() throws DebugException {
		System.out.println("PDebugTarget.suspend()");
		
	}

	public void breakpointAdded(IBreakpoint breakpoint) {
		System.out.println("PDebugTarget.breakpointAdded()");
		
	}

	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
		System.out.println("PDebugTarget.breakpointRemoved()");
		
	}

	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
		System.out.println("PDebugTarget.breakpointChanged()");
		
	}

	public boolean canDisconnect() {
		System.out.println("PDebugTarget.canDisconnect()");
		return false;
	}

	public void disconnect() throws DebugException {
		System.out.println("PDebugTarget.disconnect()");
		
	}

	public boolean isDisconnected() {
		System.out.println("PDebugTarget.isDisconnected()");
		return false;
	}

	public boolean supportsStorageRetrieval() {
		System.out.println("PDebugTarget.supportsStorageRetrieval()");
		return false;
	}

	public IMemoryBlock getMemoryBlock(long startAddress, long length) throws DebugException {
		System.out.println("PDebugTarget.getMemoryBlock()");
		return null;
	}

	public IBinaryObject getExecFile() {
		System.out.println("PDebugTarget.getExecFile()");
		return null;
	}

	public IGlobalVariableDescriptor[] getGlobals() throws DebugException {
		System.out.println("PDebugTarget.getGlobals()");
		return null;
	}

	public boolean canRestart() {
		System.out.println("PDebugTarget.canRestart()");
		return false;
	}

	public void restart() throws DebugException {
		System.out.println("PDebugTarget.restart()");
		
	}

	public boolean canRunToLine(IFile file, int lineNumber) {
		System.out.println("PDebugTarget.canRunToLine()");
		return false;
	}

	public void runToLine(IFile file, int lineNumber, boolean skipBreakpoints) throws DebugException {
		System.out.println("PDebugTarget.runToLine()");
		
	}

	public boolean canRunToLine(String fileName, int lineNumber) {
		System.out.println("PDebugTarget.canRunToLine()");
		return false;
	}

	public void runToLine(String fileName, int lineNumber, boolean skipBreakpoints) throws DebugException {
		System.out.println("PDebugTarget.runToLine()");
		
	}

	public boolean canRunToAddress(IAddress address) {
		System.out.println("PDebugTarget.canRunToAddress()");
		return false;
	}

	public void runToAddress(IAddress address, boolean skipBreakpoints) throws DebugException {
		System.out.println("PDebugTarget.runToAddress()");
		
	}

	public boolean canJumpToLine(IFile file, int lineNumber) {
		System.out.println("PDebugTarget.canJumpToLine()");
		return false;
	}

	public void jumpToLine(IFile file, int lineNumber) throws DebugException {
		System.out.println("PDebugTarget.jumpToLine()");
		
	}

	public boolean canJumpToLine(String fileName, int lineNumber) {
		System.out.println("PDebugTarget.canJumpToLine()");
		return false;
	}

	public void jumpToLine(String fileName, int lineNumber) throws DebugException {
		System.out.println("PDebugTarget.jumpToLine()");
		
	}

	public boolean canJumpToAddress(IAddress address) {
		System.out.println("PDebugTarget.canJumpToAddress()");
		return false;
	}

	public void jumpToAddress(IAddress address) throws DebugException {
		System.out.println("PDebugTarget.jumpToAddress()");
		
	}

	public void resumeWithoutSignal() throws DebugException {
		System.out.println("PDebugTarget.resumeWithoutSignal()");
		
	}

	public boolean canResumeWithoutSignal() {
		System.out.println("PDebugTarget.canResumeWithoutSignal()");
		return false;
	}

	public CDebugElementState getState() {
		System.out.println("PDebugTarget.getState()");
		return null;
	}

	public Object getCurrentStateInfo() {
		System.out.println("PDebugTarget.getCurrentStateInfo()");
		return null;
	}

	public boolean isTargetBreakpoint(ICBreakpoint breakpoint) {
		System.out.println("PDebugTarget.isTargetBreakpoint()");
		return false;
	}

	public IAddress getBreakpointAddress(ICLineBreakpoint breakpoint) throws DebugException {
		System.out.println("PDebugTarget.getBreakpointAddress()");
		return null;
	}

	public boolean supportsInstructionStepping() {
		System.out.println("PDebugTarget.supportsInstructionStepping()");
		return false;
	}

	public void enableInstructionStepping(boolean enabled) {
		System.out.println("PDebugTarget.enableInstructionStepping()");
		
	}

	public boolean isInstructionSteppingEnabled() {
		System.out.println("PDebugTarget.isInstructionSteppingEnabled()");
		return false;
	}

	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		System.out.println("PDebugTarget.addPropertyChangeListener()");
		
	}

	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		System.out.println("PDebugTarget.removePropertyChangeListener()");
		
	}

	public void handleDebugEvents(ICDIEvent[] event) {
		System.out.println("PDebugTarget.handleDebugEvents()");
		
	}

	public void launchRemoved(ILaunch launch) {
		System.out.println("PDebugTarget.launchRemoved()");
		
	}

	public void launchAdded(ILaunch launch) {
		System.out.println("PDebugTarget.launchAdded()");
		
	}

	public void launchChanged(ILaunch launch) {
		System.out.println("PDebugTarget.launchChanged()");
		
	}

	public void expressionAdded(IExpression expression) {
		System.out.println("PDebugTarget.expressionAdded()");
		
	}

	public void expressionRemoved(IExpression expression) {
		System.out.println("PDebugTarget.expressionRemoved()");
		
	}

	public void expressionChanged(IExpression expression) {
		System.out.println("PDebugTarget.expressionChanged()");
		
	}

	public boolean isOK() {
		System.out.println("PDebugTarget.isOK()");
		return false;
	}

	public int getSeverity() {
		System.out.println("PDebugTarget.getSeverity()");
		return 0;
	}

	public String getMessage() {
		System.out.println("PDebugTarget.getMessage()");
		return null;
	}
}
