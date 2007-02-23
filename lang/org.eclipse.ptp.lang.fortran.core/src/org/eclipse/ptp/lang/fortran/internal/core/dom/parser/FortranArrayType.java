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
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.c.ICArrayType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;

/**
 * @author dsteffle
 */
public class FortranArrayType implements ICArrayType, ITypeContainer {

	IType type = null;
	ICASTArrayModifier mod = null;
	
	public FortranArrayType(IType type) {
		this.type = type;
	}
	
    public boolean isSameType(IType obj) {
        if( obj == this )
            return true;
        if( obj instanceof ITypedef )
            return obj.isSameType( this );
        if( obj instanceof ICArrayType ){
            ICArrayType at = (ICArrayType) obj;
            try {
		        if( isConst() != at.isConst() ) return false;
		        if( isRestrict() != at.isRestrict() ) return false;
		        if( isStatic() != at.isStatic() ) return false;
		        if( isVolatile() != at.isVolatile() ) return false;
		        if( isVariableLength() != at.isVariableLength() ) return false;
            
                return at.getType().isSameType( type );
            } catch ( DOMException e ) {
                return false;
            }
        }
    	return false;
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IArrayType#getType()
	 */
	public IType getType() {
		return type;
	}
	
	public void setType( IType t ){
	    this.type = t;
	}
	
	public void setModifiedArrayModifier(ICASTArrayModifier mod) {
		this.mod = mod;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.c.ICArrayType#isConst()
	 */
	public boolean isConst() {
		if (mod==null) return false;
		return mod.isConst();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.c.ICArrayType#isRestrict()
	 */
	public boolean isRestrict() {
		if (mod==null) return false;
		return mod.isRestrict();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.c.ICArrayType#isVolatile()
	 */
	public boolean isVolatile() {
		if (mod==null) return false;
		return mod.isVolatile();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.c.ICArrayType#isStatic()
	 */
	public boolean isStatic() {
		if (mod==null) return false;
		return mod.isStatic();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.c.ICArrayType#isVariableLength()
	 */
	public boolean isVariableLength() {
		if( mod == null ) return false;
		return mod.isVariableSized();
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

    /**
     * @return
     */
    public ICASTArrayModifier getModifier() {
        return mod;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IArrayType#getArraySizeExpression()
     */
    public IASTExpression getArraySizeExpression() {
        if( mod != null )
            return mod.getConstantExpression();
        return null;
    }
}
