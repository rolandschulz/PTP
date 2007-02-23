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
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;

/**
 * @author dsteffle
 */
public class FortranFunctionType implements IFunctionType {
    IType[] parameters = null;
    IType returnType = null;
    
    /**
     * @param returnType
     * @param types
     */
    public FortranFunctionType( IType returnType, IType []  types ) {
        this.returnType = returnType;
        this.parameters = types;
    }

    public boolean isSameType( IType o ){
        if( o == this )
            return true;
        if( o instanceof ITypedef )
            return o.isSameType( this );
        if( o instanceof IFunctionType ){
            IFunctionType ft = (IFunctionType) o;
            IType [] fps;
            try {
                fps = ft.getParameterTypes();
            } catch ( DOMException e ) {
                return false;
            }
            if( fps.length != parameters.length )
                return false;
            try {
                if( ! returnType.isSameType( ft.getReturnType() ) )
                    return false;
            } catch ( DOMException e1 ) {
                return false;
            }
            for( int i = 0; i < parameters.length; i++ )
                if( ! parameters[i].isSameType( fps[i] ) )
                    return false;
            return true;
        }
        return false;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IFunctionType#getReturnType()
     */
    public IType getReturnType() {
        return returnType;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IFunctionType#getParameterTypes()
     */
    public IType[] getParameterTypes() {
        return parameters;
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
