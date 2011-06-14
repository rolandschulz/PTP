/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/ 

package org.eclipse.ptp.internal.rdt.core.model;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IVariable;

public class Variable extends VariableDeclaration implements IVariable {
	private static final long serialVersionUID = 1L;

	public Variable(Parent parent, String variableName) {
		super(parent, ICElement.C_VARIABLE, variableName);
	}

	protected Variable(Parent parent, int kind, String name) {
		super(parent, kind, name);
	}
	
	public Variable(Parent parent, IVariable element) throws CModelException {
		super(parent, element);
	}

	public Variable(Parent parent, org.eclipse.cdt.core.dom.ast.IVariable binding) throws DOMException {
		super(parent, ICElement.C_VARIABLE, binding);
	}

	public String getInitializer() {
		// TODO Auto-generated method stub
		return null;
	}

}
