/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.ptp.internal.rdt.core.search;

import java.io.Serializable;

import org.eclipse.cdt.core.browser.IQualifiedTypeName;
import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.browser.ITypeReference;
import org.eclipse.cdt.core.browser.ITypeSearchScope;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;

public class RemoteTypeInfo implements ITypeInfo, Serializable {
	private static final long serialVersionUID = 1L;
	
	protected ICProject fProject;
	protected int fElementType;
	protected IQualifiedTypeName fQualifiedName;
	
	public RemoteTypeInfo(ICProject project, int elementType, IQualifiedTypeName typeName) {
		fProject = project;
		fElementType = elementType;
		fQualifiedName = new RemoteQualifiedTypeName(typeName);
	}

	public void addDerivedReference(ITypeReference location) {
		throw new UnsupportedOperationException();
	}

	public void addReference(ITypeReference location) {
		throw new UnsupportedOperationException();
	}

	public boolean canSubstituteFor(ITypeInfo info) {
		return false;
	}

	public boolean encloses(ITypeInfo info) {
		return false;
	}

	public boolean exists() {
		return true;
	}

	public int getCElementType() {
		return fElementType;
	}

	public ITypeReference[] getDerivedReferences() {
		return null;
	}

	public ITypeInfo[] getEnclosedTypes() {
		return null;
	}

	public ITypeInfo[] getEnclosedTypes(int[] kinds) {
		return null;
	}

	public ITypeInfo getEnclosingNamespace(boolean includeGlobalNamespace) {
		return null;
	}

	public ICProject getEnclosingProject() {
		return fProject;
	}

	public ITypeInfo getEnclosingType() {
		return null;
	}

	public ITypeInfo getEnclosingType(int[] kinds) {
		return null;
	}

	public String getName() {
		return fQualifiedName.getName();
	}

	public IQualifiedTypeName getQualifiedTypeName() {
		return fQualifiedName;
	}

	public ITypeReference[] getReferences() {
		return null;
	}

	public ITypeReference getResolvedReference() {
		return null;
	}

	public ITypeInfo getRootNamespace(boolean includeGlobalNamespace) {
		return null;
	}

	public ITypeInfo[] getSubTypes() {
		return null;
	}

	public ASTAccessVisibility getSuperTypeAccess(ITypeInfo subType) {
		return null;
	}

	public ITypeInfo[] getSuperTypes() {
		return null;
	}

	public boolean hasEnclosedTypes() {
		return false;
	}

	public boolean hasSubTypes() {
		return false;
	}

	public boolean hasSuperTypes() {
		return false;
	}

	public boolean isClass() {
		return false;
	}

	public boolean isEnclosed(ITypeInfo info) {
		return false;
	}

	public boolean isEnclosed(ITypeSearchScope scope) {
		return false;
	}

	public boolean isEnclosedType() {
		return false;
	}

	public boolean isEnclosingType() {
		return false;
	}

	public boolean isReferenced(ITypeSearchScope scope) {
		return false;
	}

	public boolean isUndefinedType() {
		return false;
	}

	public void setCElementType(int type) {
		fElementType = type;
	}

	public int compareTo(Object obj) {
		if (obj == this) {
			return 0;
		}
		ITypeInfo info= (ITypeInfo) obj;
		if (fElementType != info.getCElementType())
			return (fElementType < info.getCElementType()) ? -1 : 1;
		return fQualifiedName.compareTo(info.getQualifiedTypeName());
	}
}
