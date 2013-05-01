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
package org.eclipse.ptp.internal.debug.core.pdi.model;

import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.model.IPDIExpression;
import org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrame;
import org.eclipse.ptp.debug.core.pdi.model.IPDITargetExpression;
import org.eclipse.ptp.debug.core.pdi.model.IPDIVariable;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIF;
import org.eclipse.ptp.internal.debug.core.pdi.SessionObject;
import org.eclipse.ptp.internal.debug.core.pdi.messages.Messages;

/**
 * @author Clement chu
 * 
 */
public class Expression extends SessionObject implements IPDITargetExpression {
	private static int ID_COUNT = 0;
	private final int id;
	private final String fExpression;
	private IPDIVariable variable = null;

	public Expression(IPDISession session, TaskSet tasks, String ex) {
		super(session, tasks);
		fExpression = ex;
		id = ++ID_COUNT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIExpression#dispose()
	 */
	public void dispose() throws PDIException {
		session.getExpressionManager().destroyExpressions(getTasks(), new Expression[] { this });
		if (variable != null) {
			session.getExpressionManager().deleteVariable(variable);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIExpression#equals(org.eclipse.ptp.debug.core.pdi.model.IPDIExpression)
	 */
	public boolean equals(IPDIExpression obj) {
		if (obj instanceof Expression) {
			Expression other = (Expression) obj;
			return other.id == id;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIExpression#getAIF()
	 */
	public IAIF getAIF() throws PDIException {
		if (variable == null) {
			return null;
		}
		return variable.getAIF();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIExpression#getExpressionText()
	 */
	public String getExpressionText() {
		return fExpression;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.model.IPDITargetExpression#getVariable(org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrame)
	 */
	public IPDIVariable getVariable(IPDIStackFrame context) throws PDIException {
		IPDIVariable var = session.getExpressionManager().createVariable(context, getExpressionText());
		if (var == null) {
			throw new PDIException(context.getTasks(), Messages.Expression_0);
		}

		variable = var;
		return var;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIExpression#setAIF(org.eclipse.ptp.debug.core.pdi.model.aif.IAIF)
	 */
	public void setAIF(IAIF aif) {
		if (variable != null) {
			variable.setAIF(aif);
		}
	}
}
