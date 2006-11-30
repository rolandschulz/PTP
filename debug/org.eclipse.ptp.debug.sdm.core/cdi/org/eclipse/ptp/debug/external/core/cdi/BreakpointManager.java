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
package org.eclipse.ptp.debug.external.core.cdi;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointsListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.PDebugUtils;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.cdi.IPCDIAddressLocation;
import org.eclipse.ptp.debug.core.cdi.IPCDIBreakpointManager;
import org.eclipse.ptp.debug.core.cdi.IPCDICondition;
import org.eclipse.ptp.debug.core.cdi.IPCDIFunctionLocation;
import org.eclipse.ptp.debug.core.cdi.IPCDILineLocation;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIAddressBreakpoint;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIBreakpoint;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIExceptionpoint;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIFunctionBreakpoint;
import org.eclipse.ptp.debug.core.cdi.model.IPCDILineBreakpoint;
import org.eclipse.ptp.debug.core.cdi.model.IPCDILocation;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIWatchpoint;
import org.eclipse.ptp.debug.core.model.IPBreakpoint;
import org.eclipse.ptp.debug.core.model.IPFunctionBreakpoint;
import org.eclipse.ptp.debug.core.model.IPLineBreakpoint;
import org.eclipse.ptp.debug.core.model.IPWatchpoint;
import org.eclipse.ptp.debug.core.sourcelookup.IPSourceLookupDirector;
import org.eclipse.ptp.debug.external.core.PTPDebugExternalPlugin;
import org.eclipse.ptp.debug.external.core.cdi.breakpoints.AddressBreakpoint;
import org.eclipse.ptp.debug.external.core.cdi.breakpoints.Breakpoint;
import org.eclipse.ptp.debug.external.core.cdi.breakpoints.FunctionBreakpoint;
import org.eclipse.ptp.debug.external.core.cdi.breakpoints.LineBreakpoint;
import org.eclipse.ptp.debug.external.core.cdi.breakpoints.Watchpoint;
import org.eclipse.ptp.debug.external.core.cdi.model.AddressLocation;
import org.eclipse.ptp.debug.external.core.cdi.model.FunctionLocation;
import org.eclipse.ptp.debug.external.core.cdi.model.LineLocation;
import org.eclipse.ptp.debug.external.core.commands.AbstractBreakpointCommand;
import org.eclipse.ptp.debug.external.core.commands.ConditionBreakpointCommand;
import org.eclipse.ptp.debug.external.core.commands.DeleteBreakpointCommand;
import org.eclipse.ptp.debug.external.core.commands.DisableBreakpointCommand;
import org.eclipse.ptp.debug.external.core.commands.EnableBreakpointCommand;
import org.eclipse.ptp.debug.external.core.commands.SetFunctionBreakpointCommand;
import org.eclipse.ptp.debug.external.core.commands.SetLineBreakpointCommand;
import org.eclipse.ptp.debug.external.core.commands.SetWatchpointCommand;

public class BreakpointManager extends SessionObject implements IPCDIBreakpointManager, IBreakpointsListener {
	public static IPCDIBreakpoint[] EMPTY_BREAKPOINTS = new IPCDIBreakpoint[0];
	Map breakMap = new HashMap();
	Map cdiBbreakMap = new HashMap();
	Map cdiBreakIDMap = new HashMap();

