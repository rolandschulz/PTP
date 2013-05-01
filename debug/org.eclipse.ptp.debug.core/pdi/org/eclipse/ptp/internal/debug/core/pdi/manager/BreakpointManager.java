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
package org.eclipse.ptp.internal.debug.core.pdi.manager;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.ptp.debug.core.TaskSet;
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
import org.eclipse.ptp.internal.debug.core.pdi.messages.Messages;

/**
 * @author clement
 * 
 */
public class BreakpointManager extends AbstractPDIManager implements IPDIBreakpointManager {
	public static IPDIBreakpoint[] EMPTY_BREAKPOINTS = {};
	private final static String[] EXCEPTION_FUNCS = new String[] { "__cxa_throw", "__cxa_begin_catch" }; //$NON-NLS-1$ //$NON-NLS-2$

	private final List<IPDIBreakpoint> breakList = Collections.synchronizedList(new ArrayList<IPDIBreakpoint>());

	private final IPDIBreakpoint[] exceptionBps = new IPDIBreakpoint[2];

	private final int EXCEPTION_THROW_IDX = 0;
	private final int EXCEPTION_CATCH_IDX = 1;

	public BreakpointManager(IPDISession session) {
		super(session, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.manager.IPDIBreakpointManager#addSetBreakpoint
	 * (org.eclipse.ptp.core.util.TaskSet,
	 * org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint)
	 */
	public void addSetBreakpoint(TaskSet tasks, IPDIBreakpoint breakpoint) throws PDIException {
		if (!isValid(breakpoint.getBreakpointID())) {
			throw new PDIException(tasks, Messages.BreakpointManager_0);
		}
		breakpoint.getTasks().or(tasks);
		breakpoint.getPendingTasks().or(tasks);
		setPendingBreakpoint(breakpoint);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIBreakpointManager#
	 * createAddressLocation(java.math.BigInteger)
	 */
	public IPDIAddressLocation createAddressLocation(BigInteger address) {
		return PDILocationFactory.newAddressLocation(address);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.manager.IPDIBreakpointManager#createCondition
	 * (int, java.lang.String, java.lang.String[])
	 */
	public IPDICondition createCondition(int ignoreCount, String expression, String[] tids) {
		return session.getModelFactory().newCondition(ignoreCount, expression, tids);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIBreakpointManager#
	 * createFunctionLocation(java.lang.String, java.lang.String)
	 */
	public IPDIFunctionLocation createFunctionLocation(String file, String function) {
		return PDILocationFactory.newFunctionLocation(file, function);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIBreakpointManager#
	 * createLineLocation(java.lang.String, int)
	 */
	public IPDILineLocation createLineLocation(String file, int line) {
		return PDILocationFactory.newLineLocation(file, line);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIBreakpointManager#
	 * deleteAllBreakpoints()
	 */
	public void deleteAllBreakpoints() throws PDIException {
		IPDIBreakpoint[] pdiBreakpoints = getAllPDIBreakpoints();
		for (IPDIBreakpoint pdiBpt : pdiBreakpoints) {
			deleteBreakpoint(session.getTasks(), pdiBpt);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIBreakpointManager#
	 * deleteAllBreakpoints(org.eclipse.ptp.core.util.TaskSet)
	 */
	public void deleteAllBreakpoints(TaskSet tasks) throws PDIException {
		IPDIBreakpoint[] pdiBreakpoints = getAllPDIBreakpoints();
		for (IPDIBreakpoint pdiBpt : pdiBreakpoints) {
			if (pdiBpt.getTasks().intersects(tasks)) {
				deleteBreakpoint(tasks, pdiBpt);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.manager.IPDIBreakpointManager#deleteBreakpoint
	 * (org.eclipse.ptp.core.util.TaskSet,
	 * org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint)
	 */
	public void deleteBreakpoint(TaskSet tasks, IPDIBreakpoint breakpoint) throws PDIException {
		if (!isValid(breakpoint.getBreakpointID())) {
			throw new PDIException(tasks, Messages.BreakpointManager_0);
		}
		breakpoint.setDeleted();
		deletePendingBreakpoint(breakpoint, true);
	}

	/**
	 * FIXME work out correct usage of allowUpdate
	 * 
	 * Delete a breakpoint from all suspended tasks.
	 * 
	 * @param bp
	 *            breakpoint to delete
	 * @throws PDIException
	 */
	public void deletePendingBreakpoint(IPDIBreakpoint bp, boolean allowUpdate) throws PDIException {
		TaskSet suspendedTasks = bp.getPendingTasks().copy();
		if (session.getStatus() == IPDISession.STARTED) {
			getSession().getTaskManager().getSuspendedTasks(suspendedTasks);
		}
		if (!suspendedTasks.isEmpty()) {
			getSession().getEventRequestManager().addEventRequest(
					session.getRequestFactory().getDeleteBreakpointRequest(suspendedTasks, bp, allowUpdate));
			bp.getPendingTasks().andNot(suspendedTasks);
			if (bp.getPendingTasks().isEmpty() && allowUpdate) {
				deleteBreakpoint(bp.getBreakpointID());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIBreakpointManager#
	 * deleteSetBreakpoint(org.eclipse.ptp.core.util.TaskSet,
	 * org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint)
	 */
	public void deleteSetBreakpoint(TaskSet tasks, IPDIBreakpoint breakpoint) throws PDIException {
		if (!isValid(breakpoint.getBreakpointID())) {
			throw new PDIException(tasks, Messages.BreakpointManager_0);
		}
		breakpoint.getTasks().andNot(tasks);
		breakpoint.getPendingTasks().or(tasks);
		breakpoint.setDeleted();
		deletePendingBreakpoint(breakpoint, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIBreakpointManager#
	 * disableBreakpoint(org.eclipse.ptp.core.util.TaskSet,
	 * org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint)
	 */
	public void disableBreakpoint(TaskSet tasks, IPDIBreakpoint breakpoint) throws PDIException {
		if (!isValid(breakpoint.getBreakpointID())) {
			throw new PDIException(tasks, Messages.BreakpointManager_0);
		}
		disableBreakpoint0(tasks, breakpoint);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.manager.IPDIBreakpointManager#enableBreakpoint
	 * (org.eclipse.ptp.core.util.TaskSet,
	 * org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint)
	 */
	public void enableBreakpoint(TaskSet tasks, IPDIBreakpoint breakpoint) throws PDIException {
		if (!isValid(breakpoint.getBreakpointID())) {
			throw new PDIException(tasks, Messages.BreakpointManager_0);
		}
		breakpoint.setEnabled(true);
		getSession().getEventRequestManager().addEventRequest(
				session.getRequestFactory().getEnableBreakpointRequest(tasks, breakpoint));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.manager.IPDIBreakpointManager#getBreakpoint
	 * (int)
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIBreakpointManager#
	 * setAddressBreakpoint(org.eclipse.ptp.core.util.TaskSet, int,
	 * org.eclipse.ptp.debug.core.pdi.IPDIAddressLocation,
	 * org.eclipse.ptp.debug.core.pdi.IPDICondition, boolean, boolean)
	 */
	public IPDIAddressBreakpoint setAddressBreakpoint(TaskSet tasks, int type, IPDIAddressLocation location,
			IPDICondition condition, boolean deferred, boolean enabled) throws PDIException {
		IPDIAddressBreakpoint bkpt = session.getModelFactory().newAddressBreakpoint(session, tasks, type, location, condition,
				enabled);
		setLocationBreakpoint(bkpt);
		addBreakpoint(bkpt);
		return bkpt;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.manager.IPDIBreakpointManager#setCondition
	 * (org.eclipse.ptp.core.util.TaskSet,
	 * org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint,
	 * org.eclipse.ptp.debug.core.pdi.IPDICondition)
	 */
	public void setCondition(TaskSet tasks, IPDIBreakpoint breakpoint, IPDICondition newCondition) throws PDIException {
		deleteBreakpoint(tasks, breakpoint);
		breakpoint.setCondition(newCondition);
		if (breakpoint instanceof IPDILocationBreakpoint) {
			setLocationBreakpoint((IPDILocationBreakpoint) breakpoint);
		} else if (breakpoint instanceof IPDIWatchpoint) {
			setWatchpoint((IPDIWatchpoint) breakpoint);
		} else {
			throw new PDIException(tasks, Messages.BreakpointManager_0);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIBreakpointManager#
	 * setExceptionpoint(org.eclipse.ptp.core.util.TaskSet, java.lang.String,
	 * boolean, boolean, boolean)
	 */
	public IPDIExceptionpoint setExceptionpoint(TaskSet tasks, String clazz, boolean stopOnThrow, boolean stopOnCatch,
			boolean enabled) throws PDIException {
		if (!stopOnThrow && !stopOnCatch) {
			throw new PDIException(tasks, Messages.BreakpointManager_1);
		}
		List<IPDIFunctionBreakpoint> funcBptList = new ArrayList<IPDIFunctionBreakpoint>(2);
		if (stopOnThrow) {
			synchronized (exceptionBps) {
				int id = EXCEPTION_THROW_IDX;
				if (exceptionBps[EXCEPTION_THROW_IDX] == null) {
					IPDIFunctionLocation location = PDILocationFactory.newFunctionLocation(null, EXCEPTION_FUNCS[id]);
					IPDIFunctionBreakpoint bp = session.getModelFactory().newFunctionBreakpoint(session, tasks,
							IPDIBreakpoint.REGULAR, location, null, enabled);
					setLocationBreakpoint(bp);
					exceptionBps[id] = bp;
					funcBptList.add(bp);
				}
			}
		}
		if (stopOnCatch) {
			synchronized (exceptionBps) {
				int id = EXCEPTION_THROW_IDX;
				if (exceptionBps[id] == null) {
					IPDIFunctionLocation location = PDILocationFactory.newFunctionLocation(null, EXCEPTION_FUNCS[id]);
					IPDIFunctionBreakpoint bp = session.getModelFactory().newFunctionBreakpoint(session, tasks,
							IPDIBreakpoint.REGULAR, location, null, enabled);
					setLocationBreakpoint(bp);
					exceptionBps[id] = bp;
					funcBptList.add(bp);
				}
			}
		}
		IPDIExceptionpoint excp = session.getModelFactory().newExceptionpoint(session, tasks, clazz, stopOnThrow, stopOnCatch,
				null, enabled, funcBptList.toArray(new IPDIFunctionBreakpoint[0]));
		addBreakpoint(excp);
		return excp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIBreakpointManager#
	 * setFunctionBreakpoint(org.eclipse.ptp.core.util.TaskSet, int,
	 * org.eclipse.ptp.debug.core.pdi.IPDIFunctionLocation,
	 * org.eclipse.ptp.debug.core.pdi.IPDICondition, boolean, boolean)
	 */
	public IPDIFunctionBreakpoint setFunctionBreakpoint(TaskSet tasks, int type, IPDIFunctionLocation location,
			IPDICondition condition, boolean deferred, boolean enabled) throws PDIException {
		IPDIFunctionBreakpoint bkpt = session.getModelFactory().newFunctionBreakpoint(session, tasks, type, location, condition,
				enabled);
		setLocationBreakpoint(bkpt);
		addBreakpoint(bkpt);
		return bkpt;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIBreakpointManager#
	 * setLineBreakpoint(org.eclipse.ptp.core.util.TaskSet, int,
	 * org.eclipse.ptp.debug.core.pdi.IPDILineLocation,
	 * org.eclipse.ptp.debug.core.pdi.IPDICondition, boolean, boolean)
	 */
	public IPDILineBreakpoint setLineBreakpoint(TaskSet tasks, int type, IPDILineLocation location, IPDICondition condition,
			boolean deferred, boolean enabled) throws PDIException {
		IPDILineBreakpoint bkpt = session.getModelFactory().newLineBreakpoint(session, tasks, type, location, condition, enabled);
		setLocationBreakpoint(bkpt);
		addBreakpoint(bkpt);
		return bkpt;
	}

	/**
	 * Set a breakpoint on all suspended tasks.
	 * 
	 * @param bp
	 *            breakpoint to set
	 * @throws PDIException
	 */
	public void setPendingBreakpoint(IPDIBreakpoint bp) throws PDIException {
		TaskSet suspendedTasks = bp.getPendingTasks().copy();
		if (session.getStatus() == IPDISession.STARTED) {
			getSession().getTaskManager().getSuspendedTasks(suspendedTasks);
		}
		if (!suspendedTasks.isEmpty()) {
			IPDIEventRequest request = null;
			if (bp instanceof IPDIFunctionBreakpoint) {
				request = session.getRequestFactory().getSetFunctionBreakpointRequest(suspendedTasks, (IPDIFunctionBreakpoint) bp,
						true);
			} else if (bp instanceof IPDIAddressBreakpoint) {
				request = session.getRequestFactory().getSetAddressBreakpointRequest(suspendedTasks, (IPDIAddressBreakpoint) bp,
						true);
			} else if (bp instanceof IPDILineBreakpoint) {
				request = session.getRequestFactory().getSetLineBreakpointRequest(suspendedTasks, (IPDILineBreakpoint) bp, true);
			} else if (bp instanceof IPDIWatchpoint) {
				request = session.getRequestFactory().getSetWatchpointRequest(suspendedTasks, (IPDIWatchpoint) bp, true);
			} else {
				throw new PDIException(bp.getTasks(), Messages.BreakpointManager_2);
			}
			bp.getPendingTasks().andNot(suspendedTasks);
			getSession().getEventRequestManager().addEventRequest(request);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.manager.IPDIBreakpointManager#setWatchpoint
	 * (org.eclipse.ptp.core.util.TaskSet, int, int, java.lang.String,
	 * org.eclipse.ptp.debug.core.pdi.IPDICondition, boolean)
	 */
	public IPDIWatchpoint setWatchpoint(TaskSet tasks, int type, int watchType, String expression, IPDICondition condition,
			boolean enabled) throws PDIException {
		IPDIWatchpoint bkpt = session.getModelFactory().newWatchpoint(session, tasks, type, expression, watchType, condition,
				enabled);
		setWatchpoint(bkpt);
		addBreakpoint(bkpt);
		return bkpt;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.pdi.AbstractPDIManager#shutdown()
	 */
	@Override
	public void shutdown() {
		// breakList.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.pdi.AbstractPDIManager#update(org
	 * .eclipse.ptp.core.util.TaskSet)
	 */
	@Override
	public void update(TaskSet tasks) throws PDIException {
		// TODO
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIBreakpointManager#
	 * updatePendingBreakpoints()
	 */
	public void updatePendingBreakpoints() throws PDIException {
		for (IPDIBreakpoint bp : getAllPDIBreakpoints()) {
			if (!bp.getPendingTasks().isEmpty()) {
				if (!bp.isDeleted()) {
					setPendingBreakpoint(bp);
				} else {
					deletePendingBreakpoint(bp, true);
				}
			}
		}
	}

	/**
	 * Add breakpoint to this manager
	 * 
	 * @param breakpoint
	 */
	private void addBreakpoint(IPDIBreakpoint breakpoint) {
		synchronized (breakList) {
			if (!breakpoint.isTemporary()) {
				breakList.add(breakpoint);
			}
		}
	}

	/**
	 * Remove breakoint from this manager
	 * 
	 * @param id
	 */
	private void deleteBreakpoint(int id) {
		IPDIBreakpoint[] pdiBreakpoints = getAllPDIBreakpoints();
		for (int i = 0; i < pdiBreakpoints.length; i++) {
			if (pdiBreakpoints[i].getBreakpointID() == id) {
				breakList.remove(i);
				break;
			}
		}
	}

	/**
	 * Disable breakpoint without verifying breakpoint is valid. Used to disable
	 * breakpoint before it is added to breakpoint manager.
	 * 
	 * @param tasks
	 * @param breakpoint
	 * @throws PDIException
	 */
	private void disableBreakpoint0(TaskSet tasks, IPDIBreakpoint breakpoint) throws PDIException {
		breakpoint.setEnabled(false);
		getSession().getEventRequestManager().addEventRequest(
				session.getRequestFactory().getDisableBreakpointRequest(tasks, breakpoint));
	}

	/**
	 * Get all breakpoints known by this breakpoint manager
	 * 
	 * @return
	 */
	private IPDIBreakpoint[] getAllPDIBreakpoints() {
		synchronized (breakList) {
			return breakList.toArray(new IPDIBreakpoint[0]);
		}
	}

	/**
	 * Check if breakpoint is known by this breakpoint manager
	 * 
	 * @param id
	 * @return
	 */
	private boolean isValid(int id) {
		return (getBreakpoint(id) != null);
	}

	/**
	 * Helper method to set a location breakpoint
	 * 
	 * @param bkpt
	 * @throws PDIException
	 */
	private void setLocationBreakpoint(IPDILocationBreakpoint bkpt) throws PDIException {
		setPendingBreakpoint(bkpt);
		if (!bkpt.isEnabled()) {
			disableBreakpoint0(bkpt.getTasks(), bkpt);
		}
	}

	/**
	 * Helper method to set a watchpoint
	 * 
	 * @param watchpoint
	 * @throws PDIException
	 */
	private void setWatchpoint(IPDIWatchpoint watchpoint) throws PDIException {
		setPendingBreakpoint(watchpoint);
	}
}
