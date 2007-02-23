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

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.parser.util.ArrayUtil;

/**
 * @author jcamelon
 */
public class FortranASTArrayDeclarator extends FortranASTDeclarator implements
        IASTArrayDeclarator {
    
    private IASTArrayModifier [] arrayMods = null;
    private int arrayModsPos=-1;

    public IASTArrayModifier[] getArrayModifiers() {
        if( arrayMods == null ) return IASTArrayModifier.EMPTY_ARRAY;
        arrayMods = (IASTArrayModifier[]) ArrayUtil.removeNullsAfter( IASTArrayModifier.class, arrayMods, arrayModsPos );
        return arrayMods;
 
    }

    public void addArrayModifier(IASTArrayModifier arrayModifier) {
    	if (arrayModifier != null) {
            arrayMods = (IASTArrayModifier[]) ArrayUtil.append( IASTArrayModifier.class, arrayMods, ++arrayModsPos, arrayModifier );    		
    	}
    }

    protected boolean postAccept( ASTVisitor action ){
        IASTArrayModifier [] mods = getArrayModifiers();
        for ( int i = 0; i < mods.length; i++ ) {
            if( !mods[i].accept( action ) ) return false;
        }
        IASTInitializer initializer = getInitializer();
        if( initializer != null ) if( !initializer.accept( action ) ) return false;
        return true;
    }
}
