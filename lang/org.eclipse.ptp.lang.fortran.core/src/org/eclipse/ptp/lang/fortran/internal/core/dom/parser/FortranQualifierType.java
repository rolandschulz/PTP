/*******************************************************************************
 * Copyright (c) 2005, 2006 Los Alamos National Security, LLC.
 * This material was produced under U.S. Government contract DE-AC52-06NA25396
 * for Los Alamos National Laboratory (LANL), which is operated by the Los Alamos
 * National Security, LLC (LANS) for the U.S. Department of Energy. The U.S. Government has
 * rights to use, reproduce, and distribute this software. NEITHER THE
 * GOVERNMENT NOR LANS MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified
 * to produce derivative works, such modified software should be clearly marked,
 * so as not to confuse it with the version available from LANL.
 *
 * Additionally, this program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.ptp.lang.fortran.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.c.ICASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypedefNameSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICQualifierType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;

/**
 * @author dsteffle
 */
public class FortranQualifierType implements ICQualifierType, ITypeContainer {

	private boolean isConst;
	private boolean isVolatile;
	private boolean isRestrict;
	private IType type = null;

	/**
	 * CQualifierType has an IBasicType to keep track of the basic type information.
	 * 
	 * @param type the CQualifierType's IBasicType
	 */
	public FortranQualifierType(ICASTDeclSpecifier declSpec) {
		this.type = resolveType( declSpec );
		this.isConst = declSpec.isConst();
		this.isVolatile = declSpec.isVolatile();
		this.isRestrict = declSpec.isRestrict();
	}
	
	public FortranQualifierType( IType type, boolean isConst, boolean isVolatile, boolean isRestrict ){
		this.type = type;
		this.isConst = isConst;
		this.isVolatile = isVolatile;
		this.isRestrict = isRestrict;
	}
	
	public boolean isSameType( IType obj ){
	    if( obj == this )
	        return true;
	    if( obj instanceof ITypedef )
	        return obj.isSameType( this );
	    
	    if( obj instanceof ICQualifierType ){
	        ICQualifierType qt = (ICQualifierType) obj;
            try {
		        if( isConst() != qt.isConst() ) return false;
		        if( isRestrict() != qt.isRestrict() ) return false;
		        if( isVolatile() != qt.isVolatile() ) return false;
            
		        if( type == null )
		        	return false;
                return type.isSameType( qt.getType() );
            } catch ( DOMException e ) {
                return false;
            }
        }
    	return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IQualifierType#isConst()
	 */
	public boolean isConst() {
		return isConst;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IQualifierType#isVolatile()
	 */
	public boolean isVolatile() {
		return isVolatile;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.c.ICQualifierType#isRestrict()
	 */
	public boolean isRestrict() {
		return isRestrict; 
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IQualifierType#getType()
	 */
	private IType resolveType( ICASTDeclSpecifier declSpec ) {
		IType t = null;
		if( declSpec instanceof ICASTTypedefNameSpecifier ){
			ICASTTypedefNameSpecifier nameSpec = (ICASTTypedefNameSpecifier) declSpec;
			t = (IType) nameSpec.getName().resolveBinding();			
		} else if( declSpec instanceof IASTElaboratedTypeSpecifier ){
			IASTElaboratedTypeSpecifier elabTypeSpec = (IASTElaboratedTypeSpecifier) declSpec;
			t = (IType) elabTypeSpec.getName().resolveBinding();
		} else if( declSpec instanceof IASTCompositeTypeSpecifier ){
			IASTCompositeTypeSpecifier compTypeSpec = (IASTCompositeTypeSpecifier) declSpec;
			t = (IType) compTypeSpec.getName().resolveBinding();
		} else if (declSpec instanceof IASTEnumerationSpecifier) {
			t = new FortranEnumeration(((IASTEnumerationSpecifier)declSpec).getName());
		} else {
		    t = new FortranBasicType((ICASTSimpleDeclSpecifier)declSpec);
		}
		
		return t;
	}
	
	public IType getType(){
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
