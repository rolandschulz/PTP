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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.pdi.IPDIExpressionManager;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEvent;
import org.eclipse.ptp.debug.core.pdi.model.IPDIExpression;
import org.eclipse.ptp.debug.core.pdi.model.IPDIMultiExpressions;
import org.eclipse.ptp.debug.core.pdi.model.IPDITargetExpression;
import org.eclipse.ptp.debug.core.pdi.model.IPDIVariable;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIF;
import org.eclipse.ptp.debug.internal.core.pdi.event.ChangedEvent;
import org.eclipse.ptp.debug.internal.core.pdi.event.DestroyedEvent;
import org.eclipse.ptp.debug.internal.core.pdi.event.VariableInfo;
import org.eclipse.ptp.debug.internal.core.pdi.model.Expression;
import org.eclipse.ptp.debug.internal.core.pdi.model.LocalVariable;
import org.eclipse.ptp.debug.internal.core.pdi.model.MultiExpressions;
import org.eclipse.ptp.debug.internal.core.pdi.model.StackFrame;
import org.eclipse.ptp.debug.internal.core.pdi.model.Target;
import org.eclipse.ptp.debug.internal.core.pdi.model.Thread;
import org.eclipse.ptp.debug.internal.core.pdi.model.Variable;
import org.eclipse.ptp.debug.internal.core.pdi.request.DataEvaluateExpressionRequest;
import org.eclipse.ptp.debug.internal.core.pdi.request.DeleteVariableRequest;
import org.eclipse.ptp.debug.internal.core.pdi.request.GetPartialAIFRequest;

/**
 * @author Clement chu
 *
 */
public class ExpressionManager extends Manager implements IPDIExpressionManager {
	final static IPDIExpression[] EMPTY_EXPRESSIONS = {};
	Map<BitList, List<IPDIExpression>> expMap;
	Map<BitList, List<Variable>> varMap;
	Map<String, IPDIMultiExpressions> mutliExprMap;