	public BreakpointManager(Session session) {
		super(session);
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
	}
	public void shutdown() {
		breakMap.clear();
		cdiBbreakMap.clear();
		cdiBreakIDMap.clear();
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
	}
	private void addBreakpoint(IPBreakpoint bpt, IPCDIBreakpoint cdiBpt) {
		if (bpt != null && !breakMap.containsKey(bpt)) {
			breakMap.put(bpt, cdiBpt);
			cdiBbreakMap.put(cdiBpt, bpt);
			cdiBreakIDMap.put(new Integer(cdiBpt.getBreakpointId()), cdiBpt);
		}
	}
	private void removeBreakpoint(IPBreakpoint bpt) {
		IPCDIBreakpoint cdiBpt = (IPCDIBreakpoint) breakMap.remove(bpt);
		cdiBbreakMap.remove(cdiBpt);
		cdiBreakIDMap.remove(new Integer(cdiBpt.getBreakpointId()));
	}
	public IPBreakpoint findBreakpoint(IPCDIBreakpoint cdiBpt) {
		return (IPBreakpoint) cdiBbreakMap.get(cdiBpt);
	}
	public IPBreakpoint findBreakpoint(int bpid) {
		IPCDIBreakpoint cdiBpt = findCDIBreakpoint(bpid);
		if (cdiBpt != null) {
			return findBreakpoint(cdiBpt);
		}
		return null;
	}
	public IPCDIBreakpoint findCDIBreakpoint(IPBreakpoint bpt) {
		synchronized (breakMap) {
			return (IPCDIBreakpoint) breakMap.get(bpt);
		}
	}
	public IPCDIBreakpoint findCDIBreakpoint(int bpid) {
		synchronized (cdiBreakIDMap) {
			return (IPCDIBreakpoint) cdiBreakIDMap.get(new Integer(bpid));
		}
	}
	
