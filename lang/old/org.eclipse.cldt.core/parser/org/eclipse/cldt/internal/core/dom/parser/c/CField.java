
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

import org.eclipse.cldt.core.dom.ast.IASTName;
import org.eclipse.cldt.core.dom.ast.IField;

/**
 * Created on Nov 8, 2004
 * @author aniefer
 */
public class CField extends CVariable implements IField {
    public static class CFieldProblem extends CVariable.CVariableProblem implements IField {
        public CFieldProblem( int id, char[] arg ) {
            super( id, arg );
        }
    }
	/**
	 * @param name
	 */
	public CField(IASTName name) {
		super(name);
	}

}
