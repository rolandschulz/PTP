/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - Initial API and implementation
 *******************************************************************************/

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.core/browser
 * Class: org.eclipse.cdt.internal.core.browser.ASTTypeInfo
 * Version: 1.2
 */

package org.eclipse.ptp.internal.rdt.core.search;

import java.util.Arrays;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.browser.IFunctionInfo;
import org.eclipse.cdt.core.browser.IQualifiedTypeName;
import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.browser.ITypeReference;
import org.eclipse.cdt.core.browser.ITypeSearchScope;
import org.eclipse.cdt.core.browser.QualifiedTypeName;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.internal.core.browser.ASTTypeReference;
import org.eclipse.cdt.internal.core.browser.IndexModelUtil;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNotImplementedError;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.internal.rdt.core.miners.RemoteIndexFileLocation;

/**
 * Type info object needed to support search for local variables.
 * @since 5.0
 */
public class RemoteASTTypeInfo implements ITypeInfo, IFunctionInfo {
	private static int hashCode(String[] array) {
		int prime = 31;
		if (array == null)
			return 0;
		int result = 1;
		for (String element : array) {
			result = prime * result + (element == null ? 0 : element.hashCode());
		}
		return result;
	}

	private final String[] fqn;
	private final int elementType;
	private final String[] params;
	private final String returnType;
	private RemoteASTTypeReference reference; 
	
	/**
	 * Creates a type info suitable for the binding.
	 * @param name the name to create the type info object for.
	 */
	public static RemoteASTTypeInfo create(IASTName name) {
		try {
			String[] fqn;
			int elementType;
			final IBinding binding = name.resolveBinding();
			final RemoteASTTypeReference ref= createReference(name);
			elementType = IndexModelUtil.getElementType(binding);
			if (binding instanceof ICPPBinding) {
				fqn= ((ICPPBinding)binding).getQualifiedName();
			} 
			else if (binding instanceof IField) {
				IField field= (IField) binding;
				ICompositeType owner= field.getCompositeTypeOwner();
				fqn= new String[] {owner.getName(), field.getName()};	
			}
			else {
				fqn= new String[] {binding.getName()};
			}
			if (binding instanceof IFunction) {
				final IFunction function= (IFunction)binding;
				final String[] paramTypes= IndexModelUtil.extractParameterTypes(function);
				final String returnType= IndexModelUtil.extractReturnType(function);
				return new RemoteASTTypeInfo(fqn, elementType, paramTypes, returnType, ref);
			}
			return new RemoteASTTypeInfo(fqn, elementType, null, null, ref);
		} catch (DOMException e) {
			Assert.isTrue(false);
		}

		return null;
	}


	private RemoteASTTypeInfo(String[] fqn, int elementType, String[] params, String returnType, RemoteASTTypeReference reference) {
		Assert.isNotNull(reference);
		this.fqn= fqn;
		this.elementType= elementType;
		this.params= params;
		this.returnType= returnType;
		this.reference= reference;
	}
		
	public void addDerivedReference(ITypeReference location) {
		throw new PDOMNotImplementedError();
	}

	public void addReference(ITypeReference location) {
		throw new PDOMNotImplementedError();
	}

	public boolean canSubstituteFor(ITypeInfo info) {
		throw new PDOMNotImplementedError();
	}

	public boolean encloses(ITypeInfo info) {
		throw new PDOMNotImplementedError();
	}

	public boolean exists() {
		throw new PDOMNotImplementedError();
	}

	public int getCElementType() {
		return elementType;
	}

	public ITypeReference[] getDerivedReferences() {
		throw new PDOMNotImplementedError();
	}

	public ITypeInfo[] getEnclosedTypes() {
		throw new PDOMNotImplementedError();
	}

	public ITypeInfo[] getEnclosedTypes(int[] kinds) {
		throw new PDOMNotImplementedError();
	}

	public ITypeInfo getEnclosingNamespace(boolean includeGlobalNamespace) {
		throw new PDOMNotImplementedError();
	}

	public ICProject getEnclosingProject() {
		if(getResolvedReference()!=null) {
			IProject project = reference.getProject();
			if(project!=null) {
				return CCorePlugin.getDefault().getCoreModel().getCModel().getCProject(project.getName());
			}
		}
		return null;
	}

	public ITypeInfo getEnclosingType() {
		// TODO not sure
		return null;
	}

	public ITypeInfo getEnclosingType(int[] kinds) {
		throw new PDOMNotImplementedError();
	}

	public String getName() {
		return fqn[fqn.length-1];
	}

	public IQualifiedTypeName getQualifiedTypeName() {
		return new QualifiedTypeName(fqn);
	}

	public ITypeReference getResolvedReference() {
		return reference;
	}

	private static RemoteASTTypeReference createReference(IASTName name) {
		IASTFileLocation floc= name.getFileLocation();
		if (floc != null) {
			String filename= floc.getFileName();
			IIndexFileLocation ifl = new RemoteIndexFileLocation(filename, null);
			return new RemoteASTTypeReference(ifl, name.resolveBinding(), new Path(filename), floc.getNodeOffset(), floc.getNodeLength());
		}
		return null;
	}


	public ITypeReference[] getReferences() {
		throw new UnsupportedOperationException();
	}

	public ITypeInfo getRootNamespace(boolean includeGlobalNamespace) {
		throw new PDOMNotImplementedError();
	}

	public ITypeInfo[] getSubTypes() {
		throw new PDOMNotImplementedError();
	}

	public ASTAccessVisibility getSuperTypeAccess(ITypeInfo subType) {
		throw new PDOMNotImplementedError();
	}

	public ITypeInfo[] getSuperTypes() {
		throw new PDOMNotImplementedError();
	}

	public boolean hasEnclosedTypes() {
		throw new PDOMNotImplementedError();
	}

	public boolean hasSubTypes() {
		throw new PDOMNotImplementedError();
	}

	public boolean hasSuperTypes() {
		throw new PDOMNotImplementedError();
	}

	public boolean isClass() {
		throw new PDOMNotImplementedError();
	}

	public boolean isEnclosed(ITypeInfo info) {
		throw new PDOMNotImplementedError();
	}

	public boolean isEnclosed(ITypeSearchScope scope) {
		throw new PDOMNotImplementedError();
	}

	public boolean isEnclosedType() {
		throw new PDOMNotImplementedError();
	}

	public boolean isEnclosingType() {
		throw new PDOMNotImplementedError();
	}

	public boolean isReferenced(ITypeSearchScope scope) {
		throw new PDOMNotImplementedError();
	}

	public boolean isUndefinedType() {
		throw new PDOMNotImplementedError();
	}

	public void setCElementType(int type) {
		throw new PDOMNotImplementedError();
	}

	/*
	 * @see org.eclipse.cdt.internal.core.browser.IFunctionInfo#getParameters()
	 */
	public String[] getParameters() {
		return params;
	}

	/*
	 * @see org.eclipse.cdt.internal.core.browser.IFunctionInfo#getReturnType()
	 */
	public String getReturnType() {
		return returnType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + elementType;
		result = prime * result + RemoteASTTypeInfo.hashCode(fqn);
		result = prime * result + RemoteASTTypeInfo.hashCode(params);
		return result;
	}

	/**
	 * Type info objects are equal if they compute the same references.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RemoteASTTypeInfo other = (RemoteASTTypeInfo) obj;
		if (elementType != other.elementType)
			return false;
		if (!Arrays.equals(fqn, other.fqn))
			return false;
		if (!Arrays.equals(params, other.params))
			return false;
		return true;
	}


	public IIndexFileLocation getIFL() {
		return reference.getIFL();
	}
}
