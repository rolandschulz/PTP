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

public class ASTSubscriptTripletNode extends InteriorNode
{
    ASTSubscriptTripletNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitASTSubscriptTripletNode(this);
    }

    public ASTExpressionNode getUb()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SUBSCRIPT_TRIPLET_447)
            return (ASTExpressionNode)getChild(1);
        else if (getProduction() == Production.SUBSCRIPT_TRIPLET_449)
            return (ASTExpressionNode)getChild(2);
        else if (getProduction() == Production.SUBSCRIPT_TRIPLET_450)
            return (ASTExpressionNode)getChild(2);
        else if (getProduction() == Production.SUBSCRIPT_TRIPLET_452)
            return (ASTExpressionNode)getChild(1);
        else
            return null;
    }

    public ASTExpressionNode getLb()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SUBSCRIPT_TRIPLET_448)
            return (ASTExpressionNode)getChild(0);
        else if (getProduction() == Production.SUBSCRIPT_TRIPLET_449)
            return (ASTExpressionNode)getChild(0);
        else if (getProduction() == Production.SUBSCRIPT_TRIPLET_450)
            return (ASTExpressionNode)getChild(0);
        else if (getProduction() == Production.SUBSCRIPT_TRIPLET_451)
            return (ASTExpressionNode)getChild(0);
        else
            return null;
    }

    public ASTExpressionNode getStep()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SUBSCRIPT_TRIPLET_450)
            return (ASTExpressionNode)getChild(4);
        else if (getProduction() == Production.SUBSCRIPT_TRIPLET_451)
            return (ASTExpressionNode)getChild(3);
        else if (getProduction() == Production.SUBSCRIPT_TRIPLET_452)
            return (ASTExpressionNode)getChild(3);
        else if (getProduction() == Production.SUBSCRIPT_TRIPLET_453)
            return (ASTExpressionNode)getChild(2);
        else
            return null;
    }

    @Override protected boolean shouldVisitChild(int index)
    {
        if (getProduction() == Production.SUBSCRIPT_TRIPLET_446 && index == 0)
            return false;
        else if (getProduction() == Production.SUBSCRIPT_TRIPLET_447 && index == 0)
            return false;
        else if (getProduction() == Production.SUBSCRIPT_TRIPLET_448 && index == 1)
            return false;
        else if (getProduction() == Production.SUBSCRIPT_TRIPLET_449 && index == 1)
            return false;
        else if (getProduction() == Production.SUBSCRIPT_TRIPLET_450 && index == 1)
            return false;
        else if (getProduction() == Production.SUBSCRIPT_TRIPLET_450 && index == 3)
            return false;
        else if (getProduction() == Production.SUBSCRIPT_TRIPLET_451 && index == 1)
            return false;
        else if (getProduction() == Production.SUBSCRIPT_TRIPLET_451 && index == 2)
            return false;
        else if (getProduction() == Production.SUBSCRIPT_TRIPLET_452 && index == 0)
            return false;
        else if (getProduction() == Production.SUBSCRIPT_TRIPLET_452 && index == 2)
            return false;
        else if (getProduction() == Production.SUBSCRIPT_TRIPLET_453 && index == 0)
            return false;
        else if (getProduction() == Production.SUBSCRIPT_TRIPLET_453 && index == 1)
            return false;
        else
            return true;
    }
}
