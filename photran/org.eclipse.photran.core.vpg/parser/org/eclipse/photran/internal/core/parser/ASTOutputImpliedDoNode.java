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

public class ASTOutputImpliedDoNode extends InteriorNode
{
    ASTOutputImpliedDoNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitASTOutputImpliedDoNode(this);
    }

    public Token getTLparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.OUTPUT_IMPLIED_DO_824)
            return (Token)getChild(0);
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_825)
            return (Token)getChild(0);
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_826)
            return (Token)getChild(0);
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_827)
            return (Token)getChild(0);
        else
            return null;
    }

    public ASTExprNode getExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.OUTPUT_IMPLIED_DO_824)
            return (ASTExprNode)getChild(1);
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_825)
            return (ASTExprNode)getChild(1);
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_826)
            return (ASTExprNode)getChild(5);
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_827)
            return (ASTExprNode)getChild(5);
        else
            return null;
    }

    public Token getTComma()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.OUTPUT_IMPLIED_DO_824)
            return (Token)getChild(2);
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_825)
            return (Token)getChild(2);
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_826)
            return (Token)getChild(2);
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_827)
            return (Token)getChild(2);
        else
            return null;
    }

    public ASTImpliedDoVariableNode getImpliedDoVariable()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.OUTPUT_IMPLIED_DO_824)
            return (ASTImpliedDoVariableNode)getChild(3);
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_825)
            return (ASTImpliedDoVariableNode)getChild(3);
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_826)
            return (ASTImpliedDoVariableNode)getChild(3);
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_827)
            return (ASTImpliedDoVariableNode)getChild(3);
        else
            return null;
    }

    public Token getTEquals()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.OUTPUT_IMPLIED_DO_824)
            return (Token)getChild(4);
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_825)
            return (Token)getChild(4);
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_826)
            return (Token)getChild(4);
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_827)
            return (Token)getChild(4);
        else
            return null;
    }

    public ASTExprNode getExpr2()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.OUTPUT_IMPLIED_DO_824)
            return (ASTExprNode)getChild(5);
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_825)
            return (ASTExprNode)getChild(5);
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_826)
            return (ASTExprNode)getChild(7);
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_827)
            return (ASTExprNode)getChild(7);
        else
            return null;
    }

    public Token getTComma2()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.OUTPUT_IMPLIED_DO_824)
            return (Token)getChild(6);
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_825)
            return (Token)getChild(6);
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_826)
            return (Token)getChild(6);
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_827)
            return (Token)getChild(6);
        else
            return null;
    }

    public ASTExprNode getExpr3()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.OUTPUT_IMPLIED_DO_824)
            return (ASTExprNode)getChild(7);
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_825)
            return (ASTExprNode)getChild(7);
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_827)
            return (ASTExprNode)getChild(9);
        else
            return null;
    }

    public Token getTRparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.OUTPUT_IMPLIED_DO_824)
            return (Token)getChild(8);
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_825)
            return (Token)getChild(10);
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_826)
            return (Token)getChild(8);
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_827)
            return (Token)getChild(10);
        else
            return null;
    }

    public Token getTComma3()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.OUTPUT_IMPLIED_DO_825)
            return (Token)getChild(8);
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_827)
            return (Token)getChild(8);
        else
            return null;
    }

    public ASTExprNode getExpr4()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.OUTPUT_IMPLIED_DO_825)
            return (ASTExprNode)getChild(9);
        else
            return null;
    }

    public ASTOutputItemList1Node getOutputItemList1()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.OUTPUT_IMPLIED_DO_826)
            return (ASTOutputItemList1Node)getChild(1);
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_827)
            return (ASTOutputItemList1Node)getChild(1);
        else
            return null;
    }
}
