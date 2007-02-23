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

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.core.runtime.PlatformObject;

/**
 * @author aniefer
 */
public class FortranEnumerator extends PlatformObject implements IEnumerator {
    public static class CEnumeratorProblem extends ProblemBinding implements IEnumerator {
        public CEnumeratorProblem( IASTNode node, int id, char[] arg ) {
            super( node, id, arg );
        }

        public IType getType() throws DOMException {
            throw new DOMException( this );
        }
    }

    private final IASTName enumeratorName;
    public FortranEnumerator( IASTEnumerator enumtor ){
		this.enumeratorName = enumtor.getName();
		enumeratorName.setBinding( this );
	}
    
    public IASTNode getPhysicalNode(){
        return enumeratorName;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
     */
    public String getName() {
        return enumeratorName.toString();
    }
    public char[] getNameCharArray(){
        return enumeratorName.toCharArray();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
     */
    public IScope getScope() {
        return FortranVisitor.getContainingScope( enumeratorName.getParent() );
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IEnumerator#getType()
	 */
	public IType getType() {
	    IASTEnumerator etor = (IASTEnumerator) enumeratorName.getParent();
		IASTEnumerationSpecifier enumSpec = (IASTEnumerationSpecifier) etor.getParent();
		IEnumeration enumeration = (IEnumeration) enumSpec.getName().resolveBinding();
		return enumeration;
	}

	public ILinkage getLinkage() {
		return Linkage.C_LINKAGE;
	}
}
