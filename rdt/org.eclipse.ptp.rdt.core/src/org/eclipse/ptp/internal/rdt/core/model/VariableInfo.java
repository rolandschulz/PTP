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

public class VariableInfo extends SourceManipulationInfo {

	private static final long serialVersionUID = 1L;
	
	protected VariableInfo (CElement element) {
		super(element);
	}
	
	protected void setTypeName(String type){
		if (fParent instanceof VariableDeclaration)
			((VariableDeclaration)fParent).setTypeName(type);
	}
	

	protected void setConst(boolean isConst){
		if (fParent instanceof VariableDeclaration)
			((VariableDeclaration)fParent).setConst(isConst);
	}

	protected void setVolatile(boolean isVolatile){
		if (fParent instanceof VariableDeclaration)
			((VariableDeclaration)fParent).setVolatile(isVolatile);
	}


	protected void setStatic(boolean isStatic) {
		if (fParent instanceof VariableDeclaration)
			((VariableDeclaration)fParent).setStatic(isStatic);
	}

}
