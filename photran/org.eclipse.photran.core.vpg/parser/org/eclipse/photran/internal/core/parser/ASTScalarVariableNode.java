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

public class ASTScalarVariableNode extends InteriorNode
{
    ASTScalarVariableNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTScalarVariableNode(this);
    }

    public Token getVariableName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SCALAR_VARIABLE_425)
            return (Token)((ASTVariableNameNode)getChild(0)).getVariableName();
        else if (getProduction() == Production.SCALAR_VARIABLE_426)
            return (Token)((ASTArrayElementNode)getChild(0)).getVariableName();
        else
            return null;
    }

    public ASTSectionSubscriptListNode getSectionSubscriptList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SCALAR_VARIABLE_426)
            return (ASTSectionSubscriptListNode)((ASTArrayElementNode)getChild(0)).getSectionSubscriptList();
        else
            return null;
    }

    public ASTStructureComponentNode getStructureComponent()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SCALAR_VARIABLE_426)
            return (ASTStructureComponentNode)((ASTArrayElementNode)getChild(0)).getStructureComponent();
        else
            return null;
    }

    public boolean hasStructureComponent()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SCALAR_VARIABLE_426)
            return ((ASTArrayElementNode)getChild(0)).hasStructureComponent();
        else
            return false;
    }

    @Override protected boolean childIsPulledUp(int index)
    {
        if (getProduction() == Production.SCALAR_VARIABLE_425 && index == 0)
            return true;
        else if (getProduction() == Production.SCALAR_VARIABLE_426 && index == 0)
            return true;
        else
            return false;
    }
}