	public ExpressionManager(Session session) {
		super(session, true);
		expMap = new Hashtable<BitList, List<IPDIExpression>>();
		varMap = new Hashtable<BitList, List<Variable>>();
		mutliExprMap = new HashMap<String, IPDIMultiExpressions>();
	}
	public void shutdown() {
		expMap.clear();
		varMap.clear();
		mutliExprMap.clear();
	}	
	private synchronized List<IPDIExpression> getExpressionList(BitList qTasks) {
		List<IPDIExpression> expList = expMap.get(qTasks);
		if (expList == null) {
			expList = Collections.synchronizedList(new ArrayList<IPDIExpression>());
			expMap.put(qTasks, expList);
		}
		return expList;
	}
	private synchronized List<Variable> getVariableList(BitList qTasks) {
		List<Variable> varList = varMap.get(qTasks);
		if (varList == null) {
			varList = Collections.synchronizedList(new ArrayList<Variable>());
			varMap.put(qTasks, varList);
		}
		return varList;
	}
	public IPDITargetExpression createExpression(BitList qTasks, String name) throws PDIException {
		Expression expression = new Expression(session, qTasks, name);
		List<IPDIExpression> exprList = getExpressionList(qTasks);
		exprList.add(expression);
		return expression;
	}
	public IPDIExpression[] getExpressions(BitList qTasks) throws PDIException {
		List<IPDIExpression> expList = expMap.get(qTasks);
		if (expList != null) {
			return (IPDIExpression[])expList.toArray(EMPTY_EXPRESSIONS);
		}
		return EMPTY_EXPRESSIONS;
	}
	public void destroyExpressions(BitList qTasks, IPDIExpression[] expressions) throws PDIException {
		List<IPDIExpression> expList = getExpressionList(qTasks);
		for (int i = 0; i < expressions.length; ++i) {
			expList.remove(expressions[i]);
		}
	}
	public void destroyAllExpressions(BitList qTasks) throws PDIException {
		IPDIExpression[] expressions = getExpressions(qTasks);
		destroyExpressions(qTasks, expressions);
	}
	public void update(BitList qTasks, String[] varList) throws PDIException {
		List<IPDIEvent> eventList = new ArrayList<IPDIEvent>();
		for (int i=0; i<varList.length; i++) {
			Variable variable = getVariable(qTasks, varList[i]);
			if (variable != null) {
				variable.setUpdated(true);
				eventList.add(new ChangedEvent(new VariableInfo(session, qTasks, varList[i], variable)));
			}
		}
		session.getEventManager().fireEvents((IPDIEvent[]) eventList.toArray(new IPDIEvent[0]));
	}
	public void update(BitList qTasks) throws PDIException {
		update(qTasks, new String[0]);
	}	
	public Variable getVariable(BitList qTasks, String varid) {
		List<Variable> varList = getVariableList(qTasks);
		Variable[] vars = (Variable[])varList.toArray(new Variable[0]);
		for (int i = 0; i < vars.length; i++) {
			if (vars[i].getVarId().equals(varid)) {
				return vars[i];
			}
			Variable v = vars[i].getChild(varid);
			if (v != null) {
				return v;
			}
		}
		return null;
	}
	public Variable createVariable(StackFrame frame, String expr) throws PDIException {
		Target target = (Target)frame.getTarget();
		Thread currentThread = (Thread)target.getCurrentThread();
		StackFrame currentFrame = currentThread.getCurrentStackFrame();
		target.lockTarget();
		try {
			target.setCurrentThread(frame.getThread(), false);
			((Thread)frame.getThread()).setCurrentStackFrame(frame, false);

			GetPartialAIFRequest request = new GetPartialAIFRequest(target.getTasks(), expr, null);
			session.getEventRequestManager().addEventRequest(request);
			IAIF aif = request.getPartialAIF(target.getTasks());
			String varid = request.getVarId(target.getTasks());
			
			Variable v = new LocalVariable(session, target.getTasks(), null, frame, expr, null, 0, 0, varid);
			v.setAIF(aif);
			
			List<Variable> varList = getVariableList(target.getTasks());
			varList.add(v);
			return v;
		} finally {
			target.setCurrentThread(currentThread, false);
			currentThread.setCurrentStackFrame(currentFrame, false);
			target.releaseTarget();
		}
	}
	public Variable removeVariableFromList(BitList qTasks, String varid) {
		Variable var = getVariable(qTasks, varid);
		if (var != null) {
			List<Variable> varList = getVariableList(qTasks);
			varList.remove(var);
			return var;
		}
		return null;
	}
	public void deleteAllVariables(BitList qTasks) throws PDIException {
		List<Variable> varList = getVariableList(qTasks);
		Variable[] variables = (Variable[]) varList.toArray(new Variable[varList.size()]);
		for (int i = 0; i < variables.length; ++i) {
			deleteVariable(variables[i]);
		}
	}	
	public void deleteVariable(Variable variable) throws PDIException {
		DeleteVariableRequest request = new DeleteVariableRequest(session, variable.getTasks(), variable.getVarId());
		session.getEventRequestManager().addEventRequest(request);
		request.waitUntilCompleted(variable.getTasks());
		
		List<Variable> varList = getVariableList(variable.getTasks());
		varList.remove(variable);
		
		List<IPDIEvent> eventList = new ArrayList<IPDIEvent>();
		
		IPDIVariable[] children = variable.children;
		if (children != null) {
			for (int i = 0; i < children.length; ++i) {
				if (children[0] instanceof Variable) {
					Variable child = (Variable)children[i];
					eventList.add(new DestroyedEvent(new VariableInfo(session, variable.getTasks(), child.getVarId(), child)));
				}
			}
		}
		eventList.add(new DestroyedEvent(new VariableInfo(session, variable.getTasks(), variable.getVarId(), variable)));
		session.getEventManager().fireEvents((IPDIEvent[]) eventList.toArray(new IPDIEvent[0]));
	}
	public IAIF getAIF(BitList qTasks, String expr) throws PDIException {
		GetPartialAIFRequest request = new GetPartialAIFRequest(qTasks, expr, null);
		session.getEventRequestManager().addEventRequest(request);
		return request.getPartialAIF(qTasks);
	}
	public IAIF getExpressValue(BitList qTasks, String expr) throws PDIException {
		DataEvaluateExpressionRequest request = new DataEvaluateExpressionRequest(qTasks, expr);
		session.getEventRequestManager().addEventRequest(request);
		return request.getAIF(qTasks);
	}
	//MultiExpression
	public void updateStatusMultiExpressions(String exprText, boolean enabled) {
		IPDIMultiExpressions mexpr = getMultiExpression(exprText);
		if (mexpr != null) {
			((MultiExpressions)mexpr).setEnabled(enabled);
		}
	}
	public void updateMultiExpressions(String exprText, BitList tasks, IProgressMonitor monitor) throws PDIException {
		IPDIMultiExpressions mexpr = getMultiExpression(exprText);
		if (mexpr == null)
			throw new PDIException(tasks, "No expression " + exprText + " found");
	
		if (mexpr.isEnabled())
			((MultiExpressions)mexpr).updateExpressionsValue(tasks, monitor);
	}
	public void cleanMultiExpressions(String exprText, BitList tasks, IProgressMonitor monitor) throws PDIException {
		IPDIMultiExpressions mexpr = getMultiExpression(exprText);
		if (mexpr == null)
			throw new PDIException(tasks, "No expression " + exprText + " found");
	
		((MultiExpressions)mexpr).cleanExpressionsValue(tasks, monitor);
	}
	
