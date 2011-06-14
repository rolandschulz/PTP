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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IMethod;

public class Method extends MethodDeclaration implements IMethod {
	private static final long serialVersionUID = 1L;

	public Method(Parent parent, String qualifiedName) {
		super(parent, ICElement.C_METHOD, qualifiedName);
	}

	protected Method(Parent parent, int kind, String name) {
		super(parent, kind, name);
	}

	public Method(Parent parent, IMethod element) throws CModelException {
		super(parent, element);
	}

	public Method(Parent parent, ICPPMethod binding) throws DOMException {
		this(parent, C_METHOD, binding);
	}
	
	protected Method(Parent parent, int type, ICPPMethod binding) throws DOMException {
		super(parent, type, binding);
	}

}
