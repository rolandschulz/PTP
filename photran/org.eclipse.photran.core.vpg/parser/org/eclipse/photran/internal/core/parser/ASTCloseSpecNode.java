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

public class ASTCloseSpecNode extends InteriorNode
{
    ASTCloseSpecNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitASTCloseSpecNode(this);
    }

    public ASTUnitIdentifierNode getUnitIdentifier()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CLOSE_SPEC_761)
            return (ASTUnitIdentifierNode)getChild(1);
        else
            return null;
    }

    public boolean hasUnitIdentifier()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CLOSE_SPEC_761)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTLblRefNode getErrLbl()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CLOSE_SPEC_762)
            return (ASTLblRefNode)getChild(1);
        else
            return null;
    }

    public boolean hasErrLbl()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CLOSE_SPEC_762)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTExpressionNode getStatusExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CLOSE_SPEC_763)
            return (ASTExpressionNode)getChild(1);
        else
            return null;
    }

    public boolean hasStatusExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CLOSE_SPEC_763)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTScalarVariableNode getIoStatVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CLOSE_SPEC_764)
            return (ASTScalarVariableNode)getChild(1);
        else
            return null;
    }

    public boolean hasIoStatVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CLOSE_SPEC_764)
            return getChild(1) != null;
        else
            return false;
    }

    @Override protected boolean shouldVisitChild(int index)
    {
        if (getProduction() == Production.CLOSE_SPEC_761 && index == 0)
            return false;
        else if (getProduction() == Production.CLOSE_SPEC_762 && index == 0)
            return false;
        else if (getProduction() == Production.CLOSE_SPEC_763 && index == 0)
            return false;
        else if (getProduction() == Production.CLOSE_SPEC_764 && index == 0)
            return false;
        else
            return true;
    }
}
