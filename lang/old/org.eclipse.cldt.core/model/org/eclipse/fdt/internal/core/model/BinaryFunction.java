/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.fdt.internal.core.model;

import org.eclipse.fdt.core.IAddress;
import org.eclipse.fdt.core.model.IBinaryFunction;
import org.eclipse.fdt.core.model.ICElement;

/**
 */
public class BinaryFunction extends BinaryElement implements IBinaryFunction {

	public BinaryFunction(ICElement parent, String name, IAddress a) {
		super(parent, name, ICElement.C_FUNCTION, a);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.model.IFunctionDeclaration#getExceptions()
	 */
	public String[] getExceptions() {
		return new String[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.model.IFunctionDeclaration#getNumberOfParameters()
	 */
	public int getNumberOfParameters() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.model.IFunctionDeclaration#getParameterInitializer(int)
	 */
	public String getParameterInitializer(int pos) {
		// TODO Auto-generated method stub
		return new String();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.model.IFunctionDeclaration#getParameterTypes()
	 */
	public String[] getParameterTypes() {
		// TODO Auto-generated method stub
		return new String[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.model.IFunctionDeclaration#getReturnType()
	 */
	public String getReturnType() {
		// TODO Auto-generated method stub
		return new String();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.model.IFunctionDeclaration#getSignature()
	 */
	public String getSignature() {
		return getElementName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.model.IDeclaration#isStatic()
	 */
	public boolean isStatic() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.model.IDeclaration#isConst()
	 */
	public boolean isConst() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.model.IDeclaration#isVolatile()
	 */
	public boolean isVolatile() {
		// TODO Auto-generated method stub
		return false;
	}

}
