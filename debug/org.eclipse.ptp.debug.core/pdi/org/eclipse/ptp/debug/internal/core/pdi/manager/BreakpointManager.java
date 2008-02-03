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
package org.eclipse.ptp.debug.internal.core.pdi.manager;


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.pdi.IPDIAddressLocation;
import org.eclipse.ptp.debug.core.pdi.IPDICondition;
import org.eclipse.ptp.debug.core.pdi.IPDIFunctionLocation;
import org.eclipse.ptp.debug.core.pdi.IPDILineLocation;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.PDILocationFactory;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIBreakpointManager;
import org.eclipse.ptp.debug.core.pdi.model.IPDIAddressBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDIExceptionpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDIFunctionBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDILineBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDILocationBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDIWatchpoint;
import org.eclipse.ptp.debug.core.pdi.request.IPDIEventRequest;

/**
 * @author clement
 *
 */
public class BreakpointManager extends AbstractPDIManager implements IPDIBreakpointManager {
	public static IPDIBreakpoint[] EMPTY_BREAKPOINTS = {};
	private final static String[] EXCEPTION_FUNCS = new String[] {"__cxa_throw", "__cxa_begin_catch"};
	private boolean allowInterrupt;

	private List<IPDIBreakpoint> breakList = null;
	
	private IPDIBreakpoint[] exceptionBps = new IPDIBreakpoint[2];
	
	private final int EXCEPTION_THROW_IDX = 0;
	
	private final int EXCEPTION_CATCH_IDX = 1;
	
