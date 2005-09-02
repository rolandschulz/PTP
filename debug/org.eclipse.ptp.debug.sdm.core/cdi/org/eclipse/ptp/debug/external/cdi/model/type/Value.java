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
package org.eclipse.ptp.debug.external.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;
import org.eclipse.ptp.debug.external.IDebugger;
import org.eclipse.ptp.debug.external.cdi.Session;
import org.eclipse.ptp.debug.external.cdi.model.DebugProcessSet;
import org.eclipse.ptp.debug.external.cdi.model.PTPObject;
import org.eclipse.ptp.debug.external.cdi.model.Target;
import org.eclipse.ptp.debug.external.cdi.model.variable.Variable;

/**
 */
public class Value extends PTPObject implements ICDIValue {

	protected Variable variable;

	public Value(Variable v) {
		super((Target)v.getTarget());
		variable = v;
	}

	public String getTypeName() throws CDIException {
		// Auto-generated method stub
		System.out.println("Value.getTypeName()");
		return variable.getTypeName();
	}

	public ICDIType getType() throws CDIException {
		// Auto-generated method stub
		System.out.println("Value.getType()");
		return variable.getType();
	}

	public String getValueString() throws CDIException {
		// Auto-generated method stub
		System.out.println("Value.getValueString()");

		Target target = (Target) variable.getTarget();
		Session session = (Session) target.getSession();
		IDebugger debugger = session.getDebugger();
		DebugProcessSet newSet = new DebugProcessSet(session, target.getTargetId());
		String valString = debugger.evaluateExpression(newSet, variable);

		return valString;
	}

	public int getChildrenNumber() throws CDIException {
		// Auto-generated method stub
		System.out.println("Value.getChildrenNumber()");
		return 0;
	}

	public boolean hasChildren() throws CDIException {
		// Auto-generated method stub
		System.out.println("Value.hasChildren()");
		return false;
	}

	public ICDIVariable[] getVariables() throws CDIException {
		// Auto-generated method stub
		System.out.println("Value.getVariables()");
		return null;
	}
}
