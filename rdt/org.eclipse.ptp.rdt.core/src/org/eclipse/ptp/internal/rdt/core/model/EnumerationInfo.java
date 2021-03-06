/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core.model;

public class EnumerationInfo extends VariableInfo{

	private static final long serialVersionUID = 1L;

	protected EnumerationInfo(CElement element) {
		super(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.rdt.core.model.VariableInfo#setTypeName(java.lang.String)
	 */
	@Override
	protected void setTypeName(String type) {
		((Enumeration)fParent).setTypeName(type);
	}	
}