	public BreakpointManager(IPDISession session) {
		super(session, false);
		breakList = Collections.synchronizedList(new ArrayList<IPDIBreakpoint>());
		allowInterrupt = true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIBreakpointManager#addSetBreakpoint(org.eclipse.ptp.core.util.BitList, org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint)
	 */
	public void addSetBreakpoint(BitList tasks, IPDIBreakpoint breakpoint) throws PDIException {
		if (!isExisted(breakpoint.getBreakpointID())) {
			throw new PDIException(tasks, PDIResources.getString("pdi.BreakpointManager.Not_a_PTP_breakpoint"));			
		}
		breakpoint.getTasks().or(tasks);
		IPDIEventRequest request = null;
		if (breakpoint instanceof IPDIFunctionBreakpoint) {
			request = session.getRequestFactory().getSetFunctionBreakpointRequest(tasks, (IPDIFunctionBreakpoint)breakpoint, false);
		}
		else if (breakpoint instanceof IPDIAddressBreakpoint) {
			request = session.getRequestFactory().getSetAddressBreakpointRequest(tasks, (IPDIAddressBreakpoint)breakpoint, false);			
		}
		else if (breakpoint instanceof IPDILineBreakpoint) {
			request = session.getRequestFactory().getSetLineBreakpointRequest(tasks, (IPDILineBreakpoint)breakpoint, false);			
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
				getSession().getEventRequestManager().addEventRequest(session.getRequestFactory().getSuspendRequest(nonTerTasks, false));
			}
		}
		
		getSession().getEventRequestManager().addEventRequest(request);
		if (resumeTasks != null)
			getSession().getEventRequestManager().addEventRequest(session.getRequestFactory().getResumeRequest(resumeTasks, false));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIBreakpointManager#createAddressLocation(java.math.BigInteger)
	 */
	public IPDIAddressLocation createAddressLocation(BigInteger address) {
		return PDILocationFactory.newAddressLocation(address);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIBreakpointManager#createCondition(int, java.lang.String, java.lang.String[])
	 */
	public IPDICondition createCondition(int ignoreCount, String expression, String[] tids) {
		return session.getModelFactory().newCondition(ignoreCount, expression, tids);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIBreakpointManager#createFunctionLocation(java.lang.String, java.lang.String)
	 */
	public IPDIFunctionLocation createFunctionLocation(String file, String function) {
		return PDILocationFactory.newFunctionLocation(file, function);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIBreakpointManager#createLineLocation(java.lang.String, int)
	 */
	public IPDILineLocation createLineLocation(String file, int line) {
		return PDILocationFactory.newLineLocation(file, line);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIBreakpointManager#deleteAllBreakpoints()
	 */
	public void deleteAllBreakpoints() throws PDIException {
		IPDIBreakpoint[] pdiBreakpoints = getAllPDIBreakpoints();
		for (IPDIBreakpoint pdiBpt : pdiBreakpoints) {
			deleteBreakpoint(session.getTasks(), pdiBpt);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIBreakpointManager#deleteAllBreakpoints(org.eclipse.ptp.core.util.BitList)
	 */
	public void deleteAllBreakpoints(BitList tasks) throws PDIException {
		IPDIBreakpoint[] pdiBreakpoints = getAllPDIBreakpoints();
		for (IPDIBreakpoint pdiBpt : pdiBreakpoints) {
			if (pdiBpt.getTasks().intersects(tasks)) {
				deleteBreakpoint(tasks, pdiBpt);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIBreakpointManager#deleteBreakpoint(org.eclipse.ptp.core.util.BitList, org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint)
	 */
	public void deleteBreakpoint(BitList tasks, IPDIBreakpoint breakpoint) throws PDIException {
		if (!isExisted(breakpoint.getBreakpointID())) {
			throw new PDIException(tasks, PDIResources.getString("pdi.BreakpointManager.Not_a_PTP_breakpoint"));			
		}
		getSession().getEventRequestManager().addEventRequest(session.getRequestFactory().getDeleteBreakpointRequest(tasks, breakpoint, true));
		deleteBreakpoint(breakpoint.getBreakpointID());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIBreakpointManager#deleteSetBreakpoint(org.eclipse.ptp.core.util.BitList, org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint)
	 */
	public void deleteSetBreakpoint(BitList tasks, IPDIBreakpoint breakpoint) throws PDIException {
		if (!isExisted(breakpoint.getBreakpointID())) {
			throw new PDIException(tasks, PDIResources.getString("pdi.BreakpointManager.Not_a_PTP_breakpoint"));			
		}
		breakpoint.getTasks().andNot(tasks);
		getSession().getEventRequestManager().addEventRequest(session.getRequestFactory().getDeleteBreakpointRequest(tasks, breakpoint, false));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIBreakpointManager#disableBreakpoint(org.eclipse.ptp.core.util.BitList, org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint)
	 */
	public void disableBreakpoint(BitList tasks, IPDIBreakpoint breakpoint) throws PDIException {
		if (!isExisted(breakpoint.getBreakpointID())) {
			throw new PDIException(tasks, PDIResources.getString("pdi.BreakpointManager.Not_a_PTP_breakpoint"));			
		}
		breakpoint.setEnabled(false);
		getSession().getEventRequestManager().addEventRequest(session.getRequestFactory().getDisableBreakpointRequest(tasks, breakpoint));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIBreakpointManager#enableBreakpoint(org.eclipse.ptp.core.util.BitList, org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint)
	 */
	public void enableBreakpoint(BitList tasks, IPDIBreakpoint breakpoint) throws PDIException {
		if (!isExisted(breakpoint.getBreakpointID())) {
			throw new PDIException(tasks, PDIResources.getString("pdi.BreakpointManager.Not_a_PTP_breakpoint"));			
		}
		breakpoint.setEnabled(true);
		getSession().getEventRequestManager().addEventRequest(session.getRequestFactory().getEnableBreakpointRequest(tasks, breakpoint));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIBreakpointManager#getBreakpoint(int)
	 */
	public IPDIBreakpoint getBreakpoint(int id) {
		IPDIBreakpoint[] pdiBreakpoints = getAllPDIBreakpoints();
		for (IPDIBreakpoint pdiBreakpoint : pdiBreakpoints) {
			if (pdiBreakpoint.getBreakpointID() == id) {
				return pdiBreakpoint;
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIBreakpointManager#setAddressBreakpoint(org.eclipse.ptp.core.util.BitList, int, org.eclipse.ptp.debug.core.pdi.IPDIAddressLocation, org.eclipse.ptp.debug.core.pdi.IPDICondition, boolean, boolean)
	 */
	public IPDIAddressBreakpoint setAddressBreakpoint(BitList tasks, int type, IPDIAddressLocation location, IPDICondition condition, boolean deferred, boolean enabled) throws PDIException {		
		IPDIAddressBreakpoint bkpt = session.getModelFactory().newAddressBreakpoint(session, tasks, type, location, condition, enabled);
		setNewLocationBreakpoint(bkpt, deferred);
		return bkpt;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIBreakpointManager#setCondition(org.eclipse.ptp.core.util.BitList, org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint, org.eclipse.ptp.debug.core.pdi.IPDICondition)
	 */
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIBreakpointManager#setExceptionpoint(org.eclipse.ptp.core.util.BitList, java.lang.String, boolean, boolean, boolean)
	 */
	public IPDIExceptionpoint setExceptionpoint(BitList tasks, String clazz, boolean stopOnThrow, boolean stopOnCatch, boolean enabled) throws PDIException {
		if (!stopOnThrow && !stopOnCatch) {
			throw new PDIException(tasks, "Must suspend on throw or catch");
		}
		List<IPDIFunctionBreakpoint> funcBptList = new ArrayList<IPDIFunctionBreakpoint>(2);
		if (stopOnThrow) {
			synchronized(exceptionBps) {
				int id = EXCEPTION_THROW_IDX;
				if (exceptionBps[EXCEPTION_THROW_IDX] == null) {
					IPDIFunctionLocation location = PDILocationFactory.newFunctionLocation(null, EXCEPTION_FUNCS[id]);
					IPDIFunctionBreakpoint bp = session.getModelFactory().newFunctionBreakpoint(session, tasks, IPDIBreakpoint.REGULAR, location, null, enabled);
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
					IPDIFunctionLocation location = PDILocationFactory.newFunctionLocation(null, EXCEPTION_FUNCS[id]);
					IPDIFunctionBreakpoint bp = session.getModelFactory().newFunctionBreakpoint(session, tasks, IPDIBreakpoint.REGULAR, location, null, enabled);
					setLocationBreakpoint(bp);
					exceptionBps[id] = bp;
					funcBptList.add(bp);
				}
			}
		}
		IPDIExceptionpoint excp = session.getModelFactory().newExceptionpoint(session, tasks, clazz, stopOnThrow, stopOnCatch, null, enabled, funcBptList.toArray(new IPDIFunctionBreakpoint[0]));
		addBreakpoint(excp);
		return excp;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIBreakpointManager#setFunctionBreakpoint(org.eclipse.ptp.core.util.BitList, int, org.eclipse.ptp.debug.core.pdi.IPDIFunctionLocation, org.eclipse.ptp.debug.core.pdi.IPDICondition, boolean, boolean)
	 */
	public IPDIFunctionBreakpoint setFunctionBreakpoint(BitList tasks, int type, IPDIFunctionLocation location, IPDICondition condition, boolean deferred, boolean enabled) throws PDIException {		
		IPDIFunctionBreakpoint bkpt = session.getModelFactory().newFunctionBreakpoint(session, tasks, type, location, condition, enabled);
		setNewLocationBreakpoint(bkpt, deferred);
		return bkpt;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIBreakpointManager#setLineBreakpoint(org.eclipse.ptp.core.util.BitList, int, org.eclipse.ptp.debug.core.pdi.IPDILineLocation, org.eclipse.ptp.debug.core.pdi.IPDICondition, boolean, boolean)
	 */
	public IPDILineBreakpoint setLineBreakpoint(BitList tasks, int type, IPDILineLocation location, IPDICondition condition, boolean deferred, boolean enabled) throws PDIException {		
		IPDILineBreakpoint bkpt = session.getModelFactory().newLineBreakpoint(session, tasks, type, location, condition, enabled);
		setNewLocationBreakpoint(bkpt, deferred);
		return bkpt;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIBreakpointManager#setWatchpoint(org.eclipse.ptp.core.util.BitList, int, int, java.lang.String, org.eclipse.ptp.debug.core.pdi.IPDICondition, boolean)
	 */
	public IPDIWatchpoint setWatchpoint(BitList tasks, int type, int watchType, String expression, IPDICondition condition, boolean enabled) throws PDIException {
		IPDIWatchpoint bkpt = session.getModelFactory().newWatchpoint(session, tasks, type, expression, watchType, condition, enabled);
		setWatchpoint(bkpt);
		addBreakpoint(bkpt);
		return bkpt;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.internal.core.pdi.AbstractPDIManager#shutdown()
	 */
	public void shutdown() {
		//breakList.clear();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.internal.core.pdi.AbstractPDIManager#update(org.eclipse.ptp.core.util.BitList)
	 */
	public void update(BitList tasks) throws PDIException {
		//TODO
	}
	
	/**
	 * @param breakpoint
	 */
	private void addBreakpoint(IPDIBreakpoint breakpoint) {
		synchronized(breakList) {
			if (!breakpoint.isTemporary()) {
				breakList.add(breakpoint);
			}
		}
	}
	
	/**
	 * @param id
	 */
	private void deleteBreakpoint(int id) {
		IPDIBreakpoint[] pdiBreakpoints = getAllPDIBreakpoints();
		for (int i=0; i<pdiBreakpoints.length; i++) {
			if (pdiBreakpoints[i].getBreakpointID() == id) {
				breakList.remove(i);
				break;
			}
		}
	}

	/**
	 * @return
	 */
	private IPDIBreakpoint[] getAllPDIBreakpoints() {
		synchronized(breakList) {
			return breakList.toArray(new IPDIBreakpoint[0]);
		}
	}
	
	/**
	 * @param id
	 * @return
	 */
	private boolean isExisted(int id) {
		return (getBreakpoint(id) != null);
	}
	
	/**
	 * @param bkpt
	 * @throws PDIException
	 */
	private void setLocationBreakpoint(IPDILocationBreakpoint bkpt) throws PDIException {
		IPDIEventRequest request = null;
		if (bkpt instanceof IPDIFunctionBreakpoint) {
			request = session.getRequestFactory().getSetFunctionBreakpointRequest(bkpt.getTasks(), (IPDIFunctionBreakpoint)bkpt, true);
		}
		else if (bkpt instanceof IPDIAddressBreakpoint) {
			request = session.getRequestFactory().getSetAddressBreakpointRequest(bkpt.getTasks(), (IPDIAddressBreakpoint)bkpt, true);			
		}
		else if (bkpt instanceof IPDILineBreakpoint) {
			request = session.getRequestFactory().getSetLineBreakpointRequest(bkpt.getTasks(), (IPDILineBreakpoint)bkpt, true);			
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
				getSession().getEventRequestManager().addEventRequest(session.getRequestFactory().getSuspendRequest(nonTerTasks, false));
			}
		}
		getSession().getEventRequestManager().addEventRequest(request);
		if (resumeTasks != null)
			getSession().getEventRequestManager().addEventRequest(session.getRequestFactory().getResumeRequest(resumeTasks, false));

		if (!bkpt.isEnabled()) {
			disableBreakpoint(bkpt.getTasks(), bkpt);
		}
	}
	
	/**
	 * @param watchpoint
	 * @throws PDIException
	 */
	private void setWatchpoint(IPDIWatchpoint watchpoint) throws PDIException {
		IPDIEventRequest request = session.getRequestFactory().getSetWatchpointRequest(watchpoint.getTasks(), watchpoint, true);
		BitList resumeTasks = null;
		if (session.getStatus() == IPDISession.STARTED) {
			BitList nonTerTasks = watchpoint.getTasks().copy();
			getSession().getTaskManager().getRunningTasks(nonTerTasks);
			if (!nonTerTasks.isEmpty()) {
				resumeTasks = nonTerTasks.copy();
				getSession().getEventRequestManager().addEventRequest(session.getRequestFactory().getSuspendRequest(nonTerTasks, false));
			}
		}
		getSession().getEventRequestManager().addEventRequest(request);
		if (resumeTasks != null)
			getSession().getEventRequestManager().addEventRequest(session.getRequestFactory().getResumeRequest(resumeTasks, false));
	}
	
	/**
	 * @param bkpt
	 * @param deferred
	 * @throws PDIException
	 */
	protected void setNewLocationBreakpoint(IPDILocationBreakpoint bkpt, boolean deferred) throws PDIException {
		setLocationBreakpoint(bkpt);
		addBreakpoint(bkpt);
	}
}
