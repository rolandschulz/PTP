package org.eclipse.fdt.internal.core.model;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * Rational Software - Initial API and implementation
***********************************************************************/

import org.eclipse.fdt.core.model.CModelException;
import org.eclipse.fdt.core.model.ICElement;
import org.eclipse.fdt.core.model.IMethodDeclaration;
import org.eclipse.fdt.core.parser.ast.ASTAccessVisibility;

public class MethodDeclaration extends FunctionDeclaration implements IMethodDeclaration{
	
	boolean isConst = false;
	boolean isConstructor = false;
	boolean isDestructor = false;

	public MethodDeclaration(ICElement parent, String name){
		super(parent, name, ICElement.C_METHOD_DECLARATION);
	}

	public MethodDeclaration(ICElement parent, String name, int type){
		super(parent, name, type);
	}
	
	public boolean isConstructor(){
		// is not implemented in the parser's quick mode
		//return isConstructor;
		return getElementName().equals(getParent().getElementName());
	}

	public boolean isDestructor() {
		// is not implemented in the parser's quick mode
		//return isDestructor;
		return getElementName().startsWith("~"); //$NON-NLS-1$
	}

	public void setConstructor(boolean isConstructor) {
		this.isConstructor = isConstructor;
	}

	public void setDestructor(boolean isDestructor) {
		this.isDestructor = isDestructor;
	}

	public boolean isOperator(){
		return getElementName().startsWith("operator"); //$NON-NLS-1$
	}

	public boolean isPureVirtual() throws CModelException{
		return getMethodInfo().isPureVirtual();
	}

	public void setPureVirtual(boolean isPureVirtual) throws CModelException{
		getMethodInfo().setPureVirtual(isPureVirtual);
	}

	public boolean isInline() throws CModelException{
		return getMethodInfo().isInline();
	}

	public void setInline(boolean isInline) throws CModelException{
		getMethodInfo().setInline(isInline);
	}

	public boolean isVirtual() throws CModelException{
		return getMethodInfo().isVirtual();
	}

	public void setVirtual(boolean isVirtual) throws CModelException{
		getMethodInfo().setVirtual(isVirtual);
	}

	public boolean isFriend() throws CModelException{
		return getMethodInfo().isFriend();
	}

	public void setFriend(boolean isFriend) throws CModelException{
		getMethodInfo().setFriend(isFriend);
	}

	public boolean isConst(){
		return isConst;
	}

	public void setConst(boolean isConst) throws CModelException{
		this.isConst = isConst;
		getMethodInfo().setConst(isConst);
	}

	public ASTAccessVisibility getVisibility() throws CModelException{
		return getMethodInfo().getVisibility();
	}
	
	public void setVisibility(ASTAccessVisibility visibility) throws CModelException{
		getMethodInfo().setVisibility(visibility);
	}
	
	protected CElementInfo createElementInfo () {
		return new MethodInfo(this);
	}
	
	private MethodInfo getMethodInfo() throws CModelException{
		return (MethodInfo) getElementInfo();
	}
	
	/*
	 * See if we need anything else to put in equals here
	 */
	public boolean equals(Object other) {
		// Two methods are equal if
		// their parents, names, parameter types and return types are equal and 
		return ( super.equals(other)
		// their constant directive is the same
		&& isConst() == ((MethodDeclaration)other).isConst()
		);
	}
		

}
