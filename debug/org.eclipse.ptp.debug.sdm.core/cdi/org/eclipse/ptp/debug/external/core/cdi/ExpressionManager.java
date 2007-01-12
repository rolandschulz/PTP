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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import org.eclipse.ptp.debug.core.aif.IAIF;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIEvent;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIExpression;
import org.eclipse.ptp.debug.external.core.cdi.event.VarChangedEvent;
import org.eclipse.ptp.debug.external.core.cdi.model.Expression;
import org.eclipse.ptp.debug.external.core.cdi.model.StackFrame;
import org.eclipse.ptp.debug.external.core.cdi.model.Target;
import org.eclipse.ptp.debug.external.core.cdi.model.Thread;
import org.eclipse.ptp.debug.external.core.cdi.model.variable.LocalVariable;
import org.eclipse.ptp.debug.external.core.cdi.model.variable.Variable;
import org.eclipse.ptp.debug.external.core.commands.GetPartialAIFCommand;
import org.eclipse.ptp.debug.external.core.commands.VariableDeleteCommand;

/**
 * @author Clement chu
 *
 */
public class ExpressionManager extends Manager {
	final static IPCDIExpression[] EMPTY_EXPRESSIONS = {};
	Map expMap;
	Map varMap;

	public ExpressionManager(Session session) {
		super(session, true);
		expMap = new Hashtable();
		varMap = new Hashtable();
	}
	public void shutdown() {
		expMap.clear();
		varMap.clear();
	}

	synchronized List getExpressionList(Target target) {
		List expList = (List)expMap.get(target);
		if (expList == null) {
			expList = Collections.synchronizedList(new ArrayList());
			expMap.put(target, expList);
		}
		return expList;
	}
	synchronized List getVariableList(Target target) {
		List varList = (List)varMap.get(target);
		if (varList == null) {
			varList = Collections.synchronizedList(new ArrayList());
			varMap.put(target, varList);
		}
		return varList;
	}
	public IPCDIExpression createExpression(Target target, String name) throws PCDIException {
		Expression expression = new Expression(target, name);
		List exprList = getExpressionList(target);
		exprList.add(expression);
		return expression;
	}
	public IPCDIExpression[] getExpressions(Target target) throws PCDIException {
		List expList = (List) expMap.get(target);
		if (expList != null) {
			return (IPCDIExpression[])expList.toArray(EMPTY_EXPRESSIONS);
		}
		return EMPTY_EXPRESSIONS;
	}
	public void destroyExpressions(Target target, IPCDIExpression[] expressions) throws PCDIException {
		List expList = getExpressionList(target);
		for (int i = 0; i < expressions.length; ++i) {
			expList.remove(expressions[i]);
		}
	}
	public void destroyAllExpressions(Target target) throws PCDIException {
		IPCDIExpression[] expressions = getExpressions(target);
		destroyExpressions(target, expressions);
	}
	public void update(Target target, String[] varList) throws PCDIException {
		//deleteAllVariables(target);
		List eventList = new ArrayList();
		for (int i=0; i<varList.length; i++) {
			Variable variable = getVariable(target, varList[i]);
			if (variable != null) {
				variable.setUpdated(true);
				eventList.add(new VarChangedEvent(target.getSession(), target.getTask(), variable, varList[i]));
			}
		}
		IPCDIEvent[] events = (IPCDIEvent[]) eventList.toArray(new IPCDIEvent[0]);
		target.getDebugger().fireEvents(events);
	}
	public void update(Target target) throws PCDIException {
		update(target, new String[0]);
		//deleteAllVariables(target);
		/*
		List eventList = new ArrayList();
		List varList = getVariableList(target);
		Variable[] variables = (Variable[]) varList.toArray(new Variable[varList.size()]);
		for (int i = 0; i < variables.length; i++) {
			Variable variable = variables[i];
			String keyName = variable.getKeyName();
			VariableUpdateCommand command = new VariableUpdateCommand(target.getTask(), keyName);
			target.getDebugger().postCommand(command);
			String[] changes = command.getChangeNames();
			variable.setUpdated(true);
			for (int j=0; j<changes.length; j++) {
				eventList.add(new VarChangedEvent(target.getSession(), target.getTask(), variable, changes[j]));
			}
		}
		IPCDIEvent[] events = (IPCDIEvent[]) eventList.toArray(new IPCDIEvent[0]);
		target.getDebugger().fireEvents(events);
		*/
	}	
	
	public Variable getVariable(Target target, String varName) {
		if (target == null)
			return null;
		
		List varList = getVariableList(target);
		Variable[] vars = (Variable[])varList.toArray(new Variable[0]);
		for (int i = 0; i < vars.length; i++) {
			if (vars[i].getName().equals(varName)) {
				return vars[i];
			}
			Variable v = vars[i].getChild(varName);
			if (v != null) {
				return v;
			}
		}
		return null;
	}
	public Variable createVariable(StackFrame frame, String code) throws PCDIException {
		Target target = (Target)frame.getTarget();
		Thread currentThread = (Thread)target.getCurrentThread();
		StackFrame currentFrame = currentThread.getCurrentStackFrame();
		target.lockTarget();
		try {
			target.setCurrentThread(frame.getThread(), false);
			((Thread)frame.getThread()).setCurrentStackFrame(frame, false);

			GetPartialAIFCommand command = new GetPartialAIFCommand(target.getTask(), code, "");
			target.getDebugger().postCommand(command);
			IAIF aif = command.getPartialAIF();
			String keyName = command.getName();
			
			Variable v = new LocalVariable(target, null, frame, code, null, 0, 0, keyName);
			v.setAIF(aif);
			
			List varList = getVariableList(target);
			varList.add(v);
			return v;
		} finally {
			target.setCurrentThread(currentThread, false);
			currentThread.setCurrentStackFrame(currentFrame, false);
			target.releaseTarget();
		}
	}
	public Variable removeVariableFromList(Target target, String varName) {
		Variable var = getVariable(target, varName);
		if (var != null) {
			List varList = getVariableList(target);
			varList.remove(var);
			return var;
		}
		return null;
	}
	public void deleteAllVariables(Target target) throws PCDIException {
		List varList = getVariableList(target);
		Variable[] variables = (Variable[]) varList.toArray(new Variable[varList.size()]);
		for (int i = 0; i < variables.length; ++i) {
			deleteVariable(variables[i]);
		}
	}	
	public void deleteVariable(Variable variable) throws PCDIException {
		Target target = (Target)variable.getTarget();
		VariableDeleteCommand command = new VariableDeleteCommand(target.getTask(), variable.getKeyName());
		target.getDebugger().postCommand(command);
		command.waitForReturn();
	}
}
