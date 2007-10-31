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
package org.eclipse.ptp.debug.internal.core.pdi;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.pdi.IPDIAddressLocation;
import org.eclipse.ptp.debug.core.pdi.IPDIBreakpointManager;
import org.eclipse.ptp.debug.core.pdi.IPDICondition;
import org.eclipse.ptp.debug.core.pdi.IPDIFunctionLocation;
import org.eclipse.ptp.debug.core.pdi.IPDILineLocation;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.model.IPDIAddressBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDIExceptionpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDIFunctionBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDILineBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDILocationBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDIWatchpoint;
import org.eclipse.ptp.debug.core.pdi.request.IPDIEventRequest;
import org.eclipse.ptp.debug.internal.core.pdi.model.AddressBreakpoint;
import org.eclipse.ptp.debug.internal.core.pdi.model.Condition;
import org.eclipse.ptp.debug.internal.core.pdi.model.Exceptionpoint;
import org.eclipse.ptp.debug.internal.core.pdi.model.FunctionBreakpoint;
import org.eclipse.ptp.debug.internal.core.pdi.model.LineBreakpoint;
import org.eclipse.ptp.debug.internal.core.pdi.model.Watchpoint;
import org.eclipse.ptp.debug.internal.core.pdi.request.DeleteBreakpointRequest;
import org.eclipse.ptp.debug.internal.core.pdi.request.DisableBreakpointRequest;
import org.eclipse.ptp.debug.internal.core.pdi.request.EnableBreakpointRequest;
import org.eclipse.ptp.debug.internal.core.pdi.request.ResumeRequest;
import org.eclipse.ptp.debug.internal.core.pdi.request.SetAddressBreakpointRequest;
import org.eclipse.ptp.debug.internal.core.pdi.request.SetFunctionBreakpointRequest;
import org.eclipse.ptp.debug.internal.core.pdi.request.SetLineBreakpointRequest;
import org.eclipse.ptp.debug.internal.core.pdi.request.SetWatchpointRequest;
import org.eclipse.ptp.debug.internal.core.pdi.request.SuspendRequest;

/**
 * @author clement
 *
 */
public class BreakpointManager extends Manager implements IPDIBreakpointManager {
	public static IPDIBreakpoint[] EMPTY_BREAKPOINTS = {};
	boolean allowInterrupt;
	List<IPDIBreakpoint> breakList = null;

