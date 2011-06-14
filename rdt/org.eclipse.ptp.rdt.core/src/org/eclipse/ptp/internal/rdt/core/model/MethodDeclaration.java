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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IMethodDeclaration;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;

public class MethodDeclaration extends FunctionDeclaration implements IMethodDeclaration {
	private static final long serialVersionUID = 1L;

	protected ASTAccessVisibility fVisibility;
	protected boolean fIsConstructor;
	protected boolean fIsDestructor;
	protected boolean fIsPureVirtual;
	protected boolean fIsVirtual;
	private boolean fIsInline;
	private boolean fIsFriend;

	public MethodDeclaration(Parent parent, String functionName) {
		super(parent, ICElement.C_METHOD_DECLARATION, functionName);
	}

	protected MethodDeclaration(Parent parent, int kind, String name) {
		super(parent, kind, name);
	}

	public MethodDeclaration(Parent parent, IMethodDeclaration element) throws CModelException {
		super(parent, element);
		fIsConstructor = element.isConstructor();
		fIsDestructor = element.isDestructor();
		fIsPureVirtual = element.isPureVirtual();
		fIsVirtual = element.isVirtual();
		fIsInline = element.isInline();
		fIsFriend = element.isFriend();
	}

	protected MethodDeclaration(Parent parent, int type, ICPPMethod method) throws DOMException {
		super(parent, type, method);
		fVisibility= getVisibility(method);
		fIsConstructor= method instanceof ICPPConstructor;
		if (!fIsConstructor) {
			fIsDestructor= method.isDestructor();
		}
		fIsVirtual = method.isVirtual();
		fIsInline = method.isInline();
	}

	public MethodDeclaration(Parent parent, ICPPMethod binding) throws DOMException {
		this(parent, C_METHOD_DECLARATION, binding);
	}

	public boolean isConstructor() throws CModelException {
		return fIsConstructor;
	}

	public boolean isDestructor() throws CModelException {
		return fIsDestructor;
	}

	public boolean isFriend() throws CModelException {
		return fIsFriend;
	}

	public boolean isInline() throws CModelException {
		return fIsInline;
	}

	public boolean isOperator() throws CModelException {
		return false;
	}

	public boolean isPureVirtual() throws CModelException {
		return fIsPureVirtual;
	}

	public boolean isVirtual() throws CModelException {
		return fIsVirtual;
	}

	public ASTAccessVisibility getVisibility() throws CModelException {
		return fVisibility;
	}

	public void setConst(boolean isConst) {
		fIsConst = isConst;
	}

	@Override
	public FunctionInfo getElementInfo() {
		if (fInfo == null) {
			fInfo = new MethodInfo(this);
		}
		return (FunctionInfo) fInfo;
	}
	
	public MethodInfo getMethodInfo() {
		return (MethodInfo) getElementInfo();
	}

	public void setConstructor(boolean isConstructor) {
		fIsConstructor = isConstructor;
	}

	public void setDestructor(boolean isDestructor) {
		fIsDestructor = isDestructor;
	}

	public void setVirtual(boolean virtual) {
		fIsVirtual = virtual;
	}

	public void setVisibility(ASTAccessVisibility visibility) {
		fVisibility = visibility;
	}

	public void setPureVirtual(boolean isPureVirtual) {
		fIsPureVirtual = isPureVirtual;
	}

	public void setInline(boolean isInline) {
		fIsInline = isInline;
	}

	public void setFriend(boolean isFriend) {
		fIsFriend = isFriend;
	}

	public void setVolatile(boolean isVolatile) {
		fIsVolatile = isVolatile;
	}

}
