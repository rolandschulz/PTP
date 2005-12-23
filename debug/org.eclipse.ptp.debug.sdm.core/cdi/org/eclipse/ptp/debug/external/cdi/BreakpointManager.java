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
package org.eclipse.ptp.debug.external.cdi;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.cdt.debug.core.cdi.ICDIAddressLocation;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDIFunctionLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILineLocation;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
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
import org.eclipse.ptp.debug.core.sourcelookup.IPSourceLookupDirector;
import org.eclipse.ptp.debug.external.PTPDebugExternalPlugin;
import org.eclipse.ptp.debug.external.cdi.breakpoints.AddressBreakpoint;
import org.eclipse.ptp.debug.external.cdi.breakpoints.Breakpoint;
import org.eclipse.ptp.debug.external.cdi.breakpoints.FunctionBreakpoint;
import org.eclipse.ptp.debug.external.cdi.breakpoints.LineBreakpoint;
import org.eclipse.ptp.debug.external.cdi.breakpoints.Watchpoint;
import org.eclipse.ptp.debug.external.cdi.model.AddressLocation;
import org.eclipse.ptp.debug.external.cdi.model.FunctionLocation;
import org.eclipse.ptp.debug.external.cdi.model.LineLocation;
import org.eclipse.ptp.debug.external.cdi.model.Target;
import org.eclipse.ptp.debug.external.commands.AbstractBreakpointCommand;
import org.eclipse.ptp.debug.external.commands.DeleteBreakpointCommand;
import org.eclipse.ptp.debug.external.commands.SetFunctionBreakpointCommand;
import org.eclipse.ptp.debug.external.commands.SetLineBreakpointCommand;

public class BreakpointManager extends Manager {
	public static IPCDIBreakpoint[] EMPTY_BREAKPOINTS = {};

	Map breakMap = new HashMap();
	Map cdiBbreakMap = new HashMap();
	Map cdiBreakIDMap = new HashMap();
	boolean allowInterrupt;

