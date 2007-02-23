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
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.c.ICFunctionScope;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class FortranASTFunctionDefinition extends FortranASTNode implements
        IASTFunctionDefinition, IASTAmbiguityParent {

    private IASTDeclSpecifier declSpecifier;
    private IASTFunctionDeclarator declarator;
    private IASTStatement bodyStatement;
    private ICFunctionScope scope;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition#getDeclSpecifier()
     */
    public IASTDeclSpecifier getDeclSpecifier() {
        return declSpecifier;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition#setDeclSpecifier(org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier)
     */
    public void setDeclSpecifier(IASTDeclSpecifier declSpec) {
        declSpecifier = declSpec;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition#getDeclarator()
     */
    public IASTFunctionDeclarator getDeclarator() {
        return declarator;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition#setDeclarator(org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator)
     */
    public void setDeclarator(IASTFunctionDeclarator declarator) {
        this.declarator = declarator;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition#getBody()
     */
    public IASTStatement getBody() {
        return bodyStatement;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition#setBody(org.eclipse.cdt.core.dom.ast.IASTStatement)
     */
    public void setBody(IASTStatement statement) {
        bodyStatement = statement;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition#getScope()
	 */
	public IScope getScope() {
		if( scope == null )
			scope = new FortranFunctionScope( this );
		return scope;
	}

    public boolean accept( ASTVisitor action ){
        if( action.shouldVisitDeclarations ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        
        if( declSpecifier != null ) if( !declSpecifier.accept( action ) ) return false;
        if( declarator != null ) if( !declarator.accept( action ) ) return false;
        if( bodyStatement != null ) if( !bodyStatement.accept( action ) ) return false;
      
        if( action.shouldVisitDeclarations ){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }

    public void replace(IASTNode child, IASTNode other) {
        if( bodyStatement == child ) 
        {
            other.setPropertyInParent( bodyStatement.getPropertyInParent() );
            other.setParent( bodyStatement.getParent() );
            bodyStatement = (IASTStatement) other;
        }
    }


}
