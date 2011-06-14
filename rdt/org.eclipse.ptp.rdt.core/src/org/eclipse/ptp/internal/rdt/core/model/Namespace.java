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

import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.INamespace;
import org.eclipse.cdt.core.model.ISourceReference;

public class Namespace extends SourceManipulation implements INamespace {
	private static final long serialVersionUID = 1L;

	int fIndex;
	
	public Namespace(Parent parent, String nsName) {
		super(parent, ICElement.C_NAMESPACE, nsName);
	}

	public Namespace(Parent parent, INamespace element) throws CModelException {
		super(parent, element, (ISourceReference) element);
	}

	public Namespace(Parent parent, ICPPNamespace binding) {
		super(parent, ICElement.C_NAMESPACE, binding.getName());
	}

	public String getTypeName() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setTypeName(String type) {
		// TODO Auto-generated method stub
		
	}

	public void setIndex(int index) {
		fIndex = index;
	}
}