	public BreakpointManager(Session session) {
		super(session, false);
		allowInterrupt = true;
	}
	public void shutdown() {
		breakMap.clear();
		cdiBbreakMap.clear();
		cdiBreakIDMap.clear();
	}
	private void addBreakpoint(IPBreakpoint bpt, IPCDIBreakpoint cdiBpt) {
		breakMap.put(bpt, cdiBpt);
		cdiBbreakMap.put(cdiBpt, bpt);
		cdiBreakIDMap.put(new Integer(cdiBpt.getBreakpointId()), cdiBpt);		
	}
	private void removeBreakpoint(IPBreakpoint bpt) {
		IPCDIBreakpoint cdiBpt = (IPCDIBreakpoint)breakMap.remove(bpt);
		cdiBbreakMap.remove(cdiBpt);
		cdiBreakIDMap.remove(new Integer(cdiBpt.getBreakpointId()));
	}
	public IPBreakpoint findBreakpoint(IPCDIBreakpoint cdiBpt) {
		return (IPBreakpoint)cdiBbreakMap.get(cdiBpt);
	}
	public IPBreakpoint findBreakpoint(int bpid) {
		IPCDIBreakpoint cdiBpt = findCDIBreakpoint(bpid);
		if (cdiBpt != null) {
			return findBreakpoint(cdiBpt);
		}
		return null;
	}
	public IPCDIBreakpoint findCDIBreakpoint(IPBreakpoint bpt) {
		return (IPCDIBreakpoint)breakMap.get(bpt);
	}
	public IPCDIBreakpoint findCDIBreakpoint(int bpid) {
		return (IPCDIBreakpoint)cdiBreakIDMap.get(new Integer(bpid));
	}
	public void deleteBreakpoint(String job_id, IPBreakpoint bpt) throws CoreException {
		BitList tasks = PTPDebugCorePlugin.getDebugModel().getTasks(job_id, bpt.getSetId());
		if (breakMap.containsKey(bpt)) {
			IPCDIBreakpoint cdiBpt = findCDIBreakpoint(bpt);
			AbstractBreakpointCommand command = getDeleteBreakpointCommand(tasks, cdiBpt);
			Session session = (Session)getSession();
			session.getDebugger().postCommandAndWait(command);
			removeBreakpoint(bpt);
		}
	}
	public void setBreakpoint(String job_id, IPBreakpoint bpt) throws CoreException {
		BitList tasks = PTPDebugCorePlugin.getDebugModel().getTasks(job_id, bpt.getSetId());
		IPCDILocation location = getLocation(bpt);
		try {
			setLocationBreakpointOnSession(bpt, location, null, bpt.isEnabled(), tasks);
		} catch (PCDIException e) {
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugExternalPlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), null));			
		}
	}	
	public void setInitialBreakpoints() throws CoreException{
		String job_id = ((Session)getSession()).getJob().getIDString();
		IPBreakpoint[] bpts = PTPDebugCorePlugin.getDebugModel().findPBreakpointsByJob(job_id, true);
		for (int i = 0; i < bpts.length; i++) {
			setBreakpoint(job_id, bpts[i]);
		}
	}
	private IPCDILocation getLocation(IPBreakpoint bpt) throws CoreException {
		IPath path = convertPath(bpt.getSourceHandle());
		if (bpt instanceof IPLineBreakpoint) {
			return createLineLocation(path.lastSegment(), ((IPLineBreakpoint)bpt).getLineNumber());
		}
		else if (bpt instanceof IPFunctionBreakpoint) {
			return createFunctionLocation(path.lastSegment(), ((IPFunctionBreakpoint)bpt).getFunction());
		}
		return null;
	}
	private void setLocationBreakpointOnSession(final IPBreakpoint bpt, final IPCDILocation location, final ICDICondition condition, final boolean enabled, final BitList tasks) throws PCDIException {
		IPCDIBreakpoint cdiBpt = null;
		if (bpt instanceof IPLineBreakpoint) {
			cdiBpt = createLineBreakpoint(tasks, IPCDIBreakpoint.REGULAR, (ICDILineLocation) location, condition, enabled);
		} else if (bpt instanceof IPFunctionBreakpoint) {
			cdiBpt = createFunctionBreakpoint(tasks, IPCDIBreakpoint.REGULAR, (ICDIFunctionLocation) location, condition, enabled);
		}
		cdiBpt = setBreakpointCommand(tasks, cdiBpt);
		if (!breakMap.containsKey(bpt)) {
			addBreakpoint(bpt, cdiBpt);
		}
	}
	private IPath convertPath(String sourceHandle) {
		IPath path = null;
		if (Path.EMPTY.isValidPath(sourceHandle)) {
			ISourceLocator sl = ((Session)getSession()).getLaunch().getSourceLocator();
			if (sl instanceof IPSourceLookupDirector) {
				path = ((IPSourceLookupDirector) sl).getCompilationPath(sourceHandle);
			}
			if (path == null) {
				path = new Path(sourceHandle);
			}
		}
		return path;
	}
	public void setInternalTemporaryBreakpoint(BitList tasks, IPCDILocation location) throws DebugException {
		IPCDIBreakpoint cdiBpt = null;
		try {
			if (location instanceof ICDIFunctionLocation) {
				cdiBpt = createFunctionBreakpoint(tasks, IPCDIBreakpoint.TEMPORARY, (ICDIFunctionLocation) location, null, false);
			} else if (location instanceof ICDILineLocation) {
				cdiBpt = createLineBreakpoint(tasks, IPCDIBreakpoint.TEMPORARY, (ICDILineLocation) location, null, false);
			} else if (location instanceof ICDIAddressLocation) {
				cdiBpt = createAddressBreakpoint(tasks, IPCDIBreakpoint.TEMPORARY, (ICDIAddressLocation) location, null, false);
			}
			cdiBpt = setBreakpointCommand(tasks, cdiBpt);
		} catch (PCDIException e) {
			throw new DebugException(new Status(IStatus.ERROR, PTPDebugExternalPlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), null));			
		}
		cdiBreakIDMap.put(new Integer(cdiBpt.getBreakpointId()), cdiBpt);
	}
	public IPCDILineBreakpoint setLineBreakpoint(BitList tasks, int type, ICDILineLocation location, ICDICondition condition, boolean deferred) throws PCDIException {
		IPCDILineBreakpoint cdiLineBpt = createLineBreakpoint(tasks, type, location, condition, deferred);
		IPCDIBreakpoint cdiBpt = setBreakpointCommand(tasks, cdiLineBpt);
		cdiBreakIDMap.put(new Integer(cdiBpt.getBreakpointId()), cdiBpt);
		return cdiLineBpt;
	}
	public IPCDIFunctionBreakpoint setFunctionBreakpoint(BitList tasks, int type, ICDIFunctionLocation location, ICDICondition condition, boolean deferred) throws PCDIException {
		IPCDIFunctionBreakpoint cdiFuncBpt = createFunctionBreakpoint(tasks, type, location, condition, deferred);
		IPCDIBreakpoint cdiBpt = setBreakpointCommand(tasks, cdiFuncBpt);
		cdiBreakIDMap.put(new Integer(cdiBpt.getBreakpointId()), cdiBpt);
		return cdiFuncBpt;
	}
	public IPCDIAddressBreakpoint setAddressBreakpoint(BitList tasks, int type, ICDIAddressLocation location, ICDICondition condition, boolean deferred) throws PCDIException {
		IPCDIAddressBreakpoint cdiAddrBpt = createAddressBreakpoint(tasks, type, location, condition, deferred);
		IPCDIBreakpoint cdiBpt = setBreakpointCommand(tasks, cdiAddrBpt);
		cdiBreakIDMap.put(new Integer(cdiBpt.getBreakpointId()), cdiBpt);
		return cdiAddrBpt;
	}
	
	public IPCDIWatchpoint setWatchpoint(BitList tasks, int type, int watchType, String expression, ICDICondition condition) throws PCDIException {
		try {
			// Check if this an address watchpoint, and add a '*'
			Integer.decode(expression);
			expression = '*' + expression;
		} catch (NumberFormatException e) {
			//
		}
		Watchpoint bkpt = new Watchpoint(expression, type, watchType, condition);
		setWatchpoint(tasks, bkpt);

		// Fire a created Event.
		throw new PCDIException("Not implement - setWatchpoint");
		//return bkpt;
	}
	public void setWatchpoint(BitList tasks, Watchpoint watchpoint) throws PCDIException {
		/*
		Session session = (Session)getSession();
		boolean access = watchpoint.isReadType() && watchpoint.isWriteType();
		boolean read = ! watchpoint.isWriteType() && watchpoint.isReadType();
		String expression = watchpoint.getWatchExpression();
		*/
		//TODO - implement set watch point
		//session.getDebugger().setWatchpoint(tasks, access, read, expression);
		throw new PCDIException("Not implement yet - setWatchpoint");
	}

	public IPCDIExceptionpoint setExceptionpoint(BitList tasks, String clazz, boolean stopOnThrow, boolean stopOnCatch) throws PCDIException {
		if (!stopOnThrow && !stopOnCatch) {
			throw new PCDIException("Must suspend on throw or catch");
		}
		//Exceptionpoint excp = new Exceptionpoint(tasks, clazz, stopOnThrow, stopOnCatch, null);
		//TODO - implement set exception point
		//session.getDebugger().setExceptionpoint(watchpoint.getTasks(), access, read, expression);
		//session.getDebugger().fireEvent(new BreakpointCreatedEvent(session, bkpt.getTasks()));
		throw new PCDIException("Not implement yet - setExceptionpoint");
		//return excp;
	}
	
	public void setBreakpointPending(BitList tasks, boolean set) throws PCDIException { 
		//TODO - implement set setBreakpointPending
		//session.getDebugger().setBreakpointPending(watchpoint.getTasks(), access, read, expression);
		throw new PCDIException("Not implement yet - setBreakpointPending");
	}
	public Condition createCondition(int ignoreCount, String expression, String[] tids) {
		return new Condition(ignoreCount, expression, tids);
	}
	public LineLocation createLineLocation(String file, int line) {
		return new LineLocation(file, line);
	}
	public FunctionLocation createFunctionLocation(String file, String function) {
		return new FunctionLocation(file, function);
	}
	public AddressLocation createAddressLocation(BigInteger address) {
		return new AddressLocation(address);
	}
	public void update(Target target) throws PCDIException {
		if (target == null)
			throw new PCDIException("No target");
		
		//TODO - dunno what implement here
		throw new PCDIException("Not implement yet -- BreakpointManager: update");
	}

	public void setCondition(Breakpoint breakpoint, ICDICondition newCondition) throws PCDIException {
		//Target target = (Target)breakpoint.getTarget();
		throw new PCDIException("Not implement yet -- BreakpointManager: setCondition");
	}
	public void disableBreakpoint(Breakpoint breakpoint) throws PCDIException {
		//Target target = (Target)breakpoint.getTarget();
		throw new PCDIException("Not implement yet -- BreakpointManager: disableBreakpoint");
	}
	public void enableBreakpoint(Breakpoint breakpoint) throws PCDIException {
		//Target target = (Target)breakpoint.getTarget();
		throw new PCDIException("Not implement yet -- BreakpointManager: enableBreakpoint");
	}

	/** create breakpoint **/
	private IPCDILineBreakpoint createLineBreakpoint(BitList tasks, int type, ICDILineLocation location, ICDICondition condition, boolean deferred) throws PCDIException {		
		return new LineBreakpoint(type, location, condition);
	}
	private IPCDIFunctionBreakpoint createFunctionBreakpoint(BitList tasks, int type, ICDIFunctionLocation location, ICDICondition condition, boolean deferred) throws PCDIException {		
		return new FunctionBreakpoint(type, location, condition);
	}
	private IPCDIAddressBreakpoint createAddressBreakpoint(BitList tasks, int type, ICDIAddressLocation location, ICDICondition condition, boolean deferred) throws PCDIException {		
		return new AddressBreakpoint(type, location, condition);
	}
	
	/** command **/
	private IPCDIBreakpoint setBreakpointCommand(BitList tasks, IPCDIBreakpoint bkpt) throws PCDIException {
		Session session = (Session)getSession();
		AbstractBreakpointCommand command = getSetBreakpointCommand(tasks, bkpt);
		if (command != null) {
			session.getDebugger().postCommand(command);
			IPCDIBreakpoint cdiBpt = command.getBreakpoint();
			if (cdiBpt != null)
				return cdiBpt;
		}
		throw new PCDIException("No breakpoint created");
	}
	private AbstractBreakpointCommand getSetBreakpointCommand(BitList tasks, IPCDIBreakpoint bkpt) {
		if (bkpt instanceof IPCDILineBreakpoint) {
			return new SetLineBreakpointCommand(tasks, (IPCDILineBreakpoint) bkpt);
		}
		else if (bkpt instanceof IPCDIFunctionBreakpoint) {
			return new SetFunctionBreakpointCommand(tasks, (IPCDIFunctionBreakpoint) bkpt);
		}
		else if (bkpt instanceof IPCDIAddressBreakpoint) {
			
		}
		return null;
	}
	private AbstractBreakpointCommand getDeleteBreakpointCommand(BitList tasks, IPCDIBreakpoint bkpt) {
		return new DeleteBreakpointCommand(tasks, bkpt);
	}	
}
