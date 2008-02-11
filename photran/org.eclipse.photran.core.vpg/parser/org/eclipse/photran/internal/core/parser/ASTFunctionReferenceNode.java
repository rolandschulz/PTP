/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.*;                   import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;

import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

class ASTFunctionReferenceNode extends InteriorNode
{
    ASTFunctionReferenceNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
        
    @Override public InteriorNode getASTParent()
    {
        InteriorNode actualParent = super.getParent();
        
        // If a node has been pulled up in an ACST, its physical parent in
        // the CST is not its logical parent in the ACST
        if (actualParent != null && actualParent.childIsPulledUp(actualParent.findChild(this)))
            return actualParent.getParent();
        else 
            return actualParent;
    }

    public ASTNameNode getName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FUNCTION_REFERENCE_961)
            return (ASTNameNode)getChild(0);
        else if (getProduction() == Production.FUNCTION_REFERENCE_962)
            return (ASTNameNode)getChild(0);
        else
            return null;
    }

    public boolean hasName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FUNCTION_REFERENCE_961)
            return getChild(0) != null;
        else if (getProduction() == Production.FUNCTION_REFERENCE_962)
            return getChild(0) != null;
        else
            return false;
    }

    public ASTFunctionArgListNode getFunctionArgList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FUNCTION_REFERENCE_962)
            return (ASTFunctionArgListNode)getChild(2);
        else
            return null;
    }

    public boolean hasFunctionArgList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FUNCTION_REFERENCE_962)
            return getChild(2) != null;
        else
            return false;
    }

    @Override protected boolean shouldVisitChild(int index)
    {
        if (getProduction() == Production.FUNCTION_REFERENCE_961 && index == 1)
            return false;
        else if (getProduction() == Production.FUNCTION_REFERENCE_961 && index == 2)
            return false;
        else if (getProduction() == Production.FUNCTION_REFERENCE_962 && index == 1)
            return false;
        else if (getProduction() == Production.FUNCTION_REFERENCE_962 && index == 3)
            return false;
        else
            return true;
    }
}
