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
/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.external.cdi;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIAddressLocation;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDIFunctionLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILineLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.model.ICDIAddressBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExceptionpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIFunctionBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDILineBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIWatchpoint;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.PCDIDebugModel;
import org.eclipse.ptp.debug.core.model.IPBreakpoint;
import org.eclipse.ptp.debug.core.model.IPFunctionBreakpoint;
import org.eclipse.ptp.debug.core.model.IPLineBreakpoint;
import org.eclipse.ptp.debug.core.sourcelookup.ICSourceLookupDirector;
import org.eclipse.ptp.debug.external.PTPDebugExternalPlugin;
import org.eclipse.ptp.debug.external.cdi.breakpoints.AddressBreakpoint;
import org.eclipse.ptp.debug.external.cdi.breakpoints.Breakpoint;
import org.eclipse.ptp.debug.external.cdi.breakpoints.FunctionBreakpoint;
import org.eclipse.ptp.debug.external.cdi.breakpoints.LineBreakpoint;
import org.eclipse.ptp.debug.external.cdi.breakpoints.LocationBreakpoint;
import org.eclipse.ptp.debug.external.cdi.breakpoints.Watchpoint;
import org.eclipse.ptp.debug.external.cdi.event.BreakpointCreatedEvent;
import org.eclipse.ptp.debug.external.cdi.model.AddressLocation;
import org.eclipse.ptp.debug.external.cdi.model.FunctionLocation;
import org.eclipse.ptp.debug.external.cdi.model.LineLocation;
import org.eclipse.ptp.debug.external.cdi.model.Target;

public class BreakpointManager extends Manager {
	public static ICDIBreakpoint[] EMPTY_BREAKPOINTS = {};

	Map breakMap;
	Map deferredMap;
	boolean allowInterrupt;

	public BreakpointManager(Session session) {
		super(session, false);
		breakMap = Collections.synchronizedMap(new HashMap());
		deferredMap = Collections.synchronizedMap(new HashMap());
		allowInterrupt = true;
	}
	synchronized List getBreakpointsList(Target target) {
		List bList = (List)breakMap.get(target);
		if (bList == null) {
			bList = Collections.synchronizedList(new ArrayList());
			breakMap.put(target, bList);
		}
		return bList;
	}
	
