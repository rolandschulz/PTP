/*******************************************************************************
 * Copyright (c) 2008, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/ 

package org.eclipse.ptp.internal.rdt.core.model;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.IVariableDeclaration;

public class VariableDeclaration extends SourceManipulation implements IVariableDeclaration {
	private static final long serialVersionUID = 1L;

	protected String fTypeName=""; //$NON-NLS-1$
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
		IType astType = binding.getType();
		if(astType!=null){
			setTypeName(ASTTypeUtil.getType(astType, false));
		}
		fIsStatic = binding.isStatic();
	}

	public String getTypeName() {
		return fTypeName;
	}

	public void setTypeName(String type) {
		fTypeName = type;
	}

	public boolean isConst() {
		return fIsConst;
	}

	public boolean isStatic() {
		return fIsStatic;
	}

	public boolean isVolatile() {
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
	
	public VariableInfo getVariableInfo(){
		if (fInfo == null) {
			fInfo = new VariableInfo(this);
		}
		return (VariableInfo) fInfo;
	}
	
	@Override
	public CElementInfo getElementInfo() {
		return getVariableInfo();
	}

}
