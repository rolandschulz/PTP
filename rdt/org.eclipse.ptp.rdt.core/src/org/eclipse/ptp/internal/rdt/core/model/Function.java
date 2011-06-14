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
import org.eclipse.cdt.core.model.IFunction;

public class Function extends FunctionDeclaration implements IFunction {
	private static final long serialVersionUID = 1L;

	public Function(Parent parent, String qualifiedName) {
		super(parent, ICElement.C_FUNCTION, qualifiedName);
	}

	protected Function(Parent parent, int kind, String name) {
		super(parent, kind, name);
	}

	public Function(Parent parent, IFunction element) throws CModelException {
		super(parent, element);
	}

	public Function(Parent parent, org.eclipse.cdt.core.dom.ast.IFunction binding) throws DOMException {
		super(parent, ICElement.C_FUNCTION, binding);
	}

	protected Function(Parent parent, int templateFunction, org.eclipse.cdt.core.dom.ast.IFunction binding) throws DOMException {
		super(parent, ICElement.C_FUNCTION, binding);
	}
}
