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

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.c.ICExternalBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunctionType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPSemantics;

/**
 * @author aniefer
 */
public class FortranExternalFunction extends FortranFunction implements IFunction, ICExternalBinding {
    private IASTName name = null;
    private IASTTranslationUnit tu = null;
    
    public FortranExternalFunction( IASTTranslationUnit tu, IASTName name ) {
    	super( null );
        this.name = name;
        this.tu = tu;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IFunction#getType()
     */
    public IFunctionType getType() {
    	IFunctionType t = super.getType();
    	if( t == null ) {
    		type = new CPPFunctionType( CPPSemantics.VOID_TYPE, IType.EMPTY_TYPE_ARRAY );
    	}
        return type;
    }
    
    protected IASTTranslationUnit getTranslationUnit() {
		return tu;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
     */
    public String getName() {
        return name.toString();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getNameCharArray()
     */
    public char[] getNameCharArray() {
        return name.toCharArray();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
     */
    public IScope getScope() {
        return tu.getScope();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IFunction#isExtern()
     */
    public boolean isExtern() {
        return true;
    }
}
