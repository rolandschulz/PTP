package org.eclipse.fdt.internal.core.model;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * Rational Software - Initial API and implementation
***********************************************************************/

import org.eclipse.fdt.core.parser.ast.ASTAccessVisibility;

public class FieldInfo extends SourceManipulationInfo {

	String typeStr = ""; //$NON-NLS-1$
	boolean isConst = false;
	boolean isVolatile = false;
	boolean isMutable = false;
	boolean isStatic = false;
	ASTAccessVisibility visibility;
	
	protected FieldInfo (CElement element) {
		super(element);
		visibility = ASTAccessVisibility.PRIVATE;
	}

	protected String getTypeName(){
		return typeStr;
	}
	
	protected void setTypeName(String type){
		typeStr = type;
	}
	
	protected boolean isConst(){
		return isConst;
	}

	protected void setConst(boolean isConst){
		this.isConst = isConst;
	}

	protected boolean isVolatile(){
		return isVolatile;
	}

	protected void setVolatile(boolean isVolatile){
		this.isVolatile = isVolatile;
	}

	public boolean isStatic() {
		return isStatic;
	}

	public void setStatic(boolean isStatic) {
		this.isStatic = isStatic;
	}

	protected boolean isMutable(){
		return isMutable;
	}

	protected void setMutable(boolean mutable){
		this.isMutable = mutable;
	}
	/**
	 * Returns the visibility.
	 * @return int
	 */
	public ASTAccessVisibility getVisibility() {
		return visibility;
	}

	/**
	 * Sets the visibility.
	 * @param visibility The visibility to set
	 */
	public void setVisibility(ASTAccessVisibility visibility) {
		this.visibility = visibility;
	}

	/**
	 * @see org.eclipse.fdt.internal.core.model.SourceManipulationInfo#hasSameContentsAs(SourceManipulationInfo)
	 */
	public boolean hasSameContentsAs( SourceManipulationInfo info){
		
		return( super.hasSameContentsAs(info)
		&&  (typeStr.equals(((FieldInfo)info).getTypeName())) 
		&&  (isConst == ((FieldInfo)info).isConst())
		&&  (isVolatile == ((FieldInfo)info).isVolatile())
		&& 	(isMutable == ((FieldInfo)info).isMutable())
		&& 	(visibility == ((FieldInfo)info).getVisibility())
		&& 	(isStatic == ((FieldInfo)info).isStatic())
		);
	}
	
}