	public void updateMultiExpressions(BitList tasks, IProgressMonitor monitor) throws PDIException {
		for (IPDIMultiExpressions mexpr : getMultiExpressions()) {
			if (mexpr.isEnabled())
				((MultiExpressions)mexpr).updateExpressionsValue(tasks, monitor);
		}
	}	
	public void cleanMultiExpressions(BitList tasks, IProgressMonitor monitor) throws PDIException {
		for (IPDIMultiExpressions mexpr : getMultiExpressions()) {
			((MultiExpressions)mexpr).cleanExpressionsValue(tasks, monitor);
		}
	}
	public IPDIExpression[] getMultiExpressions(int task) {
		List<IPDIExpression> exprList = new ArrayList<IPDIExpression>();
		for (IPDIMultiExpressions mexpr : getMultiExpressions()) {
			if (!mexpr.isEnabled())
				continue;
			IPDIExpression expr = ((MultiExpressions)mexpr).getExpression(task);
			if (expr != null) {
				exprList.add(expr);
			}
		}
		return exprList.toArray(new IPDIExpression[0]);
	}
	public IPDIMultiExpressions getMultiExpression(String exprText) {
		return mutliExprMap.get(exprText);
	}
	public void createMutliExpressions(BitList tasks, String exprText, boolean enabled) {
		mutliExprMap.put(exprText, new MultiExpressions(session, tasks, exprText, enabled));
	}
	public IPDIMultiExpressions[] getMultiExpressions() {
		return mutliExprMap.values().toArray(new IPDIMultiExpressions[0]);
	}
	public void removeMutliExpressions(BitList tasks, String exprText) {
		IPDIMultiExpressions mexpr = getMultiExpression(exprText);
		if (mexpr != null) {
			mexpr.removeExpression(tasks);
		}
	}
	public void removeMutliExpressions(String exprText) {
		IPDIMultiExpressions mexpr = getMultiExpression(exprText);
		if (mexpr != null) {
			((MultiExpressions)mexpr).shutdown();
		}
		mutliExprMap.remove(exprText);
	}
}