	public BreakpointManager(Session session) {
		super(session, false);
		breakList = Collections.synchronizedList(new ArrayList<IPDIBreakpoint>());
		allowInterrupt = true;
	}
	public void shutdown() {
		//breakList.clear();
	}
	private IPDIBreakpoint[] getAllPDIBreakpoints() {
		synchronized(breakList) {
			return breakList.toArray(new IPDIBreakpoint[0]);
		}
	}
	private boolean isExisted(int id) {
		return (getBreakpoint(id) != null);
	}
	public IPDIBreakpoint getBreakpoint(int id) {
		IPDIBreakpoint[] pdiBreakpoints = getAllPDIBreakpoints();
		for (IPDIBreakpoint pdiBreakpoint : pdiBreakpoints) {
			if (pdiBreakpoint.getBreakpointID() == id) {
				return pdiBreakpoint;
			}
		}
		return null;
	}
	private void deleteBreakpoint(int id) {
		IPDIBreakpoint[] pdiBreakpoints = getAllPDIBreakpoints();
		for (int i=0; i<pdiBreakpoints.length; i++) {
			if (pdiBreakpoints[i].getBreakpointID() == id) {
				breakList.remove(i);
				break;
			}
		}
	}
	private void addBreakpoint(IPDIBreakpoint breakpoint) {
		synchronized(breakList) {
			if (!breakpoint.isTemporary()) {
				breakList.add(breakpoint);
			}
		}
	}
	public void update(BitList tasks) throws PDIException {
		//TODO
	}
	public void deleteSetBreakpoint(BitList tasks, IPDIBreakpoint breakpoint) throws PDIException {
		if (!isExisted(breakpoint.getBreakpointID())) {
			throw new PDIException(tasks, PDIResources.getString("pdi.BreakpointManager.Not_a_PTP_breakpoint"));			
		}
		breakpoint.getTasks().andNot(tasks);
		getSession().getEventRequestManager().addEventRequest(new DeleteBreakpointRequest(tasks, breakpoint, false));
	}
	public void addSetBreakpoint(BitList tasks, IPDIBreakpoint breakpoint) throws PDIException {
		if (!isExisted(breakpoint.getBreakpointID())) {
			throw new PDIException(tasks, PDIResources.getString("pdi.BreakpointManager.Not_a_PTP_breakpoint"));			
		}
		breakpoint.getTasks().or(tasks);
		IPDIEventRequest request = null;
		if (breakpoint instanceof IPDIFunctionBreakpoint) {
			request = new SetFunctionBreakpointRequest(tasks, (IPDIFunctionBreakpoint)breakpoint, false);
		}
		else if (breakpoint instanceof IPDIAddressBreakpoint) {
			request = new SetAddressBreakpointRequest(tasks, (IPDIAddressBreakpoint)breakpoint, false);			
		}
		else if (breakpoint instanceof IPDILineBreakpoint) {
			request = new SetLineBreakpointRequest(tasks, (IPDILineBreakpoint)breakpoint, false);			
		}
		else {
			throw new PDIException(tasks, PDIResources.getString("pdi.Common.Not_implemented"));
		}
		BitList resumeTasks = null;
		if (session.getStatus() == IPDISession.STARTED) {
			BitList nonTerTasks = tasks.copy();
			getSession().getTaskManager().getRunningTasks(nonTerTasks);
			if (!nonTerTasks.isEmpty()) {
				resumeTasks = nonTerTasks.copy();
				getSession().getEventRequestManager().addEventRequest(new SuspendRequest(nonTerTasks, false));
			}
		}
		
		getSession().getEventRequestManager().addEventRequest(request);
		if (resumeTasks != null)
			getSession().getEventRequestManager().addEventRequest(new ResumeRequest(resumeTasks, false));
	}
	public void enableBreakpoint(BitList tasks, IPDIBreakpoint breakpoint) throws PDIException {
		if (!isExisted(breakpoint.getBreakpointID())) {
			throw new PDIException(tasks, PDIResources.getString("pdi.BreakpointManager.Not_a_PTP_breakpoint"));			
		}
		breakpoint.setEnabled(true);
		getSession().getEventRequestManager().addEventRequest(new EnableBreakpointRequest(tasks, breakpoint));
	}
	public void disableBreakpoint(BitList tasks, IPDIBreakpoint breakpoint) throws PDIException {
		if (!isExisted(breakpoint.getBreakpointID())) {
			throw new PDIException(tasks, PDIResources.getString("pdi.BreakpointManager.Not_a_PTP_breakpoint"));			
		}
		breakpoint.setEnabled(false);
		getSession().getEventRequestManager().addEventRequest(new DisableBreakpointRequest(tasks, breakpoint));
	}
	public void setCondition(BitList tasks, IPDIBreakpoint breakpoint, IPDICondition newCondition) throws PDIException {
		deleteBreakpoint(tasks, breakpoint);
		breakpoint.setCondition(newCondition);
		if (breakpoint instanceof IPDILocationBreakpoint) {
			setLocationBreakpoint((IPDILocationBreakpoint)breakpoint);
		} else if (breakpoint instanceof IPDIWatchpoint) {
			setWatchpoint((IPDIWatchpoint)breakpoint);
		} else {
			throw new PDIException(tasks, PDIResources.getString("pdi.BreakpointManager.Not_a_PTP_breakpoint"));
		}
	}
	public void deleteAllBreakpoints(BitList tasks) throws PDIException {
		IPDIBreakpoint[] pdiBreakpoints = getAllPDIBreakpoints();
		for (IPDIBreakpoint pdiBpt : pdiBreakpoints) {
			if (pdiBpt.getTasks().intersects(tasks)) {
				deleteBreakpoint(tasks, pdiBpt);
			}
		}
	}
	// delete breakpoints
	public void deleteAllBreakpoints() throws PDIException {
		IPDIBreakpoint[] pdiBreakpoints = getAllPDIBreakpoints();
		for (IPDIBreakpoint pdiBpt : pdiBreakpoints) {
			deleteBreakpoint(session.getTasks(), pdiBpt);
		}
	}
	public void deleteBreakpoint(BitList tasks, IPDIBreakpoint breakpoint) throws PDIException {
		if (!isExisted(breakpoint.getBreakpointID())) {
			throw new PDIException(tasks, PDIResources.getString("pdi.BreakpointManager.Not_a_PTP_breakpoint"));			
		}
		getSession().getEventRequestManager().addEventRequest(new DeleteBreakpointRequest(tasks, breakpoint, true));
		deleteBreakpoint(breakpoint.getBreakpointID());
	}
	// set breakpoint
	public IPDILineBreakpoint setLineBreakpoint(BitList tasks, int type, IPDILineLocation location, IPDICondition condition, boolean deferred, boolean enabled) throws PDIException {		
		IPDILineBreakpoint bkpt = new LineBreakpoint(session, tasks, type, location, condition, enabled);
		setNewLocationBreakpoint(bkpt, deferred);
		return bkpt;
	}
	public IPDIFunctionBreakpoint setFunctionBreakpoint(BitList tasks, int type, IPDIFunctionLocation location, IPDICondition condition, boolean deferred, boolean enabled) throws PDIException {		
		IPDIFunctionBreakpoint bkpt = new FunctionBreakpoint(session, tasks, type, location, condition, enabled);
		setNewLocationBreakpoint(bkpt, deferred);
		return bkpt;
	}
	public IPDIAddressBreakpoint setAddressBreakpoint(BitList tasks, int type, IPDIAddressLocation location, IPDICondition condition, boolean deferred, boolean enabled) throws PDIException {		
		IPDIAddressBreakpoint bkpt = new AddressBreakpoint(session, tasks, type, location, condition, enabled);
		setNewLocationBreakpoint(bkpt, deferred);
		return bkpt;
	}
	protected void setNewLocationBreakpoint(IPDILocationBreakpoint bkpt, boolean deferred) throws PDIException {
		setLocationBreakpoint(bkpt);
		addBreakpoint(bkpt);
	}
	public IPDIWatchpoint setWatchpoint(BitList tasks, int type, int watchType, String expression, IPDICondition condition, boolean enabled) throws PDIException {
		IPDIWatchpoint bkpt = new Watchpoint(session, tasks, type, expression, watchType, condition, enabled);
		setWatchpoint(bkpt);
		addBreakpoint(bkpt);
		return bkpt;
	}

