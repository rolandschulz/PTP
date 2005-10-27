/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.external.cdi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExpression;
import org.eclipse.ptp.debug.external.cdi.model.Expression;
import org.eclipse.ptp.debug.external.cdi.model.StackFrame;
import org.eclipse.ptp.debug.external.cdi.model.Target;
import org.eclipse.ptp.debug.external.cdi.model.variable.Variable;

/**
 */
public class ExpressionManager extends Manager {
	final static ICDIExpression[] EMPTY_EXPRESSIONS = {};
	Map expMap;
	Map varMap;

	public void update(Target target) throws CDIException {
		//TODO dunno what implement here?
	}
	
	public ExpressionManager(Session session) {
		super(session, true);
		expMap = new Hashtable();
		varMap = new Hashtable();
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
	public ICDIExpression createExpression(Target target, String name) throws CDIException {
		Expression expression = new Expression(target, name);
		List exprList = getExpressionList(target);
		exprList.add(expression);
		return expression;
	}
	public ICDIExpression[] getExpressions(Target target) throws CDIException {
		List expList = (List) expMap.get(target);
		if (expList != null) {
			return (ICDIExpression[])expList.toArray(EMPTY_EXPRESSIONS);
		}
		return EMPTY_EXPRESSIONS;
	}
	public void destroyExpressions(Target target, ICDIExpression[] expressions) throws CDIException {
		List expList = getExpressionList(target);
		for (int i = 0; i < expressions.length; ++i) {
			expList.remove(expressions[i]);
		}
	}
	public void destroyAllExpressions(Target target) throws CDIException {
		ICDIExpression[] expressions = getExpressions(target);
		destroyExpressions(target, expressions);
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
	public Variable createVariable(StackFrame frame, String code) throws CDIException {
		//TODO - implement later
		throw new CDIException("Not implement yet - ExpressManager: createVaraible");
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
	public void deleteAllVariables(Target target) throws CDIException {
		List varList = getVariableList(target);
		Variable[] variables = (Variable[]) varList.toArray(new Variable[varList.size()]);
		for (int i = 0; i < variables.length; ++i) {
			deleteVariable(variables[i]);
		}
	}	
	public void deleteVariable(Variable variable) throws CDIException {
		//Target target = (Target)variable.getTarget();
		//TODO - not implement yet
		//target.getDebugger().vardelete(((Session)getSession().createBitList(target.getTargetID()), variable.getName());
		//fire variable change event maybe
		throw new CDIException("Not implement yet - ExpressionManager: deleteVariable");
	}
}
