/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.fdt.internal.core.dom.parser.cpp;

import org.eclipse.fdt.core.dom.ast.IASTExpression;
import org.eclipse.fdt.core.dom.ast.IASTName;
import org.eclipse.fdt.core.dom.ast.IASTNode;
import org.eclipse.fdt.core.dom.ast.IASTTypeId;
import org.eclipse.fdt.core.dom.ast.IBinding;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTTemplateId;

/**
 * @author jcamelon
 */
public class CPPASTTemplateId extends CPPASTNode implements ICPPASTTemplateId {
    private static final char[] EMPTY_CHAR_ARRAY = { };
    private static final String EMPTY_STRING = ""; //$NON-NLS-1$
    private IASTName templateName;

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.cpp.ICPPASTTemplateId#getTemplateName()
     */
    public IASTName getTemplateName() {
        return templateName;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.cpp.ICPPASTTemplateId#setTemplateName(org.eclipse.fdt.core.dom.ast.IASTName)
     */
    public void setTemplateName(IASTName name) {
        templateName = name;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.cpp.ICPPASTTemplateId#addTemplateArgument(org.eclipse.fdt.core.dom.ast.IASTTypeId)
     */
    public void addTemplateArgument(IASTTypeId typeId) {
        if( templateArguments == null )
        {
            templateArguments = new IASTNode[ DEFAULT_ARGS_LIST_SIZE ];
            currentIndex = 0;
        }
        if( templateArguments.length == currentIndex )
        {
            IASTNode [] old = templateArguments;
            templateArguments = new IASTNode[ old.length * 2 ];
            for( int i = 0; i < old.length; ++i )
                templateArguments[i] = old[i];
        }
        templateArguments[ currentIndex++ ] = typeId;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.cpp.ICPPASTTemplateId#addTemplateArgument(org.eclipse.fdt.core.dom.ast.IASTExpression)
     */
    public void addTemplateArgument(IASTExpression expression) {
        if( templateArguments == null )
        {
            templateArguments = new IASTNode[ DEFAULT_ARGS_LIST_SIZE ];
            currentIndex = 0;
        }
        if( templateArguments.length == currentIndex )
        {
            IASTNode [] old = templateArguments;
            templateArguments = new IASTNode[ old.length * 2 ];
            for( int i = 0; i < old.length; ++i )
                templateArguments[i] = old[i];
        }
        templateArguments[ currentIndex++ ] = expression;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.cpp.ICPPASTTemplateId#getTemplateArguments()
     */
    public IASTNode[] getTemplateArguments() {
        if( templateArguments == null ) return ICPPASTTemplateId.EMPTY_ARG_ARRAY;
        removeNullArguments();
        return templateArguments;
    }
    
    private void removeNullArguments() {
        int nullCount = 0; 
        for( int i = 0; i < templateArguments.length; ++i )
            if( templateArguments[i] == null )
                ++nullCount;
        if( nullCount == 0 ) return;
        IASTNode [] old = templateArguments;
        int newSize = old.length - nullCount;
        templateArguments = new IASTNode[ newSize ];
        for( int i = 0; i < newSize; ++i )
            templateArguments[i] = old[i];
        currentIndex = newSize;
    }

    private int currentIndex = 0;    
    private IASTNode [] templateArguments = null;
    private static final int DEFAULT_ARGS_LIST_SIZE = 4;


    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTName#resolveBinding()
     */
    public IBinding resolveBinding() {
        // TODO templates not yet supported
        return new CPPScope.CPPTemplateProblem( -1, templateName.toCharArray() );
    }

	public IBinding[] resolvePrefix() {
		// TODO Auto-generated method stub
		return null;
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTName#toCharArray()
     */
    public char[] toCharArray() {
        return EMPTY_CHAR_ARRAY;
    }
    public String toString() {
        return EMPTY_STRING;
    }

}
