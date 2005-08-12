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
package org.eclipse.ptp.debug.external.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIIntType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;
import org.eclipse.ptp.debug.external.cdi.model.type.IntValue;
import org.eclipse.ptp.debug.external.simulator.SimVariable;

/**
 */
public abstract class Variable extends VariableDescriptor implements ICDIVariable {
	
	SimVariable fVar;
	Value value;
	
	public Variable(VariableDescriptor obj, SimVariable sVar) {
		super(obj);
		fVar = sVar;
	}

	public Variable(Target target, Thread thread, StackFrame frame, String n, String q, int pos, int depth, SimVariable sVar) {
		super(target, thread, frame, n, q, pos, depth);
		fVar = sVar;
	}
	
	public SimVariable getSimVariable() {
		return fVar;
	}
	
	public void setValue(ICDIValue value) throws CDIException {
		System.out.println("Variable.setValue()");
	}
	
	public void setValue(String expression) throws CDIException {
		System.out.println("Variable.setValue()");
	}
	
	public ICDIValue getValue() throws CDIException {
		System.out.println("Variable.getValue()");
		if (value == null) {
			ICDIType t = getType();
			if (t instanceof ICDIIntType) {
				value = new IntValue(this);
			} else {
				value = new Value(this);
			}
		}
		return value;
	}
	
	public boolean isEditable() throws CDIException {
		System.out.println("Variable.isEditable()");
		return false;
	}
	
	public void setFormat(int format) throws CDIException {
		System.out.println("Variable.setFormat()");
	}
	
	public boolean equals(ICDIVariable var) {
		System.out.println("Variable.equals()");
		if (var instanceof Variable) {
			Variable variable = (Variable) var;
			return fVar.getName().equals(variable.getSimVariable().getName());
		}
		return super.equals(var);
	}

	public void dispose() throws CDIException {
		System.out.println("Variable.dispose()");
	}
}