	public void setLocationBreakpoint(IPDILocationBreakpoint bkpt) throws PDIException {
		IPDIEventRequest request = null;
		if (bkpt instanceof IPDIFunctionBreakpoint) {
			request = new SetFunctionBreakpointRequest(bkpt.getTasks(), (IPDIFunctionBreakpoint)bkpt, true);
		}
		else if (bkpt instanceof IPDIAddressBreakpoint) {
			request = new SetAddressBreakpointRequest(bkpt.getTasks(), (IPDIAddressBreakpoint)bkpt, true);			
		}
		else if (bkpt instanceof IPDILineBreakpoint) {
			request = new SetLineBreakpointRequest(bkpt.getTasks(), (IPDILineBreakpoint)bkpt, true);			
		}
		else {
			throw new PDIException(bkpt.getTasks(), PDIResources.getString("pdi.Common.Not_implemented"));
		}
		BitList resumeTasks = null;
		if (session.getStatus() == IPDISession.STARTED) {
			BitList nonTerTasks = bkpt.getTasks().copy();
			getSession().getTaskManager().getRunningTasks(nonTerTasks);
			if (!nonTerTasks.isEmpty()) {
				resumeTasks = nonTerTasks.copy();
				getSession().getEventRequestManager().addEventRequest(new SuspendRequest(nonTerTasks, false));
			}
		}
		getSession().getEventRequestManager().addEventRequest(request);
		if (resumeTasks != null)
			getSession().getEventRequestManager().addEventRequest(new ResumeRequest(resumeTasks, false));

		if (!bkpt.isEnabled()) {
			disableBreakpoint(bkpt.getTasks(), bkpt);
		}
	}
	public void setWatchpoint(IPDIWatchpoint watchpoint) throws PDIException {
		IPDIEventRequest request = new SetWatchpointRequest(watchpoint.getTasks(), watchpoint, true);
		BitList resumeTasks = null;
		if (session.getStatus() == IPDISession.STARTED) {
			BitList nonTerTasks = watchpoint.getTasks().copy();
			getSession().getTaskManager().getRunningTasks(nonTerTasks);
			if (!nonTerTasks.isEmpty()) {
				resumeTasks = nonTerTasks.copy();
				getSession().getEventRequestManager().addEventRequest(new SuspendRequest(nonTerTasks, false));
			}
		}
		getSession().getEventRequestManager().addEventRequest(request);
		if (resumeTasks != null)
			getSession().getEventRequestManager().addEventRequest(new ResumeRequest(resumeTasks, false));
	}

