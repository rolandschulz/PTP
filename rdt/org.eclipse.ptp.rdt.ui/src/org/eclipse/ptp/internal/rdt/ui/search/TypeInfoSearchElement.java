/*******************************************************************************
 * Copyright (c) 2008, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.ui/src
 * Class: org.eclipse.cdt.internal.ui.search.TypeInfoSearchElement
 * Version: 1.4
 */

package org.eclipse.ptp.internal.rdt.ui.search;

import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.internal.rdt.core.search.RemoteASTTypeInfo;
import org.eclipse.ptp.internal.rdt.core.search.RemoteSearchElement;


/**
 * Represents a a c/c++-entity in a search.
 */
public class TypeInfoSearchElement extends RemoteSearchElement {
	private final ITypeInfo typeInfo;

	public TypeInfoSearchElement(IIndexName name, ITypeInfo typeInfo) throws CoreException {
		super(name.getFile().getLocation());
		this.typeInfo= typeInfo;
	}
	
	public TypeInfoSearchElement(RemoteASTTypeInfo typeInfo) {
		super(typeInfo.getIFL());
		this.typeInfo= typeInfo;
	}


	@Override
	public int hashCode() {
		return super.hashCode() + (typeInfo.getCElementType() *31 + typeInfo.getName().hashCode())*31;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof TypeInfoSearchElement))
			return false;
		TypeInfoSearchElement other= (TypeInfoSearchElement)obj;
		return super.equals(other) && typeInfo.equals(other.typeInfo);
	}

	public final ITypeInfo getTypeInfo() {
		return typeInfo;
	}
}
