/**********************************************************************
 * Copyright (c) 2002,2003,2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/

package org.eclipse.fdt.internal.core.model;

import org.eclipse.fdt.core.model.CModelException;
import org.eclipse.fdt.core.model.ICElement;
import org.eclipse.fdt.core.model.IStructureDeclaration;

/**
 * StructureDeclaration
 */
public class StructureDeclaration extends SourceManipulation implements IStructureDeclaration {

	/**
	 * @param parent
	 * @param name
	 * @param type
	 */
	public StructureDeclaration(ICElement parent, String name, int kind) {
		super(parent, name, kind);
	}

	public String getTypeName() throws CModelException {
		return getStructureInfo().getTypeName();
	}

	public void setTypeName(String type) throws CModelException {
		getStructureInfo().setTypeName(type);
	}

	public boolean isUnion() throws CModelException {
		return getStructureInfo().isUnion();
	}

	public boolean isClass() throws CModelException {
		return getStructureInfo().isClass();
	}

	public boolean isStruct() throws CModelException {
		return getStructureInfo().isStruct();
	}

	public StructureInfo getStructureInfo() throws CModelException{
		return (StructureInfo) getElementInfo();
	}

	protected CElementInfo createElementInfo () {
		return new StructureInfo(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.model.IDeclaration#isStatic()
	 */
	public boolean isStatic() throws CModelException {
		return getStructureInfo().isStatic();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.model.IDeclaration#isConst()
	 */
	public boolean isConst() throws CModelException {
		return getStructureInfo().isConst();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.model.IDeclaration#isVolatile()
	 */
	public boolean isVolatile() throws CModelException {
		return getStructureInfo().isVolatile();
	}

}
