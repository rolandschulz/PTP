/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation 
 **********************************************************************/
package org.eclipse.cldt.internal.core.dom.parser.c;

import org.eclipse.cldt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cldt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cldt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cldt.core.dom.ast.IType;
import org.eclipse.cldt.core.dom.ast.c.ICASTDeclSpecifier;
import org.eclipse.cldt.core.dom.ast.c.ICASTSimpleDeclSpecifier;
import org.eclipse.cldt.core.dom.ast.c.ICASTTypedefNameSpecifier;
import org.eclipse.cldt.core.dom.ast.c.ICQualifierType;
import org.eclipse.cldt.internal.core.dom.parser.ITypeContainer;

/**
 * @author dsteffle
 */
public class CQualifierType implements ICQualifierType, ITypeContainer {

	IASTDeclSpecifier declSpec = null;
	IType type = null;

	/**
	 * CQualifierType has an IBasicType to keep track of the basic type information.
	 * 
	 * @param type the CQualifierType's IBasicType
	 */
	public CQualifierType(IASTDeclSpecifier declSpec) {
		this.declSpec = declSpec;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IQualifierType#isConst()
	 */
	public boolean isConst() {
		if (declSpec == null) return false;
		return declSpec.isConst();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IQualifierType#isVolatile()
	 */
	public boolean isVolatile() {
		if (declSpec == null) return false;
		return declSpec.isVolatile();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.c.ICQualifierType#isRestrict()
	 */
	public boolean isRestrict() {
		if (declSpec == null) return false;
		return (declSpec instanceof ICASTDeclSpecifier && ((ICASTDeclSpecifier)declSpec).isRestrict()); 
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IQualifierType#getType()
	 */
	public IType getType() {
		if (type == null) {
			if( declSpec instanceof ICASTTypedefNameSpecifier ){
				ICASTTypedefNameSpecifier nameSpec = (ICASTTypedefNameSpecifier) declSpec;
				type = (IType) nameSpec.getName().resolveBinding();			
			} else if( declSpec instanceof IASTElaboratedTypeSpecifier ){
				IASTElaboratedTypeSpecifier elabTypeSpec = (IASTElaboratedTypeSpecifier) declSpec;
				type = (IType) elabTypeSpec.getName().resolveBinding();
			} else if( declSpec instanceof IASTCompositeTypeSpecifier ){
				IASTCompositeTypeSpecifier compTypeSpec = (IASTCompositeTypeSpecifier) declSpec;
				type = (IType) compTypeSpec.getName().resolveBinding();
			} else {
			    type = new CBasicType((ICASTSimpleDeclSpecifier)declSpec);
			}
		}
		
		return type;
	}
	
	public void setType( IType t ){
	    type = t;
	}
	
    public Object clone(){
        IType t = null;
   		try {
            t = (IType) super.clone();
        } catch ( CloneNotSupportedException e ) {
            //not going to happen
        }
        return t;
    }
}
