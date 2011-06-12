/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/ 

package org.eclipse.ptp.internal.rdt.core.model;

import java.util.Arrays;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.core.model.ISourceReference;

public class FunctionDeclaration extends SourceManipulation implements IFunctionDeclaration {
	private static final long serialVersionUID = 1L;
	
	protected String[] fParameterTypes;
	protected String fReturnType;
	protected boolean fIsConst;
	protected boolean fIsStatic;
	protected boolean fIsVolatile;
	
	public FunctionDeclaration(Parent parent, String functionName) {
		super(parent, ICElement.C_FUNCTION_DECLARATION, functionName);
	}

	protected FunctionDeclaration(Parent parent, int kind, String name) {
		super(parent, kind, name);
	}

	protected FunctionDeclaration(Parent parent, IFunctionDeclaration element) throws CModelException {
		super(parent, element, (ISourceReference) element);
		fParameterTypes = element.getParameterTypes();
		fReturnType = element.getReturnType();
		fIsConst = element.isConst();
		fIsStatic = element.isStatic();
		fIsVolatile = element.isVolatile();
	}

	public FunctionDeclaration(Parent parent, int type, IFunction function) throws DOMException {
		super(parent, type, function.getName());
		fParameterTypes = extractParameterTypes(function);
		fReturnType = function.getType().getReturnType().toString();
		fIsStatic = function.isStatic();
	}

	public FunctionDeclaration(Parent parent, IFunction binding) throws DOMException {
		this(parent, ICElement.C_FUNCTION_DECLARATION, binding);
	}

	public String[] getExceptions() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getNumberOfParameters() {
		return fParameterTypes.length;
	}

	public String getParameterInitializer(int pos) {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getParameterTypes() {
		return fParameterTypes;
	}

	public String getReturnType() {
		return fReturnType;
	}

	public String getSignature() throws CModelException {
		// TODO Auto-generated method stub
		return null;
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

	public void setParameterTypes(String[] parameterTypes) {
		fParameterTypes = parameterTypes;
	}

	public void setReturnType(String returnType) {
		fReturnType = returnType;
	}

	public FunctionInfo getFunctionInfo() {
		if (fInfo == null) {
			fInfo = new FunctionInfo(this);
		}
		return (FunctionInfo) fInfo;
	}

	public FunctionInfo getElementInfo() {
		return getFunctionInfo();
	}

	public void setConst(boolean isConst) {
		fIsConst = isConst;
	}

	public void setStatic(boolean isStatic) {
		fIsStatic = isStatic;
	}

	@Override
	public boolean equals(Object o) {
		if (!super.equals(o)) {
			return false;
		}
		if (!(o instanceof IFunctionDeclaration)) {
			return false;
		}
		IFunctionDeclaration other = (IFunctionDeclaration) o;
		if (!Arrays.equals(fParameterTypes, other.getParameterTypes())) {
			return false;
		}
		String returnType = other.getReturnType();
		if (fReturnType != null && !fReturnType.equals(returnType)) {
			return false;
		}
		return true;
	}
}
