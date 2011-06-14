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


public class StructureInfo extends SourceManipulationInfo {

	private static final long serialVersionUID = 1L;
	
	protected String typeStr;
	protected boolean isStatic;
	protected boolean isVolatile;
	protected boolean isConst;

	protected StructureInfo (CElement element) {
		super(element);		
	}

	protected void setTypeName(String type){
		((StructureDeclaration)fParent).setTypeName(type);
	}


	/**
	 * Sets the isStatic.
	 * @param isStatic The isStatic to set
	 */
	public void setStatic(boolean isStatic) {
		((StructureDeclaration)fParent).setStatic(isStatic);
	}


	/**
	 * Sets the isVolatile.
	 * @param isVolatile The isVolatile to set
	 */
	public void setVolatile(boolean isVolatile) {
		((StructureDeclaration)fParent).setVolatile(isVolatile);
	}


	/**
	 * Sets the isConst.
	 * @param isConst The isConst to set
	 */
	public void setConst(boolean isConst) {
		((StructureDeclaration)fParent).setConst(isConst);
	}

}
