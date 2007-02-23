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

import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.c.ICPointerType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.index.IIndexType;

/**
 * @author dsteffle
 */
public class FortranQualifiedPointerType implements ICPointerType, ITypeContainer {
	IType nextType = null;
	IASTArrayModifier mod = null;
	
	public FortranQualifiedPointerType(IType next, IASTArrayModifier mod) {
		this.nextType = next;
		if (mod instanceof ICASTArrayModifier) this.mod = mod;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.c.ICPointerType#isRestrict()
	 */
	public boolean isRestrict() {
		if (mod == null || !(mod instanceof ICASTArrayModifier)) return false;
		return ((ICASTArrayModifier)mod).isRestrict();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IPointerType#getType()
	 */
	public IType getType() {
		return nextType;
	}
	
	public void setType(IType type) {
		nextType = type;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IPointerType#isConst()
	 */
	public boolean isConst() {
		if (mod == null || !(mod instanceof ICASTArrayModifier)) return false;
		return ((ICASTArrayModifier)mod).isConst();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IPointerType#isVolatile()
	 */
	public boolean isVolatile() {
		if (mod == null || !(mod instanceof ICASTArrayModifier)) return false;
		return ((ICASTArrayModifier)mod).isVolatile();
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

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IType#isSameType(org.eclipse.cdt.core.dom.ast.IType)
     */
    public boolean isSameType( IType type ) {
        if( type == this )
            return true;
        if( type instanceof ITypedef || type instanceof IIndexType)
            return type.isSameType( this );

        if( type instanceof FortranQualifiedPointerType ){
            FortranQualifiedPointerType qual = (FortranQualifiedPointerType) type;
            if( qual.isConst() == isConst() && qual.isRestrict() == isRestrict() && qual.isVolatile() == isVolatile() )
                return getType().isSameType( qual.getType() );
        }
        return false;
    }
}