	IPDIBreakpoint[] exceptionBps = new IPDIBreakpoint[2];
	final int EXCEPTION_THROW_IDX = 0;
	final int EXCEPTION_CATCH_IDX = 1;
	final static String[] EXCEPTION_FUNCS = new String[] {"__cxa_throw", "__cxa_begin_catch"};

	public IPDIExceptionpoint setExceptionpoint(BitList tasks, String clazz, boolean stopOnThrow, boolean stopOnCatch, boolean enabled) throws PDIException {
		if (!stopOnThrow && !stopOnCatch) {
			throw new PDIException(tasks, "Must suspend on throw or catch");
		}
		List<IPDIFunctionBreakpoint> funcBptList = new ArrayList<IPDIFunctionBreakpoint>(2);
		if (stopOnThrow) {
			synchronized(exceptionBps) {
				int id = EXCEPTION_THROW_IDX;
				if (exceptionBps[EXCEPTION_THROW_IDX] == null) {
					FunctionLocation location = new FunctionLocation(null, EXCEPTION_FUNCS[id]);
					IPDIFunctionBreakpoint bp = new FunctionBreakpoint(session, tasks, IPDIBreakpoint.REGULAR, location, null, enabled);
					setLocationBreakpoint(bp);
					exceptionBps[id] = bp;
					funcBptList.add(bp);
				}
			}
		}
		if (stopOnCatch) {
			synchronized(exceptionBps) {
				int id = EXCEPTION_THROW_IDX;
				if (exceptionBps[id] == null) {
					FunctionLocation location = new FunctionLocation(null, EXCEPTION_FUNCS[id]);
					IPDIFunctionBreakpoint bp = new FunctionBreakpoint(session, tasks, IPDIBreakpoint.REGULAR, location, null, enabled);
					setLocationBreakpoint(bp);
					exceptionBps[id] = bp;
					funcBptList.add(bp);
				}
			}
		}
		IPDIExceptionpoint excp = new Exceptionpoint(session, tasks, clazz, stopOnThrow, stopOnCatch, null, enabled, funcBptList.toArray(new IPDIFunctionBreakpoint[0]));
		addBreakpoint(excp);
		return excp;
	}
	public IPDICondition createCondition(int ignoreCount, String expression, String[] tids) {
		return new Condition(ignoreCount, expression, tids);
	}
	public IPDILineLocation createLineLocation(String file, int line) {
		return new LineLocation(file, line);
	}
	public IPDIFunctionLocation createFunctionLocation(String file, String function) {
		return new FunctionLocation(file, function);
	}
	public IPDIAddressLocation createAddressLocation(BigInteger address) {
		return new AddressLocation(address);
	}
}
