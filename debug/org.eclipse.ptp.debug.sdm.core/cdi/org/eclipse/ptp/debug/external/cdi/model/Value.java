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
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;

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
		return null;
	}

	public ICDIType getType() throws CDIException {
		// Auto-generated method stub
		System.out.println("Value.getType()");
		return null;
	}

	public String getValueString() throws CDIException {
		// Auto-generated method stub
		System.out.println("Value.getValueString()");
		return null;
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
