/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Dec 21, 2004
 */
package org.eclipse.cldt.internal.core.dom.parser.cpp;

import org.eclipse.cldt.core.dom.ast.DOMException;
import org.eclipse.cldt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cldt.core.dom.ast.cpp.ICPPConstructor;

/**
 * @author aniefer
 */
public class CPPConstructor extends CPPMethod implements ICPPConstructor {

    static public class CPPConstructorProblem extends CPPMethod.CPPMethodProblem implements ICPPConstructor {
        public CPPConstructorProblem( int id, char[] arg ) {
            super( id, arg );
        }

        public boolean isExplicit() throws DOMException{
            throw new DOMException( this );
        }
    }
	/**
	 * @param declarator
	 */
	public CPPConstructor(ICPPASTFunctionDeclarator declarator) {
		super( declarator );
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor#isExplicit()
     */
    public boolean isExplicit() {
        // TODO Auto-generated method stub
        return false;
    }

}
