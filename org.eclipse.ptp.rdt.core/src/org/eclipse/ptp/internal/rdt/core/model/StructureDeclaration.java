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
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.IStructureDeclaration;

public class StructureDeclaration extends SourceManipulation implements IStructureDeclaration {
	private static final long serialVersionUID = 1L;

	protected boolean fIsStatic;
	protected boolean fIsVolatile;
	protected String fTypeName;

	public StructureDeclaration(Parent parent, String className, int kind) {
		super(parent, kind, className);
	}

	public StructureDeclaration(Parent parent, IStructureDeclaration element) throws CModelException {
		super(parent, element, (ISourceReference) element);
		fTypeName = element.getTypeName();
		fIsStatic = element.isStatic();
		fIsVolatile = element.isVolatile();
	}

	public StructureDeclaration(Parent parent, int type, ICompositeType binding) {
		super(parent, type, binding.getName());
	}
	
	public StructureDeclaration(Parent parent, ICompositeType binding) throws DOMException {
		super(parent, adaptASTType(binding), binding.getName());
	}

	static int adaptASTType(ICompositeType type) throws DOMException {
		switch (type.getKey()) {
		case ICompositeType.k_struct:
			return ICElement.C_STRUCT_DECLARATION;
		case ICompositeType.k_union:
			return ICElement.C_UNION_DECLARATION;
		default:
			return ICElement.C_CLASS_DECLARATION;
		}
	}
	
	public String getTypeName() throws CModelException {
		return fTypeName;
	}

	public boolean isClass() throws CModelException {
		switch (getElementType()) {
		case ICElement.C_CLASS:
		case ICElement.C_CLASS_DECLARATION:
		case ICElement.C_TEMPLATE_CLASS:
		case ICElement.C_TEMPLATE_CLASS_DECLARATION:
			return true;
		default:
			return false;
		}
	}

	public boolean isStruct() throws CModelException {
		switch (getElementType()) {
		case ICElement.C_STRUCT:
		case ICElement.C_STRUCT_DECLARATION:
		case ICElement.C_TEMPLATE_STRUCT:
		case ICElement.C_TEMPLATE_STRUCT_DECLARATION:
			return true;
		default:
			return false;
		}
	}

	public boolean isUnion() throws CModelException {
		switch (getElementType()) {
		case ICElement.C_UNION:
		case ICElement.C_UNION_DECLARATION:
		case ICElement.C_TEMPLATE_UNION:
		case ICElement.C_TEMPLATE_UNION_DECLARATION:
			return true;
		default:
			return false;
		}
	}

	public boolean isConst() throws CModelException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isStatic() throws CModelException {
		return fIsStatic;
	}

	public boolean isVolatile() throws CModelException {
		return fIsVolatile;
	}

	public void setTypeName(String type) {
		fTypeName = type;
	}

}
