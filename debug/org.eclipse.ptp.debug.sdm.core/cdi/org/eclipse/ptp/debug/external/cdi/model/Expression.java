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
package org.eclipse.ptp.debug.external.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExpression;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;
import org.eclipse.ptp.debug.external.aif.IAIF;
import org.eclipse.ptp.debug.external.cdi.ExpressionManager;
import org.eclipse.ptp.debug.external.cdi.Session;
import org.eclipse.ptp.debug.external.cdi.SourceManager;
import org.eclipse.ptp.debug.external.cdi.model.type.IncompleteType;
import org.eclipse.ptp.debug.external.cdi.model.type.Type;
import org.eclipse.ptp.debug.external.cdi.model.variable.Variable;

public class Expression extends PTPObject implements ICDIExpression {
	private static int ID_COUNT = 0;
	private int id;
	String fExpression;

	public Expression(Target target, String ex) {
		super(target);
		fExpression = ex;
		id = ++ID_COUNT;
	}
	public String getExpressionText() {
		return fExpression;
	}
	public boolean equals(ICDIExpression obj) {
		if (obj instanceof Expression) {
			Expression other = (Expression) obj;
			return other.id == id;
		}
		return false;
	}
	public ICDIType getType(ICDIStackFrame frame) throws CDIException {
		Type type = null;
		Target target = (Target) getTarget();
		Session session = (Session) (target.getSession());
		SourceManager sourceMgr = session.getSourceManager();
		IAIF aif = sourceMgr.getAIFFromVariable((StackFrame) frame, getExpressionText());
		try {
			type = sourceMgr.getType(target, aif);
		} catch (CDIException e) {
			// Try with ptype.
			try {
				String ptype = sourceMgr.getDetailTypeName(target, aif.getDescription());
				type = sourceMgr.getType(target, ptype);
			} catch (CDIException ex) {
				// Some version of gdb does not work with the name of the class
				// ex: class data foo --> ptype data --> fails
				// ex: class data foo --> ptype foo --> succeed
				try {
					String ptype = sourceMgr.getDetailTypeNameFromVariable((StackFrame) frame, getExpressionText());
					type = sourceMgr.getType(target, ptype);
				} catch (CDIException e2) {
					// give up.
				}
			}
		}
		if (type == null) {
			type = new IncompleteType(target, aif.getDescription());
		}
		return type;
	}
	public ICDIValue getValue(ICDIStackFrame context) throws CDIException {
		Session session = (Session) getTarget().getSession();
		ExpressionManager mgr = session.getExpressionManager();
		Variable var = mgr.createVariable((StackFrame) context, getExpressionText());
		return var.getValue();
	}
	public void dispose() throws CDIException {
		Session session = (Session) getTarget().getSession();
		ExpressionManager mgr = session.getExpressionManager();
		mgr.destroyExpressions((Target) getTarget(), new Expression[] { this });
	}
}
