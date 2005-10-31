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
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIArrayValue;
import org.eclipse.ptp.debug.external.cdi.Session;
import org.eclipse.ptp.debug.external.cdi.VariableManager;
import org.eclipse.ptp.debug.external.cdi.model.variable.Variable;
import org.eclipse.ptp.debug.external.cdi.model.variable.VariableDescriptor;

/**
 * @author Clement chu
 * 
 */
public class ArrayValue extends DerivedValue implements ICDIArrayValue {
	public ArrayValue(Variable v) {
		super(v);
	}
	public ICDIVariable[] getVariables() throws CDIException {
		//FIXME ignore NumChild
		//int timeout = getVariable().getMIVar().getNumChild() * 8 + 5000;
		return getVariable().getChildren();
	}
	public ICDIVariable[] getVariables(int index, int length) throws CDIException {
		//int children = getChildrenNumber();
		//if (index >= children || index + length >= children) {
		//	throw new CDIException("Index out of bound");
		//}

		// Overload for registers.
		Variable variable = getVariable();
		//FIXME -- not implemented Register yet
		/*
		if (variable instanceof Register) {
			ICDIVariable[] vars = getVariables();
			
			if (index < vars.length && (index + length) <= vars.length) {
				ICDIVariable[] newVars = new ICDIVariable[length];
				System.arraycopy(vars, index, newVars, 0, length);
				return newVars;
			}
			return new ICDIVariable[0];
		}
		*/
		//String subarray = "*(" + variable.getName() + "+" + index + ")@" + length;
		ICDITarget target = getTarget();
		Session session = (Session) (target.getSession());
		VariableManager mgr = session.getVariableManager();
		ICDIVariableDescriptor vo = mgr.getVariableDescriptorAsArray(variable, index, length);
		return mgr.createVariable((VariableDescriptor)vo).getValue().getVariables();
	}
}
