/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cldt.internal.core.parser.ast;

import java.util.ArrayList;

import org.eclipse.cldt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cldt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cldt.core.parser.ast.IASTOffsetableNamedElement;
import org.eclipse.cldt.core.parser.ast.IASTQualifiedNameElement;
import org.eclipse.cldt.core.parser.ast.IASTScope;
import org.eclipse.cldt.core.parser.ast.IASTScopedElement;
import org.eclipse.cldt.core.parser.ast.IASTTemplateDeclaration;

/**
 * @author jcamelon
 *
 */
public class ASTQualifiedNamedElement implements IASTQualifiedNameElement
{
    /**
     * @param scope
     */
    public ASTQualifiedNamedElement(IASTScope scope, char[] name )
    {
        ArrayList names = new ArrayList(4);
		IASTScope parent = scope;
        
		names.add( name ); // push on our own name
		while (parent != null)
		{
			if (parent instanceof IASTNamespaceDefinition
				|| parent instanceof IASTClassSpecifier )
			{
				names.add( ((IASTOffsetableNamedElement)parent).getNameCharArray() );
				if( parent instanceof IASTScopedElement  )
					parent = ((IASTScopedElement)parent).getOwnerScope();				
			}
			else if( parent instanceof IASTTemplateDeclaration )
			{
				if( parent instanceof IASTScopedElement  )
					parent = ((IASTScopedElement)parent).getOwnerScope();
				continue;
			}
			else 
				break;
		}
		if (names.size() != 0)
		{
			qualifiedNames = new char[names.size()][];
			int counter = 0;
			for( int i = names.size() - 1; i >= 0; i-- )
				qualifiedNames[counter++] = (char[])names.get(i);
		}
		else 
			qualifiedNames = null;

    }
    
	public String[] getFullyQualifiedName()
	{
	    String[] result = new String[qualifiedNames.length ];
	    for( int i = 0; i < qualifiedNames.length; i++ ){
	        result[i] = String.valueOf(qualifiedNames[i]);
	    }
		return result;
	}
	
	public char[][] getFullyQualifiedNameCharArrays(){
	    return qualifiedNames;
	}

    private final char[][] qualifiedNames;

}