	public void setBreakpoint(String job_id, IPBreakpoint bpt) throws CoreException {
		BitList tasks = PCDIDebugModel.getDefault().getTasks(job_id, bpt.getSetId());
		ICDILocation location = getLocation(bpt);
		try {
			setLocationBreakpointOnSession(bpt, location, null, bpt.isEnabled(), tasks);
		} catch (CDIException e) {
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugExternalPlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), null));			
		}
	}	
	public void setInitialBreakpoints() throws CoreException{
		String job_id = ((Session)getSession()).getJob().getIDString();
		IPBreakpoint[] bpts = PCDIDebugModel.getDefault().findPBreakpointsByJob(job_id, true);
		for (int i = 0; i < bpts.length; i++) {
			setBreakpoint(job_id, bpts[i]);
		}
	}
	private ICDILocation getLocation(IPBreakpoint bpt) throws CoreException {
		IPath path = convertPath(bpt.getSourceHandle());
		if (bpt instanceof IPLineBreakpoint) {
			return createLineLocation(path.lastSegment(), ((IPLineBreakpoint)bpt).getLineNumber());
		}
		else if (bpt instanceof IPFunctionBreakpoint) {
			return createFunctionLocation(path.lastSegment(), ((IPFunctionBreakpoint)bpt).getFunction());
		}
		return null;
	}
	private void setLocationBreakpointOnSession(final IPBreakpoint breakpoint, final ICDILocation location, final ICDICondition condition, final boolean enabled, final BitList tasks) throws CDIException {
		if (breakpoint instanceof IPLineBreakpoint) {
			setLineBreakpoint(tasks, ICDIBreakpoint.REGULAR, (ICDILineLocation) location, condition, enabled);
		} else if (breakpoint instanceof IPFunctionBreakpoint) {
			setFunctionBreakpoint(tasks, ICDIBreakpoint.REGULAR, (ICDIFunctionLocation) location, condition, enabled);
		}
	}
	private IPath convertPath(String sourceHandle) {
		IPath path = null;
		if (Path.EMPTY.isValidPath(sourceHandle)) {
			ISourceLocator sl = ((Session)getSession()).getLaunch().getSourceLocator();
			if (sl instanceof ICSourceLookupDirector) {
				path = ((ICSourceLookupDirector) sl).getCompilationPath(sourceHandle);
			}
			if (path == null) {
				path = new Path(sourceHandle);
			}
		}
		return path;
	}		
	public void setInternalTemporaryBreakpoint(BitList tasks, ICDILocation location) throws DebugException {
		try {
			if (location instanceof ICDIFunctionLocation) {
				setFunctionBreakpoint(tasks, ICDIBreakpoint.TEMPORARY, (ICDIFunctionLocation) location, null, false);
			} else if (location instanceof ICDILineLocation) {
				setLineBreakpoint(tasks, ICDIBreakpoint.TEMPORARY, (ICDILineLocation) location, null, false);
			} else if (location instanceof ICDIAddressLocation) {
				setAddressBreakpoint(tasks, ICDIBreakpoint.TEMPORARY, (ICDIAddressLocation) location, null, false);
			}
		} catch (CDIException e) {
		}
	}
	
	public ICDILineBreakpoint setLineBreakpoint(BitList tasks, int type, ICDILineLocation location, ICDICondition condition, boolean deferred) throws CDIException {		
		LineBreakpoint bkpt = new LineBreakpoint(type, location, condition);
		setNewLocationBreakpoint(tasks, bkpt, deferred);
		return bkpt;
	}
	public ICDIFunctionBreakpoint setFunctionBreakpoint(BitList tasks, int type, ICDIFunctionLocation location, ICDICondition condition, boolean deferred) throws CDIException {		
		FunctionBreakpoint bkpt = new FunctionBreakpoint(type, location, condition);
		setNewLocationBreakpoint(tasks, bkpt, deferred);
		return bkpt;
	}
	public ICDIAddressBreakpoint setAddressBreakpoint(BitList tasks, int type, ICDIAddressLocation location, ICDICondition condition, boolean deferred) throws CDIException {		
		AddressBreakpoint bkpt = new AddressBreakpoint(type, location, condition);
		setNewLocationBreakpoint(tasks, bkpt, deferred);
		return bkpt;
	}
	protected void setNewLocationBreakpoint(BitList tasks, LocationBreakpoint bkpt, boolean deferred) throws CDIException {
		setLocationBreakpoint(tasks, bkpt);
		Session session = (Session)getSession();
		session.getDebugger().fireEvent(new BreakpointCreatedEvent(session, tasks));
	}
	public ICDIWatchpoint setWatchpoint(BitList tasks, int type, int watchType, String expression, ICDICondition condition) throws CDIException {
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
		throw new CDIException("Not implement - setWatchpoint");
		//return bkpt;
	}
	public void setLocationBreakpoint(BitList tasks, LocationBreakpoint bkpt) throws CDIException {
		Session session = (Session)getSession();

		if (bkpt instanceof LineBreakpoint) {
			session.getDebugger().setLineBreakpoint(tasks, (ICDILineBreakpoint) bkpt);
		} else if (bkpt instanceof FunctionBreakpoint) {
			session.getDebugger().setFunctionBreakpoint(tasks, (ICDIFunctionBreakpoint) bkpt);
		}
	}

	public void setWatchpoint(BitList tasks, Watchpoint watchpoint) throws CDIException {
		/*
		Session session = (Session)getSession();
		boolean access = watchpoint.isReadType() && watchpoint.isWriteType();
		boolean read = ! watchpoint.isWriteType() && watchpoint.isReadType();
		String expression = watchpoint.getWatchExpression();
		*/
		//TODO - implement set watch point
		//session.getDebugger().setWatchpoint(tasks, access, read, expression);
		throw new CDIException("Not implement yet - setWatchpoint");
	}

	public ICDIExceptionpoint setExceptionpoint(BitList tasks, String clazz, boolean stopOnThrow, boolean stopOnCatch) throws CDIException {
		if (!stopOnThrow && !stopOnCatch) {
			throw new CDIException("Must suspend on throw or catch");
		}
		//Exceptionpoint excp = new Exceptionpoint(tasks, clazz, stopOnThrow, stopOnCatch, null);
		//TODO - implement set exception point
		//session.getDebugger().setExceptionpoint(watchpoint.getTasks(), access, read, expression);
		//session.getDebugger().fireEvent(new BreakpointCreatedEvent(session, bkpt.getTasks()));
		throw new CDIException("Not implement yet - setExceptionpoint");
		//return excp;
	}
	
	public void setBreakpointPending(BitList tasks, boolean set) throws CDIException { 
		//TODO - implement set setBreakpointPending
		//session.getDebugger().setBreakpointPending(watchpoint.getTasks(), access, read, expression);
		throw new CDIException("Not implement yet - setBreakpointPending");
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
	public void update(Target target) throws CDIException {
		if (target == null)
			throw new CDIException("No target");
		
		//TODO - dunno what implement here
		throw new CDIException("Not implement yet -- BreakpointManager: update");
	}

	public void setCondition(Breakpoint breakpoint, ICDICondition newCondition) throws CDIException {
		//Target target = (Target)breakpoint.getTarget();
		throw new CDIException("Not implement yet -- BreakpointManager: setCondition");
	}
	public void disableBreakpoint(Breakpoint breakpoint) throws CDIException {
		//Target target = (Target)breakpoint.getTarget();
		throw new CDIException("Not implement yet -- BreakpointManager: disableBreakpoint");
	}
	public void enableBreakpoint(Breakpoint breakpoint) throws CDIException {
		//Target target = (Target)breakpoint.getTarget();
		throw new CDIException("Not implement yet -- BreakpointManager: enableBreakpoint");
	}
}
