/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.IVariableDeclaration;

public class VariableDeclaration extends SourceManipulation implements IVariableDeclaration {
	private static final long serialVersionUID = 1L;

	protected String fTypeName;
	protected boolean fIsStatic;
	protected boolean fIsConst;
	protected boolean fIsVolatile;
	
	public VariableDeclaration(Parent parent, String variableName) {
		super(parent, ICElement.C_VARIABLE_DECLARATION, variableName);
	}
	
	protected VariableDeclaration(Parent parent, int kind, String name) {
		super(parent, kind, name);
	}
	
	protected VariableDeclaration(Parent parent, IVariableDeclaration element) throws CModelException {
		super(parent, element, (ISourceReference) element);
		fTypeName = element.getTypeName();
		fIsStatic = element.isStatic();
		fIsConst = element.isConst();
		fIsVolatile = element.isVolatile();
	}

	protected VariableDeclaration(Parent parent, int type, IVariable binding) throws DOMException {
		super(parent, type, binding.getName());
		fIsStatic = binding.isStatic();
	}

	public String getTypeName() throws CModelException {
		return fTypeName;
	}

	public void setTypeName(String type) throws CModelException {
		fTypeName = type;
	}

	public boolean isConst() throws CModelException {
		return fIsConst;
	}

	public boolean isStatic() throws CModelException {
		return fIsStatic;
	}

	public boolean isVolatile() throws CModelException {
		return fIsVolatile;
	}

	public void setConst(boolean isConst) {
		fIsConst = isConst;
	}

	public void setVolatile(boolean isVolatile) {
		fIsVolatile = isVolatile;
	}

	public void setStatic(boolean isStatic) {
		fIsStatic = isStatic;
	}

}
