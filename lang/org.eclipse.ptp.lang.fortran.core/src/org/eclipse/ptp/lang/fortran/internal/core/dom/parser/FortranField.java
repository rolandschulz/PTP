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
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.c.ICCompositeTypeScope;

/**
 * Created on Nov 8, 2004
 * @author aniefer
 */
public class FortranField extends FortranVariable implements IField {
    public static class FortranFieldProblem extends FortranVariable.FortranVariableProblem implements IField {
        public FortranFieldProblem( IASTNode node, int id, char[] arg ) {
            super( node, id, arg );
        }

		public ICompositeType getCompositeTypeOwner() throws DOMException {
			throw new DOMException(this);
		}
    }
	/**
	 * @param name
	 */
	public FortranField(IASTName name) {
		super(name);
	}
	
	public ICompositeType getCompositeTypeOwner() throws DOMException {
		ICCompositeTypeScope scope = (ICCompositeTypeScope) getScope();
		return scope.getCompositeType();
	}

}
