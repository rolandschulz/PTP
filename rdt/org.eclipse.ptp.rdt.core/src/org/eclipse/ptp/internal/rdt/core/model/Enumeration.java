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

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IEnumeration;
import org.eclipse.cdt.core.model.ISourceReference;

public class Enumeration extends SourceManipulation implements IEnumeration {
	private static final long serialVersionUID = 1L;

	public Enumeration(Parent parent, String enumName) {
		super(parent, ICElement.C_ENUMERATION, enumName);
	}

	public Enumeration(Parent parent, IEnumeration element) throws CModelException {
		super(parent, element, (ISourceReference) element);
	}

	public Enumeration(Parent parent, org.eclipse.cdt.core.dom.ast.IEnumeration binding) {
		super(parent, ICElement.C_ENUMERATION, binding.getName());
	}

	public String getTypeName() throws CModelException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isConst() throws CModelException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isStatic() throws CModelException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isVolatile() throws CModelException {
		// TODO Auto-generated method stub
		return false;
	}

	public void setTypeName(String type) {
		// TODO Auto-generated method stub
		
	}
	
	public EnumerationInfo getEnumerationInfo(){
		if (fInfo == null) {
			fInfo = new EnumerationInfo(this);
		}
		return (EnumerationInfo) fInfo;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.rdt.core.model.CElement#getElementInfo()
	 */
	@Override
	public CElementInfo getElementInfo() {
		return getEnumerationInfo();
	}
	
	

}
