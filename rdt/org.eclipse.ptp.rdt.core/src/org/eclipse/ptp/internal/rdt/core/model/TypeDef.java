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

import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITypeDef;

public class TypeDef extends SourceManipulation implements ITypeDef {
	private static final long serialVersionUID = 1L;

	protected String fTypeName;

	public TypeDef(Parent parent, String name) {
		super(parent, ICElement.C_TYPEDEF, name);
	}

	public TypeDef(Parent parent, ITypeDef element) throws CModelException {
		super(parent, element, (ISourceReference) element);
		fTypeName = element.getTypeName();
	}

	public TypeDef(Parent parent, ITypedef binding) {
		super(parent, ICElement.C_TYPEDEF, binding.getName());
	}

	public String getTypeName() {
		return fTypeName;
	}

	public void setTypeName(String typeName) {
		fTypeName = typeName;
	}

}
