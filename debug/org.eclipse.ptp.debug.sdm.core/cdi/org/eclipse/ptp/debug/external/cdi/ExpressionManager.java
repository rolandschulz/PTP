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
import org.eclipse.ptp.debug.external.PTPDebugExternalPlugin;
import org.eclipse.ptp.debug.external.cdi.model.Expression;
import org.eclipse.ptp.debug.external.cdi.model.StackFrame;
import org.eclipse.ptp.debug.external.cdi.model.Target;
import org.eclipse.ptp.debug.external.cdi.model.variable.LocalVariable;
import org.eclipse.ptp.debug.external.cdi.model.variable.Variable;

/**
 */
public class ExpressionManager extends Manager {

	final static ICDIExpression[] EMPTY_EXPRESSIONS = {};
	Map expMap;

	public ExpressionManager(Session session) {
		super(session, true);
		expMap = new Hashtable();
	}

	protected void update(Target target) throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");	}
	
	synchronized List getExpressionList(Target target) {
		List expList = (List)expMap.get(target);
		if (expList == null) {
			expList = Collections.synchronizedList(new ArrayList());
			expMap.put(target, expList);
		}
		return expList;
	}

	public ICDIExpression[] getExpressions(Target target) throws CDIException {
		List expList = (List) expMap.get(target);
		if (expList != null) {
			return (ICDIExpression[])expList.toArray(EMPTY_EXPRESSIONS);
		}
		return EMPTY_EXPRESSIONS;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIExpressionManager#removeExpression(ICDIExpression)
	 */
	public void destroyExpressions(Target target, ICDIExpression[] expressions) throws CDIException {
		List expList = getExpressionList(target);
		for (int i = 0; i < expressions.length; ++i) {
			expList.remove(expressions[i]);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIExpressionManager#removeExpression(ICDIExpression)
	 */
	public void destroyAllExpressions(Target target) throws CDIException {
		ICDIExpression[] expressions = getExpressions(target);
		destroyExpressions(target, expressions);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIExpressionManager#createExpression(String)
	 */
	public ICDIExpression createExpression(Target target, String name) throws CDIException {
		Expression expression = new Expression(target, name);
		return expression;
	}

	public Variable createVariable(StackFrame frame, String code) throws CDIException {
		Target target = (Target)frame.getTarget();
		// Thread currentThread = (Thread)target.getCurrentThread();
		// StackFrame currentFrame = currentThread.getCurrentStackFrame();
		// target.setCurrentThread(frame.getThread(), false);
		// ((Thread)frame.getThread()).setCurrentStackFrame(frame, false);

		Variable variable = new LocalVariable(target, null, frame, code, null, 0, 0);
		return variable;
	}
}
