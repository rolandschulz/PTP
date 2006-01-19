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
package org.eclipse.ptp.debug.external.core.cdi.model;

import org.eclipse.ptp.debug.core.aif.IAIF;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIExpression;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIStackFrame;
import org.eclipse.ptp.debug.external.core.cdi.ExpressionManager;
import org.eclipse.ptp.debug.external.core.cdi.Session;
import org.eclipse.ptp.debug.external.core.cdi.SourceManager;

/**
 * @author Clement chu
 *
 */
public class Expression extends PObject implements IPCDIExpression {
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
	public boolean equals(IPCDIExpression obj) {
		if (obj instanceof Expression) {
			Expression other = (Expression) obj;
			return other.id == id;
		}
		return false;
	}
	public IAIF getAIF(IPCDIStackFrame frame) throws PCDIException {
		Target target = (Target) getTarget();
		Session session = (Session) (target.getSession());
		SourceManager sourceMgr = session.getSourceManager();
		try {
			return sourceMgr.getAIFFromVariable((StackFrame) frame, getExpressionText());
		} catch (PCDIException e) {
			throw new PCDIException(e.getMessage());
		}
	}

	/*
	public ICDIType getType(ICDIStackFrame frame) throws PCDIException {
		Type type = null;
		Target target = (Target) getTarget();
		Session session = (Session) (target.getSession());
		SourceManager sourceMgr = session.getSourceManager();
		IAIF aif = sourceMgr.getAIFFromVariable((StackFrame) frame, getExpressionText());
		try {
			type = sourceMgr.getType(target, aif);
		} catch (PCDIException e) {
			// Try with ptype.
			try {
				String ptype = sourceMgr.getDetailTypeName(target, aif.getDescription());
				type = sourceMgr.getType(target, ptype);
			} catch (PCDIException ex) {
				// Some version of gdb does not work with the name of the class
				// ex: class data foo --> ptype data --> fails
				// ex: class data foo --> ptype foo --> succeed
				try {
					String ptype = sourceMgr.getDetailTypeNameFromVariable((StackFrame) frame, getExpressionText());
					type = sourceMgr.getType(target, ptype);
				} catch (PCDIException e2) {
					// give up.
				}
			}
		}
		if (type == null) {
			type = new IncompleteType(target, aif.getDescription());
		}
		return type;
	}
	public ICDIValue getValue(ICDIStackFrame context) throws PCDIException {
		Session session = (Session) getTarget().getSession();
		ExpressionManager mgr = session.getExpressionManager();
		Variable var = mgr.createVariable((StackFrame) context, getExpressionText());
		return var.getValue();
	}
	*/
	public void dispose() throws PCDIException {
		Session session = (Session) getTarget().getSession();
		ExpressionManager mgr = session.getExpressionManager();
		mgr.destroyExpressions((Target) getTarget(), new Expression[] { this });
	}
}
