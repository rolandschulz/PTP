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
package org.eclipse.ptp.debug.external.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;
import org.eclipse.ptp.debug.core.aif.IAIF;
import org.eclipse.ptp.debug.external.cdi.Session;
import org.eclipse.ptp.debug.external.cdi.model.PTPObject;
import org.eclipse.ptp.debug.external.cdi.model.Target;
import org.eclipse.ptp.debug.external.cdi.model.variable.Variable;

public class Value extends PTPObject implements ICDIValue {
	protected Variable variable;

	public Value(Variable var) {
		super((Target) var.getTarget());
		variable = var;
	}
	protected Variable getVariable() throws CDIException {
		return variable;
	}	
	public String getTypeName() throws CDIException {
		return variable.getTypeName();
	}
	public String getValueString() throws CDIException {
		IAIF aif = variable.getAIF();
		if (aif == null) {
			Target target = (Target) variable.getTarget();
			Session session = (Session) target.getSession();
			return target.getDebugger().evaluateExpression(session.createBitList(target.getTargetID()), variable.getName());
		}
		//TODO - fix the toString later
		return aif.getValue().toString();
	}
	public int getChildrenNumber() throws CDIException {
		//FIXME - how to get the number of children??
		return 0;
	}
	public boolean hasChildren() throws CDIException {
		return (getChildrenNumber() > 0);	
	}
	public ICDIVariable[] getVariables() throws CDIException {
		return getVariable().getChildren();
	}
	public ICDIType getType() throws CDIException {
		return getVariable().getType();
	}
}
