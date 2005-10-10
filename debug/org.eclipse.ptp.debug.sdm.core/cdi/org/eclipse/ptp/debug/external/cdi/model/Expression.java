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
package org.eclipse.ptp.debug.external.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExpression;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;
import org.eclipse.ptp.debug.external.PTPDebugExternalPlugin;
import org.eclipse.ptp.debug.external.cdi.ExpressionManager;
import org.eclipse.ptp.debug.external.cdi.Session;
import org.eclipse.ptp.debug.external.cdi.model.type.IntType;
import org.eclipse.ptp.debug.external.cdi.model.variable.Variable;

/**
 */
public class Expression extends PTPObject implements ICDIExpression {

	private static int ID_COUNT = 0;
	private int id;
	String fExpression;
	
	public Expression(Target target, String ex) {
		super(target);
		fExpression  = ex;
		id = ++ID_COUNT;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExpression#getExpressionText()
	 */
	public String getExpressionText() {
		return fExpression;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExpression#equals(org.eclipse.cdt.debug.core.cdi.model.ICDIExpression)
	 */
	public boolean equals(ICDIExpression obj) {
		if (obj instanceof Expression) {
			Expression other = (Expression)obj;
			return other.id == id;
		}
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#getType()
	 */
	public ICDIType getType(ICDIStackFrame frame) throws CDIException {
		Target target = (Target) getTarget();

		String typeName = "int";
		
		if (typeName.equals("int")) {
			return new IntType(target, typeName);
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExpression#getValue(org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame)
	 */
	public ICDIValue getValue(ICDIStackFrame context) throws CDIException {
		Session session = (Session)getTarget().getSession();
		ExpressionManager mgr = session.getExpressionManager();
		Variable var = mgr.createVariable((StackFrame)context, getExpressionText());
		return var.getValue();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExpression#dispose()
	 */
	public void dispose() throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		Session session = (Session)getTarget().getSession();
		ExpressionManager mgr = session.getExpressionManager();
		mgr.destroyExpressions((Target)getTarget(), new Expression[] {this});
	}

}
