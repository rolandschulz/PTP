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

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IField;
import org.eclipse.cdt.core.model.IMethodDeclaration;
import org.eclipse.cdt.core.model.IStructure;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;

public class Structure extends StructureDeclaration implements IStructure {
	private static final long serialVersionUID = 1L;

	protected List<SuperClassInfo> fSuperClasses;
	
	public Structure(Parent parent, int kind, String className) {
		super(parent, className, kind);
		fSuperClasses = new LinkedList<SuperClassInfo>(); 
	}

	public Structure(Parent parent, IStructure element) throws CModelException {
		super(parent, element);
		fSuperClasses = new LinkedList<SuperClassInfo>(); 
		for (String superClass : element.getSuperClassesNames()) {
			addSuperClass(superClass, element.getSuperClassAccess(superClass));
		}
	}

	public Structure(Parent parent, ICompositeType binding) throws DOMException {
		super(parent, adaptASTClassType(binding), binding);
		fSuperClasses = new LinkedList<SuperClassInfo>(); 
	}

	protected Structure(Parent parent, int type, ICompositeType binding) {
		super(parent, type, binding);
		fSuperClasses = new LinkedList<SuperClassInfo>(); 
	}
	
	static int adaptASTClassType(ICompositeType type) throws DOMException {
		switch (type.getKey()) {
		case ICompositeType.k_struct:
			return ICElement.C_STRUCT;
		case ICompositeType.k_union:
			return ICElement.C_UNION;
		default:
			return ICElement.C_CLASS;
		}
	}
	
	public IField getField(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public IField[] getFields() throws CModelException {
		// TODO Auto-generated method stub
		return null;
	}

	public IMethodDeclaration getMethod(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public IMethodDeclaration[] getMethods() throws CModelException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isAbstract() throws CModelException {
		// TODO Auto-generated method stub
		return false;
	}

	public ASTAccessVisibility getSuperClassAccess(String name) {
		for (SuperClassInfo info : fSuperClasses) {
			if (info.name.equals(name)) {
				return info.visibility;
			}
		}
		return null;
	}

	public String[] getSuperClassesNames() {
		String[] names = new String[fSuperClasses.size()];
		for (int i = 0; i < fSuperClasses.size(); i++) {
			names[i] = fSuperClasses.get(i).name;
		}
		return names;
	}

	public void addSuperClass(String simpleName, ASTAccessVisibility visibility) {
		fSuperClasses.add(new SuperClassInfo(simpleName, visibility));
	}
	
	private static class SuperClassInfo implements Serializable {
		private static final long serialVersionUID = 1L;
		
		public String name;
		public ASTAccessVisibility visibility;
		
		public SuperClassInfo(String name, ASTAccessVisibility visibility) {
			this.name = name;
			this.visibility = visibility;
		}
	}
}
