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

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ILabel;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.c.CASTVisitor;
import org.eclipse.cdt.core.dom.ast.c.ICFunctionScope;
import org.eclipse.cdt.core.parser.util.ArrayUtil;

/**
 * Created on Nov 8, 2004
 * @author aniefer
 */
public class FortranFunctionScope extends FortranScope implements ICFunctionScope {
	public FortranFunctionScope( IASTFunctionDefinition function ){
	    super( function );
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICFunctionScope#getBinding(char[])
     */
    public IBinding getBinding( char[] name ) {
        return super.getBinding( NAMESPACE_TYPE_OTHER, name );
    }

    
	public IScope getBodyScope(){
	    IASTNode node = getPhysicalNode();
	    IASTStatement statement = ((IASTFunctionDefinition)node).getBody();
	    if( statement instanceof IASTCompoundStatement ){
	        return ((IASTCompoundStatement)statement).getScope();
	    }
	    return null;
	}

	public ILabel[] getLabels(){
	    FindLabelsAction action = new FindLabelsAction();
	    
        getPhysicalNode().accept( action );
	    
	    ILabel [] result = null;
	    if( action.labels != null ){
		    for( int i = 0; i < action.labels.length && action.labels[i] != null; i++ ){
		        IASTLabelStatement labelStatement = action.labels[i];
		        IBinding binding = labelStatement.getName().resolveBinding();
		        if( binding != null )
		            result = (ILabel[]) ArrayUtil.append( ILabel.class, result, binding );
		    }
	    }
	    return (ILabel[]) ArrayUtil.trim( ILabel.class, result );
	}
	
	static private class FindLabelsAction extends CASTVisitor {
        public IASTLabelStatement [] labels = null;
        
        public FindLabelsAction(){
            shouldVisitStatements = true;
        }
        
        public int visit( IASTStatement statement ) {
            if( statement instanceof IASTLabelStatement ){
               labels = (IASTLabelStatement[]) ArrayUtil.append( IASTLabelStatement.class, labels, statement );
            }
            return PROCESS_CONTINUE;
        }
	}
}