	public void setConditionBreakpoint(String job_id, IPBreakpoint bpt) throws CoreException {
		deleteBreakpoint(job_id, bpt);
		setBreakpoint(job_id, bpt, false);
	}
	public void setEnableBreakpoint(String job_id, IPBreakpoint bpt) throws CoreException {
		BitList tasks = PTPDebugCorePlugin.getDebugModel().getTasks(job_id, bpt.getSetId());
		if (breakMap.containsKey(bpt)) {
			IPCDIBreakpoint cdiBpt = findCDIBreakpoint(bpt);
			boolean isEnable = bpt.isEnabled();
			AbstractBreakpointCommand command = isEnable?getEnableBreakpointCommand(tasks, cdiBpt):getDisableBreakpointCommand(tasks, cdiBpt);
			getSession().getDebugger().postCommand(command);
			try {
				if (command.waitForReturn())
					cdiBpt.setEnabled(isEnable);
			} catch (PCDIException e) {
				throw new CoreException(new Status(IStatus.ERROR, PTPDebugExternalPlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), null));
			}
		}
	}
	public void deleteBreakpoint(String job_id, IPBreakpoint bpt) throws CoreException {
		BitList tasks = PTPDebugCorePlugin.getDebugModel().getTasks(job_id, bpt.getSetId());
		if (breakMap.containsKey(bpt)) {
			IPCDIBreakpoint cdiBpt = findCDIBreakpoint(bpt);
			AbstractBreakpointCommand command = getDeleteBreakpointCommand(tasks, cdiBpt);
			getSession().getDebugger().postCommand(command);
			try {
				if (command.waitForReturn())
					removeBreakpoint(bpt);
			} catch (PCDIException e) {
				throw new CoreException(new Status(IStatus.ERROR, PTPDebugExternalPlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), null));
			}
		}
	}
	public void setBreakpoint(String job_id, IPBreakpoint bpt, boolean ignoreCheck) throws CoreException {
		BitList tasks = PTPDebugCorePlugin.getDebugModel().getTasks(job_id, bpt.getSetId());
		try {
			setBreakpoint(tasks, bpt, ignoreCheck);
		} catch (PCDIException e) {
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugExternalPlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), null));
		}
	}
	private void setBreakpoint(BitList tasks, IPBreakpoint bpt, boolean ignoreCheck) throws CoreException, PCDIException {
		IPCDIBreakpoint cdiBpt = null;
		if (bpt instanceof IPLineBreakpoint) {
			cdiBpt = createCDILocationBreakpoint(tasks, IPCDIBreakpoint.REGULAR, getLocation(bpt), getCondition(bpt), bpt.isEnabled());			
		}
		else if (bpt instanceof IPWatchpoint) {
			int accessType = 0;
			accessType |= (((IPWatchpoint)bpt).isWriteType()) ? IPCDIWatchpoint.WRITE : 0;
			accessType |= (((IPWatchpoint)bpt).isReadType()) ? IPCDIWatchpoint.READ : 0;
			String expression = ((IPWatchpoint)bpt).getExpression();		
			try {
				// Check if this an address watchpoint, and add a '*'
				Integer.decode(expression);
				expression = '*' + expression;
			} catch (NumberFormatException e) {
				//
			}
			cdiBpt = new Watchpoint(expression, IPCDIBreakpoint.REGULAR, accessType, getCondition(bpt));
		}
		else {
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugExternalPlugin.getUniqueIdentifier(), IStatus.ERROR, "This is not ptp breakpoint supported", null));
		}
		postBreakpointCommand(tasks, cdiBpt, bpt, ignoreCheck);
	}
	public void setInitialBreakpoints() throws CoreException {
		String job_id = ((Session) getSession()).getJob().getIDString();
		IPBreakpoint[] bpts = PTPDebugCorePlugin.getDebugModel().findPBreakpoints(job_id, true);
		for (int i = 0; i < bpts.length; i++) {
			setBreakpoint(job_id, bpts[i], true);
		}
	}
	private IPCDICondition getCondition(IPBreakpoint breakpoint) throws CoreException {
		return createCondition(breakpoint.getIgnoreCount(), breakpoint.getCondition(), null);
	}
	private IPCDILocation getLocation(IPBreakpoint bpt) throws CoreException {
		IPath path = convertPath(bpt.getSourceHandle());
		if (bpt instanceof IPLineBreakpoint) {
			return createLineLocation(path.lastSegment(), ((IPLineBreakpoint) bpt).getLineNumber());
		} else if (bpt instanceof IPFunctionBreakpoint) {
			return createFunctionLocation(path.lastSegment(), ((IPFunctionBreakpoint) bpt).getFunction());
		}
		return null;
	}
	private IPath convertPath(String sourceHandle) {
		IPath path = null;
		if (Path.EMPTY.isValidPath(sourceHandle)) {
			ISourceLocator sl = ((Session) getSession()).getLaunch().getSourceLocator();
			if (sl instanceof IPSourceLookupDirector) {
				path = ((IPSourceLookupDirector) sl).getCompilationPath(sourceHandle);
			}
			if (path == null) {
				path = new Path(sourceHandle);
			}
		}
		return path;
	}
	private IPCDIBreakpoint createCDILocationBreakpoint(BitList tasks, int type, IPCDILocation location, IPCDICondition condition, boolean enabled) throws PCDIException {
		IPCDIBreakpoint cdiBpt = null;
		if (location instanceof IPCDIFunctionLocation) {
			cdiBpt = createFunctionBreakpoint(tasks, type, (IPCDIFunctionLocation) location, condition, enabled);
		} else if (location instanceof IPCDIAddressLocation) {
			cdiBpt = createAddressBreakpoint(tasks, type, (IPCDIAddressLocation) location, condition, enabled);
		} else {
			cdiBpt = createLineBreakpoint(tasks, type, (IPCDILineLocation) location, condition, enabled);
		}
		return cdiBpt;
	}
	public void setInternalTemporaryBreakpoint(BitList tasks, IPCDILocation location) throws DebugException {
		try {
			IPCDIBreakpoint cdiBpt = createCDILocationBreakpoint(tasks, IPCDIBreakpoint.TEMPORARY, location, null, false);
			postBreakpointCommand(tasks, cdiBpt, null, true);
		} catch (PCDIException e) {
			throw new DebugException(new Status(IStatus.ERROR, PTPDebugExternalPlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), null));
		}
	}
	public IPCDIExceptionpoint setExceptionpoint(BitList tasks, String clazz, boolean stopOnThrow, boolean stopOnCatch) throws PCDIException {
		if (!stopOnThrow && !stopOnCatch) {
			throw new PCDIException("Must suspend on throw or catch");
		}
		// Exceptionpoint excp = new Exceptionpoint(tasks, clazz, stopOnThrow, stopOnCatch, null);
		// TODO - implement set exception point
		// session.getDebugger().setExceptionpoint(watchpoint.getTasks(), access, read, expression);
		// session.getDebugger().fireEvent(new BreakpointCreatedEvent(session, bkpt.getTasks()));
		// return excp;
		PDebugUtils.println("Not implement yet - setExceptionpoint");
		throw new PCDIException("Not implement yet - setExceptionpoint");
	}
	public void setBreakpointPending(BitList tasks, boolean set) throws PCDIException {
		// TODO - implement set setBreakpointPending
		// session.getDebugger().setBreakpointPending(watchpoint.getTasks(), access, read, expression);
		PDebugUtils.println("Not implement yet - setBreakpointPending");
		throw new PCDIException("Not implement yet - setBreakpointPending");
	}
	
	public IPCDICondition createCondition(int ignoreCount, String expression, String[] tids) {
		return new Condition(ignoreCount, expression, tids);
	}
	public IPCDILineLocation createLineLocation(String file, int line) {
		return new LineLocation(file, line);
	}
	public IPCDIFunctionLocation createFunctionLocation(String file, String function) {
		return new FunctionLocation(file, function);
	}
	public IPCDIAddressLocation createAddressLocation(BigInteger address) {
		return new AddressLocation(address);
	}
	public void setCondition(Breakpoint breakpoint, IPCDICondition newCondition) throws PCDIException {
		// Target target = (Target)breakpoint.getTarget();
		throw new PCDIException("Not implement yet -- BreakpointManager: setCondition");
	}
	/** create breakpoint * */
	private IPCDILineBreakpoint createLineBreakpoint(BitList tasks, int type, IPCDILineLocation location, IPCDICondition condition, boolean deferred) throws PCDIException {
		return new LineBreakpoint(type, location, condition);
	}
	private IPCDIFunctionBreakpoint createFunctionBreakpoint(BitList tasks, int type, IPCDIFunctionLocation location, IPCDICondition condition, boolean deferred) throws PCDIException {
		return new FunctionBreakpoint(type, location, condition);
	}
	private IPCDIAddressBreakpoint createAddressBreakpoint(BitList tasks, int type, IPCDIAddressLocation location, IPCDICondition condition, boolean deferred) throws PCDIException {
		return new AddressBreakpoint(type, location, condition);
	}
	/** command * */
	private void postBreakpointCommand(BitList tasks, IPCDIBreakpoint cdiBpt, IPBreakpoint bpt, boolean ignoreCheck) throws PCDIException {
		AbstractBreakpointCommand command = getSetBreakpointCommand(tasks, cdiBpt, ignoreCheck);
		if (command == null) {
			throw new PCDIException("No breakpoint command created");
		}
		getSession().getDebugger().postCommand(command);
		addBreakpoint(bpt, command.getPCDIBreakpoint());
	}
	private AbstractBreakpointCommand getSetBreakpointCommand(BitList tasks, IPCDIBreakpoint bkpt, boolean ignoreCheck) {
		if (bkpt instanceof IPCDILineBreakpoint) {
			return new SetLineBreakpointCommand(tasks, (IPCDILineBreakpoint) bkpt, ignoreCheck);
		} else if (bkpt instanceof IPCDIFunctionBreakpoint) {
			return new SetFunctionBreakpointCommand(tasks, (IPCDIFunctionBreakpoint) bkpt, ignoreCheck);
		} else if (bkpt instanceof IPCDIAddressBreakpoint) {
		} else if (bkpt instanceof IPCDIWatchpoint) {
			return new SetWatchpointCommand(tasks, (IPCDIWatchpoint) bkpt, ignoreCheck);
		}
		return null;
	}
	private AbstractBreakpointCommand getDeleteBreakpointCommand(BitList tasks, IPCDIBreakpoint bkpt) {
		return new DeleteBreakpointCommand(tasks, bkpt, false);
	}
	private AbstractBreakpointCommand getEnableBreakpointCommand(BitList tasks, IPCDIBreakpoint bkpt) {
		return new EnableBreakpointCommand(tasks, bkpt, false);
	}
	private AbstractBreakpointCommand getDisableBreakpointCommand(BitList tasks, IPCDIBreakpoint bkpt) {
		return new DisableBreakpointCommand(tasks, bkpt, false);
	}
	private AbstractBreakpointCommand getConditionBreakpointCommand(BitList tasks, IPCDIBreakpoint bkpt, String expr) {
		return new ConditionBreakpointCommand(tasks, bkpt, expr, false);
	}
	/***************************************************************************************************************************************************************************************************
	 * Breakpoint listener
	 **************************************************************************************************************************************************************************************************/
	public void breakpointsAdded(IBreakpoint[] breakpoints){
		synchronized (breakMap) {
			for (int i=0; i<breakpoints.length; ++i) {
				if (breakpoints[i] instanceof IPBreakpoint) {
					String job_id = getSession().getJob().getIDString();
					try {
						String bp_job_id = ((IPBreakpoint)breakpoints[i]).getJobId(); 
						if (bp_job_id.equals(job_id) || bp_job_id.equals(IPBreakpoint.GLOBAL)) {
							setBreakpoint(job_id, (IPBreakpoint)breakpoints[i], false);
						}
					} catch (CoreException e) {
						PTPDebugExternalPlugin.log(e.getStatus());
						try {
							//if the breakpoint cannot added to debugger, delete the UI breakpoint.
							breakpoints[i].delete();
						} catch (CoreException ce) {
							ce.printStackTrace();
						}
					}
				}
			}
		}
	}
	public void breakpointsChanged(IBreakpoint[] breakpoints, IMarkerDelta[] deltas) {
		synchronized (breakMap) {
			for (int i=0; i<breakpoints.length; ++i) {
				if (breakpoints[i] instanceof IPBreakpoint) {
					IPBreakpoint breakpoint = (IPBreakpoint)breakpoints[i];
					String job_id = getSession().getJob().getIDString();
					try {
						String bp_job_id = ((IPBreakpoint)breakpoints[i]).getJobId(); 
						if (bp_job_id.equals(job_id) || bp_job_id.equals(IPBreakpoint.GLOBAL)) {
							IPCDIBreakpoint cdiBpt = findCDIBreakpoint(breakpoint);
							if (cdiBpt == null) {
								continue;
							}

							IPCDICondition condition0 = null;
							int ignoreCount = breakpoint.getIgnoreCount();
							int oldIgnoreCount = (deltas != null)?deltas[i].getAttribute(IPBreakpoint.IGNORE_COUNT, 0):ignoreCount;
							String condition = breakpoint.getCondition();
							String oldCondition = (deltas != null)?deltas[i].getAttribute(IPBreakpoint.CONDITION, ""):condition;

							Boolean enabled0 = null;
							boolean enabled = breakpoint.isEnabled();
							boolean oldEnabled = (deltas[i] != null)?deltas[i].getAttribute(IBreakpoint.ENABLED, true):enabled;
							if (enabled != oldEnabled && enabled != cdiBpt.isEnabled()) {
								enabled0 = (enabled)?Boolean.TRUE:Boolean.FALSE;
							}
							if (ignoreCount != oldIgnoreCount || condition.compareTo(oldCondition) != 0) {
								IPCDICondition cdiCondition = createCondition(ignoreCount, condition, null);
								if (!cdiCondition.equals(cdiBpt.getCondition())) {
									condition0 = cdiCondition;
								}
							}
							if (enabled0 != null) {
								setEnableBreakpoint(job_id, breakpoint);
							}
							if (condition0 != null) {
								setConditionBreakpoint(job_id, breakpoint);
							}
						}
					} catch (CoreException e) {
						PTPDebugExternalPlugin.log(e.getStatus());
					} catch(PCDIException e1) {
						PTPDebugExternalPlugin.log(e1);
					}
				}
			}
		}
	}
	public void breakpointsRemoved(IBreakpoint[] breakpoints, IMarkerDelta[] deltas) {
		synchronized (breakMap) {
			for (int i=0; i<breakpoints.length; ++i) {
				if (breakpoints[i] instanceof IPBreakpoint) {
					String job_id = getSession().getJob().getIDString();
					try {
						String bp_job_id = ((IPBreakpoint)breakpoints[i]).getJobId(); 
						if (bp_job_id.equals(job_id) || bp_job_id.equals(IPBreakpoint.GLOBAL)) {
							deleteBreakpoint(job_id, (IPBreakpoint)breakpoints[i]);
						}
					} catch (CoreException e) {
						PTPDebugExternalPlugin.log(e.getStatus());
					}
				}
			}
		}
	}
}
