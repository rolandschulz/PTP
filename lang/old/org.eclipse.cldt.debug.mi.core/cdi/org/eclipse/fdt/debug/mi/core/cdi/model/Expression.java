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
package org.eclipse.fdt.debug.mi.core.cdi.model;

import org.eclipse.fdt.debug.core.cdi.CDIException;
import org.eclipse.fdt.debug.core.cdi.model.ICDIExpression;
import org.eclipse.fdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.fdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.fdt.debug.core.cdi.model.type.ICDIType;
import org.eclipse.fdt.debug.mi.core.cdi.ExpressionManager;
import org.eclipse.fdt.debug.mi.core.cdi.Session;
import org.eclipse.fdt.debug.mi.core.cdi.SourceManager;
import org.eclipse.fdt.debug.mi.core.cdi.model.type.IncompleteType;
import org.eclipse.fdt.debug.mi.core.cdi.model.type.Type;


/**
 */
public class Expression extends CObject implements ICDIExpression {

	private static int ID_COUNT = 0;
	private int id;
	String fExpression;
	Type fType;
	
	public Expression(Target target, String ex) {
		super(target);
		fExpression  = ex;
		id = ++ID_COUNT;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.debug.core.cdi.model.ICDIExpression#getExpressionText()
	 */
	public String getExpressionText() {
		return fExpression;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.debug.core.cdi.model.ICDIExpression#equals(org.eclipse.fdt.debug.core.cdi.model.ICDIExpression)
	 */
	public boolean equals(ICDIExpression obj) {
		if (obj instanceof Expression) {
			Expression other = (Expression)obj;
			return other.id == id;
		}
		return false;
	}

	/**
	 * @see org.eclipse.fdt.debug.core.cdi.model.ICDIVariable#getType()
	 */
	public ICDIType getType(ICDIStackFrame frame) throws CDIException {
		Type type = null;
		Target target = (Target)getTarget();
		Session session = (Session) (target.getSession());
		SourceManager sourceMgr = session.getSourceManager();
		String nametype = sourceMgr.getTypeNameFromVariable((StackFrame)frame, getExpressionText());
		try {
			type = sourceMgr.getType(target, nametype);
		} catch (CDIException e) {
			// Try with ptype.
			try {
				String ptype = sourceMgr.getDetailTypeName(target, nametype);
				type = sourceMgr.getType(target, ptype);
			} catch (CDIException ex) {
				// Some version of gdb does not work with the name of the class
				// ex: class data foo --> ptype data --> fails
				// ex: class data foo --> ptype foo --> succeed
				try {
					String ptype = sourceMgr.getDetailTypeNameFromVariable((StackFrame)frame, getExpressionText());
					type = sourceMgr.getType(target, ptype);
				} catch (CDIException e2) {
					// give up.
				}
			}
		}
		if (type == null) {
			type = new IncompleteType(target, nametype);
		}

		return type;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.debug.core.cdi.model.ICDIExpression#getValue(org.eclipse.fdt.debug.core.cdi.model.ICDIStackFrame)
	 */
	public ICDIValue getValue(ICDIStackFrame context) throws CDIException {
		Session session = (Session)getTarget().getSession();
		ExpressionManager mgr = session.getExpressionManager();
		Variable var = mgr.createVariable((StackFrame)context, getExpressionText());
		return var.getValue();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.debug.core.cdi.model.ICDIExpression#dispose()
	 */
	public void dispose() throws CDIException {
		Session session = (Session)getTarget().getSession();
		ExpressionManager mgr = session.getExpressionManager();
		mgr.destroyExpressions((Target)getTarget(), new Expression[] {this});
	}

}
