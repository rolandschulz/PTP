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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEvent;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIExpressionManager;
import org.eclipse.ptp.debug.core.pdi.model.IPDIExpression;
import org.eclipse.ptp.debug.core.pdi.model.IPDIMultiExpressions;
import org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrame;
import org.eclipse.ptp.debug.core.pdi.model.IPDITarget;
import org.eclipse.ptp.debug.core.pdi.model.IPDITargetExpression;
import org.eclipse.ptp.debug.core.pdi.model.IPDIThread;
import org.eclipse.ptp.debug.core.pdi.model.IPDIVariable;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIF;
import org.eclipse.ptp.debug.core.pdi.request.IPDIDeleteVariableRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIEvaluateExpressionRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIEvaluatePartialExpressionRequest;
import org.eclipse.ptp.internal.debug.core.pdi.messages.Messages;

/**
 * @author Clement chu
 * 
 */
public class ExpressionManager extends AbstractPDIManager implements IPDIExpressionManager {
	final static IPDIExpression[] EMPTY_EXPRESSIONS = {};
	Map<TaskSet, List<IPDIExpression>> expMap;
	Map<TaskSet, List<IPDIVariable>> varMap;
	Map<String, IPDIMultiExpressions> mutliExprMap;

	public ExpressionManager(IPDISession session) {
		super(session, true);
		expMap = new Hashtable<TaskSet, List<IPDIExpression>>();
		varMap = new Hashtable<TaskSet, List<IPDIVariable>>();
		mutliExprMap = new HashMap<String, IPDIMultiExpressions>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIExpressionManager#
	 * cleanMultiExpressions(org.eclipse.ptp.core.util.TaskSet,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void cleanMultiExpressions(TaskSet tasks, IProgressMonitor monitor) throws PDIException {
		for (IPDIMultiExpressions mexpr : getMultiExpressions()) {
			mexpr.cleanExpressionsValue(tasks, monitor);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIExpressionManager#
	 * cleanMultiExpressions(java.lang.String,
	 * org.eclipse.ptp.core.util.TaskSet,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void cleanMultiExpressions(String exprText, TaskSet tasks, IProgressMonitor monitor) throws PDIException {
		IPDIMultiExpressions mexpr = getMultiExpression(exprText);
		if (mexpr == null) {
			throw new PDIException(tasks, NLS.bind(Messages.ExpressionManager_0, exprText));
		}

		mexpr.cleanExpressionsValue(tasks, monitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.manager.IPDIExpressionManager#createExpression
	 * (org.eclipse.ptp.core.util.TaskSet, java.lang.String)
	 */
	public IPDITargetExpression createExpression(TaskSet qTasks, String name) throws PDIException {
		IPDITargetExpression expression = session.getModelFactory().newExpression(session, qTasks, name);
		List<IPDIExpression> exprList = getExpressionList(qTasks);
		exprList.add(expression);
		return expression;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIExpressionManager#
	 * createMutliExpressions(org.eclipse.ptp.core.util.TaskSet,
	 * java.lang.String, boolean)
	 */
	public void createMutliExpressions(TaskSet tasks, String exprText, boolean enabled) {
		mutliExprMap.put(exprText, session.getModelFactory().newMultiExpressions(session, tasks, exprText, enabled));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.manager.IPDIExpressionManager#createVariable
	 * (org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrame, java.lang.String)
	 */
	public IPDIVariable createVariable(IPDIStackFrame frame, String expr) throws PDIException {
		IPDITarget target = frame.getTarget();
		IPDIThread currentThread = target.getCurrentThread();
		IPDIStackFrame currentFrame = currentThread.getCurrentStackFrame();
		target.lockTarget();
		try {
			target.setCurrentThread(frame.getThread(), false);
			frame.getThread().setCurrentStackFrame(frame, false);

			IPDIEvaluatePartialExpressionRequest request = session.getRequestFactory().getEvaluatePartialExpressionRequest(
					target.getTasks(), expr, null, false);
			session.getEventRequestManager().addEventRequest(request);
			IAIF aif = request.getPartialAIF(target.getTasks());
			String varid = request.getId(target.getTasks());

			IPDIVariable v = session.getModelFactory().newLocalVariable(session, target.getTasks(), null, frame, expr, null, 0, 0,
					varid);
			v.setAIF(aif);

			List<IPDIVariable> varList = getVariableList(target.getTasks());
			varList.add(v);
			return v;
		} finally {
			target.setCurrentThread(currentThread, false);
			currentThread.setCurrentStackFrame(currentFrame, false);
			target.releaseTarget();
		}
	}

	/**
	 * @param qTasks
	 * @throws PDIException
	 */
	public void deleteAllVariables(TaskSet qTasks) throws PDIException {
		List<IPDIVariable> varList = getVariableList(qTasks);
		IPDIVariable[] variables = varList.toArray(new IPDIVariable[varList.size()]);
		for (IPDIVariable variable : variables) {
			deleteVariable(variable);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.manager.IPDIExpressionManager#deleteVariable
	 * (org.eclipse.ptp.debug.core.pdi.model.IPDIVariable)
	 */
	public void deleteVariable(IPDIVariable variable) throws PDIException {
		IPDIDeleteVariableRequest request = session.getRequestFactory().getDeletePartialExpressionRequest(variable.getTasks(),
				variable.getId());
		session.getEventRequestManager().addEventRequest(request);
		request.waitUntilCompleted(variable.getTasks());

		List<IPDIVariable> varList = getVariableList(variable.getTasks());
		varList.remove(variable);

		List<IPDIEvent> eventList = new ArrayList<IPDIEvent>();

		IPDIVariable[] children = variable.getChildren();
		if (children != null) {
			for (IPDIVariable child : children) {
				if (children[0] instanceof IPDIVariable) {
					eventList.add(session.getEventFactory().newDestroyedEvent(
							session.getEventFactory().newVariableInfo(session, variable.getTasks(), child.getId(), child)));
				}
			}
		}
		eventList.add(session.getEventFactory().newDestroyedEvent(
				session.getEventFactory().newVariableInfo(session, variable.getTasks(), variable.getId(), variable)));
		session.getEventManager().fireEvents(eventList.toArray(new IPDIEvent[0]));
	}

	/**
	 * @param qTasks
	 * @throws PDIException
	 */
	public void destroyAllExpressions(TaskSet qTasks) throws PDIException {
		IPDIExpression[] expressions = getExpressions(qTasks);
		destroyExpressions(qTasks, expressions);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIExpressionManager#
	 * destroyExpressions(org.eclipse.ptp.core.util.TaskSet,
	 * org.eclipse.ptp.debug.core.pdi.model.IPDIExpression[])
	 */
	public void destroyExpressions(TaskSet qTasks, IPDIExpression[] expressions) throws PDIException {
		List<IPDIExpression> expList = getExpressionList(qTasks);
		for (IPDIExpression expression : expressions) {
			expList.remove(expression);
		}
	}

	/**
	 * @param qTasks
	 * @param expr
	 * @return
	 * @throws PDIException
	 */
	public IAIF getAIF(TaskSet qTasks, String expr) throws PDIException {
		IPDIEvaluatePartialExpressionRequest request = session.getRequestFactory().getEvaluatePartialExpressionRequest(qTasks,
				expr, null, false);
		session.getEventRequestManager().addEventRequest(request);
		return request.getPartialAIF(qTasks);
	}

	/**
	 * @param qTasks
	 * @return
	 * @throws PDIException
	 */
	public IPDIExpression[] getExpressions(TaskSet qTasks) throws PDIException {
		List<IPDIExpression> expList = expMap.get(qTasks);
		if (expList != null) {
			return expList.toArray(EMPTY_EXPRESSIONS);
		}
		return EMPTY_EXPRESSIONS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIExpressionManager#
	 * getExpressionValue(org.eclipse.ptp.core.util.TaskSet, java.lang.String)
	 */
	public IAIF getExpressionValue(TaskSet qTasks, String expr) throws PDIException {
		IPDIEvaluateExpressionRequest request = session.getRequestFactory().getEvaluateExpressionRequest(qTasks, expr);
		session.getEventRequestManager().addEventRequest(request);
		return request.getAIF(qTasks);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIExpressionManager#
	 * getMultiExpression(java.lang.String)
	 */
	public IPDIMultiExpressions getMultiExpression(String exprText) {
		return mutliExprMap.get(exprText);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIExpressionManager#
	 * getMultiExpressions()
	 */
	public IPDIMultiExpressions[] getMultiExpressions() {
		return mutliExprMap.values().toArray(new IPDIMultiExpressions[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIExpressionManager#
	 * getMultiExpressions(int)
	 */
	public IPDIExpression[] getMultiExpressions(int task) {
		List<IPDIExpression> exprList = new ArrayList<IPDIExpression>();
		for (IPDIMultiExpressions mexpr : getMultiExpressions()) {
			if (!mexpr.isEnabled()) {
				continue;
			}
			IPDIExpression expr = mexpr.getExpression(task);
			if (expr != null) {
				exprList.add(expr);
			}
		}
		return exprList.toArray(new IPDIExpression[0]);
	}

	/**
	 * @param qTasks
	 * @param varId
	 * @return
	 */
	public IPDIVariable getVariable(TaskSet qTasks, String varid) {
		List<IPDIVariable> varList = getVariableList(qTasks);
		IPDIVariable[] vars = varList.toArray(new IPDIVariable[0]);
		for (IPDIVariable var : vars) {
			if (var.getId().equals(varid)) {
				return var;
			}
			IPDIVariable v = var.getChild(varid);
			if (v != null) {
				return v;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIExpressionManager#
	 * removeMutliExpressions(org.eclipse.ptp.core.util.TaskSet,
	 * java.lang.String)
	 */
	public void removeMutliExpressions(TaskSet tasks, String exprText) {
		IPDIMultiExpressions mexpr = getMultiExpression(exprText);
		if (mexpr != null) {
			mexpr.removeExpression(tasks);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIExpressionManager#
	 * removeMutliExpressions(java.lang.String)
	 */
	public void removeMutliExpressions(String exprText) {
		IPDIMultiExpressions mexpr = getMultiExpression(exprText);
		if (mexpr != null) {
			mexpr.shutdown();
		}
		mutliExprMap.remove(exprText);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.pdi.AbstractPDIManager#shutdown()
	 */
	@Override
	public void shutdown() {
		expMap.clear();
		varMap.clear();
		mutliExprMap.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.pdi.AbstractPDIManager#update(org
	 * .eclipse.ptp.core.util.TaskSet)
	 */
	@Override
	public void update(TaskSet qTasks) throws PDIException {
		update(qTasks, new String[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.manager.IPDIExpressionManager#update(org
	 * .eclipse.ptp.core.util.TaskSet, java.lang.String[])
	 */
	public void update(TaskSet qTasks, String[] varList) throws PDIException {
		List<IPDIEvent> eventList = new ArrayList<IPDIEvent>();
		for (String element : varList) {
			IPDIVariable variable = getVariable(qTasks, element);
			if (variable != null) {
				eventList.add(session.getEventFactory().newChangedEvent(
						session.getEventFactory().newVariableInfo(session, qTasks, element, variable)));
			}
		}
		session.getEventManager().fireEvents(eventList.toArray(new IPDIEvent[0]));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIExpressionManager#
	 * updateMultiExpressions(org.eclipse.ptp.core.util.TaskSet,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void updateMultiExpressions(TaskSet tasks, IProgressMonitor monitor) throws PDIException {
		for (IPDIMultiExpressions mexpr : getMultiExpressions()) {
			if (mexpr.isEnabled()) {
				mexpr.updateExpressionsValue(tasks, monitor);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIExpressionManager#
	 * updateMultiExpressions(java.lang.String,
	 * org.eclipse.ptp.core.util.TaskSet,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void updateMultiExpressions(String exprText, TaskSet tasks, IProgressMonitor monitor) throws PDIException {
		IPDIMultiExpressions mexpr = getMultiExpression(exprText);
		if (mexpr == null) {
			throw new PDIException(tasks, NLS.bind(Messages.ExpressionManager_0, exprText));
		}

		if (mexpr.isEnabled()) {
			mexpr.updateExpressionsValue(tasks, monitor);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIExpressionManager#
	 * updateStatusMultiExpressions(java.lang.String, boolean)
	 */
	public void updateStatusMultiExpressions(String exprText, boolean enabled) {
		IPDIMultiExpressions mexpr = getMultiExpression(exprText);
		if (mexpr != null) {
			mexpr.setEnabled(enabled);
		}
	}

	/**
	 * @param qTasks
	 * @return
	 */
	private synchronized List<IPDIExpression> getExpressionList(TaskSet qTasks) {
		List<IPDIExpression> expList = expMap.get(qTasks);
		if (expList == null) {
			expList = Collections.synchronizedList(new ArrayList<IPDIExpression>());
			expMap.put(qTasks, expList);
		}
		return expList;
	}

	/**
	 * @param qTasks
	 * @return
	 */
	private synchronized List<IPDIVariable> getVariableList(TaskSet qTasks) {
		List<IPDIVariable> varList = varMap.get(qTasks);
		if (varList == null) {
			varList = Collections.synchronizedList(new ArrayList<IPDIVariable>());
			varMap.put(qTasks, varList);
		}
		return varList;
	}
}
